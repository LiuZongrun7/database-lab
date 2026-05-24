# 组员分工说明

提交前把 Member A-E 改成真实姓名。

## Member A - 项目集成与预约模块

主要负责：

- Maven 项目和启动入口；
- Java Web Server 启动；
- 登录后的整体流程；
- 预约申请，包括一次预约多台设备；
- 预约时填写耗材需求；
- 老师/管理员审批；
- 运行说明和 demo 串联。

应该看的文件：

- `pom.xml`
- `README.md`
- `run.sh`
- `src/main/java/edu/ucd/comp2013j/lab/App.java`
- `src/main/java/edu/ucd/comp2013j/lab/web/LabWebServer.java`
- `src/main/resources/web/app.js`
- `src/main/java/edu/ucd/comp2013j/lab/dao/ReservationDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/ReservationService.java`
- `docs/member-guides/cn/member-a-预约和集成.md`

必须能解释：

- 项目怎么运行；
- 预约申请怎么创建；
- 单设备固定时段预约如何检查冲突；
- 时间冲突怎么逐台设备判断；
- 预约耗材需求怎么保存；
- 为什么要用事务和 `FOR UPDATE`。

## Member B - 数据库设计

主要负责：

- ER 图；
- 关系模型；
- SQL 建表；
- 主键、外键、约束、索引；
- SQL 视图；
- seed data；
- 多对多关系表；
- 预约耗材需求表；
- 数据库设计文档。

应该看的文件：

- `src/main/resources/db/schema.sql`
- `src/main/resources/db/seed.sql`
- `src/main/java/edu/ucd/comp2013j/lab/db/Database.java`
- `docs/03-uml-modeling.md`
- `docs/04-database-design.md`
- `docs/member-guides/cn/member-b-数据库设计.md`

必须能解释：

- 主要有哪些表；
- 多对多关系怎么实现；
- 主键和外键是什么；
- 有哪些约束和视图；
- 预约表直接保存设备，`reservation_consumables` 保存耗材需求；
- 为什么审批和库存流水要单独建表。

## Member C - 设备与维修模块

主要负责：

- 设备列表；
- 设备搜索；
- 管理员新增、编辑设备；
- 管理员退役设备，而不是真删除；
- 上报设备故障；
- 创建维修工单；
- 更新维修工单状态；
- 设备状态从 AVAILABLE 变成 MAINTENANCE，再变回 AVAILABLE。

应该看的文件：

- `src/main/resources/web/index.html`
- `src/main/resources/web/app.js`
- `src/main/java/edu/ucd/comp2013j/lab/dao/EquipmentDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/MaintenanceDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/MaintenanceService.java`
- `docs/member-guides/cn/member-c-设备和维修.md`

必须能解释：

- 用户报修后发生了什么；
- 为什么设备用“退役”而不是真删除；
- 为什么设备状态会变成 `MAINTENANCE`；
- 技术员如何把工单改成已解决；
- 为什么维修更新记录要单独存。

## Member D - 库存与报表模块

主要负责：

- 实验室耗材库存；
- 管理员新增耗材种类；
- 库存增加/减少；
- 防止库存变负数；
- 库存流水；
- 报表查询。

应该看的文件：

- `src/main/resources/web/index.html`
- `src/main/resources/web/app.js`
- `src/main/java/edu/ucd/comp2013j/lab/dao/InventoryDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/ReportDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/InventoryService.java`
- `docs/member-guides/cn/member-d-库存和报表.md`

必须能解释：

- 为什么要有 `stock_transactions`；
- 新增耗材种类和调整库存有什么区别；
- 如何防止库存变负；
- 报表展示什么；
- SQL 聚合查询怎么用。

## Member E - Web 前端、测试和文档

主要负责：

- HTML/CSS/JavaScript 前端；
- 页面布局；
- 浏览器里的交互；
- 不同角色看到不同入口；
- 学生自助注册入口；
- 预约设备默认收起和已选摘要；
- 登录支持；
- 测试；
- 文档和视频脚本整理。

应该看的文件：

- `src/main/resources/web/index.html`
- `src/main/resources/web/style.css`
- `src/main/resources/web/app.js`
- `src/main/java/edu/ucd/comp2013j/lab/web/FormData.java`
- `src/main/java/edu/ucd/comp2013j/lab/web/Json.java`
- `src/main/java/edu/ucd/comp2013j/lab/model/*.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/UserDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/CourseDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/AuthService.java`
- `src/test/java/edu/ucd/comp2013j/lab/SystemServiceTest.java`
- `READ_CN_04_Web前端说明.md`
- `docs/member-guides/cn/member-e-Web前端测试文档.md`

必须能解释：

- 为什么前端是 Web 页面；
- JavaScript 怎么调用 Java API；
- 为什么学生、老师、管理员、技术员的界面入口不同；
- 注册为什么只开放学生身份；
- model class 有什么用；
- 自动化测试覆盖了什么；
- report 需要哪些截图。
