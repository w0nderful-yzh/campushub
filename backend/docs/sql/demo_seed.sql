-- CampusHub defense demo data
-- All demo accounts use password: 123456
-- Safe to run repeatedly. Only data owned by the usernames below is rebuilt.

SET NAMES utf8mb4;
SET time_zone = '+08:00';

START TRANSACTION;

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_demo_users (
    id BIGINT PRIMARY KEY
);
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_demo_users_2 (
    id BIGINT PRIMARY KEY
);
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_demo_users_3 (
    id BIGINT PRIMARY KEY
);
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_demo_users_4 (
    id BIGINT PRIMARY KEY
);
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_demo_posts (
    id BIGINT PRIMARY KEY
);
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_demo_comments (
    id BIGINT PRIMARY KEY
);
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_demo_votes (
    id BIGINT PRIMARY KEY
);
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_demo_activities (
    id BIGINT PRIMARY KEY
);
TRUNCATE TABLE tmp_demo_users;
TRUNCATE TABLE tmp_demo_users_2;
TRUNCATE TABLE tmp_demo_users_3;
TRUNCATE TABLE tmp_demo_users_4;
TRUNCATE TABLE tmp_demo_posts;
TRUNCATE TABLE tmp_demo_comments;
TRUNCATE TABLE tmp_demo_votes;
TRUNCATE TABLE tmp_demo_activities;

INSERT INTO tmp_demo_users (id)
SELECT id
FROM `user`
WHERE username IN (
    'demo_admin', 'demo_student', 'chen_yiming', 'sun_yue', 'wu_yue',
    'li_laoshi', 'he_yuqing', 'zhao_kexin', 'wang_haoran', 'guo_xinyi'
);

INSERT INTO tmp_demo_users_2 (id) SELECT id FROM tmp_demo_users;
INSERT INTO tmp_demo_users_3 (id) SELECT id FROM tmp_demo_users;
INSERT INTO tmp_demo_users_4 (id) SELECT id FROM tmp_demo_users;

INSERT INTO tmp_demo_posts (id)
SELECT id FROM post WHERE user_id IN (SELECT id FROM tmp_demo_users);

INSERT INTO tmp_demo_comments (id)
SELECT id
FROM comment
WHERE user_id IN (SELECT id FROM tmp_demo_users)
   OR post_id IN (SELECT id FROM tmp_demo_posts);

INSERT INTO tmp_demo_votes (id)
SELECT id FROM vote WHERE user_id IN (SELECT id FROM tmp_demo_users);

INSERT INTO tmp_demo_activities (id)
SELECT id FROM activity WHERE user_id IN (SELECT id FROM tmp_demo_users);

-- Remove the previous demo dataset in foreign-key-safe order.
DELETE m
FROM message m
JOIN conversation c ON c.id = m.conversation_id
WHERE c.user_a_id IN (SELECT id FROM tmp_demo_users)
   OR c.user_b_id IN (SELECT id FROM tmp_demo_users_2);

DELETE FROM conversation
WHERE user_a_id IN (SELECT id FROM tmp_demo_users)
   OR user_b_id IN (SELECT id FROM tmp_demo_users_2);

DELETE FROM vote_record
WHERE user_id IN (SELECT id FROM tmp_demo_users)
   OR vote_id IN (SELECT id FROM tmp_demo_votes);

DELETE FROM vote_option
WHERE vote_id IN (SELECT id FROM tmp_demo_votes);

DELETE FROM vote
WHERE id IN (SELECT id FROM tmp_demo_votes);

DELETE FROM activity_signup
WHERE user_id IN (SELECT id FROM tmp_demo_users)
   OR activity_id IN (SELECT id FROM tmp_demo_activities);

DELETE FROM activity
WHERE id IN (SELECT id FROM tmp_demo_activities);

DELETE r
FROM report r
LEFT JOIN post p ON r.target_type = 1 AND r.target_id = p.id
LEFT JOIN comment c ON r.target_type = 2 AND r.target_id = c.id
WHERE r.report_user_id IN (SELECT id FROM tmp_demo_users)
   OR r.handle_user_id IN (SELECT id FROM tmp_demo_users_2)
   OR (r.target_type = 1 AND r.target_id IN (SELECT id FROM tmp_demo_posts))
   OR (r.target_type = 2 AND r.target_id IN (SELECT id FROM tmp_demo_comments));

DELETE n
FROM notice n
LEFT JOIN post p ON p.id = n.post_id
LEFT JOIN comment c ON c.id = n.comment_id
WHERE n.receive_user_id IN (SELECT id FROM tmp_demo_users)
   OR n.sender_user_id IN (SELECT id FROM tmp_demo_users_2)
   OR n.post_id IN (SELECT id FROM tmp_demo_posts)
   OR n.comment_id IN (SELECT id FROM tmp_demo_comments);

DELETE FROM `like`
WHERE user_id IN (SELECT id FROM tmp_demo_users)
   OR post_id IN (SELECT id FROM tmp_demo_posts);

DELETE FROM favorite
WHERE user_id IN (SELECT id FROM tmp_demo_users)
   OR post_id IN (SELECT id FROM tmp_demo_posts);

DELETE FROM comment
WHERE id IN (SELECT id FROM tmp_demo_comments);

DELETE FROM image
WHERE post_id IN (SELECT id FROM tmp_demo_posts);

DELETE FROM post
WHERE id IN (SELECT id FROM tmp_demo_posts);

DELETE FROM follow
WHERE user_id IN (SELECT id FROM tmp_demo_users)
   OR follow_user_id IN (SELECT id FROM tmp_demo_users_2);

