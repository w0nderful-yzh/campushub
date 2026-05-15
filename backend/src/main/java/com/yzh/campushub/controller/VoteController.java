package com.yzh.campushub.controller;

import com.yzh.campushub.dto.CreateVoteDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    @Autowired
    private VoteService voteService;

    @PostMapping
    public Result createVote(@RequestBody CreateVoteDTO dto) {
        return voteService.createVote(dto);
    }

    @GetMapping
    public Result listVotes(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return voteService.listVotes(pageNum, pageSize);
    }

    @GetMapping("/{id}")
    public Result getVoteDetail(@PathVariable Long id) {
        return voteService.getVoteDetail(id);
    }

    @PostMapping("/{id}/vote")
    public Result castVote(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<Long> optionIds = ((java.util.List<Number>) body.get("optionIds"))
                .stream().map(Number::longValue).collect(java.util.stream.Collectors.toList());
        return voteService.castVote(id, optionIds);
    }

    @DeleteMapping("/{id}")
    public Result deleteVote(@PathVariable Long id) {
        return voteService.deleteVote(id);
    }

    @GetMapping("/post/{postId}")
    public Result getVotesByPost(@PathVariable Long postId) {
        return voteService.getVotesByPost(postId);
    }
}
