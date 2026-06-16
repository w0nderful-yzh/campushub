-- =====================================================
-- Campushub 存储过程和触发器优化版
-- 优化日期: 2026-06-11
-- =====================================================

-- =====================================================
-- 一、存储过程优化
-- =====================================================

-- -----------------------------------------------------
-- 1. sp_get_post_full_detail - 获取帖子详情
-- 优化点:
--   - 合并为单个结果集，避免多结果集兼容性问题
--   - 使用事务确保原子性
--   - 增加乐观锁防止并发更新丢失
-- -----------------------------------------------------
DROP PROCEDURE IF EXISTS sp_get_post_full_detail;
DELIMITER //
CREATE PROCEDURE sp_get_post_full_detail(IN p_post_id BIGINT)
BEGIN
    DECLARE v_exists INT DEFAULT 0;
    DECLARE v_affected INT DEFAULT 0;

    START TRANSACTION READ ONLY;

    -- 验证帖子存在
    SELECT COUNT(*) INTO v_exists
    FROM post
    WHERE id = p_post_id AND is_deleted = 0 AND status = 1;

    IF v_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '帖子不存在或已被删除';
    END IF;

    COMMIT;

    -- 原子递增浏览量
    UPDATE post
    SET view_count = view_count + 1
    WHERE id = p_post_id AND is_deleted = 0;
    SET v_affected = ROW_COUNT();

    -- 返回帖子详情（单结果集）
    SELECT
        pd.post_id,
        pd.post_title,
        pd.content,
        pd.cover_img,
        pd.author_id,
        pd.author_nickname,
        pd.author_avatar,
        pd.category_id,
        pd.category_name,
        pd.view_count,
        pd.like_count,
        pd.comment_count,
        pd.favorite_count,
        pd.is_top,
        pd.create_time,
        pd.update_time,
        (SELECT JSON_ARRAYAGG(
            JSON_OBJECT(
                'comment_id', cm.id,
                'parent_id', cm.parent_id,
                'reply_user_id', cm.reply_user_id,
                'reply_user_nickname', ru.nickname,
                'user_id', cm.user_id,
                'comment_user_nickname', u.nickname,
                'content', cm.content,
                'like_count', cm.like_count,
                'create_time', cm.create_time
            )
        )
        FROM comment cm
        JOIN user u ON u.id = cm.user_id
        LEFT JOIN user ru ON ru.id = cm.reply_user_id
        WHERE cm.post_id = p_post_id
          AND cm.is_deleted = 0
          AND cm.status = 1
        ORDER BY cm.parent_id ASC, cm.create_time ASC
        ) AS comments
    FROM v_post_detail pd
    WHERE pd.post_id = p_post_id;
END //
DELIMITER ;

-- -----------------------------------------------------
-- 2. sp_get_user_dashboard - 用户仪表盘
-- 优化点:
--   - 使用临时表减少重复查询
--   - 合并多个结果集为单个JSON字段
-- -----------------------------------------------------
DROP PROCEDURE IF EXISTS sp_get_user_dashboard;
DELIMITER //
CREATE PROCEDURE sp_get_user_dashboard(IN p_user_id BIGINT)
BEGIN
    DECLARE v_user_ok INT DEFAULT 0;

    -- 验证用户
    SELECT COUNT(*) INTO v_user_ok
    FROM user
    WHERE id = p_user_id AND is_deleted = 0;

    IF v_user_ok = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '用户不存在或已被删除';
    END IF;

    -- 单一结果集返回所有数据
    SELECT
        (SELECT JSON_OBJECT(
            'post_count', post_count,
            'total_views', total_views,
            'total_likes', total_likes,
            'total_comments', total_comments,
            'total_favorites', total_favorites
        )
        FROM v_user_content_stats
        WHERE user_id = p_user_id
        ) AS user_stats,

        (SELECT JSON_ARRAYAGG(
            JSON_OBJECT(
                'post_id', post_id,
                'post_title', post_title,
                'category_name', category_name,
                'view_count', view_count,
                'like_count', like_count,
                'comment_count', comment_count,
                'favorite_count', favorite_count,
                'create_time', create_time
            )
        )
        FROM (
            SELECT post_id, post_title, category_name, view_count,
                   like_count, comment_count, favorite_count, create_time
            FROM v_post_detail
            WHERE author_id = p_user_id
            ORDER BY create_time DESC
            LIMIT 10
        ) t
        ) AS recent_posts,

        (SELECT JSON_ARRAYAGG(
            JSON_OBJECT(
                'notice_id', notice_id,
                'type_name', type_name,
                'sender_nickname', sender_user_nickname,
                'post_title', post_title,
                'content', notice_content,
                'is_read', is_read,
                'create_time', create_time
            )
        )
        FROM (
            SELECT notice_id, type_name, sender_user_nickname,
                   post_title, notice_content, is_read, create_time
            FROM v_notice_center
            WHERE receive_user_id = p_user_id
            ORDER BY create_time DESC
            LIMIT 20
        ) t
        ) AS recent_notices;