DELETE FROM `user`
WHERE id IN (SELECT id FROM tmp_demo_users);

-- Keep the six application categories available even on a fresh database.
INSERT INTO category (name, icon, sort, discription, status, is_deleted)
VALUES
    ('二手交易', NULL, 1, '闲置物品与毕业季好物交换', 1, 0),
    ('失物招领', NULL, 2, '校园失物发布与认领', 1, 0),
    ('校园求助', NULL, 3, '学习生活问题互助', 1, 0),
    ('拼车拼饭', NULL, 4, '出行与美食搭伴', 1, 0),
    ('兼职实习', NULL, 5, '校内岗位、实习与求职信息', 1, 0),
    ('学习交流', NULL, 6, '课程资料、竞赛与经验分享', 1, 0)
ON DUPLICATE KEY UPDATE
    sort = VALUES(sort),
    discription = VALUES(discription),
    status = 1,
    is_deleted = 0;

-- This BCrypt value is the password 123456.
SET @demo_password = '$2a$10$gDh8KlErPGYqk6RH83AvQuaR2.yufsy4VDVHSq1jdgJoPETV5.18a';

INSERT INTO `user` (
    username, password, nickname, avatar, gender, phone, email, student_no,
    college, major, profile, status, role, is_deleted, create_time, update_time
)
VALUES
    ('demo_admin', @demo_password, '周老师', NULL, 1, '13800001001', 'zhou@campushub.edu.cn', '2026001',
     '计算机学院', '软件工程', 'CampusHub 演示管理员，负责社区内容审核与答辩展示。', 1, 1, 0, '2026-03-01 09:00:00', NOW()),
    ('demo_student', @demo_password, '林知夏', NULL, 2, '13800001002', 'lin.zhixia@campus.edu.cn', '2023010201',
     '计算机学院', '软件工程', '喜欢产品设计、前端开发和记录校园生活。', 1, 0, 0, '2026-03-08 10:20:00', NOW()),
    ('chen_yiming', @demo_password, '陈一鸣', NULL, 1, NULL, 'chen.yiming@campus.edu.cn', '2023010215',
     '计算机学院', '计算机科学与技术', '后端开发爱好者，正在准备秋招。', 1, 0, 0, '2026-03-10 14:30:00', NOW()),
    ('sun_yue', @demo_password, '孙悦', NULL, 2, NULL, 'sun.yue@campus.edu.cn', '2024020312',
     '商学院', '市场营销', '校园活动策划、摄影和探店。', 1, 0, 0, '2026-03-12 18:10:00', NOW()),
    ('wu_yue', @demo_password, '吴越', NULL, 1, NULL, 'wu.yue@campus.edu.cn', '2023020408',
     '体育学院', '运动训练', '羽毛球校队成员，欢迎约球。', 1, 0, 0, '2026-03-14 08:45:00', NOW()),
    ('li_laoshi', @demo_password, '李老师', NULL, 2, NULL, 'li.career@campus.edu.cn', '2026018',
     '就业指导中心', '职业发展', '提供简历修改、实习咨询和就业政策答疑。', 1, 0, 0, '2026-03-18 09:15:00', NOW()),
    ('he_yuqing', @demo_password, '何雨晴', NULL, 2, NULL, 'he.yuqing@campus.edu.cn', '2023040122',
     '人文学院', '汉语言文学', '青年志愿者协会成员，热衷公益活动。', 1, 0, 0, '2026-03-20 16:00:00', NOW()),
    ('zhao_kexin', @demo_password, '赵可心', NULL, 2, NULL, 'zhao.kexin@campus.edu.cn', '2025010109',
     '外国语学院', '英语', '大一新生，正在努力解锁校园地图。', 1, 0, 0, '2026-03-25 12:20:00', NOW()),
    ('wang_haoran', @demo_password, '王浩然', NULL, 1, NULL, 'wang.haoran@campus.edu.cn', '2022010331',
     '电子信息学院', '通信工程', '毕业季清理宿舍，也分享考研经验。', 1, 0, 0, '2026-03-28 20:05:00', NOW()),
    ('guo_xinyi', @demo_password, '郭心怡', NULL, 2, NULL, 'guo.xinyi@campus.edu.cn', '2023030516',
     '艺术学院', '视觉传达设计', '做海报、拍照片，偶尔参加乐队排练。', 1, 0, 0, '2026-04-02 11:40:00', NOW());

SELECT id INTO @u_admin FROM `user` WHERE username = 'demo_admin';
SELECT id INTO @u_demo FROM `user` WHERE username = 'demo_student';
SELECT id INTO @u_chen FROM `user` WHERE username = 'chen_yiming';
SELECT id INTO @u_sun FROM `user` WHERE username = 'sun_yue';
SELECT id INTO @u_wu FROM `user` WHERE username = 'wu_yue';
SELECT id INTO @u_li FROM `user` WHERE username = 'li_laoshi';
SELECT id INTO @u_he FROM `user` WHERE username = 'he_yuqing';
SELECT id INTO @u_zhao FROM `user` WHERE username = 'zhao_kexin';
SELECT id INTO @u_wang FROM `user` WHERE username = 'wang_haoran';
SELECT id INTO @u_guo FROM `user` WHERE username = 'guo_xinyi';

SELECT id INTO @c_trade FROM category WHERE name = '二手交易';
SELECT id INTO @c_lost FROM category WHERE name = '失物招领';
SELECT id INTO @c_help FROM category WHERE name = '校园求助';
SELECT id INTO @c_food FROM category WHERE name = '拼车拼饭';
SELECT id INTO @c_job FROM category WHERE name = '兼职实习';
SELECT id INTO @c_study FROM category WHERE name = '学习交流';

