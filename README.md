# CampusHub 校园社区论坛系统

基于 Spring Boot + Vue 3 的校园社区论坛系统，支持帖子发布、互动社交、私信聊天、校园活动、投票等功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite 5 + Naive UI + Pinia + Axios |
| 后端 | Spring Boot 4.0 + MyBatis-Plus 3.5 + MySQL 8.0 + Redis + JWT |
| 构建 | Maven (后端) / npm (前端) |

## 项目结构

```
campushub/
├── backend/                        # Spring Boot 后端
│   ├── src/main/java/com/yzh/campushub/
│   │   ├── config/                 # 配置类（跨域、拦截器、WebSocket 等）
│   │   ├── controller/             # 控制器层（13 个）
│   │   ├── dto/                    # 数据传输对象
│   │   ├── entity/                 # 实体类（17 个，对应 17 张数据库表）
│   │   ├── mapper/                 # MyBatis-Plus Mapper 接口
│   │   ├── service/                # 业务逻辑接口 + 实现
│   │   ├── utils/                  # 工具类（JWT、Redis、UserContext 等）
│   │   └── vo/                     # 视图对象（返回给前端的数据结构）
│   └── src/main/resources/
│       ├── application.yml         # 配置文件
│       └── static/                 # 前端构建产物（生产部署用）
└── frontend/                       # Vue 3 前端
    ├── src/
    │   ├── api/                    # API 请求模块（Axios 封装）
    │   ├── assets/                 # 静态资源
    │   ├── components/             # 公共组件
    │   ├── router/                 # 路由配置
    │   ├── stores/                 # Pinia 状态管理
    │   ├── utils/                  # 工具函数
    │   └── views/                  # 页面视图
    └── package.json
```

---

## 功能模块

### 1. 用户系统
- 注册 / 登录（JWT 令牌认证，Redis 存储会话）
- 个人资料编辑（昵称、头像、学院、个人简介）
- 他人主页查看

### 2. 帖子系统
- 帖子发布（支持图片上传、分类选择）
- 帖子编辑 / 删除（软删除）
- 帖子列表（分页、按分类筛选、关键词搜索）
- 帖子详情（点赞数、评论数、收藏数实时展示）

### 3. 评论系统
- 发表评论（支持嵌套回复，树形结构展示）
- 评论列表（按时间排序，父子关系自动构建）
- 评论删除

### 4. 点赞 / 收藏
- 帖子点赞 toggle（原子计数器，防并发）
- 帖子收藏 toggle
- 我的收藏列表
- 点赞 / 收藏状态实时同步

### 5. 关注系统
- 关注 / 取消关注 toggle
- 我关注的人列表（分页）
- 我的粉丝列表（分页）
- 关注数 / 粉丝数统计
- 互相关注标识

### 6. 通知系统
- 点赞通知（type=1）
- 评论通知（type=2）
- 关注通知（type=3）
- 通知列表（按类型筛选、分页）
- 未读通知数 badge
- 标记已读 / 全部已读

### 7. 私信系统
- 用户间一对一私信
- 自动创建会话（保证 userAId < userBId 唯一性）
- 会话列表（按最后消息时间排序，显示未读数）
- 消息列表（进入会话自动标记已读）
- 未读消息数 badge

### 8. 校园活动
- 发布活动（标题、类型、描述、地点、时间、人数上限）
- 活动列表（按类型筛选、分页）
- 活动详情 + 报名 / 取消报名
- 容量限制（人数上限检查）
- 我创建的活动 / 我报名的活动

### 9. 投票系统
- 创建投票（标题、描述、多选上限、截止时间、2~10 个选项）
- 投票列表 + 详情
- 投票（校验最多可选项数，防重复投票）
- 投票结果（百分比 + 进度条展示）

### 10. 举报系统
- 提交举报（帖子 / 评论，选择原因）
- 举报列表（管理员，按状态筛选）
- 处理举报（管理员，标记处理结果）

### 11. 分类管理
- 帖子分类列表
- 按分类筛选帖子

---

## 数据库设计

共 **17 张表**，分为基础模块（10 张）和扩展模块（7 张）。

### 基础模块（10 张表）

