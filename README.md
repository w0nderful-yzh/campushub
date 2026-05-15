# CampusHub 校园社区论坛系统

基于 Spring Boot + Vue 3 的校园社区论坛系统。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite + Naive UI + Pinia |
| 后端 | Spring Boot 4 + MyBatis-Plus + MySQL + Redis + JWT |

## 项目结构

```
campushub/
├── backend/          # Spring Boot 后端
│   ├── src/          # Java 源码
│   ├── pom.xml       # Maven 配置
│   └── docs/         # API 文档
└── frontend/         # Vue 3 前端
    ├── src/          # 前端源码
    └── package.json  # 依赖配置
```

## 快速启动

### 环境要求
- JDK 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0+
- Redis

### 后端

```bash
cd backend
# 配置数据库连接（src/main/resources/application.yml）
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

## 功能模块

- 用户注册 / 登录（JWT 认证）
- 帖子发布 / 编辑 / 删除 / 搜索
- 评论系统（支持嵌套回复）
- 点赞 / 收藏
- 分类管理
- 个人主页 / 资料编辑

## 规划中功能

- 推荐系统
- 实时动态流
- 关注 + Feed 流
- AI 自动内容审核
- 校园地理社区
- 二手交易模块
- 排行榜系统
- 帖子全文搜索
- 消息系统
- 数据统计后台