INSERT INTO post (
    user_id, category_id, title, content, cover_img, view_count,
    like_count, comment_count, favorite_count, status, is_top, is_deleted,
    create_time, update_time
)
VALUES
    (@u_admin, @c_study, 'CampusHub 期末答辩体验指南',
     '欢迎体验 CampusHub 校园社区。本次答辩可以依次演示帖子搜索、评论回复、点赞收藏、活动报名、在线投票、关注私信、消息通知和管理员举报处理。演示账号均已准备好，祝大家展示顺利！',
     NULL, 386, 0, 0, 0, 1, 1, 0, '2026-06-15 09:20:00', '2026-06-15 09:20:00'),
    (@u_demo, @c_study, '数据库系统课程答辩资料互助帖',
     '整理了一份答辩检查清单：先讲业务痛点，再展示 E-R 图和表结构，随后演示索引、事务、视图、存储过程与触发器，最后用慢查询对比说明优化效果。大家还缺哪部分资料，可以在评论区互相补充。',
     NULL, 328, 0, 0, 0, 1, 0, 0, '2026-06-15 08:35:00', '2026-06-15 08:35:00'),
    (@u_chen, @c_help, '寻找一位前端同学组队参加软件设计大赛',
     '我们已有两名后端和一名产品同学，项目方向是面向校园的智能信息聚合平台。技术栈 Spring Boot、MySQL、Redis，希望找到熟悉 Vue 3 的同学，一起冲刺校赛。',
     NULL, 214, 0, 0, 0, 1, 0, 0, '2026-06-14 21:10:00', '2026-06-14 21:10:00'),
    (@u_sun, @c_food, '周五晚去南门新开的潮汕牛肉火锅，三缺一',
     '本周五 18:20 南门集合，目前三个人，想再找一位同学拼桌。人均预计 70 元左右，口味清淡也可以，吃完还能一起逛夜市。',
     NULL, 176, 0, 0, 0, 1, 0, 0, '2026-06-14 17:45:00', '2026-06-14 17:45:00'),
    (@u_wang, @c_trade, '毕业季出 24 寸显示器和机械键盘',
     '显示器 1080P、75Hz，无坏点，带原装支架和 HDMI 线；机械键盘是茶轴，键帽刚清洗。可在东区宿舍楼下验货，打包带走更优惠。',
     NULL, 295, 0, 0, 0, 1, 0, 0, '2026-06-14 14:20:00', '2026-06-14 14:20:00'),
    (@u_zhao, @c_lost, '在图书馆捡到一张蓝色校园卡',
     '今天 12:10 左右在图书馆三楼靠窗位置捡到，卡面姓名是“刘同学”。已经交到一楼服务台，请失主带有效信息前往认领。',
     NULL, 142, 0, 0, 0, 1, 0, 0, '2026-06-14 12:35:00', '2026-06-14 12:35:00'),
    (@u_li, @c_job, '就业中心暑期实习简历门诊开放预约',
     '面向大二至研二同学，提供一对一简历诊断、岗位匹配建议和模拟面试。每人 25 分钟，本周共开放 40 个名额，请在校园活动页面预约。',
     NULL, 261, 0, 0, 0, 1, 0, 0, '2026-06-13 16:30:00', '2026-06-13 16:30:00'),
    (@u_he, @c_job, '青年志愿者协会招募暑期支教志愿者',
     '服务地点为市郊社区，活动时间 7 月 8 日至 7 月 14 日。主要负责阅读陪伴、趣味科学和美术课程，提供培训、保险与志愿时长证明。',
     NULL, 238, 0, 0, 0, 1, 0, 0, '2026-06-13 10:05:00', '2026-06-13 10:05:00'),
    (@u_wu, @c_help, '北区体育馆约羽毛球搭子',
     '每周二、周四晚上七点固定打球，水平在业余中级，双打为主。希望再找两位能稳定参加的同学，球费和场地费 AA。',
     NULL, 189, 0, 0, 0, 1, 0, 0, '2026-06-12 19:40:00', '2026-06-12 19:40:00'),
    (@u_guo, @c_lost, '寻找一个黑色无线耳机充电盒',
     '可能遗落在艺术楼 204 或二食堂二楼，盒盖贴着一枚银色星星贴纸。耳机还在我这里，找到充电盒的同学请私信我，非常感谢。',
     NULL, 117, 0, 0, 0, 1, 0, 0, '2026-06-12 15:25:00', '2026-06-12 15:25:00'),
    (@u_chen, @c_study, '计算机网络期末复习思维导图分享',
     '把应用层到数据链路层的重点整理成了五张思维导图，特别标注了 TCP 拥塞控制、子网划分和可靠传输计算题。评论区留下邮箱，我晚上统一发送。',
     NULL, 304, 0, 0, 0, 1, 0, 0, '2026-06-11 22:15:00', '2026-06-11 22:15:00'),
    (@u_sun, @c_food, '周六上午拼车去高铁站',
     '周六 9:00 从学校东门出发去高铁站，目前两人，网约车预计还能坐两位。10:30 前到站都合适，有行李也没问题。',
     NULL, 132, 0, 0, 0, 1, 0, 0, '2026-06-11 18:05:00', '2026-06-11 18:05:00'),
    (@u_wang, @c_trade, '打包出考研数学和英语资料',
     '包含高数讲义、真题册、英语阅读笔记和作文模板，书上有少量重点标记。适合刚开始准备考研的同学，全部打包 45 元。',
     NULL, 156, 0, 0, 0, 1, 0, 0, '2026-06-10 13:50:00', '2026-06-10 13:50:00'),
    (@u_zhao, @c_help, '新生求助：校园网在宿舍总是断开怎么办',
     '手机连接正常，但电脑每隔十几分钟就会断开一次，重新认证后才能使用。已经排除网线问题，请问需要修改认证客户端或网卡设置吗？',
     NULL, 203, 0, 0, 0, 1, 0, 0, '2026-06-09 20:30:00', '2026-06-09 20:30:00');

