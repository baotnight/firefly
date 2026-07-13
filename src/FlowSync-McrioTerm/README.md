# FlowSync — 学生小组任务协同管理系统

## 一、项目概述

FlowSync 是一个面向高校学生的轻量级小组任务协同管理系统，覆盖**项目创建 → 任务分配 → 进度记录 → 总结归档**的全流程。

### 核心特性

- **JWT Token 认证** — 无状态认证，前后端分离，Token 过期自动跳转登录
- **BCrypt 密码加密** — 密码哈希存储，杜绝明文泄露
- **用户注册** — 自助注册，可选组员（直接）或项目负责人（需管理员邀请码，2min 有效）
- **角色权限控制** — 前后端双重校验，前端控制 UI 显隐，后端 Service 层校验所有权
- **数据隔离** — 组员只能看到自己参与的项目和任务
- **操作日志** — 记录登录、创建、编辑、删除等关键操作
- **级联删除** — 删除项目时自动清理关联的任务、进度和总结
- **AI 任务拆解** — 接入 DeepSeek 大模型（免费额度），自动拆解项目目标为可执行任务并推荐负责人
- **管理员系统** — 管理员可生成邀请码、升降级用户角色（负责人↔组员）
- **内网穿透支持** — 集成 ngrok，一键生成公网访问地址
- **一键启动** — `start.bat` 自动加载 `.env` 配置，前后端 + ngrok 三窗口并行

### 核心业务闭环

```
用户注册/登录 → 创建项目 → 拆分任务 → 分配负责人 → 记录进度 → 撰写总结
```

### 角色与权限

| 角色       | 角色值     | 核心权限                                             |
| ---------- | ---------- | ---------------------------------------------------- |
| 管理员     | `管理员` | 生成邀请码、升降级用户角色、查看全系统数据           |
| 项目负责人 | `负责人` | 创建/编辑/删除自己的项目和任务，查看全部可见数据     |
| 组员       | `组员`   | 查看自己参与的项目，更新自己被分配任务的状态和进度   |

---

## 二、技术栈

| 层级        | 技术                           | 版本      | 说明 |
| ----------- | ------------------------------ | --------- | ---- |
| 前端框架    | Vue 3 + Vue Router 4           | 3.x       | SPA 单页应用 |
| 前端 UI     | Element Plus                   | —        | 侧边菜单 + 动态面板 |
| HTTP 客户端 | Axios                          | —        | 拦截器自动附加 JWT Token |
| 后端框架    | Spring Boot                    | 3.3.5     | Java 17+ |
| ORM 框架    | MyBatis-Plus                   | 3.5.8     | 含分页插件 PaginationInnerInterceptor |
| 数据库      | MySQL                          | 8.x       | 7 张业务表，外键 ON DELETE CASCADE |
| 密码加密    | BCrypt (spring-security-crypto) | —       | 注册时加密存储，登录时密文比对 |
| 认证        | JWT (jjwt 0.12.6)              | —        | 无状态 Token，24h 过期 |
| AI 模型     | DeepSeek（OpenAI 兼容 API）     | deepseek-chat | 免费额度 |
| API 文档    | SpringDoc OpenAPI              | 2.1.0     | /doc.html |
| 构建工具    | Maven Wrapper + Vue CLI        | —        | 无需预装 Maven |
| 内网穿透    | ngrok                          | —        | 可选，一键公网访问 |

### 前后端通信

- 前端 `:8081`，通过 Vue CLI `devServer.proxy` 将 `/api` 代理到后端 `:8080`
- API 响应统一 `ApiResponse` 包装：`{ success, message, data }`
- 登录/注册返回 `{ token, user }`，前端存入 `sessionStorage`
- 后续请求 Axios 拦截器自动附加 `Authorization: Bearer <token>`
- 后端 `JwtInterceptor` 解析 Token → `request.setAttribute("currentUserId", xxx)`
- 401 响应前端自动清除登录态并跳转登录页

---

## 三、数据库设计

**数据库名：** `flowsync_simple`，字符集 `utf8mb4`

### 6 张业务表

