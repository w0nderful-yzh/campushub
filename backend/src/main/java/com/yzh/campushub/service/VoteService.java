package com.yzh.campushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.campushub.dto.CreateVoteDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Vote;

import java.util.List;

public interface VoteService extends IService<Vote> {
    Result createVote(CreateVoteDTO dto);
    Result listVotes(Integer pageNum, Integer pageSize);
    Result getVoteDetail(Long id);
    Result castVote(Long voteId, List<Long> optionIds);
    Result deleteVote(Long id);
    Result getVotesByPost(Long postId);
}
