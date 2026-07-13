# 数据库占位文件

## 文件说明

- `init.sql` — 完整的 DDL + 预置数据，可直接执行初始化数据库。

## 执行方式

```bash
# 方式一：命令行
mysql -u root -p < database/init.sql

# 方式二：登录 MySQL 后
source database/init.sql;
```

## 初始化后检查

```sql
USE flowsync_simple;
SHOW TABLES;
SELECT * FROM sys_user;
```