#### `user` — 用户表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 用户 ID |
| username | VARCHAR | 用户名（唯一） |
| password | VARCHAR | 密码（BCrypt 加密） |
| nickname | VARCHAR | 昵称 |
| avatar | VARCHAR | 头像 URL |
| college | VARCHAR | 学院 |
| bio | VARCHAR | 个人简介 |
| role | INT | 角色（0 普通用户 1 管理员） |
| create_time | DATETIME | 注册时间 |
| update_time | DATETIME | 更新时间 |

#### `category` — 分类表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 分类 ID |
| name | VARCHAR | 分类名称 |
| sort | INT | 排序权重 |
| create_time | DATETIME | 创建时间 |

#### `post` — 帖子表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 帖子 ID |
| user_id | BIGINT | 发布者 ID |
| category_id | BIGINT | 分类 ID |
| title | VARCHAR | 标题 |
| content | TEXT | 内容 |
| like_count | INT | 点赞数（冗余计数） |
| comment_count | INT | 评论数（冗余计数） |
| favorite_count | INT | 收藏数（冗余计数） |
| is_deleted | INT | 软删除标记 |
| create_time | DATETIME | 发布时间 |
| update_time | DATETIME | 更新时间 |

#### `image` — 图片表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 图片 ID |
| post_id | BIGINT | 所属帖子 ID |
| url | VARCHAR | 图片 URL |
| sort | INT | 排序 |
| create_time | DATETIME | 上传时间 |

#### `comment` — 评论表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 评论 ID |
| post_id | BIGINT | 所属帖子 ID |
| user_id | BIGINT | 评论者 ID |
| parent_id | BIGINT | 父评论 ID（NULL 为一级评论） |
| reply_user_id | BIGINT | 被回复者 ID |
| content | TEXT | 评论内容 |
| is_deleted | INT | 软删除标记 |
| create_time | DATETIME | 评论时间 |

#### `like` — 点赞表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 点赞 ID |
| user_id | BIGINT | 点赞者 ID |
| post_id | BIGINT | 帖子 ID |
| create_time | DATETIME | 点赞时间 |
| UNIQUE KEY | | (user_id, post_id) 防重复 |

#### `favorite` — 收藏表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 收藏 ID |
| user_id | BIGINT | 收藏者 ID |
| post_id | BIGINT | 帖子 ID |
| create_time | DATETIME | 收藏时间 |
| UNIQUE KEY | | (user_id, post_id) 防重复 |

#### `follow` — 关注表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 关注 ID |
| user_id | BIGINT | 关注者 ID |
| follow_user_id | BIGINT | 被关注者 ID |
| create_time | DATETIME | 关注时间 |
| UNIQUE KEY | | (user_id, follow_user_id) 防重复 |

#### `notice` — 通知表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 通知 ID |
| receive_user_id | BIGINT | 接收者 ID |
| sender_user_id | BIGINT | 发送者 ID |
| type | INT | 类型（1 点赞 2 评论 3 关注） |
| post_id | BIGINT | 关联帖子 ID |
| comment_id | BIGINT | 关联评论 ID |
| content | VARCHAR | 通知内容 |
| is_read | INT | 是否已读 |
| create_time | DATETIME | 创建时间 |

#### `report` — 举报表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 举报 ID |
| report_user_id | BIGINT | 举报者 ID |
| target_type | INT | 目标类型（1 帖子 2 评论） |
| target_id | BIGINT | 目标 ID |
| reason | VARCHAR | 举报原因 |
| status | INT | 状态（0 待处理 1 已处理 2 已驳回） |
| handle_user_id | BIGINT | 处理人 ID |
| handle_result | VARCHAR | 处理结果 |
| create_time | DATETIME | 举报时间 |
| update_time | DATETIME | 处理时间 |

---

### 扩展模块（7 张表）

#### `conversation` — 私信会话表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 会话 ID |
| user_a_id | BIGINT | 参与者 A（ID 较小者） |
| user_b_id | BIGINT | 参与者 B（ID 较大者） |
| last_message_id | BIGINT | 最后一条消息 ID |
| last_message_time | DATETIME | 最后消息时间 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| UNIQUE KEY | | (user_a_id, user_b_id) 保证唯一会话 |

#### `message` — 私信消息表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 消息 ID |
| conversation_id | BIGINT | 所属会话 ID |
| sender_id | BIGINT | 发送者 ID |
| content | TEXT | 消息内容 |
| is_read | INT | 是否已读 |
| create_time | DATETIME | 发送时间 |