| 表名             | 说明       | 关键字段 |
| ---------------- | ---------- | -------- |
| `sys_user`     | 用户表     | id, username, password(BCrypt), real_name, role, create_time |
| `project_info` | 项目表     | id, name, description, status, priority, owner_id(FK), start/end_date |
| `task_info`    | 任务表     | id, project_id(FK CASCADE), parent_id(自关联), assignee_id(FK), creator_id(FK), status, priority |
| `task_log`     | 进度记录表 | id, task_id(FK CASCADE), progress_percent, content, operator_id(FK) |
| `task_summary` | 总结表     | id, project_id(FK CASCADE), task_id(FK SET NULL), summary_type, content, created_by(FK) |
| `operation_log` | 操作日志表 | id, operator_id(FK SET NULL), action, target_type, target_id, detail, create_time |
| `invite_code` | 邀请码表 | id, code, created_by(FK), used, create_time(2min过期) |

### 预置用户（密码均为 `123456` 的 BCrypt 哈希）

| 用户名  | 真实姓名   | 角色   |
| ------- | ---------- | ------ |
| admin   | 系统管理员 | 管理员 |
| leader  | 项目负责人 | 负责人 |
| member1 | 王小明     | 组员   |
| member2 | 李小华     | 组员   |

---

## 四、后端架构

### 4.1 包结构

```
hgc.flowsyncapi
├── controller/
│   ├── AuthController           # 登录/注册 → 返回 JWT Token
│   ├── ProjectController        # 项目 CRUD + 负责人权限校验 + 操作日志
│   ├── TaskController           # 任务 CRUD + 数据隔离 + 操作日志
│   ├── TaskLogController        # 进度记录 + 操作日志
│   ├── TaskSummaryController    # 总结管理 + 操作日志
│   ├── OverviewController       # 仪表盘统计
│   ├── UserController           # 用户列表 + 修改密码
│   └── AiController             # AI 任务建议 + 拆解 + 导入
├── service/
│   ├── AuthService              # login(BCrypt验证) / register(BCrypt加密) / updatePassword
│   ├── ProjectInfoService       # CRUD + isProjectOwner / listVisibleProjectIds + listOwnedProjects / transferOwnership
│   ├── TaskInfoService          # CRUD + updateTaskStatus + getById
│   ├── TaskLogService           # CRUD
│   ├── TaskSummaryService       # CRUD
│   ├── OverviewService          # 统计查询
│   ├── UserService              # 用户列表
│   ├── OperationLogService      # 操作日志记录 / 分页查询
│   └── QwenService              # DeepSeek API 调用 + 降级方案
├── service/impl/                # 8 个实现类（全部已实现）
├── mapper/                      # 6 个 MyBatis-Plus Mapper
├── entity/                      # 6 个实体类
├── dto/                         # 8 个 DTO（LoginRequest, RegisterRequest, PasswordUpdateRequest + AI 骨架）
├── common/
│   ├── ApiResponse.java         # 统一响应 {success, message, data}
│   └── JwtUtils.java            # JWT 生成/解析/验证
└── config/
    ├── CorsConfig.java          # 跨域（allow all origins for LAN/ngrok）
    ├── OpenApiConfig.java       # SpringDoc
    ├── PasswordConfig.java      # BCryptPasswordEncoder Bean
    ├── JwtInterceptor.java      # JWT 拦截器（除 login/register 外全部校验）
    ├── WebMvcConfig.java        # 注册拦截器
    ├── MybatisPlusConfig.java   # 分页插件
    └── MetaObjectHandlerConfig.java  # createTime 自动填充
```

### 4.2 API 接口一览

