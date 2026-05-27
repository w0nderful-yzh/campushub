package com.yzh.campushub.listener;

import com.yzh.campushub.entity.Post;
import com.yzh.campushub.event.PostSearchSyncEvent;
import com.yzh.campushub.mapper.PostMapper;
import com.yzh.campushub.service.PostSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class PostSearchSyncListener {

    private final PostMapper postMapper;
    private final PostSearchService postSearchService;

    public PostSearchSyncListener(PostMapper postMapper, PostSearchService postSearchService) {
        this.postMapper = postMapper;
        this.postSearchService = postSearchService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostSearchSyncEvent event) {
        try {
            if (event.action() == PostSearchSyncEvent.Action.DELETE) {
                postSearchService.deletePost(event.postId());
                return;
            }

            Post post = postMapper.selectById(event.postId());
            if (post == null || Integer.valueOf(1).equals(post.getIsDeleted())) {
                postSearchService.deletePost(event.postId());
                return;
            }
            postSearchService.indexPost(post);
        } catch (Exception e) {
            log.warn("同步帖子到 Elasticsearch 失败, postId={}", event.postId(), e);
        }
    }
}
