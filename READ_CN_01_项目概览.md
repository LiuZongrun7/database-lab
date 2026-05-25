# 项目概览

## 项目名称

Campus Laboratory Equipment Reservation and Maintenance System

中文可以叫：

校园实验室设备预约与维护系统

## 这个系统是做什么的

这是一个 Java 数据库信息系统，用来管理大学实验室里共享设备的预约、审批、维修和耗材库存。

主要功能：

- 用户登录；
- 查看和搜索实验室设备；
- 提交设备预约申请；
- 管理员审核预约；
- 用户上报设备故障；
- 技术员接单并标记维修完成；
- 管理实验室耗材库存；
- 记录库存变动历史；
- 查看管理报表。

## 为什么这个题目适合数据库大作业

这个题目不是简单的学生管理/图书馆/电商系统，但又很容易做出数据库课程需要的复杂度。

它自然包含：

- 多种用户角色；
- 多张互相关联的数据表；
- 一对多关系；
- 多对多关系；
- 预约时间冲突检查；
- 设备维修状态；
- 库存管理；
- 库存流水记录；
- SQL 视图；
- 聚合报表；
- 事务处理；
- 主键、外键、唯一约束、检查约束和索引。

## 技术栈

| 部分 | 技术 |
| --- | --- |
| 编程语言 | Java |
| 前端 | HTML、CSS、JavaScript Web 页面 |
| 后端 | Java 内置轻量 HTTP Server |
| 数据库访问 | JDBC |
| 数据库 | 本地 MySQL 数据库 |
| 构建工具 | Maven |
| 测试 | JUnit |

作业 PDF 里说 UI 可以是 Web 或 application。我们现在选择的是 **Web 前端**，也就是浏览器打开的网页界面。

## 怎么运行

在项目根目录运行：

```bash
DB_PASSWORD='你的MySQL密码' mvn exec:java
```

或者：

```bash
DB_PASSWORD='你的MySQL密码' ./run.sh
```

然后浏览器打开：

```text
http://localhost:8080
```

## Demo 账号

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| admin | 123 | ADMIN |
| tech | 123 | TECHNICIAN |
| student_ai | 123 | STUDENT |
| student_bio | 123 | STUDENT |
| student_net | 123 | STUDENT |
| student_multi | 123 | STUDENT |

## 推荐演示流程

1. 用 `student_ai/123` 登录。
2. 在 Equipment 页面搜索设备。
3. 在 Reservations 页面提交预约申请。
4. 用 `admin/123` 登录。
5. 审批刚才的预约。
6. 上报一个设备故障。
7. 用 `tech/123` 登录。
8. 接单并标记维修完成。
9. 修改库存。
10. 打开 Reports 页面展示报表。

## 答辩时最重要的数据库点

- `student_labs` 是学生和实验室之间的多对多关系表。
- 学生只能预约自己关联实验室里的设备。
- `reservations` 有预约时间冲突检查。
- `approvals` 用来保存审批历史。
- `maintenance_tickets` 和 `maintenance_updates` 保存维修流程和更新记录。
- `stock_transactions` 保存库存变动流水。
- SQL 视图用于设备状态和报表。
- 预约、维修、库存更新都用到了事务。