| 模块 | 接口 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 认证 | `/api/auth/login` | POST | 无需 | 返回 `{token, user}` |
| 认证 | `/api/auth/register` | POST | 无需 | 注册后直接返回 `{token, user}` |
| 项目 | `/api/projects` | GET | JWT | 按数据隔离规则返回可见项目 |
| 项目 | `/api/projects` | POST | JWT | 新建/编辑（编辑时校验负责人） |
| 项目 | `/api/projects/{id}` | DELETE | JWT | 级联删除（校验负责人/管理员） |
| 项目 | `/api/projects/batch-delete` | POST | JWT | 批量删除项目 |
| 任务 | `/api/tasks` | GET | JWT | 按数据隔离规则过滤（管理员看全部） |
| 任务 | `/api/tasks/batch-delete` | POST | JWT | 批量删除任务 |
| 任务 | `/api/tasks` | POST | JWT | 新建/编辑（校验项目负责人） |
| 任务 | `/api/tasks/{id}/status` | POST | JWT | 更新任务状态 |
| 任务 | `/api/tasks/{id}` | DELETE | JWT | 删除（校验项目负责人） |
| 进度 | `/api/task-logs` | GET | JWT | 获取进度记录列表 |
| 进度 | `/api/task-logs` | POST | JWT | 新增进度记录 |
| 总结 | `/api/summaries` | GET | JWT | 获取总结列表 |
| 总结 | `/api/summaries` | POST | JWT | 新增总结 |
| 概览 | `/api/overview` | GET | JWT | 统计数据 |
| 用户 | `/api/users` | GET | JWT | 全部用户列表 |
| 用户 | `/api/users/update-password` | POST | JWT | 修改密码（BCrypt 验证旧密码） |
| 用户 | `/api/users/update-profile` | POST | JWT | 修改电话/邮箱 |
| 管理 | `/api/admin/invite-code` | POST | JWT+管理员 | 生成邀请码（2 分钟有效） |
| 管理 | `/api/admin/change-role` | POST | JWT+管理员 | 升降级用户角色（降级时自动处理项目转让） |
| 管理 | `/api/admin/users` | GET | JWT+管理员 | 用户列表（管理视图） |
| 管理 | `/api/admin/transfer-candidates` | GET | JWT+管理员 | 可接手项目的人选 |
| AI | `/api/ai/task-suggestion` | POST | JWT | 单任务 AI 建议 |
| AI | `/api/ai/task-plan` | POST | JWT | AI 任务拆解（含降级方案） |
| AI | `/api/ai/task-plan/import` | POST | JWT | 导入 AI 拆解结果 |

---

## 五、前端架构

### 5.1 目录结构

```
frontend/src/
├── views/HomeView.vue            # 主容器（登录/注册 + 侧边菜单 + 8 面板动态切换）
├── components/
│   ├── DashboardPanel.vue        # 仪表盘 — 4 统计卡片
│   ├── ProjectPanel.vue          # 项目管理 — 表格 + 弹窗 CRUD + 角色按钮显隐
│   ├── AiTaskPlanPanel.vue       # AI 任务拆解 — 选择项目→AI拆解→调整→导入
│   ├── TaskPanel.vue             # 任务管理 — 表格 + 弹窗 + 项目筛选 + 组员状态更新
│   ├── TaskLogPanel.vue          # 进度更新 — 进度条 + 新增弹窗
│   ├── SummaryPanel.vue          # 总结管理 — 列表 + 新增弹窗
│   ├── MemberListPanel.vue       # 成员列表 — 含注册时间
│   ├── AdminPanel.vue            # 系统管理 — 邀请码 + 升降级（仅管理员可见）
│   └── ProfilePanel.vue          # 个人信息 — 浮窗编辑电话/邮箱/密码
├── router/index.js               # SPA 路由
├── api/index.js                  # Axios + JWT 拦截器 + 401 处理
├── App.vue
└── main.js
```

### 5.2 权限控制

**前端（UI 显隐）：**
- `currentUser.role === '管理员'` → 显示「系统管理」菜单
- `currentUser.role === '负责人'` → 显示 AI 菜单 + CRUD 按钮
- 组员 → 隐藏 AI 菜单、项目/任务的编辑删除按钮

**后端（数据+操作校验）：**
- `JwtInterceptor` → 解析 Token，注入 userId + role
- `ProjectInfoService.isProjectOwner()` → 管理员直接返回 true，数据全透明
- `ProjectInfoService.listVisibleProjectIds()` → 管理员返回全部项目 ID
- `AdminController.checkAdmin()` → 管理操作前校验管理员身份
- `AuthServiceImpl.register()` → 注册负责人时验证邀请码有效性
- `QwenServiceImpl` → AI 拆解时排除管理员（不作为任务候选人）
- 一个项目只有一个 `owner_id`，其余人（即使系统角色是负责人）也无法编辑不属于自己的项目

---

## 六、快速启动

### 6.1 环境要求