#### `activity` — 校园活动表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 活动 ID |
| user_id | BIGINT | 发起者 ID |
| title | VARCHAR | 活动标题 |
| description | TEXT | 活动描述 |
| cover_img | VARCHAR | 封面图 URL |
| location | VARCHAR | 活动地点 |
| activity_type | INT | 类型（1 讲座 2 聚会 3 运动 4 其他） |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| max_participants | INT | 人数上限（0 不限） |
| current_count | INT | 当前报名人数 |
| status | INT | 状态（0 已取消 1 报名中 2 已结束） |
| is_deleted | INT | 软删除标记 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### `activity_signup` — 活动报名表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 报名 ID |
| activity_id | BIGINT | 活动 ID |
| user_id | BIGINT | 报名者 ID |
| status | INT | 状态（1 正常 0 已取消） |
| create_time | DATETIME | 报名时间 |
| UNIQUE KEY | | (activity_id, user_id) 防重复报名 |

#### `vote` — 投票表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 投票 ID |
| user_id | BIGINT | 发起者 ID |
| post_id | BIGINT | 关联帖子 ID（可选） |
| title | VARCHAR | 投票标题 |
| description | VARCHAR | 投票描述 |
| max_select | INT | 最多可选项数 |
| is_anonymous | INT | 是否匿名 |
| end_time | DATETIME | 截止时间 |
| total_count | INT | 总参与人数 |
| status | INT | 状态（0 已关闭 1 进行中） |
| is_deleted | INT | 软删除标记 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### `vote_option` — 投票选项表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 选项 ID |
| vote_id | BIGINT | 所属投票 ID |
| content | VARCHAR | 选项内容 |
| sort | INT | 排序 |
| count | INT | 得票数 |
| create_time | DATETIME | 创建时间 |

#### `vote_record` — 投票记录表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 记录 ID |
| vote_id | BIGINT | 投票 ID |
| option_id | BIGINT | 选项 ID |
| user_id | BIGINT | 投票者 ID |
| create_time | DATETIME | 投票时间 |
| UNIQUE KEY | | (vote_id, user_id, option_id) 防重复 |

---

## API 接口一览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | `/api/auth/register` | 注册 |
| 认证 | POST | `/api/auth/login` | 登录 |
| 用户 | GET | `/api/users/{id}` | 获取用户信息 |
| 用户 | PUT | `/api/users/profile` | 编辑个人资料 |
| 帖子 | POST | `/api/posts` | 发布帖子 |
| 帖子 | GET | `/api/posts` | 帖子列表（分页、筛选） |
| 帖子 | GET | `/api/posts/{id}` | 帖子详情 |
| 帖子 | PUT | `/api/posts/{id}` | 编辑帖子 |
| 帖子 | DELETE | `/api/posts/{id}` | 删除帖子 |
| 评论 | POST | `/api/comments` | 发表评论 |
| 评论 | GET | `/api/comments/post/{postId}` | 帖子评论列表 |
| 评论 | DELETE | `/api/comments/{id}` | 删除评论 |
| 点赞 | POST | `/api/likes/{postId}` | 点赞/取消 |
| 收藏 | POST | `/api/favorites/{postId}` | 收藏/取消 |
| 收藏 | GET | `/api/favorites` | 我的收藏列表 |
| 分类 | GET | `/api/categories` | 分类列表 |
| 关注 | POST | `/api/follows/{userId}` | 关注/取关 |
| 关注 | GET | `/api/follows/following` | 我关注的人 |
| 关注 | GET | `/api/follows/followers` | 我的粉丝 |
| 关注 | GET | `/api/follows/{userId}/count` | 关注/粉丝数 |
| 通知 | GET | `/api/notices` | 通知列表 |
| 通知 | GET | `/api/notices/unread-count` | 未读通知数 |
| 通知 | PUT | `/api/notices/{id}/read` | 标记已读 |
| 通知 | PUT | `/api/notices/read-all` | 全部已读 |
| 私信 | POST | `/api/messages` | 发送消息 |
| 私信 | GET | `/api/messages/conversations` | 会话列表 |
| 私信 | GET | `/api/messages/conversations/{id}` | 会话消息 |
| 私信 | GET | `/api/messages/unread-count` | 未读消息数 |
| 活动 | POST | `/api/activities` | 创建活动 |
| 活动 | GET | `/api/activities` | 活动列表 |
| 活动 | GET | `/api/activities/{id}` | 活动详情 |
| 活动 | PUT | `/api/activities/{id}` | 编辑活动 |
| 活动 | DELETE | `/api/activities/{id}` | 取消活动 |
| 活动 | POST | `/api/activities/{id}/signup` | 报名 |
| 活动 | DELETE | `/api/activities/{id}/signup` | 取消报名 |
| 活动 | GET | `/api/activities/{id}/signups` | 报名列表 |
| 活动 | GET | `/api/activities/my` | 我创建的活动 |
| 活动 | GET | `/api/activities/my-signups` | 我报名的活动 |
| 投票 | POST | `/api/votes` | 创建投票 |
| 投票 | GET | `/api/votes` | 投票列表 |
| 投票 | GET | `/api/votes/{id}` | 投票详情 |
| 投票 | POST | `/api/votes/{id}/vote` | 投票 |
| 投票 | DELETE | `/api/votes/{id}` | 删除投票 |
| 投票 | GET | `/api/votes/post/{postId}` | 帖子关联投票 |
| 举报 | POST | `/api/reports` | 提交举报 |
| 举报 | GET | `/api/reports` | 举报列表（管理员） |
| 举报 | PUT | `/api/reports/{id}/handle` | 处理举报（管理员） |