SELECT id INTO @p_guide FROM post WHERE user_id = @u_admin AND title = 'CampusHub 期末答辩体验指南';
SELECT id INTO @p_db FROM post WHERE user_id = @u_demo AND title = '数据库系统课程答辩资料互助帖';
SELECT id INTO @p_team FROM post WHERE user_id = @u_chen AND title = '寻找一位前端同学组队参加软件设计大赛';
SELECT id INTO @p_hotpot FROM post WHERE user_id = @u_sun AND title LIKE '周五晚去南门新开的潮汕牛肉火锅%';
SELECT id INTO @p_monitor FROM post WHERE user_id = @u_wang AND title LIKE '毕业季出 24 寸显示器%';
SELECT id INTO @p_card FROM post WHERE user_id = @u_zhao AND title = '在图书馆捡到一张蓝色校园卡';
SELECT id INTO @p_resume FROM post WHERE user_id = @u_li AND title = '就业中心暑期实习简历门诊开放预约';
SELECT id INTO @p_volunteer FROM post WHERE user_id = @u_he AND title = '青年志愿者协会招募暑期支教志愿者';
SELECT id INTO @p_badminton FROM post WHERE user_id = @u_wu AND title = '北区体育馆约羽毛球搭子';
SELECT id INTO @p_earphone FROM post WHERE user_id = @u_guo AND title = '寻找一个黑色无线耳机充电盒';
SELECT id INTO @p_network FROM post WHERE user_id = @u_chen AND title = '计算机网络期末复习思维导图分享';
SELECT id INTO @p_station FROM post WHERE user_id = @u_sun AND title = '周六上午拼车去高铁站';
SELECT id INTO @p_books FROM post WHERE user_id = @u_wang AND title = '打包出考研数学和英语资料';
SELECT id INTO @p_wifi FROM post WHERE user_id = @u_zhao AND title LIKE '新生求助：校园网%';

-- Real interaction rows drive the counters through the installed triggers.
INSERT INTO `like` (post_id, user_id, create_time)
VALUES
    (@p_guide, @u_demo, '2026-06-15 09:30:00'),
    (@p_guide, @u_chen, '2026-06-15 09:32:00'),
    (@p_guide, @u_sun, '2026-06-15 09:36:00'),
    (@p_guide, @u_li, '2026-06-15 09:40:00'),
    (@p_guide, @u_guo, '2026-06-15 09:48:00'),
    (@p_db, @u_chen, '2026-06-15 08:45:00'),
    (@p_db, @u_sun, '2026-06-15 08:51:00'),
    (@p_db, @u_li, '2026-06-15 08:55:00'),
    (@p_db, @u_wang, '2026-06-15 09:02:00'),
    (@p_db, @u_guo, '2026-06-15 09:08:00'),
    (@p_db, @u_zhao, '2026-06-15 09:12:00'),
    (@p_team, @u_demo, '2026-06-14 21:20:00'),
    (@p_team, @u_wang, '2026-06-14 21:28:00'),
    (@p_team, @u_guo, '2026-06-14 21:31:00'),
    (@p_hotpot, @u_demo, '2026-06-14 18:01:00'),
    (@p_hotpot, @u_zhao, '2026-06-14 18:08:00'),
    (@p_hotpot, @u_guo, '2026-06-14 18:12:00'),
    (@p_monitor, @u_demo, '2026-06-14 14:36:00'),
    (@p_monitor, @u_chen, '2026-06-14 14:40:00'),
    (@p_monitor, @u_zhao, '2026-06-14 14:58:00'),
    (@p_card, @u_demo, '2026-06-14 12:50:00'),
    (@p_card, @u_he, '2026-06-14 12:54:00'),
    (@p_card, @u_li, '2026-06-14 13:01:00'),
    (@p_resume, @u_demo, '2026-06-13 16:42:00'),
    (@p_resume, @u_chen, '2026-06-13 16:45:00'),
    (@p_resume, @u_wang, '2026-06-13 17:02:00'),
    (@p_volunteer, @u_demo, '2026-06-13 10:22:00'),
    (@p_volunteer, @u_sun, '2026-06-13 10:29:00'),
    (@p_volunteer, @u_zhao, '2026-06-13 10:35:00'),
    (@p_badminton, @u_demo, '2026-06-12 19:55:00'),
    (@p_badminton, @u_chen, '2026-06-12 20:00:00'),
    (@p_badminton, @u_wang, '2026-06-12 20:08:00'),
    (@p_earphone, @u_demo, '2026-06-12 15:40:00'),
    (@p_earphone, @u_he, '2026-06-12 15:47:00'),
    (@p_network, @u_demo, '2026-06-11 22:25:00'),
    (@p_network, @u_wang, '2026-06-11 22:36:00'),
    (@p_station, @u_demo, '2026-06-11 18:20:00'),
    (@p_station, @u_chen, '2026-06-11 18:30:00'),
    (@p_books, @u_demo, '2026-06-10 14:06:00'),
    (@p_wifi, @u_chen, '2026-06-09 20:45:00'),
    (@p_wifi, @u_demo, '2026-06-09 20:50:00');

