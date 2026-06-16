package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.dto.CreateVoteDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.User;
import com.yzh.campushub.entity.Vote;
import com.yzh.campushub.entity.VoteOption;
import com.yzh.campushub.entity.VoteRecord;
import com.yzh.campushub.mapper.UserMapper;
import com.yzh.campushub.mapper.VoteMapper;
import com.yzh.campushub.mapper.VoteOptionMapper;
import com.yzh.campushub.mapper.VoteRecordMapper;
import com.yzh.campushub.service.VoteService;
import com.yzh.campushub.utils.UserContext;
import com.yzh.campushub.vo.VoteOptionVO;
import com.yzh.campushub.vo.VoteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VoteServiceImpl extends ServiceImpl<VoteMapper, Vote> implements VoteService {

    @Autowired
    private VoteOptionMapper optionMapper;

    @Autowired
    private VoteRecordMapper recordMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createVote(CreateVoteDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return Result.fail("投票标题不能为空");
        }
        if (dto.getOptions() == null || dto.getOptions().size() < 2) {
            return Result.fail("至少需要2个选项");
        }

        Vote vote = new Vote();
        vote.setUserId(userId);
        vote.setPostId(dto.getPostId());
        vote.setTitle(dto.getTitle().trim());
        vote.setDescription(dto.getDescription());
        vote.setMaxSelect(dto.getMaxSelect() != null ? dto.getMaxSelect() : 1);
        vote.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : 0);
        vote.setEndTime(dto.getEndTime());
        vote.setTotalCount(0);
        vote.setStatus(1);
        vote.setIsDeleted(0);
        vote.setCreateTime(LocalDateTime.now());
        vote.setUpdateTime(LocalDateTime.now());
        save(vote);

        for (int i = 0; i < dto.getOptions().size(); i++) {
            VoteOption option = new VoteOption();
            option.setVoteId(vote.getId());
            option.setContent(dto.getOptions().get(i).trim());
            option.setSort(i);
            option.setCount(0);
            option.setCreateTime(LocalDateTime.now());
            optionMapper.insert(option);
        }

        return Result.ok(vote.getId());
    }

    @Override
    public Result listVotes(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();

        Page<Vote> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Vote> query = new LambdaQueryWrapper<>();
        query.eq(Vote::getIsDeleted, 0);
        query.orderByDesc(Vote::getCreateTime);
        Page<Vote> resultPage = page(page, query);

        List<Vote> votes = resultPage.getRecords();
        if (votes.isEmpty()) {
            return Result.ok(new ArrayList<>(), 0L);
        }

        List<VoteVO> voList = buildVoteVOList(votes, userId);
        return Result.ok(voList, resultPage.getTotal());
    }

    @Override
    public Result getVoteDetail(Long id) {
        Long userId = UserContext.getUserId();

        Vote vote = getById(id);
        if (vote == null || vote.getIsDeleted() == 1) {
            return Result.fail("投票不存在");
        }

        VoteVO vo = buildVoteVO(vote, userId);
        return Result.ok(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result castVote(Long voteId, List<Long> optionIds) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        Vote vote = baseMapper.selectByIdForUpdate(voteId);
        if (vote == null || vote.getIsDeleted() == 1) {
            return Result.fail("投票不存在");
        }
        if (vote.getStatus() != 1) {
            return Result.fail("投票已关闭");
        }
        if (vote.getEndTime() != null && LocalDateTime.now().isAfter(vote.getEndTime())) {
            return Result.fail("投票已截止");
        }

        // Check already voted
        LambdaQueryWrapper<VoteRecord> checkQuery = new LambdaQueryWrapper<>();
        checkQuery.eq(VoteRecord::getVoteId, voteId);
        checkQuery.eq(VoteRecord::getUserId, userId);
        if (recordMapper.selectCount(checkQuery) > 0) {
            return Result.fail("你已经投过票了");
        }

        if (optionIds == null || optionIds.isEmpty()) {
            return Result.fail("请选择至少一个选项");
        }
        Integer maxSelect = vote.getMaxSelect() == null ? 1 : vote.getMaxSelect();
        if (optionIds.size() > maxSelect) {
            return Result.fail("最多只能选择 " + maxSelect + " 项");
        }

        // Validate options belong to this vote
        LambdaQueryWrapper<VoteOption> optQuery = new LambdaQueryWrapper<>();
        optQuery.eq(VoteOption::getVoteId, voteId);
        optQuery.in(VoteOption::getId, optionIds);
        List<VoteOption> validOptions = optionMapper.selectList(optQuery);
        if (validOptions.size() != optionIds.size()) {
            return Result.fail("无效的选项");
        }

        for (Long optionId : optionIds) {
            VoteRecord record = new VoteRecord();
            record.setVoteId(voteId);
            record.setOptionId(optionId);
            record.setUserId(userId);
            record.setCreateTime(LocalDateTime.now());
            recordMapper.insert(record);
            optionMapper.refreshCount(optionId);
        }

        baseMapper.refreshTotalCount(voteId);

        return Result.ok();
    }

    @Override
    public Result deleteVote(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        Vote vote = getById(id);
        if (vote == null) {
            return Result.fail("投票不存在");
        }
        if (!vote.getUserId().equals(userId)) {
            return Result.fail("无权删除此投票");
        }

        vote.setIsDeleted(1);
        vote.setUpdateTime(LocalDateTime.now());
        updateById(vote);

        return Result.ok();
    }

    @Override
    public Result getVotesByPost(Long postId) {
        Long userId = UserContext.getUserId();

        LambdaQueryWrapper<Vote> query = new LambdaQueryWrapper<>();
        query.eq(Vote::getPostId, postId);
        query.eq(Vote::getIsDeleted, 0);
        query.orderByDesc(Vote::getCreateTime);
        List<Vote> votes = list(query);

        if (votes.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }

        List<VoteVO> voList = buildVoteVOList(votes, userId);
        return Result.ok(voList);
    }

    private List<VoteVO> buildVoteVOList(List<Vote> votes, Long userId) {
        List<Long> authorIds = votes.stream().map(Vote::getUserId).distinct().collect(Collectors.toList());
        List<User> authors = userMapper.selectBatchIds(authorIds);
        Map<Long, User> authorMap = authors.stream().collect(Collectors.toMap(User::getId, u -> u));

        List<Long> voteIds = votes.stream().map(Vote::getId).collect(Collectors.toList());

        // Get all options
        LambdaQueryWrapper<VoteOption> oq = new LambdaQueryWrapper<>();
        oq.in(VoteOption::getVoteId, voteIds);
        oq.orderByAsc(VoteOption::getSort);
        List<VoteOption> allOptions = optionMapper.selectList(oq);
        Map<Long, List<VoteOption>> optionMap = allOptions.stream()
                .collect(Collectors.groupingBy(VoteOption::getVoteId));

        // Get user's vote records
        Set<Long> votedOptionIds = new HashSet<>();
        Map<Long, Set<Long>> userVoteMap = new HashMap<>();
        if (userId != null) {
            LambdaQueryWrapper<VoteRecord> rq = new LambdaQueryWrapper<>();
            rq.in(VoteRecord::getVoteId, voteIds);
            rq.eq(VoteRecord::getUserId, userId);
            List<VoteRecord> records = recordMapper.selectList(rq);
            votedOptionIds = records.stream().map(VoteRecord::getOptionId).collect(Collectors.toSet());
            userVoteMap = records.stream().collect(Collectors.groupingBy(
                    VoteRecord::getVoteId, Collectors.mapping(VoteRecord::getOptionId, Collectors.toSet())));
        }

        Set<Long> finalVotedOptionIds = votedOptionIds;
        Map<Long, Set<Long>> finalUserVoteMap = userVoteMap;

        return votes.stream().map(v -> {
            VoteVO vo = new VoteVO();
            vo.setId(v.getId());
            vo.setUserId(v.getUserId());
            vo.setPostId(v.getPostId());
            vo.setTitle(v.getTitle());
            vo.setDescription(v.getDescription());
            vo.setMaxSelect(v.getMaxSelect());
            vo.setIsAnonymous(v.getIsAnonymous());
            vo.setEndTime(v.getEndTime());
            vo.setTotalCount(v.getTotalCount());
            vo.setStatus(v.getStatus());
            vo.setCreateTime(v.getCreateTime());

            User author = authorMap.get(v.getUserId());
            if (author != null) {
                vo.setAuthorNickname(author.getNickname());
                vo.setAuthorAvatar(author.getAvatar());
            }

            Set<Long> userSelected = finalUserVoteMap.getOrDefault(v.getId(), Collections.emptySet());
            vo.setIsVoted(!userSelected.isEmpty());

            List<VoteOption> options = optionMap.getOrDefault(v.getId(), Collections.emptyList());
            List<VoteOptionVO> optionVOs = options.stream().map(opt -> {
                VoteOptionVO ovo = new VoteOptionVO();
                ovo.setId(opt.getId());
                ovo.setContent(opt.getContent());
                ovo.setCount(opt.getCount());
                ovo.setIsSelected(finalVotedOptionIds.contains(opt.getId()));
                if (v.getTotalCount() != null && v.getTotalCount() > 0) {
                    ovo.setPercentage(Math.round(opt.getCount() * 100.0 / v.getTotalCount() * 10.0) / 10.0);
                } else {
                    ovo.setPercentage(0.0);
                }
                return ovo;
            }).collect(Collectors.toList());
            vo.setOptions(optionVOs);

            return vo;
        }).collect(Collectors.toList());
    }

    private VoteVO buildVoteVO(Vote vote, Long userId) {
        List<Vote> single = Collections.singletonList(vote);
        List<VoteVO> vos = buildVoteVOList(single, userId);
        return vos.isEmpty() ? null : vos.get(0);
    }
}