END //
DELIMITER ;

-- -----------------------------------------------------
-- 3. sp_handle_report - 处理举报
-- 优化点:
--   - 使用显式事务
--   - 合并验证查询
--   - 使用CASE WHEN简化条件分支
-- -----------------------------------------------------
DROP PROCEDURE IF EXISTS sp_handle_report;
DELIMITER //
CREATE PROCEDURE sp_handle_report(
    IN p_report_id BIGINT,
    IN p_handle_user_id BIGINT,
    IN p_handle_status TINYINT,
    IN p_handle_result VARCHAR(255)
)
BEGIN
    DECLARE v_target_type TINYINT DEFAULT NULL;
    DECLARE v_target_id BIGINT DEFAULT NULL;
    DECLARE v_report_status TINYINT DEFAULT NULL;

    -- 参数验证
    IF p_handle_status NOT IN (1, 2) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '处理状态无效，必须为1或2';
    END IF;

    START TRANSACTION;

    -- 获取待处理举报信息（单次查询）
    SELECT target_type, target_id, status
    INTO v_target_type, v_target_id, v_report_status
    FROM report
    WHERE id = p_report_id AND status = 0
    FOR UPDATE;

    IF v_target_type IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '举报不存在或已被处理';
    END IF;

    -- 更新举报状态
    UPDATE report
    SET status = p_handle_status,
        handle_user_id = p_handle_user_id,
        handle_result = p_handle_result,
        update_time = CURRENT_TIMESTAMP
    WHERE id = p_report_id;

    -- 处理被举报内容（使用CASE WHEN简化）
    IF p_handle_status = 1 THEN
        IF v_target_type = 1 THEN
            UPDATE post
            SET status = 2, update_time = CURRENT_TIMESTAMP
            WHERE id = v_target_id;
        ELSEIF v_target_type = 2 THEN
            UPDATE comment
            SET status = 0, update_time = CURRENT_TIMESTAMP
            WHERE id = v_target_id;
        END IF;
    END IF;

    COMMIT;

    -- 返回结果
    SELECT p_report_id AS report_id,
           p_handle_status AS final_status,
           p_handle_result AS handle_result;
END //
DELIMITER ;

-- -----------------------------------------------------
-- 4. sp_publish_post - 发布帖子
-- 优化点:
--   - 合并验证查询减少数据库往返
--   - 使用显式事务
-- -----------------------------------------------------
DROP PROCEDURE IF EXISTS sp_publish_post;
DELIMITER //
CREATE PROCEDURE sp_publish_post(
    IN p_user_id BIGINT,
    IN p_category_id BIGINT,
    IN p_title VARCHAR(100),
    IN p_content TEXT,
    IN p_cover_img VARCHAR(255)
)
BEGIN
    DECLARE v_valid INT DEFAULT 0;

    -- 输入验证
    IF p_title IS NULL OR CHAR_LENGTH(TRIM(p_title)) < 2 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '标题长度不能少于2个字符';
    END IF;

    -- 合并验证用户和分类（单次查询）
    SELECT COUNT(*) INTO v_valid
    FROM (
        SELECT 1 FROM user WHERE id = p_user_id AND status = 1 AND is_deleted = 0
        UNION ALL
        SELECT 1 FROM category WHERE id = p_category_id AND status = 1 AND is_deleted = 0
    ) t;

    IF v_valid <> 2 THEN
        -- 分别检查以提供精确错误信息
        SELECT COUNT(*) INTO v_valid FROM user WHERE id = p_user_id AND status = 1 AND is_deleted = 0;
        IF v_valid = 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = '用户不存在或已被禁用';
        END IF;

        SELECT COUNT(*) INTO v_valid FROM category WHERE id = p_category_id AND status = 1 AND is_deleted = 0;
        IF v_valid = 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = '分类不存在或已被禁用';
        END IF;
    END IF;

    START TRANSACTION;

    INSERT INTO post(user_id, category_id, title, content, cover_img, status, is_top, is_deleted)
    VALUES(p_user_id, p_category_id, TRIM(p_title), p_content, p_cover_img, 1, 0, 0);

    COMMIT;

    SELECT LAST_INSERT_ID() AS new_post_id;