INSERT INTO favorite (post_id, user_id, create_time)
VALUES
    (@p_guide, @u_demo, '2026-06-15 09:31:00'),
    (@p_guide, @u_chen, '2026-06-15 09:33:00'),
    (@p_db, @u_chen, '2026-06-15 08:46:00'),
    (@p_db, @u_li, '2026-06-15 08:56:00'),
    (@p_db, @u_wang, '2026-06-15 09:03:00'),
    (@p_team, @u_demo, '2026-06-14 21:21:00'),
    (@p_monitor, @u_zhao, '2026-06-14 14:59:00'),
    (@p_resume, @u_demo, '2026-06-13 16:43:00'),
    (@p_resume, @u_chen, '2026-06-13 16:46:00'),
    (@p_volunteer, @u_zhao, '2026-06-13 10:36:00'),
    (@p_network, @u_demo, '2026-06-11 22:26:00'),
    (@p_books, @u_demo, '2026-06-10 14:07:00'),
    (@p_wifi, @u_chen, '2026-06-09 20:46:00');

INSERT INTO comment (
    post_id, user_id, parent_id, reply_user_id, content, like_count,
    status, is_deleted, create_time, update_time
)
VALUES
    (@p_guide, @u_demo, 0, NULL, '功能路线很清楚，答辩时按这个顺序展示就不容易漏项。', 6, 1, 0, '2026-06-15 09:42:00', '2026-06-15 09:42:00');
SET @cm_guide = LAST_INSERT_ID();

INSERT INTO comment (post_id, user_id, parent_id, reply_user_id, content, like_count, status, is_deleted, create_time, update_time)
VALUES (@p_guide, @u_admin, @cm_guide, @u_demo, '也可以先准备两个浏览器窗口，一个普通用户，一个管理员，切换会更流畅。', 3, 1, 0, '2026-06-15 09:50:00', '2026-06-15 09:50:00');

INSERT INTO comment (post_id, user_id, parent_id, reply_user_id, content, like_count, status, is_deleted, create_time, update_time)
VALUES (@p_db, @u_chen, 0, NULL, '建议再加一页 explain 执行计划截图，优化前后的差异会很直观。', 8, 1, 0, '2026-06-15 08:48:00', '2026-06-15 08:48:00');
SET @cm_db = LAST_INSERT_ID();

INSERT INTO comment (post_id, user_id, parent_id, reply_user_id, content, like_count, status, is_deleted, create_time, update_time)
VALUES
    (@p_db, @u_demo, @cm_db, @u_chen, '好主意，我把分类组合索引的对比补进去。', 4, 1, 0, '2026-06-15 08:58:00', '2026-06-15 08:58:00'),
    (@p_db, @u_li, 0, NULL, '最后记得说明数据安全、异常回滚和后续扩展，这些通常是老师追问的重点。', 7, 1, 0, '2026-06-15 09:06:00', '2026-06-15 09:06:00'),
    (@p_team, @u_demo, 0, NULL, '我熟悉 Vue 3 和 Naive UI，可以先看看需求文档和目前的接口清单。', 5, 1, 0, '2026-06-14 21:25:00', '2026-06-14 21:25:00'),
    (@p_team, @u_guo, 0, NULL, '如果需要视觉和路演海报，我也可以一起做。', 3, 1, 0, '2026-06-14 21:35:00', '2026-06-14 21:35:00'),
    (@p_hotpot, @u_zhao, 0, NULL, '报名！我六点下课，从一教过去刚好。', 2, 1, 0, '2026-06-14 18:10:00', '2026-06-14 18:10:00'),
    (@p_monitor, @u_chen, 0, NULL, '显示器接口除了 HDMI 还有 DP 吗？想接实验室电脑。', 2, 1, 0, '2026-06-14 14:45:00', '2026-06-14 14:45:00'),
    (@p_card, @u_he, 0, NULL, '帮忙顶一下，校园卡丢了确实很着急。', 4, 1, 0, '2026-06-14 12:56:00', '2026-06-14 12:56:00'),
    (@p_resume, @u_chen, 0, NULL, '老师，后端开发方向的项目经历可以带代码仓库一起看吗？', 3, 1, 0, '2026-06-13 16:50:00', '2026-06-13 16:50:00'),
    (@p_volunteer, @u_zhao, 0, NULL, '大一学生可以报名吗？有过社区绘本活动经验。', 3, 1, 0, '2026-06-13 10:40:00', '2026-06-13 10:40:00'),
    (@p_badminton, @u_chen, 0, NULL, '周四可以稳定参加，水平一般但会轮转和基本配合。', 2, 1, 0, '2026-06-12 20:04:00', '2026-06-12 20:04:00'),
    (@p_earphone, @u_he, 0, NULL, '我转到艺术学院群里问问，看到会联系你。', 2, 1, 0, '2026-06-12 15:50:00', '2026-06-12 15:50:00'),
    (@p_network, @u_demo, 0, NULL, '已收藏，TCP 那部分正好是我的薄弱点。', 6, 1, 0, '2026-06-11 22:30:00', '2026-06-11 22:30:00'),
    (@p_station, @u_chen, 0, NULL, '我 10:20 的车，可以一起，只有一个背包。', 2, 1, 0, '2026-06-11 18:35:00', '2026-06-11 18:35:00'),
    (@p_books, @u_demo, 0, NULL, '想要，今晚方便在东区宿舍门口看一下吗？', 1, 1, 0, '2026-06-10 14:12:00', '2026-06-10 14:12:00'),
    (@p_wifi, @u_chen, 0, NULL, '先在网卡电源管理里取消“允许计算机关闭此设备”，再更新无线网卡驱动试试。', 9, 1, 0, '2026-06-09 20:52:00', '2026-06-09 20:52:00');

