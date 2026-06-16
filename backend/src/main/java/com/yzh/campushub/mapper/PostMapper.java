package com.yzh.campushub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yzh.campushub.entity.Post;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface PostMapper extends BaseMapper<Post> {
    @Update("""
            UPDATE post
            SET like_count = (
                    SELECT COUNT(*)
                    FROM `like`
                    WHERE post_id = #{postId}
                ),
                favorite_count = (
                    SELECT COUNT(*)
                    FROM favorite
                    WHERE post_id = #{postId}
                ),
                comment_count = (
                    SELECT COUNT(*)
                    FROM comment
                    WHERE post_id = #{postId}
                      AND is_deleted = 0
                      AND status = 1
                ),
                update_time = NOW()
            WHERE id = #{postId}
            """)
    int refreshInteractionCounts(@Param("postId") Long postId);
}
