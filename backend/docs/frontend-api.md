# CampusHub 前端对接接口文档（按当前代码精确整理）

> 生成时间：2026-04-22  
> 说明：本文档严格依据后端 Controller/DTO/VO 源码整理，**HTTP 方法与路径逐字对应**。

## 1. 基础信息

- Base URL：`http://localhost:8080`
- 统一返回结构：`Result`

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "total": 0,
  "success": true
}
```

字段说明：
- `code`: 状态码（与 HTTP 状态码语义一致）
- `message`: 提示信息
- `data`: 业务数据（可为对象、数组或 null）
- `total`: 分页总数（仅分页接口有值）
- `success`: 兼容字段，`code == 200` 时为 `true`

## 2. 鉴权说明（按当前项目实际代码）

- 代码中存在 `Authorization` Token 拦截器 `AuthInterceptor`，支持 `Bearer <token>`。
- 但 `WebMvcConfig` 里拦截器注册代码目前是**注释状态**，即当前默认不会统一拦截 `/api/**`。
- 多数需要用户身份的 Service 方法中，当 `UserContext` 为空时会回退到 `userId = 1`（测试兜底逻辑）。
- `GET /api/auth/me` 依赖 `UserContext`，在未启用拦截器时通常会返回未登录。

前端建议：
- 登录后仍按规范携带请求头：`Authorization: Bearer <token>`。
- 若后端后续启用拦截器，当前前端实现可无缝适配。

---

## 3. 认证模块 `/api/auth`

### 3.1 用户注册
- 方法：`POST`
- 路径：`/api/auth/register`
- 请求体：`application/json`

```json
{
  "username": "string",
  "password": "string",
  "confirmPassword": "string",
  "nickname": "string"
}
```

- 返回：`Result`
  - 成功 `data`: `"register success"`

### 3.2 用户登录
- 方法：`POST`
- 路径：`/api/auth/login`
- 请求体：`application/json`

```json
{
  "username": "string",
  "password": "string"
}
```

- 返回：`Result`
  - 成功 `data` 结构：`LoginVO`

```json
{
  "token": "string",
  "userInfo": {
    "id": 1,
    "username": "string",
    "nickname": "string",
    "avatar": "string",
    "role": "string"
  }
}
```

### 3.3 获取当前登录用户信息
- 方法：`GET`
- 路径：`/api/auth/me`
- 请求参数：无
- 返回：`Result`
  - 成功 `data`：`UserInfoVO`

---

## 4. 用户模块 `/api/users`

### 4.1 获取用户主页信息
- 方法：`GET`
- 路径：`/api/users/{userId}`
- 路径参数：
  - `userId` (Long)
- 返回：`Result`
  - 成功 `data`：`UserHomeVO`

```json
{
  "id": 1,
  "username": "string",
  "nickname": "string",
  "avatar": "string",
  "gender": 0,
  "college": "string",
  "major": "string",
  "profile": "string",
  "createTime": "2026-04-22T10:00:00"
}
```

### 4.2 更新用户资料
- 方法：`PUT`
- 路径：`/api/users/profile`
- 请求参数（Query）：
  - `userId` (Long)
- 请求体：`application/json`

```json
{
  "nickname": "string",
  "gender": 0,
  "email": "string",
  "college": "string",
  "major": "string",
  "profile": "string"
}
```

- 返回：`Result`
  - 成功 `data`: `"个人资料更新成功"`

### 4.3 更新用户头像
- 方法：`POST`
- 路径：`/api/users/avatar`
- 请求类型：`multipart/form-data`
- 表单字段：
  - `userId` (Long)
  - `file` (文件)
- 返回：`Result`
  - 成功 `data`: `"uploads/xxx.ext"`（新头像相对路径）

---

## 5. 分类模块 `/api/categories`

### 5.1 获取分类列表
- 方法：`GET`
- 路径：`/api/categories`
- 请求参数：无
- 返回：`Result`
  - 成功 `data`：`CategoryVO[]`

```json
[
  { "id": 1, "name": "校园资讯" }
]
```

---

## 6. 帖子模块 `/api/posts`

### 6.1 帖子列表
- 方法：`GET`
- 路径：`/api/posts`
- 请求参数（Query，对应 `PostQueryDTO`）：
  - `pageNum` (Integer, 默认 1)
  - `pageSize` (Integer, 默认 10)
  - `categoryId` (Long, 可选)
  - `sortType` (String, `latest` / `hottest`)
  - `keyword` (String, 可选)
- 返回：`Result`
  - `data`: `PostVO[]`
  - `total`: 总条数

`PostVO` 字段：
- `id`, `userId`, `nickname`, `avatar`, `categoryId`, `categoryName`
- `title`, `content`, `coverImg`
- `viewCount`, `likeCount`, `commentCount`, `favoriteCount`
- `status`, `isTop`, `createTime`, `updateTime`

### 6.2 我发布的帖子
- 方法：`GET`
- 路径：`/api/posts/my`
- 请求参数（Query）：同 `PostQueryDTO`
- 返回：`Result`
  - `data`: `PostVO[]`
  - `total`: 总条数

### 6.3 发布帖子
- 方法：`POST`
- 路径：`/api/posts`
- 请求体：`application/json`

```json
{
  "categoryId": 1,
  "title": "string",
  "content": "string",
  "images": ["string", "string"]
}
```

- 返回：`Result`
  - 成功时 `data` 为 `null`

### 6.4 帖子详情
- 方法：`GET`
- 路径：`/api/posts/{postId}`
- 路径参数：
  - `postId` (Long)
- 返回：`Result`
  - 成功 `data`：`PostDetailVO`

```json
{
  "id": 1,
  "title": "string",
  "content": "string",
  "viewCount": 0,
  "likeCount": 0,
  "commentCount": 0,
  "favoriteCount": 0,
  "createTime": "2026-04-22T10:00:00",
  "author": {
    "id": 1,
    "nickname": "string",
    "avatar": "string"
  },
  "images": ["string"],
  "isLiked": false,
  "isFavorited": false
}
```

### 6.5 修改帖子
- 方法：`PUT`
- 路径：`/api/posts/{postId}`
- 路径参数：
  - `postId` (Long)
- 请求体：`application/json`

```json
{
  "categoryId": 1,
  "title": "string",
  "content": "string",
  "images": ["string", "string"]
}
```

- 返回：`Result`

### 6.6 删除帖子
- 方法：`DELETE`
- 路径：`/api/posts/{postId}`
- 路径参数：
  - `postId` (Long)
- 返回：`Result`

---

## 7. 评论模块 `/api/comments`

### 7.1 发表评论
- 方法：`POST`
- 路径：`/api/comments`
- 请求体：`application/json`

```json
{
  "postId": 1,
  "parentId": 0,
  "replyUserId": 2,
  "content": "string"
}
```

说明：
- 一级评论可传 `parentId = 0` 或不传（后端会置为 0）。
- 回复评论时传 `parentId`（被回复评论 ID）与 `replyUserId`（被回复用户 ID）。

- 返回：`Result`

### 7.2 获取帖子评论列表
- 方法：`GET`
- 路径：`/api/comments/post/{postId}`
- 路径参数：
  - `postId` (Long)
- 返回：`Result`
  - 成功 `data`：`CommentVO[]`（树形，仅一级评论挂 `children`）

`CommentVO` 字段：
- `id`, `postId`, `userId`, `nickname`, `avatar`
- `parentId`, `replyUserId`, `replyNickname`
- `content`, `likeCount`, `createTime`
- `children: CommentVO[]`

### 7.3 删除评论
- 方法：`DELETE`
- 路径：`/api/comments/{commentId}`
- 路径参数：
  - `commentId` (Long)
- 返回：`Result`

---

## 8. 点赞模块 `/api/post-likes`

### 8.1 点赞（含切换逻辑）
- 方法：`POST`
- 路径：`/api/post-likes/{postId}`
- 路径参数：
  - `postId` (Long)
- 返回：`Result`

说明：该接口在后端实现为“切换”逻辑：
- 未点赞 -> 点赞
- 已点赞 -> 取消点赞

### 8.2 取消点赞
- 方法：`DELETE`
- 路径：`/api/post-likes/{postId}`
- 路径参数：
  - `postId` (Long)
- 返回：`Result`

### 8.3 我点赞的帖子
- 方法：`GET`
- 路径：`/api/post-likes/my`
- 请求参数（Query）：同 `PostQueryDTO`
- 返回：`Result`
  - `data`: `PostVO[]`
  - `total`: 点赞记录总数

---

## 9. 收藏模块 `/api/post-favorites`

### 9.1 收藏（含切换逻辑）
- 方法：`POST`
- 路径：`/api/post-favorites/{postId}`
- 路径参数：
  - `postId` (Long)
- 返回：`Result`

说明：该接口在后端实现为“切换”逻辑：
- 未收藏 -> 收藏
- 已收藏 -> 取消收藏

### 9.2 取消收藏
- 方法：`DELETE`
- 路径：`/api/post-favorites/{postId}`
- 路径参数：
  - `postId` (Long)
- 返回：`Result`

### 9.3 我收藏的帖子
- 方法：`GET`
- 路径：`/api/post-favorites/my`
- 请求参数（Query）：同 `PostQueryDTO`
- 返回：`Result`
  - `data`: `PostVO[]`
  - `total`: 收藏记录总数

---

## 10. 错误与状态码（代码中实际出现）

常见 `code`：
- `200`: 成功
- `400`: 参数错误 / 业务校验失败
- `401`: 未登录或登录失败
- `403`: 账号禁用
- `404`: 资源不存在
- `500`: 服务器错误

常见 `message`（中英文混用，按代码原样）：
- `success`
- `parameter error`
- `username or password incorrect`
- `user not found`
- `account is disabled`
- `Post not found`
- `评论不存在`
- `无权删除此评论`
- `无权删除此帖子`
- `无权修改此帖子`

---

## 11. 前端对接注意事项（基于当前实现）

1. 路径必须与本文档一致（本文件已按 Controller 注解逐字整理）。
2. 分页接口的总数在 `Result.total`，列表在 `Result.data`。
3. `POST /api/post-likes/{postId}` 与 `POST /api/post-favorites/{postId}` 是“切换”语义，不是纯新增。
4. `PUT /api/users/profile` 与 `POST /api/users/avatar` 都需要通过 Query 传 `userId`。
5. 若后端后续启用拦截器，前端需确保全局带 `Authorization: Bearer <token>`。

---

## 12. 前端开发规范（页面简单明了，但功能完整）

本项目的前端实现必须遵循：**视觉简洁、信息清晰、交互直接、功能不缩水**。

### 12.1 设计与交互规范

1. 页面结构尽量采用“列表 + 详情 + 表单”三段式，避免复杂嵌套布局。
2. 单页主操作不超过 1 个主按钮 + 2 个次按钮（如：发布、取消、返回）。
3. 使用统一色板与间距体系，避免每个页面单独设计风格。
4. 文案要短句直达（如“发布成功”“请先登录”），禁止含糊提示。
5. 每个接口请求都要有三态反馈：`loading`、`success`、`error`。

### 12.2 功能完整性硬性要求

以下功能必须全部实现，不能因为“页面简化”而缺失：

1. 认证相关：注册、登录、获取当前用户信息。
2. 用户相关：用户主页、资料编辑、头像上传。
3. 分类相关：分类列表读取并用于发帖筛选。
4. 帖子相关：列表、搜索、分类筛选、详情、发布、编辑、删除、我的帖子。
5. 评论相关：评论列表、发表评论、删除评论、回复评论。
6. 点赞相关：点赞/取消点赞、我的点赞列表。
7. 收藏相关：收藏/取消收藏、我的收藏列表。
8. 分页相关：`pageNum`、`pageSize`、`total` 全链路可用。

### 12.3 接口对接规范（必须执行）

1. 请求方法、路径、参数名必须与本文档保持完全一致（区分大小写）。
2. 所有接口响应统一按 `Result` 处理：`code/message/data/total/success`。
3. 登录成功后必须持久化 `token`，并在请求头统一携带：
  - `Authorization: Bearer <token>`
4. 对于“切换语义”接口（点赞/收藏 POST），前端必须按当前状态刷新 UI，不得写死为“仅新增”。
5. 错误提示优先展示后端 `message` 原文，必要时再做前端兜底映射。

### 12.4 页面验收清单（交付前自检）

- [ ] 所有接口均已接入并有真实请求，不允许用假数据替代主流程。
- [ ] 关键页面（登录、帖子列表、帖子详情、个人主页）在无数据时有空态展示。
- [ ] 列表页支持分页翻页，且 `total` 与分页器显示一致。
- [ ] 提交类操作（发帖、改资料、发评论）有防重复提交与成功/失败提示。
- [ ] 删除/高风险操作有二次确认。
- [ ] 移动端宽度（常见 375px）下不出现横向滚动条。

### 12.5 性能与可维护性要求（简洁实现）

1. 优先复用通用组件：分页、弹窗、空态、加载骨架、错误提示。
2. 接口层统一封装（含 baseURL、token 注入、错误拦截），禁止页面内散写请求。
3. 页面首次渲染优先保证核心内容可见，非核心模块可延后加载。
4. 代码命名与目录结构保持语义化，便于后续扩展 Feed、通知、搜索等模块。

> 结论：**UI 可以朴素，但流程必须闭环；样式可以克制，但接口能力必须全量落地。**

---

## 13. 推荐技术栈与本地运行要求（轻量化）

为满足“本地可运行、好用、轻量化”的目标，前端建议采用以下最小可用技术栈。

### 13.1 推荐技术栈（优先）

1. 框架：`Vue 3`（Composition API）
2. 构建工具：`Vite`
3. 语言：`TypeScript`
4. 路由：`Vue Router`
5. 状态管理：`Pinia`
6. 网络请求：`Axios`
7. UI 组件库（轻量优先）：`Naive UI` 或 `Element Plus`（二选一，不混用）
8. CSS 方案：`UnoCSS` 或原生 `SCSS`（按团队熟悉度选其一）

选择原则：
- 不引入重量级 SSR 或微前端框架，先保障论坛主流程稳定。
- 不同时混用多个 UI 库，减少体积和心智负担。
- 保持依赖精简，能不用插件就不用插件。

### 13.2 本地运行环境建议

1. Node.js：建议 `18 LTS` 或 `20 LTS`
2. 包管理器：`pnpm`（推荐）或 `npm`（可选）
3. 浏览器：最新版 Chrome/Edge
4. 后端联调地址：`http://localhost:8080`

### 13.3 本地联调与可用性要求

1. 必须支持本地一键启动开发环境（前端 dev server）。
2. 必须支持 `.env.development` 配置后端地址（如 `VITE_API_BASE_URL=http://localhost:8080`）。
3. 请求层必须统一读取环境变量，不允许在页面里写死地址。
4. 登录后 token 持久化，刷新页面后应保持登录态（直到 token 失效）。
5. 本地断网或后端异常时，页面需给出可读错误提示，不得白屏。

### 13.4 轻量化工程约束（必须执行）

1. 首版仅实现论坛核心页面，不提前引入推荐流、IM、可视化大屏等重模块。
2. 路由按模块懒加载，首屏仅加载必要资源。
3. 公共能力抽离为 `api`、`store`、`components`，避免重复代码。
4. 图片资源优先压缩与懒加载，避免大图阻塞。
5. 统一错误处理与请求重试策略，减少页面层复杂判断。

### 13.5 最小页面清单（保证“简单但完整”）

必须包含以下页面：
- 登录页 / 注册页
- 帖子列表页（分类、搜索、分页）
- 帖子详情页（评论、点赞、收藏）
- 发帖/编辑页
- 我的主页页（我的帖子、我的点赞、我的收藏）
- 用户主页页（查看他人信息）

### 13.6 交付验收标准（本地可跑）

- [ ] 本地启动后可直接访问并完成注册 -> 登录 -> 浏览 -> 发帖 -> 评论 -> 点赞/收藏全流程。
- [ ] 所有请求路径与本文档保持一致，接口联调无“临时兼容路径”。
- [ ] 页面视觉简洁（信息层级清晰），但功能无缺项。
- [ ] 项目依赖数量可控，无明显冗余库。
- [ ] 新同学克隆项目后，可在短时间内完成本地启动与联调。

> 建议：先做“可用闭环”再做“视觉增强”，确保每个核心页面都能在本地稳定跑通。
