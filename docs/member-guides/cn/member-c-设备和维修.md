# Member C 指南 - 设备和维修模块

## 你负责什么

你负责设备列表、设备搜索、管理员设备维护、报修、维修工单和设备状态变化。

## 你应该看的文件

- `src/main/resources/web/index.html`
- `src/main/resources/web/app.js`
- `src/main/java/edu/ucd/comp2013j/lab/dao/EquipmentDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/MaintenanceDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/MaintenanceService.java`
- `src/main/java/edu/ucd/comp2013j/lab/model/Equipment.java`
- `src/main/java/edu/ucd/comp2013j/lab/model/MaintenanceTicket.java`

## 你必须理解的点

Equipment 页面读取的是 `v_equipment_status` 视图，所以页面可以显示设备所属实验室、设备状态和 open ticket count。

管理员可以新增、编辑和退役设备。这里的“退役”是把状态改成 `RETIRED`，不是从数据库真删除，因为设备可能已经被预约、报修或进入统计报表。

用户报修时：

1. Web 页面收集 title、description、priority；
2. `app.js` 调用 `POST /api/maintenance/report`；
3. Java 后端调用 `MaintenanceService.reportProblem()`；
4. 插入一条 `maintenance_tickets`；
5. 同一个事务里把设备状态改成 `MAINTENANCE`；
6. commit。

技术员把工单改成 `RESOLVED` 或 `CLOSED` 后，设备状态会变回 `AVAILABLE`。

## 你要自己手动做的事

- 用 `student1` 登录并上报一个设备问题。
- 用 `admin` 登录，新增一台设备，再编辑它。
- 把一台不再使用的设备标记为 `RETIRED`。
- 确认设备状态变成 `MAINTENANCE`。
- 用 `tech` 登录并把工单改成 `RESOLVED`。
- 确认设备状态变回 `AVAILABLE`。
- 截 Equipment 和 Maintenance 页面的图。

## 答辩可能问你

### Q: 为什么报修和改设备状态要放在同一个事务？

A: 如果只创建工单但没改设备状态，坏设备还可能被预约。如果只改状态但没创建工单，技术员不知道要修什么。所以两个操作要一起成功或一起失败。

### Q: 为什么设备不建议真删除？

A: 因为设备可能已经关联预约、维修工单、审批记录和报表。真删除容易破坏外键关系和历史记录，所以用 `RETIRED` 状态更安全。

### Q: 为什么要有 `maintenance_updates`？

A: 一个维修工单可能有多次进度更新，单独建表可以保存历史，而不是覆盖旧备注。

### Q: 维修工单有哪些状态？

A: `OPEN`、`ASSIGNED`、`IN_PROGRESS`、`RESOLVED`、`CLOSED`。

### Q: 为什么 `technician_id` 可以为空？

A: 因为工单刚创建时可能还没分配技术员。

### Q: 维修如何影响预约？

A: 预约服务会检查设备状态。如果设备是 `MAINTENANCE`，就不能预约。