| 工具 | 要求 | 备注 |
|------|------|------|
| JDK | 21（LTS）或 24 | JDK 24 需 Lombok 1.18.38（已配置） |
| Node.js | 16+ | |
| MySQL | 8.x | 端口 3306 |
| Maven | 无需安装 | 项目自带 Maven Wrapper |

### 6.2 一键启动（推荐）

```powershell
# PowerShell
.\start.ps1

# CMD 或双击
start.bat
```

自动弹出 2-3 个窗口：后端、前端、（如果装了 ngrok）公网隧道。

### 6.3 配置 AI（可选）

项目已集成 DeepSeek 免费 API。不配置也能正常使用，只是 AI 拆解会返回固定模板。

1. 注册 [DeepSeek](https://platform.deepseek.com/) → 获取 API Key（`sk-` 开头）
2. 复制 `backend\.env.example` → `backend\.env`
3. 编辑 `.env`，填入真实 Key：

```
DEEPSEEK_API_KEY=sk-你的Key
```

> `.env` 已加入 `.gitignore`，不会被提交到 Git。`start.bat` 启动时自动加载。
> 未配置时启动日志显示 `DeepSeek API Key NOT configured`，AI 使用降级方案。

### 6.4 手动启动

**第一步：初始化数据库（CMD 终端）**

```cmd
mysql -u root -p -e "DROP DATABASE IF EXISTS flowsync_simple;"
mysql -u root -p < database\init.sql
```

验证：

```cmd
mysql -u root -p -e "USE flowsync_simple; SHOW TABLES;"
```

应看到 7 张表：`sys_user`, `project_info`, `task_info`, `task_log`, `task_summary`, `operation_log`, `invite_code`。

**第二步：配置数据库密码**

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/flowsync_simple?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: 你的密码
```

> JDBC URL 关键参数（不要改动）：
> - `127.0.0.1` 不用 `localhost` — 避免 Windows IPv6 问题
> - `characterEncoding=UTF-8` 不能写 `utf8mb4` — Java 不认识
> - `allowPublicKeyRetrieval=true` — MySQL 8.x 认证必须

**第三步：启动后端**

```powershell
cd backend
java -Xmx1024m -classpath ".mvn/wrapper/maven-wrapper.jar" "-Dmaven.home=$PWD" "-Dmaven.multiModuleProjectDirectory=$PWD" org.apache.maven.wrapper.MavenWrapperMain spring-boot:run
```

等待 `Started FlowsyncApiApplication`。

**第四步：启动前端**

```powershell
cd frontend
npm install
npm run serve
```

浏览器访问 `http://localhost:8081`，用 `leader / 123456` 登录。

### 6.5 局域网/公网访问

**局域网：** 本机开热点 → 其他设备连热点 → 访问 `http://192.168.137.1:8081`

**公网（ngrok）：**
1. 下载 [ngrok](https://ngrok.com) → `ngrok.exe` 放项目根目录
2. 一次性配置：`ngrok config add-authtoken 你的token`
3. 运行 `start.bat`，会自动弹出 ngrok 窗口
4. 分享 ngrok 提供的 `https://xxx.ngrok-free.app` 地址

> WebSocket 已配置 `auto://` 协议自动适配 HTTPS/WSS。

---

## 七、Debug 历程（踩坑记录）

以下是开发过程中遇到的主要问题和解决方案。

| # | 问题 | 原因 | 解决 |
|---|------|------|------|
| 1 | `mvnw` 命令不存在 | 系统未装 Maven，项目无 Wrapper | 手动创建 `.mvn/wrapper/` + 下载 `maven-wrapper.jar` |
| 2 | `mvnw.cmd` 在 PowerShell 闪退 | `%~dp0` 在 PS 中解析为空 | 改用 `java -classpath .mvn/wrapper/...` 直调 |
| 3 | JDBC `Unsupported character encoding 'utf8mb4'` | Java `Charset` 只认 `UTF-8`，不认 MySQL 的 `utf8mb4` | URL 改为 `characterEncoding=UTF-8` |
| 4 | JDBC `Failed to obtain JDBC Connection` | `localhost` 在 Windows 优先解析 IPv6 `::1` | URL 改为 `127.0.0.1` |
| 5 | MySQL 8.x 认证失败 | 默认 `caching_sha2_password` 需公钥 | URL 加 `allowPublicKeyRetrieval=true&useSSL=false` |
| 6 | 编译 `找不到符号 getXxx/setXxx` | Lombok 注解处理器未配置 | pom.xml 加 `annotationProcessorPaths` |
| 7 | 编译 `TypeTag :: UNKNOWN` | Lombok 版本过旧不支持 JDK 24 | 升级 Lombok 1.18.36 → 1.18.38 |
| 8 | `init.sql` 导入乱码/超长 | MySQL 客户端默认 latin1 编码 | SQL 开头加 `SET NAMES utf8mb4;` |
| 9 | 删除项目报 FK 约束错误 | 未级联删除关联数据 | Service 层 `@Transactional` 按序删子表；SQL 加 `ON DELETE CASCADE` |
| 10 | `.bat` 双击乱码闪退 | UTF-8 中文在 CMD 中乱码→被当成命令 | 改为纯英文 |
| 11 | ngrok 访问报 WebSocket 错误 | HTTPS 页面不允许 `ws://` | `vue.config.js` 设 `webSocketURL: 'auto://'` |
| 12 | FK `fk_log_operator` 重名 | `task_log` 和 `operation_log` 用了同名 FK | 重命名为 `fk_oplog_operator` |
| 13 | 500 错误无堆栈 | 数据库未同步（缺 `operation_log` 表） | 重建数据库执行新版 init.sql |
| 14 | AI 始终显示"不可用" | DeepSeek API Key 未注入到后端进程 | 通过环境变量 `DEEPSEEK_API_KEY` 或 `.env` 文件配置 |
| 15 | `start.bat` 找不到 `.env` | `%ROOT%backend` 拼接时缺少 `\` 分隔符 | 改为 `%ROOT%\backend\.env` |
| 16 | `findstr` 匹配不到 Key | `^` 在 findstr 中不是正则行首符 | 改用 `findstr /B` 匹配行首 |
| 17 | `cmd /k` 嵌套引号截断路径 | 双引号嵌套导致 CMD 解析错乱 | 去外层引号，`&&` 改为 `^&^&` |
| 18 | `start` 子窗口不继承 `set` 变量 | `start` 新建 CMD 进程，不继承父进程的 `set` | 父进程直接 `set "DEEPSEEK_API_KEY=xxx"` → 子进程继承环境变量 |

---

## 八、常见问题排查

### Q1: 编译报错

- `找不到符号 getXxx` → Lombok 版本与 JDK 不兼容，换 JDK 21 或用 Lombok 1.18.38+
- `TypeTag :: UNKNOWN` → JDK 24 需 Lombok ≥ 1.18.38

### Q2: 数据库连接失败

1. `sc query MySQL` 确认 MySQL 服务运行
2. `mysql -u root -p` 验证密码
3. 确认已执行 `init.sql`，`SHOW TABLES` 有 6 张表
4. URL 中用 `127.0.0.1` 不要用 `localhost`

### Q3: 网页 401 / Token 过期

`sessionStorage` 中的 token 过期或丢失，刷新页面重新登录即可。Token 默认有效期 24 小时。

### Q4: PowerShell 执行 SQL 报错

PowerShell 不支持 `<` 重定向，换成 CMD 或 `Get-Content init.sql | mysql -u root -p`

### Q5: npm 安装慢

```bash
npm config set registry https://registry.npmmirror.com
```

---

## 九、权限控制速查

| 操作 | 管理员 | 负责人 | 组员 | 后端校验 |
|------|--------|--------|------|----------|
| 查看仪表盘 | ✅ | ✅ | ✅ | — |
| 查看项目列表 | ✅(全部) | ✅(自己的) | ✅(参与的) | `listVisibleProjectIds` |
| 新建项目 | ✅ | ✅ | ❌ | — |
| 编辑/删除项目 | ✅(任意) | ✅(自己的) | ❌ | `isProjectOwner`(管理员=true) |
| 批量删除项目 | ✅ | ✅ | ❌ | `isProjectOwner` |
| 查看任务列表 | ✅(全部) | ✅ | ✅ | `listVisibleProjectIds` |
| 新建/编辑/删除任务 | ✅(任意) | ✅(自己的项目) | ❌ | `isProjectOwner` |
| 批量删除任务 | ✅ | ✅(自己的) | ❌ | `isProjectOwner` |
| 更新自己任务状态 | ✅ | ✅ | ✅ | assignee 校验 |
| 新增进度/总结 | ✅ | ✅ | ✅ | — |
| 修改个人密码 | ✅ | ✅ | ✅ | BCrypt 验证 |
| 修改电话/邮箱 | ✅ | ✅ | ✅ | 浮窗编辑 |
| 生成邀请码 | ✅ | ❌ | ❌ | 2min 有效 |
| 升降级用户角色 | ✅ | ❌ | ❌ | 降级时自动处理项目转让 |
| AI 任务拆解 | ✅ | ✅ | ❌ | 管理员不被列为候选人 |
| 批量删除项目/任务 | ✅ | ✅(自己的) | ❌ | `isProjectOwner` |

---

## 十、文件树全览

```
appForXiaoxueqi/
├── README.md
├── .gitignore                        # 排除 .env / target / node_modules
├── start.bat                         # 一键启动（CMD / 双击，自动加载 .env）
├── start.ps1                         # 一键启动（PowerShell，自动加载 .env）
├── database/
│   ├── README.md
│   └── init.sql                      # 7 张表 DDL + BCrypt 预置 + ON DELETE CASCADE
├── backend/
│   ├── pom.xml                       # Lombok 1.18.38, jjwt 0.12.6, BCrypt, 分页插件
│   ├── .env.example                   # DeepSeek API Key 模板（不提交 Git）
│   ├── .env                           # 实际 Key（.gitignore 排除）
│   ├── mvnw.cmd
│   ├── .mvn/wrapper/
│   └── src/main/
│       ├── resources/
│       │   ├── application.yml       # MySQL 数据源 + DeepSeek Key 引用
│       │   └── init-h2.sql
│       └── java/hgc/flowsyncapi/
│           ├── FlowsyncApiApplication.java
│           ├── config/
│           │   ├── CorsConfig.java           # CORS allow all
│           │   ├── OpenApiConfig.java        # SpringDoc
│           │   ├── PasswordConfig.java       # BCrypt Bean
│           │   ├── JwtInterceptor.java       # JWT 拦截器
│           │   ├── WebMvcConfig.java         # 拦截器注册
│           │   ├── MybatisPlusConfig.java    # 分页插件
│           │   └── MetaObjectHandlerConfig.java # createTime 自动填充
│           ├── common/
│           │   ├── ApiResponse.java          # 统一响应
│           │   └── JwtUtils.java             # JWT 工具
│           ├── entity/                       # 7 个实体（含 InviteCode/OperationLog）
│           ├── dto/                          # 9 个 DTO（含 ProfileUpdateRequest）
│           ├── mapper/                       # 7 个 Mapper（含 InviteCodeMapper）
│           ├── service/
│           │   ├── AuthService.java          # login/register(含邀请码验证)/updatePwd
│           │   ├── ProjectInfoService.java   # isProjectOwner/listVisibleProjectIds
│           │   ├── TaskInfoService.java      # getById
│           │   ├── TaskLogService.java
│           │   ├── TaskSummaryService.java
│           │   ├── OverviewService.java
│           │   ├── UserService.java          # updateProfile/changeRole
│           │   ├── InviteCodeService.java    # 邀请码生成/验证
│           │   ├── OperationLogService.java  # 操作日志
│           │   ├── QwenService.java          # DeepSeek AI
│           │   └── impl/                     # 10 个实现类
│           └── controller/
│               ├── AdminController.java      # 系统管理（新增）
└── frontend/
    ├── package.json
    ├── vue.config.js                  # host:0.0.0.0, auto:// WebSocket
    ├── public/index.html
    └── src/
        ├── main.js
        ├── App.vue
        ├── router/index.js
        ├── api/index.js               # Axios + JWT Bearer + 401 拦截
        ├── views/HomeView.vue         # 登录/注册 + 侧栏 + 面板切换
        └── components/                # 8 个面板组件
```

---

## 十一、完整搭建流程（教学参考）

### 阶段一：基础设施（第 1～2 天）

| 步骤 | 内容 | 交付物 |
|------|------|--------|
| 1 | 设计 6 张表 ER 图，写 DDL + FK + CASCADE + 预置数据 | `database/init.sql` |
| 2 | Spring Boot 3.3.5 + Maven Wrapper + 依赖（Web, MyBatis-Plus 3.5.8, MySQL, Lombok 1.18.38, jjwt 0.12.6, BCrypt, SpringDoc） | `pom.xml` |
| 3 | `application.yml`（MySQL 数据源 127.0.0.1:3306, UTF-8） + `ApiResponse.java` + CORS | 后端骨架可启动 |
| 4 | `maven-compiler-plugin` 配置 `annotationProcessorPaths`（Lombok） | 编译通过 |
| 5 | Vue 3 + Element Plus + Axios + Router + `vue.config.js`（proxy + host 0.0.0.0） | 前端骨架 |

### 阶段二：认证模块（第 3 天）

| 步骤 | 内容 | 关键点 |
|------|------|--------|
| 6 | `User.java` + `UserMapper.java` | |
| 7 | `PasswordConfig.java`（BCrypt Bean）+ `JwtUtils.java` | |
| 8 | `LoginRequest.java` + `RegisterRequest.java` DTO | |
| 9 | `AuthService` → `login()` BCrypt 验证 / `register()` BCrypt 加密 + 查重 / `updatePassword()` | |
| 10 | `JwtInterceptor` + `WebMvcConfig` — 拦截 `/api/**`，放行 login/register | |
| 11 | `AuthController` → `POST /auth/login` + `POST /auth/register`，均返回 `{token, user}` | |
| 12 | 前端：登录页 + 注册页切换 + 主界面布局 + Axios Bearer 拦截器 | 前后端贯通 |

### 阶段三：项目与任务（第 4～5 天）

| 步骤 | 内容 | 关键点 |
|------|------|--------|
| 13 | `ProjectInfo.java` + `ProjectInfoMapper.java` | |
| 14 | `ProjectInfoService` → `listProjects(isolation)` / `saveProject(ownerCheck)` / `deleteProject(cascade+ownerCheck)` / `isProjectOwner` / `listVisibleProjectIds` | 数据隔离核心 |
| 15 | `ProjectController` → GET/POST/DELETE，全部从 `HttpServletRequest` 取 userId | |
| 16 | `TaskInfo.java` + `TaskInfoMapper.java` | |
| 17 | `TaskInfoService` → `listTasks` / `saveTask` / `updateTaskStatus` / `getById` / `deleteTask` | |
| 18 | `TaskController` → GET(数据隔离) / POST(ownerCheck) / DELETE(ownerCheck) / status | |
| 19 | 前端 `ProjectPanel.vue` + `TaskPanel.vue`（角色按钮显隐 + 组员状态更新弹窗） | |

### 阶段四：进度与总结（第 6 天）

| 步骤 | 内容 |
|------|------|
| 20 | `TaskLog.java` + `TaskLogMapper.java` + Service + Controller |
| 21 | `TaskSummary.java` + `TaskSummaryMapper.java` + Service + Controller |
| 22 | 前端 `TaskLogPanel.vue`（进度条 + 滑块）+ `SummaryPanel.vue` |

### 阶段五：操作日志与级联删除（第 7 天）

| 步骤 | 内容 | 关键点 |
|------|------|--------|
| 23 | `OperationLog.java` + `OperationLogMapper.java` + `OperationLogService` | 新表 DDL |
| 24 | `MetaObjectHandlerConfig` → `createTime` 自动填充 | MyBatis-Plus |
| 25 | `MybatisPlusConfig` → 分页插件 `PaginationInnerInterceptor` | |
| 26 | 各 Controller 注入 `OperationLogService`，关键操作写入日志 | login/register/CRUD |
| 27 | `deleteProject()` 级联删除：task_log → task_summary(by task) → task_summary(by project) → task_info → project | `@Transactional` |

### 阶段六：AI 能力（第 7 天）

| 步骤 | 内容 | 关键点 |
|------|------|--------|
| 28 | 注册 DeepSeek 账号获取 API Key → 配置 `.env` | 免费 500 万 token |
| 29 | `QwenService` → `getTaskSuggestion()` + `generateTaskPlan()` | RestTemplate 调 OpenAI 兼容 API |
| 30 | `QwenServiceImpl` → System Prompt 工程 + JSON 解析 + `assigneeId` 校验 + 降级方案 | |
| 31 | `AiController` → `/task-suggestion` + `/task-plan` + `/task-plan/import` | 导入时批量创建 TaskInfo |
| 32 | 前端 `AiTaskPlanPanel.vue`：选择项目→填目标→AI拆解→调整负责人→一键导入 | |

### 阶段七：管理员与权限增强（第 8 天）

| 步骤 | 内容 | 关键点 |
|------|------|--------|
| 33 | `InviteCode.java` + `InviteCodeMapper.java` + `InviteCodeService` | 邀请码表 DDL |
| 34 | `AdminController` → 生成邀请码 + 升降级角色 + 转让候选人 | checkAdmin 校验 |
| 35 | `AuthServiceImpl.register()` → 注册负责人时验证邀请码（2min 过期） | `validateAndConsume()` |
| 36 | `ProjectInfoService.listOwnedProjects()` + `transferOwnership()` | 批量转让项目所有权 |
| 37 | 降级负责人时自动检测拥有的项目 → 弹出转让弹窗选择接手人 | 防止"幽灵项目" |
| 38 | 前端 `AdminPanel.vue`：邀请码区 + 用户角色管理表格 + 转让弹窗 | 仅管理员可见菜单 |
| 39 | 注册表单增加角色选择：组员/负责人（选负责人时显示邀请码输入框） | |
| 40 | `ProfilePanel.vue` → 电话/邮箱/密码改为行尾「修改」按钮 → 浮窗编辑 | |
| 41 | 退出登录增加确认弹窗 | `el-popconfirm` |

### 阶段八：联调与交付

| 步骤 | 内容 |
|------|------|
| 42 | `OverviewController` + `DashboardPanel.vue` 统计卡片 |
| 43 | 端到端联调：admin 生成邀请码 → 注册负责人 → leader 创建项目 → AI拆解 → member1 更新进度 → 级联删除 |
| 44 | 权限边界全验证：管理员全透明 + 降级转让项目 + 批量删除 |
| 45 | `start.bat` / `start.ps1` + `.env` 自动加载 + ngrok + 窗口完整性测试 |
| 46 | API 文档 `http://localhost:8080/doc.html` |

---

## 十二、变更记录

| 日期 | 变更 |
|------|------|
| 2026-07-10 | 初始搭建：Spring Boot + Vue3 + MySQL + 5 表 CRUD + 前端权限控制 |
| 2026-07-10 | BCrypt 密码加密 + 用户注册 |
| 2026-07-10 | `createTime` 自动填充 |
| 2026-07-10 | `start.bat` / `start.ps1` + ngrok 外网访问 |
| 2026-07-10 | 后端权限校验：`isProjectOwner` + `listVisibleProjectIds` + 数据隔离 |
| 2026-07-10 | 级联删除：`@Transactional` + `ON DELETE CASCADE` |
| 2026-07-10 | JWT Token 认证 |
| 2026-07-10 | MyBatis-Plus 分页插件 |
| 2026-07-10 | 操作日志：`operation_log` 表 + 全 Controller 埋点 |
| 2026-07-10 | AI 能力：DeepSeek API + 任务拆解 + 降级方案 + `.env` 配置 |
| 2026-07-10 | `start.bat` 调通：findstr /B + 路径修复 + 环境变量继承 |
| 2026-07-10 | 管理员系统：邀请码(2min) + 角色升降级 + 注册角色选择 |
| 2026-07-10 | 个人信息：电话/邮箱/密码浮窗编辑 + 退出确认 |
| 2026-07-10 | 管理员全透明：`isProjectOwner`/`listVisibleProjectIds` 管理员分支 |
| 2026-07-10 | 前端权限：管理员可见所有 CRUD 按钮、AI 菜单 |
| 2026-07-10 | AI 排除管理员：`generateTaskPlan` 查询成员时过滤管理员 |
| 2026-07-10 | 批量删除：项目/任务多选 + 批量删除按钮 + 确认弹窗 |
| 2026-07-10 | 项目转让：降级负责人时检测拥有项目 → 选择接手人 → 批量转让 + 降级 |
