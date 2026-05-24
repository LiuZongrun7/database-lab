# 代码阅读与注释指南

这个文件帮助组员按自己的分工读代码。代码里的注释不是越多越好，重点是解释“为什么这样设计”，而不是重复“这一行给变量赋值”。

## 注释风格说明

现在项目里的注释风格是统一的、简短的，比较像课程项目里学生自己写的说明：

- 事务、权限、关系表、视图这些容易被问到的地方会写注释；
- getter、setter、普通循环、普通 SQL 参数绑定不写废话注释；
- 每个成员可以在自己的 member guide 和自评里用自己的语气解释，但代码注释建议保持统一，避免看起来像临时拼出来的。

## Member A：预约与集成

建议先看：

- `src/main/java/edu/ucd/comp2013j/lab/service/ReservationService.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/ReservationDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/web/LabWebServer.java`
- `src/main/resources/web/app.js`

重点注释：

- `ReservationService.requestReservation()` 里的事务注释；
- `ReservationDao.hasTimeConflict()` 里的时间重叠解释；
- `ReservationDao.create()` 里说明一条预约可以关联多台设备和耗材需求；
- `app.js` 里 `createReservation()` 对 `equipmentIds` 和 `consumableRequests` 格式的说明。

答辩时可以说：预约不是简单插入一行，而是先锁设备、检查冲突，再写预约、设备关联和耗材需求。

## Member B：数据库设计

建议先看：

- `src/main/resources/db/schema.sql`
- `src/main/resources/db/seed.sql`
- `src/main/java/edu/ucd/comp2013j/lab/db/Database.java`

重点注释：

- `users`、`labs`、`equipment` 上面的表说明；
- `reservations.equipment_id` 和 `reservation_consumables` 的关系说明；
- `approvals` 和 `stock_transactions` 为什么单独建表；
- 三个 view 上面的说明；
- `Database.init()` 说明为什么 demo 每次重建数据。

答辩时可以说：这个数据库覆盖了主键、外键、约束、多对多关系、视图、索引和事务，不是一个很薄的 CRUD 表。

## Member C：设备与维修

建议先看：

- `src/main/java/edu/ucd/comp2013j/lab/dao/EquipmentDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/MaintenanceService.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/MaintenanceDao.java`

重点注释：

- `EquipmentDao.findAll()` 说明设备页来自视图；
- `EquipmentDao.retire()` 说明退役不是删除；
- `MaintenanceService.reportProblem()` 说明报修后设备不能继续被预约；
- `MaintenanceDao.assign()` 说明更新工单同时保留维修记录。

答辩时可以说：设备管理保留历史，维修流程用事务保证工单和设备状态一起变化。

## Member D：耗材与报表

建议先看：

- `src/main/java/edu/ucd/comp2013j/lab/service/InventoryService.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/InventoryDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/ReportDao.java`

重点注释：

- `InventoryService.changeStock()` 里的事务和负库存判断；
- `InventoryService.addConsumable()` 说明新增耗材种类和库存流水不同；
- `ReportDao.labUsage()` 说明报表读取视图；
- `ReportDao.equipmentStatusSummary()` 说明用 `GROUP BY` 做统计。

答辩时可以说：库存当前数量和库存变动历史分开保存，报表使用视图和聚合查询。

## Member E：前端、注册与测试

建议先看：

- `src/main/resources/web/index.html`
- `src/main/resources/web/style.css`
- `src/main/resources/web/app.js`
- `src/test/java/edu/ucd/comp2013j/lab/SystemServiceTest.java`

重点注释：

- `ROLE_TABS` 说明不同角色看到不同工作台；
- `loadAll()` 说明按角色加载数据；
- `applyRoleVisibility()` 说明隐藏无关入口；
- `renderEquipmentPicker()` 和 `toggleEquipmentPicker()` 说明设备选择区为什么默认收起；
- `initializeReservationTimeInputs()` 说明为什么用浏览器原生时间控件。

答辩时可以说：前端没有用复杂框架，而是用简单 HTML/CSS/JavaScript 调 Java API；注册只创建学生账号，是为了权限安全。

## 如果还要继续加注释

优先加在这些地方：

- 事务开始前；
- 关系表插入前；
- 权限判断前；
- 报表 SQL 前；
- 前端状态比较绕的地方。

不要加在这些地方：

- 每一个变量声明；
- 每一个 `if`；
- 每一个 HTML 标签；
- 很明显的按钮文案旁边。

这样代码读起来更像认真维护过，而不是为了凑注释数量。
