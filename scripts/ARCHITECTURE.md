# SLE 微服务架构分析报告

## 📊 项目概述

**项目名称**: Sle Automation Suite  
**版本**: 2.0.1  
**技术栈**: Spring Boot 2.7.3 + Java 11 + PostgreSQL + Maven  
**架构类型**: 微服务架构

---

## 🏛️ 架构设计

### 核心技术组件

| 组件 | 技术选型 | 说明 |
|------|---------|------|
| 基础框架 | Spring Boot 2.7.3 | 核心应用框架 |
| 微服务治理 | Spring Cloud + Nacos | 服务注册、配置中心 |
| 数据库 | PostgreSQL + Liquibase | 关系型数据库 + 数据库版本管理 |
| 缓存 | Redis + Redisson | 分布式缓存 |
| 安全 | Spring Security OAuth 2.0 | 认证授权 |
| 搜索引擎 | Elasticsearch 7.17.4 | 日志搜索 |
| 存储 | MinIO | 对象存储 |
| 日志 | Logback | 日志管理 |
| 监控 | Micrometer + Prometheus | 指标监控 |

### 项目模块结构

```
sle-service (父项目)
├── sle-as-gateway     # API网关
├── sle-as-mdm         # 主数据管理 (Master Data Management)
├── sle-as-mes         # 制造执行系统 (Manufacturing Execution System)
├── sle-as-tpm         # 刀具管理 (Tool Management)
├── sle-as-qms         # 质量管理 (Quality Management)
├── sle-as-spm         # 调度管理 (Scheduling Management)
├── sle-as-ies         # 智能工程系统 (Intelligent Engineering System)
├── sle-as-nms         # 节点管理 (Node Management)
├── sle-as-wms         # 仓库管理 (Warehouse Management)
└── sle-as-uaa         # 用户认证 (User Authentication)
```

---

## 📦 各服务模块详情

### 1. sle-as-gateway (API网关)

**功能定位**: 统一入口，路由转发，负载均衡

**技术特性**:
- Spring Cloud Gateway
- 路由配置
- 过滤器链
- 限流熔断

### 2. sle-as-mdm (主数据管理)

**功能定位**: 基础数据维护和管理

**核心功能**:
- 物料主数据
- 设备主数据
- 工艺路线
- 工厂日历

**数据库**: PostgreSQL (Schema: SleAsMdm)

**配置文件**: `src/main/resources/config/application.yml`

### 3. sle-as-mes (制造执行系统)

**功能定位**: 生产执行管理

**核心功能**:
- 生产工单
- 工序作业
- 生产报工
- 质量追溯

**数据库**: PostgreSQL

### 4. sle-as-tpm (刀具管理)

**功能定位**: 刀具全生命周期管理

**核心功能**:
- 刀具台账
- 刃磨管理
- 寿命管理
- 库存管理

### 5. sle-as-qms (质量管理)

**功能定位**: 质量检验和控制

**核心功能**:
- 检验标准
- 检验执行
- 不良品管理
- 质量分析

### 6. sle-as-spm (调度管理)

**功能定位**: 生产调度优化

**核心功能**:
- 排程管理
- 资源调度
- 产能分析
- 紧急插单

### 7. sle-as-ies (智能工程系统)

**功能定位**: 智能制造工程支持

**核心功能**:
- 工艺设计
- 参数优化
- 智能决策

### 8. sle-as-nms (节点管理)

**功能定位**: 设备节点管理

**核心功能**:
- 设备联网
- 数据采集
- 设备监控

### 9. sle-as-wms (仓库管理)

**功能定位**: 仓储物流管理

**核心功能**:
- 入库管理
- 出库管理
- 库存盘点
- 库位管理

### 10. sle-as-uaa (用户认证)

**功能定位**: 统一身份认证

**核心功能**:
- 用户管理
- 角色权限
- OAuth 2.0
- 单点登录

---

## 🔗 服务间通信

### 通信方式

| 通信类型 | 技术方案 | 适用场景 |
|---------|---------|---------|
| REST API | OpenFeign | 同步调用 |
| 消息队列 | (待集成) | 异步消息 |
| 配置中心 | Nacos | 配置共享 |

### 服务依赖关系

```
sle-as-gateway (入口)
    ↓
sle-as-uaa (认证)
sle-as-mdm (数据)
sle-as-mes (执行)
    ↓
sle-as-tpm / sle-as-qms / sle-as-spm / sle-as-wms / sle-as-ies / sle-as-nms
```

---

## 📁 关键配置文件

### Maven 配置

