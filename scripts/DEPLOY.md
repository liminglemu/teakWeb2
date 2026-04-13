# SLE 微服务自动化部署指南

## 📋 概述

本文档介绍如何使用 `deploy-putty.ps1` 脚本将已编译的 JAR 文件部署到远程服务器。

### 部署流程

```
本地 JAR 文件 → 上传到服务器 → 执行重启脚本 → 服务启动
```

### 前提条件

- ✅ 已编译的 JAR 文件位于 `target/` 目录
- ✅ PuTTY 工具已安装（Windows）
- ✅ SSH 访问权限

---

## 🚀 快速开始

### Windows 环境

#### 1. 安装 PuTTY

下载地址：https://www.chiark.greenend.org.uk/~sgtatham/putty/latest.html

安装组件：
- [x] plink.exe
- [x] pscp.exe

#### 2. 运行部署脚本

```powershell
# 进入脚本目录
cd D:\idealProjects\guoke\sle-service\scripts

# 部署服务（上传 + 重启）
.\deploy-putty.ps1 -Service sle-as-mdm -All

# 仅上传文件（不重启）
.\deploy-putty.ps1 -Service sle-as-mdm -Upload

# 仅重启服务
.\deploy-putty.ps1 -Service sle-as-mdm -Restart

# 测试服务器连接
.\deploy-putty.ps1 -Test

# 查看帮助
.\deploy-putty.ps1 -Help
```

#### 3. 交互式部署

```powershell
cd D:\idealProjects\guoke\sle-service\scripts
.\deploy-putty.ps1
```

脚本会提示选择服务：
```
Available services:
  1). sle-as-gateway
  2). sle-as-mdm
  3). sle-as-mes
  ...
  
Enter service name or number: sle-as-mdm
```

---

### Linux/macOS 环境

#### 1. 安装 SSH 工具

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install -y sshpass openssh-client

# CentOS/RHEL
sudo yum install -y sshpass openssh-clients

# macOS (通常已预装)
brew install sshpass  # 如需要
```

#### 2. 使用命令行部署

```bash
# 进入脚本目录
cd /path/to/sle-service/scripts

# 赋予执行权限
chmod +x deploy.sh

# 部署服务（完整流程）
./deploy.sh -a -s sle-as-mdm

# 仅上传文件
./deploy.sh -u -s sle-as-mdm

# 仅重启服务
./deploy.sh -r -s sle-as-mdm

# 测试连接
./deploy.sh -t

# 交互式部署
./deploy.sh
```

#### 3. 手动部署命令

如果不用脚本，手动执行：

```bash
# 服务器配置
SERVER_HOST="192.168.2.36"
SERVER_USER="root"
SERVER_PASSWORD="Renzhong@123456"
REMOTE_PATH="/usr/local/project/sle/sle-as-mdm/target"
LOCAL_PATH="/path/to/sle-service/sle-as-mdm/target"

# 1. 创建远程目录
sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=no \
  "$SERVER_USER@$SERVER_HOST" \
  "mkdir -p $REMOTE_PATH"

# 2. 上传 JAR 文件
sshpass -p "$SERVER_PASSWORD" scp -o StrictHostKeyChecking=no \
  "$LOCAL_PATH"/sle-as-mdm-0.0.1-SNAPSHOT.jar \
  "$SERVER_USER@$SERVER_HOST:$REMOTE_PATH/"

sshpass -p "$SERVER_PASSWORD" scp -o StrictHostKeyChecking=no \
  "$LOCAL_PATH"/sle-as-mdm-0.0.1-SNAPSHOT.jar.original \
  "$SERVER_USER@$SERVER_HOST:$REMOTE_PATH/"

# 3. 重启服务
sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=no \
  "$SERVER_USER@$SERVER_HOST" \
  "cd /usr/local/project/sle && sh docker-rm-mdm.sh"

# 4. 查看服务状态
sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=no \
  "$SERVER_USER@$SERVER_HOST" \
  "docker ps | grep mdm"