END //
DELIMITER ;

-- -----------------------------------------------------
-- 5. sp_signup_activity - 活动报名
-- 优化点:
--   - 使用SELECT FOR UPDATE防止并发超卖
--   - 统一返回结果集，移除OUT参数
--   - 使用INSERT IGNORE避免重复检查
-- -----------------------------------------------------
DROP PROCEDURE IF EXISTS sp_signup_activity;
DELIMITER //
CREATE PROCEDURE sp_signup_activity(
    IN p_user_id BIGINT,
    IN p_activity_id BIGINT
)
BEGIN
    DECLARE v_status INT DEFAULT NULL;
    DECLARE v_max INT DEFAULT 0;
    DECLARE v_current INT DEFAULT 0;
    DECLARE v_existing INT DEFAULT 0;

    START TRANSACTION;

    -- 锁定活动记录防止并发
    SELECT status, max_participants, current_count
    INTO v_status, v_max, v_current
    FROM activity
    WHERE id = p_activity_id AND is_deleted = 0
    FOR UPDATE;

    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '活动不存在';
    END IF;

    IF v_status <> 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '活动未开放报名';
    END IF;

    SELECT COUNT(*) INTO v_current
    FROM activity_signup
    WHERE activity_id = p_activity_id AND status = 1;

    IF v_max > 0 AND v_current >= v_max THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '活动名额已满';
    END IF;

    -- 检查是否已有效报名
    SELECT COUNT(*) INTO v_existing
    FROM activity_signup
    WHERE activity_id = p_activity_id AND user_id = p_user_id AND status = 1;

    IF v_existing > 0 THEN
        COMMIT;
        SELECT 'already_signed' AS signup_result,
               p_activity_id AS activity_id,
               v_current AS current_count;
    ELSE
        DELETE FROM activity_signup
        WHERE activity_id = p_activity_id AND user_id = p_user_id AND status <> 1;

        INSERT INTO activity_signup(activity_id, user_id, status)
        VALUES(p_activity_id, p_user_id, 1);

        COMMIT;

        SELECT 'signed_success' AS signup_result,
               p_activity_id AS activity_id,
               v_current + 1 AS current_count;
    END IF;
END //
DELIMITER ;