-- Social graph and follow notices.
INSERT INTO follow (user_id, follow_user_id, create_time)
VALUES
    (@u_demo, @u_chen, '2026-06-10 10:00:00'),
    (@u_demo, @u_li, '2026-06-10 10:02:00'),
    (@u_demo, @u_he, '2026-06-10 10:04:00'),
    (@u_chen, @u_demo, '2026-06-11 09:20:00'),
    (@u_sun, @u_demo, '2026-06-11 09:25:00'),
    (@u_zhao, @u_demo, '2026-06-11 09:30:00'),
    (@u_guo, @u_sun, '2026-06-12 11:00:00'),
    (@u_he, @u_li, '2026-06-12 11:05:00'),
    (@u_wang, @u_chen, '2026-06-12 11:10:00'),
    (@u_wu, @u_demo, '2026-06-13 12:00:00');

INSERT INTO notice (receive_user_id, sender_user_id, type, post_id, comment_id, content, is_read, create_time)
VALUES
    (@u_demo, @u_chen, 3, NULL, NULL, '陈一鸣 关注了你', 0, '2026-06-11 09:20:00'),
    (@u_demo, @u_sun, 3, NULL, NULL, '孙悦 关注了你', 0, '2026-06-11 09:25:00'),
    (@u_demo, @u_zhao, 3, NULL, NULL, '赵可心 关注了你', 1, '2026-06-11 09:30:00'),
    (@u_demo, @u_wu, 3, NULL, NULL, '吴越 关注了你', 0, '2026-06-13 12:00:00'),
    (@u_admin, NULL, 5, NULL, NULL, '系统提示：答辩演示数据已准备完成。', 0, '2026-06-15 09:55:00');

-- Activities: normalize genuinely expired legacy rows, then add current demo events.
UPDATE activity
SET status = 2, update_time = NOW()
WHERE status = 1 AND end_time IS NOT NULL AND end_time < NOW();

INSERT INTO activity (
    user_id, title, description, cover_img, location, activity_type,
    start_time, end_time, max_participants, current_count, status,
    is_deleted, create_time, update_time
)
VALUES
    (@u_admin, 'CampusHub 项目答辩开放体验',
     '现场体验校园社区完整业务流程，并交流数据库设计、搜索同步和前后端实现。',
     NULL, '计算机楼 A301', 1, '2026-06-18 14:00:00', '2026-06-18 16:00:00', 40, 0, 1, 0, '2026-06-12 09:00:00', '2026-06-12 09:00:00'),
    (@u_li, 'AI 时代的简历优化与模拟面试',
     '就业中心老师带领大家拆解岗位 JD，现场修改项目经历并进行结构化面试练习。',
     NULL, '大学生活动中心 205', 1, '2026-06-20 09:30:00', '2026-06-20 11:30:00', 30, 0, 1, 0, '2026-06-12 10:00:00', '2026-06-12 10:00:00'),
    (@u_wu, '周末羽毛球双打交流赛',
     '自由组队，采用小组循环赛制。请自带球拍，主办方提供比赛用球和饮用水。',
     NULL, '北区体育馆 2-4 号场', 3, '2026-06-20 15:00:00', '2026-06-20 18:00:00', 24, 0, 1, 0, '2026-06-12 11:00:00', '2026-06-12 11:00:00'),
    (@u_sun, '夏日晚风草坪音乐会',
     '校园乐队、弹唱和街舞社联合演出，欢迎带上野餐垫来草坪听歌。',
     NULL, '镜湖草坪', 2, '2026-06-21 19:00:00', '2026-06-21 21:00:00', 0, 0, 1, 0, '2026-06-12 12:00:00', '2026-06-12 12:00:00'),
    (@u_he, '旧书整理与社区捐赠志愿活动',
     '整理毕业生捐赠的教材和课外书，完成分类、登记和打包，计入志愿服务时长。',
     NULL, '图书馆一楼共享空间', 4, '2026-06-22 13:30:00', '2026-06-22 17:00:00', 20, 0, 1, 0, '2026-06-12 13:00:00', '2026-06-12 13:00:00');

SELECT id INTO @a_demo FROM activity WHERE user_id = @u_admin AND title = 'CampusHub 项目答辩开放体验';
SELECT id INTO @a_resume FROM activity WHERE user_id = @u_li AND title = 'AI 时代的简历优化与模拟面试';
SELECT id INTO @a_ball FROM activity WHERE user_id = @u_wu AND title = '周末羽毛球双打交流赛';
SELECT id INTO @a_music FROM activity WHERE user_id = @u_sun AND title = '夏日晚风草坪音乐会';
SELECT id INTO @a_book FROM activity WHERE user_id = @u_he AND title = '旧书整理与社区捐赠志愿活动';

INSERT INTO activity_signup (activity_id, user_id, status, create_time)
VALUES
    (@a_demo, @u_wang, 1, '2026-06-12 14:00:00'),
    (@a_demo, @u_chen, 1, '2026-06-12 14:05:00'),
    (@a_demo, @u_sun, 1, '2026-06-12 14:10:00'),
    (@a_demo, @u_zhao, 1, '2026-06-12 14:15:00'),
    (@a_demo, @u_guo, 1, '2026-06-12 14:20:00'),
    (@a_resume, @u_demo, 1, '2026-06-13 09:00:00'),
    (@a_resume, @u_chen, 1, '2026-06-13 09:05:00'),
    (@a_resume, @u_wang, 1, '2026-06-13 09:10:00'),
    (@a_ball, @u_demo, 1, '2026-06-13 10:00:00'),
    (@a_ball, @u_chen, 1, '2026-06-13 10:05:00'),
    (@a_ball, @u_wang, 1, '2026-06-13 10:10:00'),
    (@a_music, @u_demo, 1, '2026-06-13 11:00:00'),
    (@a_music, @u_zhao, 1, '2026-06-13 11:05:00'),
    (@a_music, @u_guo, 1, '2026-06-13 11:10:00'),
    (@a_book, @u_demo, 1, '2026-06-13 12:00:00'),
    (@a_book, @u_sun, 1, '2026-06-13 12:05:00'),
    (@a_book, @u_zhao, 1, '2026-06-13 12:10:00');