```

---

## 📦 支持的服务模块

| 服务名称 | 重启脚本 | 说明 |
|---------|---------|------|
| sle-as-gateway | docker-rm-gateway.sh | API 网关 |
| sle-as-mdm | docker-rm-mdm.sh | 主数据管理 |
| sle-as-mes | docker-rm-mes.sh | 制造执行系统 |
| sle-as-tpm | docker-rm-tpm.sh | 刀具管理 |
| sle-as-qms | docker-rm-qms.sh | 质量管理 |
| sle-as-spm | docker-rm-spm.sh | 调度管理 |
| sle-as-ies | docker-rm-ies.sh | 智能工程系统 |
| sle-as-nms | docker-rm-nms.sh | 节点管理 |
| sle-as-wms | docker-rm-wms.sh | 仓库管理 |
| sle-as-uaa | docker-rm-uaa.sh | 用户认证 |

---

## ⚙️ 配置说明

### 修改服务器配置

编辑 `deploy-putty.ps1` 文件中的配置：

```powershell
# Server Configuration
$SERVER_HOST = "192.168.2.36"
$SERVER_USER = "root"
$SERVER_PASSWORD = "Renzhong@123456"
$SERVER_PORT = 22

# Remote Path Configuration
$REMOTE_BASE_PATH = "/usr/local/project/sle"
$PROJECT_ROOT = "D:\idealProjects\guoke\sle-service"
```

### 修改 Linux 脚本配置

编辑 `deploy.sh` 文件中的配置：

```bash
# 服务器配置
SERVER_HOST="192.168.2.36"
SERVER_USER="root"
SERVER_PASSWORD="Renzhong@123456"
SERVER_PORT=22

# 部署配置
REMOTE_BASE_PATH="/usr/local/project/sle"
PROJECT_ROOT="/path/to/sle-service"
```

---

## 🔧 详细操作流程

### 完整部署流程（推荐）

```powershell
# Windows PowerShell
cd D:\idealProjects\guoke\sle-service\scripts
.\deploy-putty.ps1 -Service sle-as-mdm -All
```

预期输出：
```
========================================
  SLE Service Deploy (PuTTY)
========================================

  Service:     sle-as-mdm
  Server:      192.168.2.36
  Remote Path: /usr/local/project/sle/sle-as-mdm/target/

[INFO] Accepting server host key...
[OK] Host key accepted

[INFO] Testing server connection...
[SUCCESS] Server connection OK

[INFO] Looking for JAR files in: D:\idealProjects\guoke\sle-service\sle-as-mdm\target
[INFO] Found JAR files:
  - sle-as-mdm-0.0.1-SNAPSHOT.jar (146.57 MB)
  - sle-as-mdm-0.0.1-SNAPSHOT.jar.original (1.50 MB)

[INFO] Creating remote directory...
[INFO] Uploading JAR files...
  Uploading: sle-as-mdm-0.0.1-SNAPSHOT.jar...
    [OK]
  Uploading: sle-as-mdm-0.0.1-SNAPSHOT.jar.original...
    [OK]

[SUCCESS] All files uploaded

[INFO] Verifying uploaded files:
[INFO] Executing restart script: docker-rm-mdm.sh
[SUCCESS] Service restarted

[INFO] Service status:
NAMES        STATUS    PORTS
sle-as-mdm   Up 10s    0.0.0.0:8081->8081/tcp

========================================
  Deploy Complete!
========================================
```

### 仅上传文件

适用于：需要先上传文件，稍后手动重启的场景

```powershell
# Windows
.\deploy-putty.ps1 -Service sle-as-mdm -Upload
```

```bash
# Linux
./deploy.sh -u -s sle-as-mdm
```

### 仅重启服务

适用于：文件已存在，只需重启的场景

```powershell
# Windows
.\deploy-putty.ps1 -Service sle-as-mdm -Restart
```

```bash
# Linux
./deploy.sh -r -s sle-as-mdm
```

---

## 📁 上传的文件说明

部署时会上传以下文件到服务器：

| 文件名 | 大小 | 说明 |
|-------|------|------|
| `sle-as-mdm-0.0.1-SNAPSHOT.jar` | ~146 MB | 可执行的 Spring Boot JAR 包 |
| `sle-as-mdm-0.0.1-SNAPSHOT.jar.original` | ~1.5 MB | 原始 JAR（不含依赖） |

**部署路径**：`/usr/local/project/sle/sle-as-mdm/target/`

---

## 🔍 服务器端操作

### 手动检查服务状态

在服务器上执行：

```bash
# 查看所有容器
docker ps -a