---

## 快速启动

### 环境要求
- JDK 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0+
- Redis

### 数据库初始化

创建数据库后执行建表 SQL（见 `docs/sql/` 或下方 SQL 语句），共 17 张表。

### 后端

```bash
cd backend
# 修改 src/main/resources/application.yml 中的数据库和 Redis 连接信息
./mvnw spring-boot:run
```

后端默认运行在 `http://localhost:8080`

### 前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，开发模式下 API 请求自动代理到后端。

### 生产部署

```bash
cd frontend
npm run build    # 构建产物自动输出到 backend/src/main/resources/static/
cd ../backend
./mvnw package   # 打包为 jar，包含前端静态资源
java -jar target/CampusHub-0.0.1-SNAPSHOT.jar
```

---

## 规划中功能

### 1. 帖子全文搜索 — Elasticsearch
**技术点：** ES 分词检索、IK 中文分词器、高亮摘要、搜索建议

- 帖子标题 + 内容同步到 Elasticsearch 索引
- 支持中文分词搜索（IK 分词器）
- 搜索结果高亮关键词、按相关度/时间排序
- 搜索建议（输入联想）
- 数据同步方案：Canal 监听 MySQL binlog 或发 MQ 消息异步同步

**新增依赖：** `spring-boot-starter-data-elasticsearch`

---

### 2. 热门帖子排行榜 — Redis Sorted Set
**技术点：** Redis ZSet、定时聚合、滑动窗口计分

- 基于帖子的浏览量、点赞数、评论数计算热度分
- 使用 Redis Sorted Set 维护 Top N 热帖
- 定时任务（Scheduled Task）每 5 分钟刷新排行榜
- 首页展示「热门帖子」tab，直接读 Redis O(logN) 复杂度
- 支持按分类（学习/生活/求职）分榜

**Redis 数据结构：** `ZADD hot_posts:{category} {score} {postId}`

---

### 3. 实时消息推送 — WebSocket + Redis Pub/Sub
**技术点：** WebSocket 长连接、STOMP 协议、Redis Pub/Sub 跨节点广播

- 私信实时推送（发消息后对方即时收到，无需刷新）
- 通知实时推送（点赞/评论/关注时即时弹窗）
- 在线状态维护（Redis 存储用户在线状态）
- 基于 STOMP 协议的消息订阅
- Redis Pub/Sub 解决多实例部署时的消息广播问题

**新增依赖：** `spring-boot-starter-websocket`、`spring-boot-starter-data-redis`

---

### 4. 异步内容审核 — RabbitMQ 消息队列
**技术点：** RabbitMQ 生产者/消费者、死信队列、异步处理

- 帖子/评论发布后异步审核（不阻塞用户操作）
- 生产者：发布内容时发送消息到 `content_audit` 队列
- 消费者：调用第三方审核 API（阿里云/百度内容安全）或敏感词匹配
- 审核不通过自动隐藏 + 通知作者
- 失败重试：消息进入死信队列，延迟重试 3 次
- 新增 `audit_status` 字段（0 待审核 1 通过 2 不通过）

**新增依赖：** `spring-boot-starter-amqp`

---

### 5. 个性化推荐系统 — Redis 缓存 + 协同过滤
**技术点：** Redis 缓存策略、用户行为画像、协同过滤算法

