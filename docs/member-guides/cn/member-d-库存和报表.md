# Member D 指南 - 库存和报表模块

## 你负责什么

你负责耗材库存、耗材种类维护、库存变动流水、防止负库存和报表。

## 你应该看的文件

- `src/main/resources/web/index.html`
- `src/main/resources/web/app.js`
- `src/main/java/edu/ucd/comp2013j/lab/dao/InventoryDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/InventoryService.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/ReportDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/model/Consumable.java`
- `src/main/java/edu/ucd/comp2013j/lab/model/ReportRow.java`

## 你必须理解的点

库存有两张表：

- `consumables`：保存当前库存数量；
- `stock_transactions`：保存每一次库存变化历史。

库存变化时，`InventoryService.changeStock()` 会：

1. 开启事务；
2. 用 `FOR UPDATE` 锁定耗材行；
3. 读取当前库存；
4. 计算新库存；
5. 如果新库存小于 0，就拒绝；
6. 更新 `consumables.quantity`；
7. 插入一条 `stock_transactions`；
8. commit。

报表是只读的：

- `ReportDao.labUsage()` 读取 `v_lab_usage_report`；
- `ReportDao.equipmentStatusSummary()` 按设备状态分组统计。

新增耗材种类和调整库存是两个不同操作：

- 新增耗材种类：往 `consumables` 插入新的物品，例如“USB-C 线”；
- 调整库存：修改已有耗材的数量，并写入 `stock_transactions`。

## 你要自己手动做的事

- 用 `tech` 登录。
- 用 `admin` 登录并新增一种耗材。
- 在 Inventory 页面把某个库存减 1。
- 再尝试减 999，确认系统拒绝。
- 打开 Reports 页面，能解释两个报表。
- 看懂 `v_lab_usage_report` 视图。

## 答辩可能问你

### Q: 为什么不只保存当前库存？

A: 因为需要历史记录。库存流水可以记录谁改了库存、改了多少、原因是什么。

### Q: 怎么防止库存变负？

A: service 层先锁定行，读取当前数量，计算新数量。如果新数量小于 0 就抛出错误。数据库表也有 `quantity >= 0` 的 check constraint。

### Q: 报表查询做了什么？

A: 一个报表按实验室统计预约数量，另一个按设备状态统计设备数量、高风险设备数量和 open tickets。

### Q: `reorder_level` 是什么？

A: 它是补货阈值。如果库存数量小于或等于这个值，前端会显示低库存。

### Q: 管理员新增耗材时为什么还要有补货线？

A: 因为系统要根据补货线判断是否低库存。新增耗材时同时填写补货线，后面的库存提醒和报表才有依据。

### Q: 以后可以怎么改进？

A: 可以在低库存时自动生成采购申请。