# 查看运行中的服务
docker ps | grep mdm

# 查看容器日志
docker logs sle-as-mdm

# 实时查看日志
docker logs -f sle-as-mdm

# 查看资源使用
docker stats sle-as-mdm
```

### 手动重启服务

```bash
# 进入部署目录
cd /usr/local/project/sle

# 执行重启脚本
sh docker-rm-mdm.sh

# 或者分步执行
docker stop sle-as-mdm
docker rm sle-as-mdm
cd /usr/local/project/sle/sle-as-mdm
docker-compose up -d
```

### 验证服务运行

```bash
# 检查容器状态
docker ps | grep sle-as-mdm

# 测试接口（需要知道服务端口）
curl http://localhost:8081/actuator/health

# 查看端口监听
netstat -tlnp | grep 8081
ss -tlnp | grep 8081
```

---

## 🐛 常见问题

### 1. SSH 连接失败

**问题**：无法连接到服务器

**解决方案**：
```powershell
# Windows - 先测试连接
.\deploy-putty.ps1 -Test
```

```bash
# Linux - 测试连接
sshpass -p "Renzhong@123456" ssh -o StrictHostKeyChecking=no root@192.168.2.36 "hostname"
```

### 2. Host Key 未缓存

**问题**：首次连接时提示 host key 验证

**解决方案**：脚本会自动处理，如需手动处理：
```bash
# Linux - 首次连接接受 key
ssh-keyscan -H 192.168.2.36 >> ~/.ssh/known_hosts
```

### 3. JAR 文件未找到

**问题**：提示找不到 JAR 文件

**解决方案**：
- 确保已执行 Maven 编译：`mvn clean package -DskipTests -Pprod`
- 检查 target 目录是否存在：`ls sle-as-mdm/target/*.jar`

### 4. 权限不足

**问题**：上传或执行脚本权限不足

**解决方案**：
```bash
# 服务器端设置权限
chmod 755 /usr/local/project/sle/docker-rm-mdm.sh
chmod -R 755 /usr/local/project/sle/sle-as-mdm/
```

### 5. PuTTY 工具未找到

**问题**：脚本提示找不到 plink.exe 或 pscp.exe

**解决方案**：
- 确认 PuTTY 已正确安装
- 或手动指定路径：
```powershell
$PUTTY_PATH = "C:\Program Files (x86)\PuTTY"
```

### 6. 服务启动失败

**问题**：容器状态为空或 Exited

**排查步骤**：
```bash
# 1. 查看详细日志
docker logs sle-as-mdm

# 2. 检查 docker-compose 配置
cat /usr/local/project/sle/sle-as-mdm/docker-compose.yml

# 3. 检查端口占用
netstat -tlnp | grep 8081

# 4. 手动启动并查看输出
cd /usr/local/project/sle/sle-as-mdm
docker-compose up
```

---

## 🔐 安全建议

### 敏感信息处理

1. **密码保护**：不要将密码提交到 Git
2. **使用密钥认证**：替代密码认证更安全
3. **环境变量**：将密码存储在环境变量中

### SSH 密钥认证（可选）

```bash
# Linux - 生成密钥
ssh-keygen -t rsa -b 4096
ssh-copy-id root@192.168.2.36

# 之后连接无需密码
ssh root@192.168.2.36
```

---

## 📊 GitHub Actions 部署

如需自动化部署，可使用 GitHub Actions：

### 配置 Secrets

在 GitHub 仓库 Settings → Secrets 中添加：
- `SERVER_PASSWORD`: 服务器密码

### 工作流文件

参考 `.github/workflows/deploy-pipeline.yml`

---

## 📞 技术支持

如遇到问题，请提供：
1. 完整的错误输出
2. 运行的命令
3. 服务器环境信息

---

**版本**: 2.0.0  
**更新日期**: 2026-04-03  
**脚本位置**: `scripts/deploy-putty.ps1`