-- Votes and actual voting records.
INSERT INTO vote (
    user_id, post_id, title, description, max_select, is_anonymous,
    end_time, total_count, status, is_deleted, create_time, update_time
)
VALUES
    (@u_admin, @p_guide, '你最想在答辩中重点体验哪个功能？',
     '选择一个你认为最能体现 CampusHub 完整业务闭环的模块。', 1, 0,
     '2026-06-20 18:00:00', 0, 1, 0, '2026-06-14 09:00:00', '2026-06-14 09:00:00'),
    (@u_sun, NULL, '下一期校园活动你更期待什么主题？',
     '最多选择两项，投票结果将作为活动策划参考。', 2, 1,
     '2026-06-22 20:00:00', 0, 1, 0, '2026-06-13 19:00:00', '2026-06-13 19:00:00'),
    (@u_li, NULL, '本学期最实用的就业服务是什么？',
     '本轮调研已结束，结果用于暑期服务安排。', 1, 0,
     '2026-06-14 12:00:00', 0, 0, 0, '2026-06-08 10:00:00', '2026-06-14 12:00:00');

SELECT id INTO @v_demo FROM vote WHERE user_id = @u_admin AND title = '你最想在答辩中重点体验哪个功能？';
SELECT id INTO @v_event FROM vote WHERE user_id = @u_sun AND title = '下一期校园活动你更期待什么主题？';
SELECT id INTO @v_career FROM vote WHERE user_id = @u_li AND title = '本学期最实用的就业服务是什么？';

INSERT INTO vote_option (vote_id, content, sort, count, create_time)
VALUES
    (@v_demo, '帖子搜索与内容互动', 1, 0, '2026-06-14 09:00:00'),
    (@v_demo, '活动发布与在线报名', 2, 0, '2026-06-14 09:00:00'),
    (@v_demo, '投票统计与结果展示', 3, 0, '2026-06-14 09:00:00'),
    (@v_demo, '通知、私信与举报管理', 4, 0, '2026-06-14 09:00:00'),
    (@v_event, '露天电影夜', 1, 0, '2026-06-13 19:00:00'),
    (@v_event, '校园音乐节', 2, 0, '2026-06-13 19:00:00'),
    (@v_event, '跳蚤市场', 3, 0, '2026-06-13 19:00:00'),
    (@v_event, '趣味运动会', 4, 0, '2026-06-13 19:00:00'),
    (@v_career, '一对一简历门诊', 1, 0, '2026-06-08 10:00:00'),
    (@v_career, '模拟面试', 2, 0, '2026-06-08 10:00:00'),
    (@v_career, '企业宣讲与双选会', 3, 0, '2026-06-08 10:00:00'),
    (@v_career, '行业与岗位咨询', 4, 0, '2026-06-08 10:00:00');

SELECT id INTO @vo_demo_1 FROM vote_option WHERE vote_id = @v_demo AND sort = 1;
SELECT id INTO @vo_demo_2 FROM vote_option WHERE vote_id = @v_demo AND sort = 2;
SELECT id INTO @vo_demo_3 FROM vote_option WHERE vote_id = @v_demo AND sort = 3;
SELECT id INTO @vo_demo_4 FROM vote_option WHERE vote_id = @v_demo AND sort = 4;
SELECT id INTO @vo_event_1 FROM vote_option WHERE vote_id = @v_event AND sort = 1;
SELECT id INTO @vo_event_2 FROM vote_option WHERE vote_id = @v_event AND sort = 2;
SELECT id INTO @vo_event_3 FROM vote_option WHERE vote_id = @v_event AND sort = 3;
SELECT id INTO @vo_event_4 FROM vote_option WHERE vote_id = @v_event AND sort = 4;
SELECT id INTO @vo_career_1 FROM vote_option WHERE vote_id = @v_career AND sort = 1;
SELECT id INTO @vo_career_2 FROM vote_option WHERE vote_id = @v_career AND sort = 2;
SELECT id INTO @vo_career_3 FROM vote_option WHERE vote_id = @v_career AND sort = 3;

