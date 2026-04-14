# 自媒体多平台发布系统

Vue3 + Spring Boot 全栈应用，支持一键发布文章到 CSDN、知乎、微信公众号。

## 技术栈

**后端**
- Spring Boot 3.2
- Java 17
- MyBatis Plus
- H2 Database

**前端**
- Vue 3 + TypeScript
- Element Plus
- Pinia + Vue Router
- Axios

## 功能

- 📝 文章管理：富文本编辑、封面上传、配图管理
- ⚙️ 平台配置：CSDN/知乎/微信公众号凭证管理
- 🚀 一键发布：多平台同时发布
- 📊 发布记录：查看历史和状态

## 快速开始

```bash
# 启动后端
cd /home/coze/publish/backend
mvn spring-boot:run

# 启动前端
cd /home/coze/publish/frontend
npm install
npm run dev
```

## 访问地址

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:5173 |
| 后端API | http://localhost:8080 |
| H2控制台 | http://localhost:8080/h2-console |

## License

MIT
