package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yzh.campushub.dto.PostSearchQueryDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Category;
import com.yzh.campushub.entity.Post;
import com.yzh.campushub.entity.User;
import com.yzh.campushub.mapper.CategoryMapper;
import com.yzh.campushub.mapper.PostMapper;
import com.yzh.campushub.mapper.UserMapper;
import com.yzh.campushub.service.PostSearchService;
import com.yzh.campushub.vo.PostSearchVO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PostSearchServiceImpl implements PostSearchService {

    private static final String HIT_START = "__HIT_START__";
    private static final String HIT_END = "__HIT_END__";

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${campushub.search.elasticsearch.enabled:true}")
    private boolean enabled;

    @Value("${campushub.search.elasticsearch.url:http://localhost:9200}")
    private String elasticsearchUrl;

    @Value("${campushub.search.elasticsearch.index:campushub_posts}")
    private String indexName;

    @Value("${campushub.search.elasticsearch.username:}")
    private String username;

    @Value("${campushub.search.elasticsearch.password:}")
    private String password;

    public PostSearchServiceImpl(PostMapper postMapper, UserMapper userMapper,
                                 CategoryMapper categoryMapper, ObjectMapper objectMapper) {
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        ensureIndex();
    }

    @Override
    public Result search(PostSearchQueryDTO queryDTO) {
        if (!StringUtils.hasText(queryDTO.getKeyword())) {
            return Result.ok(List.of(), 0L);
        }

        if (!enabled) {
            return mysqlFallbackSearch(queryDTO);
        }

        try {
            ensureIndex();
            HttpResponse<String> response = request("POST", "/" + indexName + "/_search", buildSearchBody(queryDTO));
            if (!isSuccess(response.statusCode())) {
                log.warn("Elasticsearch 搜索失败, status={}, body={}", response.statusCode(), response.body());
                return mysqlFallbackSearch(queryDTO);
            }
            Result result = parseSearchResponse(response.body());
            if (result.getTotal() != null && result.getTotal() == 0) {
                return mysqlFallbackSearch(queryDTO);
            }
            return result;
        } catch (Exception e) {
            log.warn("Elasticsearch 搜索不可用，使用 MySQL LIKE 降级查询", e);
            return mysqlFallbackSearch(queryDTO);
        }
    }

    @Override
    public Result suggest(String keyword, Integer size) {
        if (!StringUtils.hasText(keyword)) {
            return Result.ok(List.of());
        }

        int suggestSize = size == null || size <= 0 ? 8 : Math.min(size, 20);
        if (!enabled) {
            return Result.ok(mysqlFallbackSuggestions(keyword, suggestSize));
        }

        try {
            ensureIndex();
            Map<String, Object> completion = new LinkedHashMap<>();
            completion.put("field", "suggest");
            completion.put("size", suggestSize);
            completion.put("skip_duplicates", true);

            Map<String, Object> suggestion = new LinkedHashMap<>();
            suggestion.put("prefix", keyword.trim());
            suggestion.put("completion", completion);

            Map<String, Object> body = Map.of("suggest", Map.of("post-suggest", suggestion));
            HttpResponse<String> response = request("POST", "/" + indexName + "/_search", body);
            if (!isSuccess(response.statusCode())) {
                return Result.ok(mysqlFallbackSuggestions(keyword, suggestSize));
            }
            List<String> suggestions = parseSuggestionResponse(response.body());
            if (suggestions.isEmpty()) {
                suggestions = mysqlFallbackSuggestions(keyword, suggestSize);
            }
            return Result.ok(suggestions);
        } catch (Exception e) {
            log.warn("Elasticsearch 搜索建议不可用，使用 MySQL 降级查询", e);
            return Result.ok(mysqlFallbackSuggestions(keyword, suggestSize));
        }
    }

    @Override
    public Result reindexAll() {
        List<Post> posts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getIsDeleted, 0)
                .eq(Post::getStatus, 1));
        for (Post post : posts) {
            indexPost(post);
        }
        return Result.ok("已提交重建索引任务，共 " + posts.size() + " 篇帖子");
    }

    @Override
    public void indexPost(Post post) {
        if (!enabled || post == null || post.getId() == null) {
            return;
        }
        if (Integer.valueOf(1).equals(post.getIsDeleted()) || !Integer.valueOf(1).equals(post.getStatus())) {
            deletePost(post.getId());
            return;
        }

        try {
            ensureIndex();
            HttpResponse<String> response = request("PUT", "/" + indexName + "/_doc/" + post.getId(), buildDocument(post));
            if (!isSuccess(response.statusCode())) {
                log.warn("写入 Elasticsearch 索引失败, postId={}, status={}, body={}",
                        post.getId(), response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("写入 Elasticsearch 索引异常, postId={}", post.getId(), e);
        }
    }

    @Override
    public void deletePost(Long postId) {
        if (!enabled || postId == null) {
            return;
        }
        try {
            HttpResponse<String> response = request("DELETE", "/" + indexName + "/_doc/" + postId, null);
            if (response.statusCode() != 404 && !isSuccess(response.statusCode())) {
                log.warn("删除 Elasticsearch 索引失败, postId={}, status={}, body={}",
                        postId, response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("删除 Elasticsearch 索引异常, postId={}", postId, e);
        }
    }

    private void ensureIndex() {
        if (!enabled) {
            return;
        }
        try {
            HttpResponse<String> exists = request("HEAD", "/" + indexName, null);
            if (exists.statusCode() == 200) {
                return;
            }
            HttpResponse<String> created = request("PUT", "/" + indexName, buildIndexMapping());
            if (!isSuccess(created.statusCode())) {
                log.warn("创建 Elasticsearch 索引失败，请确认已安装 IK 分词器, status={}, body={}",
                        created.statusCode(), created.body());
            }
        } catch (Exception e) {
            log.warn("Elasticsearch 暂不可用，搜索接口会自动降级到 MySQL LIKE", e);
        }
    }

    private Map<String, Object> buildIndexMapping() {
        Map<String, Object> titleField = Map.of(
                "type", "text",
                "analyzer", "ik_max_word",
                "search_analyzer", "ik_smart",
                "fields", Map.of("keyword", Map.of("type", "keyword"))
        );
        Map<String, Object> contentField = Map.of(
                "type", "text",
                "analyzer", "ik_max_word",
                "search_analyzer", "ik_smart"
        );
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("id", Map.of("type", "long"));
        properties.put("userId", Map.of("type", "long"));
        properties.put("categoryId", Map.of("type", "long"));
        properties.put("categoryName", Map.of("type", "keyword"));
        properties.put("nickname", Map.of("type", "keyword"));
        properties.put("title", titleField);
        properties.put("content", contentField);
        properties.put("coverImg", Map.of("type", "keyword", "index", false));
        properties.put("viewCount", Map.of("type", "integer"));
        properties.put("likeCount", Map.of("type", "integer"));
        properties.put("commentCount", Map.of("type", "integer"));
        properties.put("favoriteCount", Map.of("type", "integer"));
        properties.put("isDeleted", Map.of("type", "integer"));
        properties.put("createTime", Map.of("type", "date", "format", "strict_date_optional_time||yyyy-MM-dd HH:mm:ss"));
        properties.put("updateTime", Map.of("type", "date", "format", "strict_date_optional_time||yyyy-MM-dd HH:mm:ss"));
        properties.put("suggest", Map.of(
                "type", "completion",
                "analyzer", "ik_max_word",
                "search_analyzer", "ik_smart"
        ));
        return Map.of("mappings", Map.of("properties", properties));
    }

    private Map<String, Object> buildDocument(Post post) {
        User user = userMapper.selectById(post.getUserId());
        Category category = categoryMapper.selectById(post.getCategoryId());
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("id", post.getId());
        doc.put("userId", post.getUserId());
        doc.put("categoryId", post.getCategoryId());
        doc.put("categoryName", category == null ? null : category.getName());
        doc.put("nickname", user == null ? null : user.getNickname());
        doc.put("avatar", user == null ? null : user.getAvatar());
        doc.put("title", post.getTitle());
        doc.put("content", post.getContent());
        doc.put("coverImg", post.getCoverImg());
        doc.put("viewCount", post.getViewCount());
        doc.put("likeCount", post.getLikeCount());
        doc.put("commentCount", post.getCommentCount());
        doc.put("favoriteCount", post.getFavoriteCount());
        doc.put("status", post.getStatus());
        doc.put("isTop", post.getIsTop());
        doc.put("isDeleted", post.getIsDeleted());
        doc.put("createTime", toDateTimeString(post.getCreateTime()));
        doc.put("updateTime", toDateTimeString(post.getUpdateTime()));
        doc.put("suggest", Map.of(
                "input", buildSuggestionInputs(post),
                "weight", safeInt(post.getViewCount()) + safeInt(post.getLikeCount()) * 2 + 1
        ));
        return doc;
    }

    private Map<String, Object> buildSearchBody(PostSearchQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1 ? 1 : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1 ? 10 : Math.min(queryDTO.getPageSize(), 50);

        Map<String, Object> multiMatch = new LinkedHashMap<>();
        multiMatch.put("query", queryDTO.getKeyword().trim());
        multiMatch.put("fields", List.of("title^3", "content"));
        multiMatch.put("type", "best_fields");
        multiMatch.put("operator", "and");

        List<Object> filters = new ArrayList<>();
        filters.add(Map.of("term", Map.of("isDeleted", 0)));
        filters.add(Map.of("term", Map.of("status", 1)));
        if (queryDTO.getCategoryId() != null) {
            filters.add(Map.of("term", Map.of("categoryId", queryDTO.getCategoryId())));
        }

        Map<String, Object> bool = new LinkedHashMap<>();
        bool.put("must", List.of(Map.of("multi_match", multiMatch)));
        bool.put("filter", filters);

        Map<String, Object> titleHighlight = Map.of("number_of_fragments", 0);
        Map<String, Object> contentHighlight = Map.of("fragment_size", 120, "number_of_fragments", 1);
        Map<String, Object> highlight = new LinkedHashMap<>();
        highlight.put("pre_tags", List.of(HIT_START));
        highlight.put("post_tags", List.of(HIT_END));
        highlight.put("fields", Map.of("title", titleHighlight, "content", contentHighlight));

        List<Object> sort = new ArrayList<>();
        if ("latest".equalsIgnoreCase(queryDTO.getSortType())) {
            sort.add(Map.of("createTime", Map.of("order", "desc")));
            sort.add("_score");
        } else {
            sort.add("_score");
            sort.add(Map.of("createTime", Map.of("order", "desc")));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from", (pageNum - 1) * pageSize);
        body.put("size", pageSize);
        body.put("query", Map.of("bool", bool));
        body.put("highlight", highlight);
        body.put("sort", sort);
        return body;
    }

    private Result parseSearchResponse(String body) throws IOException {
        Map<String, Object> root = objectMapper.readValue(body, new TypeReference<>() {});
        Map<String, Object> hits = asMap(root.get("hits"));
        long total = parseTotal(hits.get("total"));
        List<Map<String, Object>> rawHits = asList(hits.get("hits"));
        List<PostSearchVO> result = new ArrayList<>();
        for (Map<String, Object> hit : rawHits) {
            Map<String, Object> source = asMap(hit.get("_source"));
            Map<String, Object> highlight = asMap(hit.get("highlight"));
            PostSearchVO vo = mapSourceToVO(source);
            vo.setScore(asDouble(hit.get("_score")));
            vo.setHighlightedTitle(toSafeHighlight(firstHighlight(highlight, "title"), vo.getTitle()));
            vo.setHighlightedContent(toSafeHighlight(firstHighlight(highlight, "content"), snippet(vo.getContent(), 120)));
            result.add(vo);
        }
        return Result.ok(result, total);
    }

    private List<String> parseSuggestionResponse(String body) throws IOException {
        Map<String, Object> root = objectMapper.readValue(body, new TypeReference<>() {});
        Map<String, Object> suggest = asMap(root.get("suggest"));
        List<Map<String, Object>> groups = asList(suggest.get("post-suggest"));
        List<String> suggestions = new ArrayList<>();
        for (Map<String, Object> group : groups) {
            List<Map<String, Object>> options = asList(group.get("options"));
            for (Map<String, Object> option : options) {
                String text = asString(option.get("text"));
                if (StringUtils.hasText(text) && !suggestions.contains(text)) {
                    suggestions.add(text);
                }
            }
        }
        return suggestions;
    }

    private Result mysqlFallbackSearch(PostSearchQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1 ? 1 : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1 ? 10 : Math.min(queryDTO.getPageSize(), 50);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getIsDeleted, 0);
        wrapper.eq(Post::getStatus, 1);
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Post::getCategoryId, queryDTO.getCategoryId());
        }
        wrapper.and(w -> w.like(Post::getTitle, queryDTO.getKeyword())
                .or().like(Post::getContent, queryDTO.getKeyword()));
        wrapper.orderByDesc(Post::getCreateTime);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Post> page =
                postMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize), wrapper);
        List<PostSearchVO> list = page.getRecords().stream().map(post -> {
            PostSearchVO vo = mapPostToVO(post);
            vo.setHighlightedTitle(highlightKeyword(vo.getTitle(), queryDTO.getKeyword()));
            vo.setHighlightedContent(highlightKeyword(snippet(vo.getContent(), 120), queryDTO.getKeyword()));
            vo.setScore(0D);
            return vo;
        }).toList();
        return Result.ok(list, page.getTotal());
    }

    private List<String> mysqlFallbackSuggestions(String keyword, int size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getIsDeleted, 0)
                .eq(Post::getStatus, 1)
                .and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword))
                .orderByDesc(Post::getUpdateTime)
                .last("LIMIT " + size);
        return postMapper.selectList(wrapper).stream()
                .map(Post::getTitle)
                .filter(StringUtils::hasText)
                .distinct()
                .limit(size)
                .toList();
    }

    private PostSearchVO mapSourceToVO(Map<String, Object> source) {
        PostSearchVO vo = new PostSearchVO();
        vo.setId(asLong(source.get("id")));
        vo.setUserId(asLong(source.get("userId")));
        vo.setCategoryId(asLong(source.get("categoryId")));
        vo.setCategoryName(asString(source.get("categoryName")));
        vo.setNickname(asString(source.get("nickname")));
        vo.setAvatar(asString(source.get("avatar")));
        vo.setTitle(asString(source.get("title")));
        vo.setContent(asString(source.get("content")));
        vo.setCoverImg(asString(source.get("coverImg")));
        vo.setViewCount(asInteger(source.get("viewCount")));
        vo.setLikeCount(asInteger(source.get("likeCount")));
        vo.setCommentCount(asInteger(source.get("commentCount")));
        vo.setFavoriteCount(asInteger(source.get("favoriteCount")));
        vo.setStatus(asInteger(source.get("status")));
        vo.setIsTop(asInteger(source.get("isTop")));
        vo.setCreateTime(parseDateTime(asString(source.get("createTime"))));
        vo.setUpdateTime(parseDateTime(asString(source.get("updateTime"))));
        return vo;
    }

    private PostSearchVO mapPostToVO(Post post) {
        PostSearchVO vo = new PostSearchVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setCategoryId(post.getCategoryId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setCoverImg(post.getCoverImg());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setFavoriteCount(post.getFavoriteCount());
        vo.setStatus(post.getStatus());
        vo.setIsTop(post.getIsTop());
        vo.setCreateTime(post.getCreateTime());
        vo.setUpdateTime(post.getUpdateTime());
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }
        Category category = categoryMapper.selectById(post.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        return vo;
    }

    private HttpResponse<String> request(String method, String path, Object body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(elasticsearchUrl.replaceAll("/+$", "") + path))
                .header("Accept", "application/json");
        if (StringUtils.hasText(username)) {
            String auth = username + ":" + password;
            builder.header("Authorization", "Basic " + Base64.getEncoder()
                    .encodeToString(auth.getBytes(StandardCharsets.UTF_8)));
        }
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private boolean isSuccess(int status) {
        return status >= 200 && status < 300;
    }

    private String firstHighlight(Map<String, Object> highlight, String field) {
        List<String> fragments = asList(highlight.get(field));
        return fragments.isEmpty() ? null : fragments.get(0);
    }

    private String toSafeHighlight(String highlighted, String fallback) {
        String text = StringUtils.hasText(highlighted) ? highlighted : fallback;
        return escapeHtml(text)
                .replace(HIT_START, "<mark class=\"search-hit\">")
                .replace(HIT_END, "</mark>");
    }

    private String highlightKeyword(String text, String keyword) {
        String escapedText = escapeHtml(text);
        String escapedKeyword = escapeHtml(keyword == null ? "" : keyword.trim());
        if (!StringUtils.hasText(escapedKeyword)) {
            return escapedText;
        }
        return Pattern.compile(Pattern.quote(escapedKeyword), Pattern.CASE_INSENSITIVE)
                .matcher(escapedText)
                .replaceAll("<mark class=\"search-hit\">$0</mark>");
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String snippet(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength) + "...";
    }

    private List<String> buildSuggestionInputs(Post post) {
        List<String> inputs = new ArrayList<>();
        if (StringUtils.hasText(post.getTitle())) {
            inputs.add(post.getTitle());
        }
        String contentSnippet = snippet(post.getContent(), 40);
        if (StringUtils.hasText(contentSnippet)) {
            inputs.add(contentSnippet);
        }
        if (inputs.isEmpty()) {
            inputs.add(String.valueOf(post.getId()));
        }
        return inputs;
    }

    private long parseTotal(Object total) {
        if (total instanceof Number number) {
            return number.longValue();
        }
        Long value = asLong(asMap(total).get("value"));
        return value == null ? 0L : value;
    }

    private String toDateTimeString(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> asList(Object value) {
        return value instanceof List<?> list ? (List<T>) list : List.of();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return value == null ? null : Integer.valueOf(String.valueOf(value));
    }

    private Double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return value == null ? null : Double.valueOf(String.valueOf(value));
    }
}
