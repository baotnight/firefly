# 后端占位文件 — 目录说明

## 目录结构

```
backend/
├── pom.xml                              # Maven 项目配置
└── src/
    ├── main/
    │   ├── java/hgc/flowsyncapi/
    │   │   ├── FlowsyncApiApplication.java    # Spring Boot 启动类
    │   │   ├── config/                        # 配置类（跨域、OpenAPI）
    │   │   ├── common/
    │   │   │   └── ApiResponse.java           # 统一响应封装 ✅ 已创建
    │   │   ├── controller/                    # 8 个 Controller
    │   │   ├── service/                       # 8 个 Service 接口
    │   │   │   └── impl/                      # Service 实现类
    │   │   ├── mapper/                        # 5 个 MyBatis-Plus Mapper
    │   │   ├── entity/                        # 5 个实体类
    │   │   └── dto/                           # 7 个 DTO
    │   └── resources/
    │       └── application.yml                # 应用配置
    └── test/java/hgc/flowsyncapi/             # 测试
```

## 依赖说明

核心 Maven 依赖（在 `pom.xml` 中配置）：
- `spring-boot-starter-web` — Web 层
- `mybatis-plus-boot-starter` 3.5.8 — ORM
- `mysql-connector-j` — MySQL 驱动
- `springdoc-openapi-starter-webmvc-ui` 2.1.0 — API 文档
- `dashscope-sdk-java` — 阿里千问 SDK
- `lombok` — 代码简化