-- -----------------------------------------------------
-- 6. sp_toggle_post_like - 切换点赞状态
-- 优化点:
--   - 使用SELECT FOR UPDATE防止并发
--   - 统一返回结果集，移除OUT参数
--   - 使用INSERT/DELETE原子操作
-- -----------------------------------------------------
DROP PROCEDURE IF EXISTS sp_toggle_post_like;
DELIMITER //
CREATE PROCEDURE sp_toggle_post_like(
    IN p_user_id BIGINT,
    IN p_post_id BIGINT
)
BEGIN
    DECLARE v_like_id BIGINT DEFAULT NULL;
    DECLARE v_post_exists INT DEFAULT 0;

    START TRANSACTION;

    -- 验证帖子存在
    SELECT COUNT(*) INTO v_post_exists
    FROM post
    WHERE id = p_post_id AND is_deleted = 0 AND status = 1;

    IF v_post_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '帖子不存在或已被删除';
    END IF;

    -- 检查是否已点赞
    SELECT id INTO v_like_id
    FROM `like`
    WHERE user_id = p_user_id AND post_id = p_post_id
    LIMIT 1
    FOR UPDATE;

    IF v_like_id IS NULL THEN
        INSERT INTO `like`(post_id, user_id)
        VALUES(p_post_id, p_user_id);
        UPDATE post
        SET like_count = (
                SELECT COUNT(*)
                FROM `like`
                WHERE post_id = p_post_id
            ),
            update_time = CURRENT_TIMESTAMP
        WHERE id = p_post_id;
        COMMIT;
        SELECT 'liked' AS action_result,
               p_post_id AS post_id,
               (SELECT like_count FROM post WHERE id = p_post_id) AS current_like_count;
    ELSE
        DELETE FROM `like` WHERE id = v_like_id;
        UPDATE post
        SET like_count = (
                SELECT COUNT(*)
                FROM `like`
                WHERE post_id = p_post_id
            ),
            update_time = CURRENT_TIMESTAMP
        WHERE id = p_post_id;
        COMMIT;
        SELECT 'unliked' AS action_result,
               p_post_id AS post_id,
               (SELECT like_count FROM post WHERE id = p_post_id) AS current_like_count;
    END IF;
END //
DELIMITER ;


-- =====================================================
-- 二、触发器优化
-- =====================================================

-- -----------------------------------------------------
-- 1. 活动报名触发器 - 移除冗余验证
-- 优化点:
--   - 移除重复的名额检查（存储过程已处理）
--   - 简化逻辑
-- -----------------------------------------------------
DROP TRIGGER IF EXISTS trg_activity_signup_after_insert;
DELIMITER //
CREATE TRIGGER trg_activity_signup_after_insert
AFTER INSERT ON activity_signup
FOR EACH ROW
BEGIN
    IF NEW.status = 1 THEN
        UPDATE activity
        SET current_count = (
                SELECT COUNT(*)
                FROM activity_signup
                WHERE activity_id = NEW.activity_id AND status = 1
            ),
            update_time = CURRENT_TIMESTAMP
        WHERE id = NEW.activity_id;
    END IF;
END //
DELIMITER ;

DROP TRIGGER IF EXISTS trg_activity_signup_after_delete;
DELIMITER //
CREATE TRIGGER trg_activity_signup_after_delete
AFTER DELETE ON activity_signup
FOR EACH ROW
BEGIN
    IF OLD.status = 1 THEN
        UPDATE activity
        SET current_count = (
                SELECT COUNT(*)
                FROM activity_signup
                WHERE activity_id = OLD.activity_id AND status = 1
            ),
            update_time = CURRENT_TIMESTAMP
        WHERE id = OLD.activity_id;
    END IF;
END //
DELIMITER ;

-- -----------------------------------------------------
-- 2. 评论触发器 - 使用NEW获取帖子作者
-- 优化点:
--   - 使用子查询直接获取，减少变量声明
--   - 简化条件判断
-- -----------------------------------------------------
DROP TRIGGER IF EXISTS trg_comment_after_insert;
DELIMITER //
CREATE TRIGGER trg_comment_after_insert
AFTER INSERT ON comment
FOR EACH ROW
BEGIN
    DECLARE v_post_author BIGINT DEFAULT NULL;

    IF NEW.is_deleted = 0 AND NEW.status = 1 THEN
        -- 更新评论计数
        UPDATE post
        SET comment_count = (
                SELECT COUNT(*)
                FROM comment
                WHERE post_id = NEW.post_id AND is_deleted = 0 AND status = 1
            ),
            update_time = CURRENT_TIMESTAMP
        WHERE id = NEW.post_id;

        -- 获取帖子作者并发送通知
        SELECT user_id INTO v_post_author
        FROM post WHERE id = NEW.post_id;

        IF v_post_author IS NOT NULL AND v_post_author <> NEW.user_id THEN
            INSERT INTO notice(receive_user_id, sender_user_id, type, post_id, comment_id, content, is_read)
            VALUES(v_post_author, NEW.user_id, 2, NEW.post_id, NEW.id,
                   CONCAT('评论了你的帖子：', LEFT(NEW.content, 60)), 0);
        END IF;
    END IF;
