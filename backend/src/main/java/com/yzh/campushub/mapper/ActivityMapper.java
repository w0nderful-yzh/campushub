package com.yzh.campushub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yzh.campushub.entity.Activity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ActivityMapper extends BaseMapper<Activity> {
    @Select("""
            SELECT *
            FROM activity
            WHERE id = #{activityId}
            FOR UPDATE
            """)
    Activity selectByIdForUpdate(@Param("activityId") Long activityId);

    @Update("""
            UPDATE activity
            SET current_count = (
                SELECT COUNT(*)
                FROM activity_signup
                WHERE activity_id = #{activityId} AND status = 1
            ),
            update_time = NOW()
            WHERE id = #{activityId}
            """)
    int refreshCurrentCount(@Param("activityId") Long activityId);
}
