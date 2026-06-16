package com.yzh.campushub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yzh.campushub.entity.VoteOption;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface VoteOptionMapper extends BaseMapper<VoteOption> {
    @Update("""
            UPDATE vote_option
            SET `count` = (
                SELECT COUNT(*)
                FROM vote_record
                WHERE option_id = #{optionId}
            )
            WHERE id = #{optionId}
            """)
    int refreshCount(@Param("optionId") Long optionId);
}