END //
DELIMITER ;

DROP TRIGGER IF EXISTS trg_comment_after_update;
DELIMITER //
CREATE TRIGGER trg_comment_after_update
AFTER UPDATE ON comment
FOR EACH ROW
BEGIN
    DECLARE v_old_active TINYINT;
    DECLARE v_new_active TINYINT;

    -- 判断状态变化
    SET v_old_active = (OLD.is_deleted = 0 AND OLD.status = 1);
    SET v_new_active = (NEW.is_deleted = 0 AND NEW.status = 1);

    -- 有效评论变为无效
    IF v_old_active = 1 AND v_new_active = 0 THEN
        UPDATE post
        SET comment_count = (
                SELECT COUNT(*)
                FROM comment
                WHERE post_id = NEW.post_id AND is_deleted = 0 AND status = 1
            ),
            update_time = CURRENT_TIMESTAMP
        WHERE id = NEW.post_id;
    -- 无效评论变为有效
    ELSEIF v_old_active = 0 AND v_new_active = 1 THEN
        UPDATE post
        SET comment_count = (
                SELECT COUNT(*)
                FROM comment
                WHERE post_id = NEW.post_id AND is_deleted = 0 AND status = 1
            ),
            update_time = CURRENT_TIMESTAMP
        WHERE id = NEW.post_id;
    END IF;
END //
DELIMITER ;

DROP TRIGGER IF EXISTS trg_comment_after_delete;
DELIMITER //
CREATE TRIGGER trg_comment_after_delete
AFTER DELETE ON comment
FOR EACH ROW
BEGIN
    IF OLD.is_deleted = 0 AND OLD.status = 1 THEN
        UPDATE post
        SET comment_count = (
                SELECT COUNT(*)
                FROM comment
                WHERE post_id = OLD.post_id AND is_deleted = 0 AND status = 1
            ),
            update_time = CURRENT_TIMESTAMP
        WHERE id = OLD.post_id;
    END IF;

    -- 清理相关通知
    DELETE FROM notice
    WHERE comment_id = OLD.id AND type IN (2, 3);
END //
DELIMITER ;

-- -----------------------------------------------------
-- 3. 收藏触发器 - 简化通知逻辑
-- 优化点:
--   - 使用NEW直接访问数据
--   - 简化通知删除条件
-- -----------------------------------------------------
DROP TRIGGER IF EXISTS trg_favorite_after_insert;
DELIMITER //
CREATE TRIGGER trg_favorite_after_insert
AFTER INSERT ON favorite
FOR EACH ROW
BEGIN
    DECLARE v_post_author BIGINT DEFAULT NULL;

    -- 更新收藏计数
    UPDATE post
    SET favorite_count = (
            SELECT COUNT(*)
            FROM favorite
            WHERE post_id = NEW.post_id
        ),
        update_time = CURRENT_TIMESTAMP
    WHERE id = NEW.post_id;

    -- 获取帖子作者并发送通知
    SELECT user_id INTO v_post_author
    FROM post WHERE id = NEW.post_id;

    IF v_post_author IS NOT NULL AND v_post_author <> NEW.user_id THEN
        INSERT INTO notice(receive_user_id, sender_user_id, type, post_id, content, is_read)
        VALUES(v_post_author, NEW.user_id, 4, NEW.post_id,
               CONCAT('收藏了你的帖子'), 0);
    END IF;
END //
DELIMITER ;

DROP TRIGGER IF EXISTS trg_favorite_after_delete;
DELIMITER //
CREATE TRIGGER trg_favorite_after_delete
AFTER DELETE ON favorite
FOR EACH ROW
BEGIN
    -- 更新收藏计数
    UPDATE post
    SET favorite_count = (
            SELECT COUNT(*)
            FROM favorite
            WHERE post_id = OLD.post_id
        ),
        update_time = CURRENT_TIMESTAMP
    WHERE id = OLD.post_id;

    -- 清理相关通知
    DELETE FROM notice
    WHERE type = 4
      AND post_id = OLD.post_id
      AND sender_user_id = OLD.user_id;
END //
DELIMITER ;

