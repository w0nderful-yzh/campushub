-- ==========================================================
-- CampusHub 示例数据插入脚本
-- 请在 MySQL 客户端中连接到 `campushub` 数据库后执行该脚本
-- ==========================================================

USE `campushub`;

-- ----------------------------
-- 1. 插入示例分类 (Category)
-- ----------------------------
-- 假设 category 表有 id, name, description/sort 等字段
INSERT IGNORE INTO `category` (`id`, `name`, `description`, `sort`, `create_time`, `update_time`) VALUES
(1, '校园新鲜事', '校园最新动态、通告', 1, NOW(), NOW()),
(2, '二手交易', '校园闲置物品买卖', 2, NOW(), NOW()),
(3, '失物招领', '找回遗失物品或发布捡到的物品', 3, NOW(), NOW()),
(4, '脱单交友', '寻找学习搭子、运动搭子', 4, NOW(), NOW()),
(5, '表白墙', '校园浪漫表白', 5, NOW(), NOW());

-- ----------------------------
-- 2. 插入示例用户 (User)
-- 明文密码仅为示例，如果是加密(例如BCrypt/MD5)请按照系统实际密码替换
-- 此处的密码明文如果是 123456，你后续可以直接用这个登录
-- 图片使用了真实可访问的随机占位图或真实URL
-- ----------------------------
INSERT IGNORE INTO `user` (`id`, `username`, `password`, `nickname`, `avatar`, `gender`, `phone`, `email`, `student_no`, `college`, `major`, `profile`, `status`, `role`, `is_deleted`, `create_time`, `update_time`) VALUES
(1, 'admin', '123456', '超级管理员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 1, '13800138000', 'admin@example.com', 20260001, '计算机学院', '软件工程', '我是管理员，拥有最高权限', 1, 1, 0, NOW(), NOW()),
(2, 'yzh2026', '123456', '杨子豪', 'https://api.dicebear.com/7.x/avataaars/svg?seed=yzh', 1, '13800138001', 'yzh@example.com', 20260002, '信息工程学院', '人工智能', '校园社区重度依赖者', 1, 0, 0, NOW(), NOW()),
(3, 'alice_wonder', '123456', '爱丽丝', 'https://api.dicebear.com/7.x/avataaars/svg?seed=alice', 2, '13800138002', 'alice@example.com', 20260003, '外国语学院', '英语', '喜爱摄影、美食与旅游', 1, 0, 0, NOW(), NOW()),
(4, 'bob_builder', '123456', '建大鲍勃', 'https://api.dicebear.com/7.x/avataaars/svg?seed=bob', 1, '13800138003', 'bob@example.com', 20260004, '建筑学院', '土木工程', '土木人，打灰魂', 1, 0, 0, NOW(), NOW());

-- ----------------------------
-- 3. 插入示例帖子 (Post)
-- 关联的 userId 分别为 1, 2, 3, 4
-- ----------------------------
INSERT IGNORE INTO `post` (`id`, `user_id`, `category_id`, `title`, `content`, `cover_img`, `view_count`, `like_count`, `comment_count`, `favorite_count`, `status`, `is_top`, `is_deleted`, `create_time`, `update_time`) VALUES
(1, 1, 1, '各位同学注意：校园迎新晚会即将举办', '请大家下周三晚在体育馆集合，有精彩的文艺表演！大家不要错过。', 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&q=80', 120, 15, 2, 5, 1, 1, 0, NOW(), NOW()),
(2, 2, 2, '出个99新机械键盘', '学长毕业血泪甩卖，罗技青轴，手感极佳，平时很爱惜，带包装盒。价格150元，有需要的滴滴。', 'https://images.unsplash.com/photo-1595225476474-87563907a212?w=800&q=80', 45, 8, 1, 2, 1, 0, 0, NOW(), NOW()),
(3, 3, 3, '捡到一张饭卡：李某某', '今天下午在二食堂一楼东侧餐桌捡到一张饭卡，名叫李某某，请认识的同学转告一下，稍后我会交到一楼失物招领处。', 'https://images.unsplash.com/photo-1563207153-f404786d11cd?w=800&q=80', 88, 3, 0, 1, 1, 0, 0, NOW(), NOW()),
(4, 4, 1, '期末考试周延期通知说明', '听导员说，因为天气原因本次期末考试统一延期到下半周，图书馆座位请大家凭自觉有序占座！', 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=800&q=80', 300, 45, 3, 10, 1, 0, 0, NOW(), NOW()),
(5, 3, 5, '暗恋了3楼靠窗的小哥哥', '每天下午都能看到他在自习室学习，好认真，想问问有没有人认识，或者给我个微信？', 'https://images.unsplash.com/photo-1518621736915-f3b1c41bfd00?w=800&q=80', 500, 120, 5, 20, 1, 0, 0, NOW(), NOW());

-- ----------------------------
-- 4. 插入示例评论 (Comment)
-- 关联前面的 post_id 和 user_id
-- ----------------------------
INSERT IGNORE INTO `comment` (`id`, `post_id`, `user_id`, `content`, `parent_id`, `reply_to_user_id`, `like_count`, `is_deleted`, `create_time`, `update_time`) VALUES
(1, 1, 2, '太棒啦！一定去看晚会！', NULL, NULL, 5, 0, NOW(), NOW()),
(2, 1, 3, '有抽奖环节吗？', NULL, NULL, 2, 0, NOW(), NOW()),
(3, 2, 4, '这键盘100出不出？学生党预算有限啊~', NULL, NULL, 1, 0, NOW(), NOW()),
(4, 4, 2, '太好啦，终于能多复习两天了。', NULL, NULL, 20, 0, NOW(), NOW()),
(5, 5, 1, '这种事最好的办法是直接递纸条！', NULL, NULL, 50, 0, NOW(), NOW()),
(6, 5, 4, '我是3楼靠窗的，但应该不是我（苦笑）', NULL, NULL, 15, 0, NOW(), NOW());

-- ----------------------------
-- 5. 插入图库 (Image) - 假设帖子里有多图
-- 这个表假设是统一管理的多图或照片墙
-- ----------------------------
INSERT IGNORE INTO `image` (`id`, `post_id`, `url`, `create_time`) VALUES
(1, 2, 'https://images.unsplash.com/photo-1595225476474-87563907a212?w=800&q=80', NOW()),
(2, 2, 'https://images.unsplash.com/photo-1555532538-dcdbd01d3738?w=800&q=80', NOW());