INSERT INTO vote_record (vote_id, option_id, user_id, create_time)
VALUES
    (@v_demo, @vo_demo_1, @u_wang, '2026-06-14 10:00:00'),
    (@v_demo, @vo_demo_1, @u_chen, '2026-06-14 10:05:00'),
    (@v_demo, @vo_demo_2, @u_sun, '2026-06-14 10:10:00'),
    (@v_demo, @vo_demo_2, @u_wu, '2026-06-14 10:15:00'),
    (@v_demo, @vo_demo_3, @u_zhao, '2026-06-14 10:20:00'),
    (@v_demo, @vo_demo_4, @u_li, '2026-06-14 10:25:00'),
    (@v_demo, @vo_demo_4, @u_he, '2026-06-14 10:30:00'),
    (@v_demo, @vo_demo_4, @u_guo, '2026-06-14 10:35:00'),
    (@v_event, @vo_event_1, @u_li, '2026-06-13 20:00:00'),
    (@v_event, @vo_event_2, @u_li, '2026-06-13 20:00:01'),
    (@v_event, @vo_event_2, @u_chen, '2026-06-13 20:05:00'),
    (@v_event, @vo_event_3, @u_zhao, '2026-06-13 20:10:00'),
    (@v_event, @vo_event_4, @u_wu, '2026-06-13 20:15:00'),
    (@v_event, @vo_event_1, @u_he, '2026-06-13 20:20:00'),
    (@v_event, @vo_event_3, @u_guo, '2026-06-13 20:25:00'),
    (@v_career, @vo_career_1, @u_demo, '2026-06-09 09:00:00'),
    (@v_career, @vo_career_1, @u_chen, '2026-06-09 09:05:00'),
    (@v_career, @vo_career_2, @u_wang, '2026-06-09 09:10:00'),
    (@v_career, @vo_career_3, @u_sun, '2026-06-09 09:15:00'),
    (@v_career, @vo_career_1, @u_zhao, '2026-06-09 09:20:00');

-- Conversations for the main demo account. Message triggers maintain previews.
INSERT INTO conversation (user_a_id, user_b_id, last_message_id, last_message_time, create_time, update_time)
VALUES
    (LEAST(@u_demo, @u_chen), GREATEST(@u_demo, @u_chen), NULL, NULL, '2026-06-14 21:40:00', '2026-06-14 21:40:00'),
    (LEAST(@u_demo, @u_li), GREATEST(@u_demo, @u_li), NULL, NULL, '2026-06-13 17:10:00', '2026-06-13 17:10:00'),
    (LEAST(@u_demo, @u_zhao), GREATEST(@u_demo, @u_zhao), NULL, NULL, '2026-06-12 18:00:00', '2026-06-12 18:00:00');

SELECT id INTO @conv_chen FROM conversation WHERE user_a_id = LEAST(@u_demo, @u_chen) AND user_b_id = GREATEST(@u_demo, @u_chen);
SELECT id INTO @conv_li FROM conversation WHERE user_a_id = LEAST(@u_demo, @u_li) AND user_b_id = GREATEST(@u_demo, @u_li);
SELECT id INTO @conv_zhao FROM conversation WHERE user_a_id = LEAST(@u_demo, @u_zhao) AND user_b_id = GREATEST(@u_demo, @u_zhao);

INSERT INTO message (conversation_id, sender_id, content, is_read, create_time)
VALUES
    (@conv_chen, @u_chen, '你好，我看到你回复了组队帖，方便发一份之前做过的页面作品吗？', 1, '2026-06-14 21:42:00'),
    (@conv_chen, @u_demo, '可以，我整理成在线地址发你。项目的接口文档也可以先给我看看。', 1, '2026-06-14 21:46:00'),
    (@conv_chen, @u_chen, '没问题，今晚十点前发你，明天下午一起开个短会。', 0, '2026-06-14 21:50:00'),
    (@conv_li, @u_demo, '李老师您好，我预约了周六的简历门诊，需要提前准备什么材料？', 1, '2026-06-13 17:12:00'),
    (@conv_li, @u_li, '准备一页中文简历和两个意向岗位 JD 即可，项目仓库链接也可以带上。', 0, '2026-06-13 17:18:00'),
    (@conv_zhao, @u_zhao, '学姐，谢谢你在校园网帖子下面的建议，设置后已经稳定多了。', 0, '2026-06-12 18:05:00');

-- One pending and two completed reports make the admin page demonstrable.
INSERT INTO report (
    report_user_id, target_type, target_id, reason, status,
    handle_user_id, handle_result, create_time, update_time
)
VALUES
    (@u_demo, 1, @p_monitor, '交易帖未明确标注最终价格，建议提醒发布者补充。', 0,
     NULL, NULL, '2026-06-15 08:10:00', '2026-06-15 08:10:00'),
    (@u_zhao, 1, @p_team, '标题容易被误解为商业招聘，建议核实是否为校内竞赛组队。', 1,
     @u_admin, '已核实为校内比赛组队，内容真实，保留帖子并补充提示。', '2026-06-14 22:00:00', '2026-06-14 22:20:00'),
    (@u_he, 2, @cm_db, '该评论包含技术术语，曾误判为广告，申请人工复核。', 1,
     @u_admin, '已人工复核，属于正常学习交流，无需处理。', '2026-06-14 09:30:00', '2026-06-14 09:45:00');

COMMIT;

DROP TEMPORARY TABLE IF EXISTS tmp_demo_users;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_users_2;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_users_3;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_users_4;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_posts;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_comments;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_votes;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_activities;

-- Compact verification summary.
SELECT 'demo_users' AS item, COUNT(*) AS total
FROM `user`
WHERE username IN (
    'demo_admin', 'demo_student', 'chen_yiming', 'sun_yue', 'wu_yue',
    'li_laoshi', 'he_yuqing', 'zhao_kexin', 'wang_haoran', 'guo_xinyi'
)
UNION ALL
SELECT 'demo_posts', COUNT(*) FROM post WHERE user_id IN (
    SELECT id FROM `user` WHERE username IN (
        'demo_admin', 'demo_student', 'chen_yiming', 'sun_yue', 'wu_yue',
        'li_laoshi', 'he_yuqing', 'zhao_kexin', 'wang_haoran', 'guo_xinyi'
    )
)
UNION ALL
SELECT 'activities', COUNT(*) FROM activity WHERE is_deleted = 0
UNION ALL
SELECT 'votes', COUNT(*) FROM vote WHERE is_deleted = 0
UNION ALL
SELECT 'reports', COUNT(*) FROM report;
