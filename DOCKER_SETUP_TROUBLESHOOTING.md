# Docker Compose 环境变量问题排查

## 问题：环境变量警告

当运行 `docker compose up -d` 时出现警告：
```
WARN[0000] The "MYSQL_ROOT_PASSWORD" variable is not set. Defaulting to a blank string.
```

## 原因

**Docker Compose 会在当前工作目录查找 `.env` 文件**。如果你在子目录（如 `feature-flag-backend`）运行命令，Docker Compose 找不到 `.env` 文件。

## 解决方案

### ✅ 正确做法：在项目根目录运行

```bash
# 确保在项目根目录
cd /Users/mac/Downloads/homework/fullstack_sample

# 然后运行 docker compose
docker compose up -d
```

### ❌ 错误做法：在子目录运行

```bash
# 不要在子目录运行
cd feature-flag-backend
docker compose up -d  # ❌ 会找不到 .env 文件
```

## 验证环境变量是否正确加载

运行以下命令验证：

```bash
# 在项目根目录运行
cd /Users/mac/Downloads/homework/fullstack_sample
docker compose config | grep -A 5 "environment:"
```

应该能看到环境变量被正确设置，例如：
```yaml
environment:
  SPRING_DATASOURCE_PASSWORD: app_password
  SPRING_DATASOURCE_USERNAME: app_user
  MYSQL_ROOT_PASSWORD: secure_password
```

## 其他可能的问题

### 1. `.env` 文件位置
确保 `.env` 文件在项目根目录（与 `docker-compose.yml` 同一目录）：
```
fullstack_sample/
├── .env                    ← 应该在这里
├── docker-compose.yml      ← 和这个文件在同一目录
├── env.example
└── ...
```

### 2. `.env` 文件格式
确保 `.env` 文件格式正确：
- 每行一个变量：`KEY=value`
- 不要有多余的空格
- 不要在值两边加引号（除非值本身需要引号）
- 注释以 `#` 开头

**正确格式：**
```bash
MYSQL_ROOT_PASSWORD=secure_password
MYSQL_DATABASE=feature_flags
```

**错误格式：**
```bash
MYSQL_ROOT_PASSWORD = secure_password  # ❌ 有空格
MYSQL_ROOT_PASSWORD="secure_password"  # ❌ 不需要引号（除非值包含空格）
```

### 3. 显式指定 `.env` 文件（不推荐）

如果必须在其他目录运行，可以显式指定 `.env` 文件：

```bash
# 在任意目录运行
docker compose --env-file /path/to/fullstack_sample/.env up -d
```

但这不推荐，因为路径可能不匹配。

## 快速检查清单

- [ ] 在项目根目录运行 `docker compose` 命令
- [ ] `.env` 文件存在于项目根目录
- [ ] `.env` 文件格式正确（`KEY=value`，无多余空格）
- [ ] 使用 `docker compose config` 验证环境变量已加载

## 常用命令

```bash
# 进入项目根目录
cd /Users/mac/Downloads/homework/fullstack_sample

# 启动所有服务
docker compose up -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f

# 停止所有服务
docker compose down

# 验证配置（包括环境变量）
docker compose config
```

