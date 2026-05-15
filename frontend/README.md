# CampusHub Web

CampusHub 的前端项目，基于 **Vue 3 + Vite + TypeScript + Pinia + Vue Router + Axios + Naive UI**。

## 1. 运行环境

建议：

- Node.js 18 LTS 或 20 LTS
- npm / pnpm
- 后端服务运行在 `http://localhost:8080`

## 2. 启动步骤

```bash
npm install
npm run dev -- --host
```

浏览器访问：

```text
http://localhost:5173
```

## 3. 环境变量

开发环境默认使用：

```env
VITE_API_BASE_URL=/api
VITE_BACKEND_ORIGIN=http://localhost:8080
VITE_PROXY_TARGET=http://localhost:8080
```

说明：

- `VITE_API_BASE_URL`：Axios 的基础请求前缀
- `VITE_BACKEND_ORIGIN`：用于拼接头像、上传文件等相对路径
- `VITE_PROXY_TARGET`：Vite 本地代理目标

## 4. 已实现页面

- 登录页
- 注册页
- 帖子列表页（分类、搜索、排序、分页）
- 帖子详情页（评论、回复、点赞、收藏、删除）
- 发帖页 / 编辑页
- 我的主页页（我的帖子 / 我的点赞 / 我的收藏）
- 用户主页页
- 编辑资料页（含头像上传）

## 5. 目录结构

```text
src/
├── api/
├── components/
├── layouts/
├── router/
├── stores/
├── types/
├── utils/
└── views/
```

## 6. 对接说明

项目已按你提供的接口文档对接：

- 所有请求路径使用 `/api/...`
- 登录后统一携带 `Authorization: Bearer <token>`
- 分页接口统一读取 `Result.data` 和 `Result.total`
- 点赞 / 收藏的 POST 接口按“切换语义”处理
- 用户资料更新、头像上传均按 query 传 `userId`

## 7. 当前版本说明

1. 后端未提供帖子图片上传接口，所以帖子编辑页暂时使用“图片 URL 数组输入”的方式管理 `images`。
2. 后端示例数据里部分头像是 Windows 本地磁盘路径，前端已做占位图兜底处理。
3. 如果后端后续启用统一鉴权拦截器，当前前端实现可直接继续使用。

## 8. 推荐联调顺序

1. 注册 / 登录
2. 首页帖子列表
3. 帖子详情 + 评论
4. 点赞 / 收藏
5. 发帖 / 编辑 / 删除
6. 我的主页与资料编辑
