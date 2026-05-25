# 服务器部署指南

这份文档给服务器上的 agent 使用。目标是在服务器上直接从 GitHub 拉取项目，连接 MySQL，然后运行 Web 系统。

## 1. 仓库地址

拉取地址：

```bash
git clone https://github.com/LiuZongrun7/database-lab.git
cd database-lab
```

如果服务器已经拉过仓库：

```bash
cd database-lab
git pull origin main
```

## 2. 服务器环境

建议环境：

- JDK 17 或更高版本；
- Maven 3.8 或更高版本；
- MySQL 8.x；
- Linux 服务器开放一个访问端口，例如 `8081`；
- 如果外网访问，需要安全组和防火墙也放行该端口。

检查命令：

```bash
java -version
mvn -v
mysql --version
```

项目的 `pom.xml` 使用 Java 17 编译目标，所以服务器至少要能编译 Java 17。

## 3. MySQL 准备

最简单的课程演示方式是使用有建库权限的 MySQL 用户，例如 root。生产环境不建议 root，但课程演示可以这样更省事。

推荐数据库名：

```text
lab_equipment
```

JDBC 地址示例：

```text
jdbc:mysql://127.0.0.1:3306/lab_equipment?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
```

如果不用 root，请先在 MySQL 里创建数据库和用户，并确保该用户至少有建表、删表、插入、更新、查询权限。

## 4. 首次启动

首次启动建议用 `DB_INIT_MODE=reset`，让程序自动创建表并插入演示数据。

```bash
APP_PORT=8081 \
DB_URL='jdbc:mysql://127.0.0.1:3306/lab_equipment?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai' \
DB_USER='root' \
DB_PASSWORD='你的MySQL密码' \
DB_INIT_MODE=reset \
mvn -q exec:java
```

启动成功后浏览器打开：

```text
http://服务器IP:8081/
```

演示账号：

```text
admin/123
tech/123
student_ai/123
student_bio/123
student_net/123
student_multi/123
```

## 5. 重要注意：数据库初始化模式

当前系统是课程演示项目，默认会初始化演示数据。

环境变量 `DB_INIT_MODE` 的建议用法：

- `DB_INIT_MODE=reset`：启动时重建表并插入演示数据，适合第一次部署或想恢复干净演示数据；
- `DB_INIT_MODE=none`：启动时不重建数据库，适合服务器已经有数据、只是重启服务。

第一次启动可以用：

```bash
DB_INIT_MODE=reset
```

之后如果想保留用户注册、预约、维修等运行数据，重启时请改成：

```bash
DB_INIT_MODE=none
```

如果一直使用 `reset`，每次重启都会回到种子数据，之前页面上新增的数据会丢失。

## 6. 后台运行方式

临时后台运行：

```bash
nohup env \
APP_PORT=8081 \
DB_URL='jdbc:mysql://127.0.0.1:3306/lab_equipment?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai' \
DB_USER='root' \
DB_PASSWORD='你的MySQL密码' \
DB_INIT_MODE=none \
mvn -q exec:java > lab-equipment.log 2>&1 &
```

查看端口：

```bash
lsof -nP -iTCP:8081 -sTCP:LISTEN
```

查看日志：

```bash
tail -f lab-equipment.log
```

## 7. systemd 示例

如果服务器使用 systemd，可以创建：

```bash
sudo nano /etc/systemd/system/lab-equipment.service
```

示例内容：

```ini
[Unit]
Description=Lab Equipment Management System
After=network.target mysql.service

[Service]
Type=simple
WorkingDirectory=/你的路径/database-lab
Environment=APP_PORT=8081
Environment=DB_URL=jdbc:mysql://127.0.0.1:3306/lab_equipment?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
Environment=DB_USER=root
Environment=DB_PASSWORD=你的MySQL密码
Environment=DB_INIT_MODE=none
ExecStart=/usr/bin/mvn -q exec:java
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

启用：

```bash
sudo systemctl daemon-reload
sudo systemctl enable lab-equipment
sudo systemctl start lab-equipment
sudo systemctl status lab-equipment
```

看日志：

```bash
journalctl -u lab-equipment -f
```

## 8. 常见问题

### 端口打不开

检查三件事：

```bash
lsof -nP -iTCP:8081 -sTCP:LISTEN
sudo ufw status
```

还要检查云服务器安全组是否开放 `8081`。

### MySQL 连接失败

重点检查：

- `DB_URL` 里的 IP、端口、数据库名；
- `DB_USER` 和 `DB_PASSWORD`；
- MySQL 是否允许本机 TCP 连接；
- MySQL 用户是否有创建数据库和表的权限。

### 页面能打开但数据不对

如果想恢复干净演示数据，临时用 `DB_INIT_MODE=reset` 启动一次。

如果想保留真实运行数据，后续都用 `DB_INIT_MODE=none`。

### 修改代码后页面没变化

服务器上执行：

```bash
git pull origin main
mvn -q -DskipTests compile
sudo systemctl restart lab-equipment
```

如果没有 systemd，就停止原来的 Java 进程，再重新运行启动命令。

## 9. 部署前快速验证

```bash
mvn -q test
mvn -q -DskipTests compile
```

如果服务器没有测试数据库，可以至少跑：

```bash
mvn -q -DskipTests compile
```

启动后检查：

```bash
curl http://127.0.0.1:8081/api/equipment
curl 'http://127.0.0.1:8081/api/reservations?userId=1'
```

能返回 JSON 就说明后端接口已经起来。