- 记录用户行为（浏览、点赞、收藏、评论）到 Redis List
- 基于用户行为的协同过滤推荐（相似用户喜欢的内容）
- 基于内容标签的推荐（TF-IDF 提取帖子关键词）
- 推荐结果缓存到 Redis，TTL 30 分钟
- 首页「推荐」feed 流，冷启动用户降级为热门帖子

**Redis 数据结构：** `user_behavior:{userId}` (List)、`user_tags:{userId}` (Set)、`recommend:{userId}` (List, 缓存)

---

### 6. 接口限流防刷 — Redis + 滑动窗口
**技术点：** Redis Lua 脚本、滑动窗口算法、自定义注解 + AOP

- 自定义 `@RateLimit` 注解，标记需要限流的接口
- AOP 拦截 + Redis Lua 脚本实现原子性滑动窗口计数
- 不同接口不同限流策略（发帖 5次/分钟、登录 10次/分钟、投票 1次/秒）
- 超限返回 HTTP 429 + 友好提示
- 支持按用户 ID 或 IP 限流

**Redis 数据结构：** `rate_limit:{api}:{userId}` (ZSET, score=时间戳)

---

### 7. 数据统计大屏 — Redis 计数 + 定时任务
**技术点：** Redis INCR/HyperLogLog、Spring Scheduled、数据可视化

- 实时统计：今日新增用户数、发帖数、活跃用户数
- 使用 Redis HyperLogLog 统计 UV（去重访客数）
- Redis INCR 原子计数器统计 PV
- 定时任务每天凌晨将 Redis 数据快照写入 MySQL `statistics` 表
- 管理后台 ECharts 数据大屏展示趋势图

**新增表：** `statistics`（date, new_users, new_posts, active_users, pv, uv）

---

### 8. 活动报名秒杀 — Redis + Lua 脚本
**技术点：** Redis Lua 原子操作、库存预热、防超卖

- 热门活动报名场景（如讲座名额 100 人，瞬间涌入大量请求）
- Redis 预热活动名额：`SET activity_cap:{id} 100`
- Lua 脚本原子性检查名额 + 扣减 + 写入报名记录，防止超卖
- 报名成功的消息通过 MQ 异步写入 MySQL
- 未支付/取消的名额定时回补

**Redis 数据结构：** `activity_cap:{id}` (String, 原子递减)、`activity_signups:{id}` (Set, 已报名用户)

---

### 9. 站内搜索热搜 — Redis List + 定时衰减
**技术点：** Redis List/ZSet、热词统计、定时衰减策略

- 记录用户搜索关键词到 Redis List
- 定时任务聚合 Top 20 热搜词，写入 Redis ZSet
- 搜索词热度分 = 搜索次数 × 时间衰减系数
- 搜索框下方展示「热搜榜」
- 搜索历史按用户维度存储（最近 20 条）

**Redis 数据结构：** `search_hot` (ZSet)、`search_history:{userId}` (List)

---

### 10. 分布式会话 — Redis + Spring Session
**技术点：** Spring Session、Redis 集中式会话、JWT + Refresh Token 双令牌

- 当前 JWT 令牌方案升级为 Access Token + Refresh Token 双令牌
- Access Token 短期有效（30 分钟），Refresh Token 长期有效（7 天）
- Refresh Token 存储在 Redis，支持主动吊销（修改密码/异地登录）
- Spring Session 将 HttpSession 存入 Redis，支持多实例部署
- 登录设备管理（查看/踢出其他设备）

**新增依赖：** `spring-session-data-redis`

---

### 技术栈全景

```
┌─────────────────────────────────────────────────────────┐
│                      Frontend                           │
│  Vue 3 + TypeScript + Naive UI + Pinia + WebSocket      │
├─────────────────────────────────────────────────────────┤
│                      Gateway                            │
│           Nginx (反向代理 + 静态资源 + 限流)              │
├─────────────────────────────────────────────────────────┤
│                      Backend                            │
│  Spring Boot + MyBatis-Plus + AOP + Scheduled Tasks     │
├──────────┬──────────┬───────────┬───────────────────────┤
│  MySQL   │  Redis   │    MQ     │  Elasticsearch        │
│  主存储   │  缓存/排行 │  异步任务  │  全文搜索              │
│  17+ 表  │  会话/计数 │  审核/通知  │  中文分词              │
└──────────┴──────────┴───────────┴───────────────────────┘
```