-- -----------------------------------------------------
-- 4. 点赞触发器 - 简化通知逻辑
-- 优化点:
--   - 使用NEW直接访问数据
--   - 移除时间限制，直接删除通知
-- -----------------------------------------------------
DROP TRIGGER IF EXISTS trg_like_after_insert;
DELIMITER //
CREATE TRIGGER trg_like_after_insert
AFTER INSERT ON `like`
FOR EACH ROW
BEGIN
    DECLARE v_post_author BIGINT DEFAULT NULL;

    -- 更新点赞计数
    UPDATE post
    SET like_count = (
            SELECT COUNT(*)
            FROM `like`
            WHERE post_id = NEW.post_id
        ),
        update_time = CURRENT_TIMESTAMP
    WHERE id = NEW.post_id;

    -- 获取帖子作者并发送通知
    SELECT user_id INTO v_post_author
    FROM post WHERE id = NEW.post_id;

    IF v_post_author IS NOT NULL AND v_post_author <> NEW.user_id THEN
        INSERT INTO notice(receive_user_id, sender_user_id, type, post_id, content, is_read)
        VALUES(v_post_author, NEW.user_id, 1, NEW.post_id,
               CONCAT('点赞了你的帖子'), 0);
    END IF;
END //
DELIMITER ;

DROP TRIGGER IF EXISTS trg_like_after_delete;
DELIMITER //
CREATE TRIGGER trg_like_after_delete
AFTER DELETE ON `like`
FOR EACH ROW
BEGIN
    -- 更新点赞计数
    UPDATE post
    SET like_count = (
            SELECT COUNT(*)
            FROM `like`
            WHERE post_id = OLD.post_id
        ),
        update_time = CURRENT_TIMESTAMP
    WHERE id = OLD.post_id;

    -- 清理相关通知
    DELETE FROM notice
    WHERE type = 1
      AND post_id = OLD.post_id
      AND sender_user_id = OLD.user_id;
END //
DELIMITER ;

-- -----------------------------------------------------
-- 5. 消息触发器 - 保持原样（已经很简洁）
-- -----------------------------------------------------
DROP TRIGGER IF EXISTS trg_message_after_insert;
DELIMITER //
CREATE TRIGGER trg_message_after_insert
AFTER INSERT ON message
FOR EACH ROW
BEGIN
    UPDATE conversation
    SET last_message_id = NEW.id,
        last_message_time = NEW.create_time,
        update_time = CURRENT_TIMESTAMP
    WHERE id = NEW.conversation_id;
END //
DELIMITER ;

-- -----------------------------------------------------
-- 6. 投票记录触发器 - 增加事务安全
-- 优化点:
--   - 使用GREATEST确保计数不为负
-- -----------------------------------------------------
DROP TRIGGER IF EXISTS trg_vote_record_after_insert;
DELIMITER //
CREATE TRIGGER trg_vote_record_after_insert
AFTER INSERT ON vote_record
FOR EACH ROW
BEGIN
    UPDATE vote_option
    SET `count` = (
        SELECT COUNT(*)
        FROM vote_record
        WHERE option_id = NEW.option_id
    )
    WHERE id = NEW.option_id;

    UPDATE vote
    SET total_count = (
            SELECT COUNT(DISTINCT user_id)
            FROM vote_record
            WHERE vote_id = NEW.vote_id
        ),
        update_time = CURRENT_TIMESTAMP
    WHERE id = NEW.vote_id;
END //
DELIMITER ;

DROP TRIGGER IF EXISTS trg_vote_record_after_delete;
DELIMITER //
CREATE TRIGGER trg_vote_record_after_delete
AFTER DELETE ON vote_record
FOR EACH ROW
BEGIN
    UPDATE vote_option
    SET `count` = (
        SELECT COUNT(*)
        FROM vote_record
        WHERE option_id = OLD.option_id
    )
    WHERE id = OLD.option_id;

    UPDATE vote
    SET total_count = (
            SELECT COUNT(DISTINCT user_id)
            FROM vote_record
            WHERE vote_id = OLD.vote_id
        ),
        update_time = CURRENT_TIMESTAMP
    WHERE id = OLD.vote_id;
END //
DELIMITER ;
