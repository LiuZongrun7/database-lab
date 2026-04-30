# Member B 指南 - 数据库设计

## 你负责什么

你负责数据库 schema、ER 图、关系模型、seed data、约束、视图、索引和数据库设计文档。

## 你应该看的文件

- `src/main/resources/db/schema.sql`
- `src/main/resources/db/seed.sql`
- `src/main/java/edu/ucd/comp2013j/lab/db/Database.java`
- `docs/03-uml-modeling.md`
- `docs/04-database-design.md`

## 你必须理解的点

数据库主要分成几组：

- 用户和角色：`users`
- 实验室和设备：`labs`, `equipment`
- 课程和访问权限：`courses`, `course_members`, `equipment_course_access`
- 预约和审批：`reservations`, `approvals`
- 维修：`maintenance_tickets`, `maintenance_updates`
- 库存：`consumables`, `stock_transactions`

两个多对多关系：

- 用户和课程：用 `course_members`
- 设备和课程：用 `equipment_course_access`

数据库里用了：

- 主键；
- 外键；
- 唯一约束；
- 检查约束；
- 视图；
- 索引；
- 事务。

## 你要自己手动做的事

- 打开 `schema.sql`，看懂每张表。
- 对照 ER 图和 SQL 表。
- 能解释为什么 `approvals` 单独建表。
- 能解释为什么 `stock_transactions` 单独建表。
- 能说出至少两个约束例子。

## 答辩可能问你

### Q: 为什么既有 `reservations.status`，又有 `approvals` 表？

A: `reservations.status` 方便快速查看当前状态。`approvals` 保存审批历史，包括审批人、审批时间、结果和备注。

### Q: 这个数据库设计最强的地方是什么？

A: 它不是单表 CRUD，有多对多关系、约束、视图、索引、事务、预约冲突检查和库存流水。

### Q: 为什么要有库存流水表？

A: 当前库存只能说明现在有多少，不能说明谁改过、什么时候改过、为什么改。`stock_transactions` 可以保存历史。

### Q: 你们创建了哪些视图？

A: `v_equipment_status`、`v_user_reservation_history`、`v_lab_usage_report`。

### Q: 真实系统中会怎么改？

A: 会加入密码哈希、更严格的权限表、更完整的审计日志，也可能迁移到 MySQL 或 PostgreSQL。
