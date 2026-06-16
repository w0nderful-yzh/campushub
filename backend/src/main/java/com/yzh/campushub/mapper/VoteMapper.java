package com.yzh.campushub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yzh.campushub.entity.Vote;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface VoteMapper extends BaseMapper<Vote> {
    @Select("""
            SELECT *
            FROM vote
            WHERE id = #{voteId}
            FOR UPDATE
            """)
    Vote selectByIdForUpdate(@Param("voteId") Long voteId);

    @Update("""
            UPDATE vote
            SET total_count = (
                SELECT COUNT(DISTINCT user_id)
                FROM vote_record
                WHERE vote_id = #{voteId}
            ),
            update_time = NOW()
            WHERE id = #{voteId}
            """)
    int refreshTotalCount(@Param("voteId") Long voteId);
}