**父 POM**: `pom.xml`
- 版本管理
- 依赖管理
- 插件配置
- 构建配置

**子模块 POM**: `sle-as-*/pom.xml`
- 模块特定依赖
- 打包配置
- Profile 配置

### 应用配置

**application.yml** (各服务):
```yaml
spring:
  application:
    name: sle-as-xxx
  datasource:
    url: jdbc:postgresql://localhost:5432/SleAsXxx
    username: root
    password: xxx
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  cloud:
    nacos:
      discovery:
        server-addr: nacos-server:8848
      config:
        server-addr: nacos-server:8848
```

### Profile 配置

| Profile | 用途 |
|---------|------|
| dev | 开发环境 |
| prod | 生产环境 |
| test | 测试环境 |

---

## 🛠️ 构建和打包

### Maven 命令

```bash
# 全量构建
mvn clean package -DskipTests -Pprod

# 单模块构建
cd sle-as-mdm
mvn clean package -DskipTests -Pprod

# 打包 JAR 文件
mvn package -Pprod
```

### 输出文件

构建产物位置: `target/sle-as-xxx-0.0.1-SNAPSHOT.jar`

| 文件 | 说明 |
|------|------|
| `sle-as-mdm-0.0.1-SNAPSHOT.jar` | 可执行 JAR |
| `sle-as-mdm-0.0.1-SNAPSHOT.jar.original` | 原始 JAR (不含依赖) |

---

## 🐳 Docker 部署

### Dockerfile 位置

`src/main/docker/Dockerfile`

### Docker Compose

项目根目录: `docker-compose.yml`

### 部署脚本

服务器路径: `/usr/local/project/sle/`

| 服务 | 重启脚本 |
|------|---------|
| sle-as-gateway | `docker-rm-gateway.sh` |
| sle-as-mdm | `docker-rm-mdm.sh` |
| sle-as-mes | `docker-rm-mes.sh` |
| sle-as-tpm | `docker-rm-tpm.sh` |
| sle-as-qms | `docker-rm-qms.sh` |
| sle-as-spm | `docker-rm-spm.sh` |
| sle-as-ies | `docker-rm-ies.sh` |
| sle-as-nms | `docker-rm-nms.sh` |
| sle-as-wms | `docker-rm-wms.sh` |
| sle-as-uaa | `docker-rm-uaa.sh` |

---

## 🔒 安全架构

### 认证机制

- **OAuth 2.0**: 基于 Spring Security OAuth 2.0
- **JWT**: Token 认证
- **Nacos**: 配置加密

### 数据库安全

- **Liquibase**: 数据库变更审计
- **Hibernate**: SQL 注入防护
- **数据脱敏**: 敏感数据加密

---

## 📈 监控和运维

### 监控端点

- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/prometheus` - 指标数据

### 日志管理

- **Logback**: 日志框架
- **Logstash**: 日志收集
- **Elasticsearch**: 日志存储和搜索

---

## 🔄 CI/CD 流程

### GitHub Actions

| Workflow | 触发条件 | 功能 |
|---------|---------|------|
| ci-cd.yml | push/PR | 质量检查、测试、构建 |
| deploy-pipeline.yml | manual | 部署到服务器 |

### 部署流程

```
代码提交 → 质量检查 → 单元测试 → 集成测试 → 镜像构建 → 部署
```

---

## 📝 开发规范

### 代码规范

- **Checkstyle**: 代码风格检查
- **SpotBugs**: 静态代码分析
- **SonarQube**: 代码质量分析

### Git 规范

- **分支策略**: GitFlow
- **Commit Message**: 约定式提交
- **Code Review**: PR 合并前审查

### API 规范

- **RESTful**: REST 设计原则
- **OpenAPI 3.0**: API 文档
- **版本管理**: URL 版本控制

---

## 🚀 性能优化

### JVM 优化

- **堆内存**: -Xmx1G
- **GC**: G1GC
- **Urandom**: -Djava.security.egd=file:/dev/./urandom

### 数据库优化

- **连接池**: HikariCP
- **缓存**: Redis + Caffeine
- **索引**: 合理索引设计

### 应用优化

- **异步处理**: Spring Async
- **批处理**: Spring Batch
- **限流熔断**: Resilience4j

---

## 📚 相关文档

- [部署指南](./DEPLOY.md)
- [API 文档](./API.md) (待生成)
- [数据库设计](./DATABASE.md) (待生成)
- [运维手册](./OPS.md) (待生成)

---

**报告生成时间**: 2026-04-03  
**分析工具**: Claude Code Assistant
