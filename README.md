# 自媒体多平台发布系统

Vue3 + Spring Boot 全栈应用，支持一键发布文章到 CSDN、知乎、微信公众号。

## 技术栈

**后端**
- Spring Boot 3.2 + Java 17
- MyBatis Plus + H2 Database
- OkHttp（HTTP请求）

**前端**
- Vue 3 + TypeScript
- Element Plus + Pinia + Vue Router

## 功能模块

| 模块 | 说明 |
|------|------|
| 📝 文章管理 | 富文本编辑、封面上传、配图管理 |
| ⚙️ 平台配置 | CSDN/知乎/微信公众号凭证管理 |
| 🚀 一键发布 | 多平台同时发布 |
| 📊 发布记录 | 查看历史和状态 |

## 使用流程

### 1. 配置平台凭证

进入【平台配置】页面，配置各平台的登录凭证：

- **CSDN**：需要 Cookie（登录后从浏览器开发者工具获取）
- **知乎**：需要 Cookie
- **微信公众号**：需要 Token

> 💡 获取 Cookie 方法：登录目标平台 → F12 打开开发者工具 → Network → 刷新页面 → 找到请求头中的 Cookie 字段复制

### 2. 创建文章

进入【文章管理】页面：
1. 点击「新建文章」
2. 填写标题、内容（支持富文本）
3. 上传封面图片（可选）
4. 保存文章

### 3. 发布文章

进入【发布管理】页面：
1. 选择要发布的文章
2. 勾选目标平台（支持多选）
3. 点击「发布」
4. 等待发布完成，查看结果

### 4. 查看历史

进入【发布历史】页面，查看所有发布记录和状态。

---

## 部署方式

### 方式一：Docker 部署（推荐）

```bash
# 克隆项目
git clone https://github.com/CatCatCccC/media-publish.git
cd media-publish

# 构建并启动
docker compose up -d --build

# 查看日志
docker compose logs -f
```

**端口说明**：
| 服务 | 端口 |
|------|------|
| 前端 | 8899 (HTTP) / 8443 (HTTPS) |
| 后端 API | 8081 |

### 方式二：本地开发

```bash
# 启动后端
cd backend
mvn spring-boot:run

# 启动前端（新终端）
cd frontend
npm install
npm run dev
```

**本地访问地址**：
| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:5173 |
| 后端 API | http://localhost:8080 |
| H2 控制台 | http://localhost:8080/h2-console |

---

## 生产环境配置

项目已部署在主服务器，通过 Nginx 反向代理实现多项目共用端口。

**公网访问地址**：https://media-publish.qinwx.top

### Nginx 配置

Nginx 配置已整合到 writeblack-blog 项目的 `frontend/nginx.conf` 中，通过域名区分转发：

- `qinwx.top` → writeblack 博客
- `media-publish.qinwx.top` → media-publish 系统

### SSL 证书

使用 Let's Encrypt 免费证书，存放在：
```
/etc/letsencrypt/live/media-publish.qinwx.top/
```

---

## 目录结构

```
media-publish/
├── backend/                # Spring Boot 后端
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/               # Vue 3 前端
│   ├── src/
│   ├── Dockerfile
│   └── package.json
└── docker-compose.yml      # Docker 编排配置
```

## 注意事项

1. **Playwright 依赖**：后端包含 Playwright 用于浏览器自动化，镜像较大（~730MB）
2. **Cookie 有效期**：平台 Cookie 会过期，需定期更新
3. **发布频率**：各平台有发布限制，请勿频繁发布
4. **数据存储**：使用 H2 文件数据库，数据存储在 `./data/` 目录

## License

MIT
