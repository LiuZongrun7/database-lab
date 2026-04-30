# Member A 指南 - 项目集成与预约模块

## 你负责什么

你负责项目集成、运行入口、Web server 启动、预约申请和审批流程。

## 你应该看的文件

- `pom.xml`
- `README.md`
- `run.sh`
- `src/main/java/edu/ucd/comp2013j/lab/App.java`
- `src/main/java/edu/ucd/comp2013j/lab/web/LabWebServer.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/ReservationDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/ReservationService.java`
- `src/main/resources/web/app.js`

## 你必须理解的点

最重要的是预约事务。

在 `ReservationService.requestReservation()` 里，系统会：

1. 检查结束时间是否晚于开始时间；
2. 开启数据库事务；
3. 用 `FOR UPDATE` 锁定选中的设备；
4. 检查设备状态是否允许预约；
5. 检查同一个设备在同一时间段有没有冲突；
6. 插入一条 `PENDING` 状态的预约；
7. 成功就 commit，失败就 rollback。

时间冲突条件：

```text
existing.start_time < new_end AND existing.end_time > new_start
```

## 你要自己手动做的事

- 运行 `mvn test`。
- 运行 `DB_PASSWORD='你的MySQL密码' mvn exec:java`。
- 浏览器打开 `http://localhost:8080`。
- 用 `student1` 创建一个预约。
- 创建一个冲突时间的预约，确认会被拒绝。
- 用 `teacher` 登录并审批预约。
- 给 report 截图。

## 答辩可能问你

### Q: 为什么预约要用事务？

A: 因为检查设备状态、检查时间冲突和插入预约应该作为一个整体。如果中间出错，就要 rollback，否则可能产生错误预约。

### Q: 为什么预约一开始是 `PENDING`？

A: 因为有些设备比较重要或者高风险，需要老师或管理员审批后才能正式使用。

### Q: 为什么不用 ORM？

A: 作业不推荐 ORM 和复杂 MVC 框架。JDBC 可以直接展示 SQL，更适合数据库课程。

### Q: 项目怎么运行？

A: 在项目根目录运行 `mvn test` 测试，运行 `DB_PASSWORD='你的MySQL密码' mvn exec:java` 启动，然后打开 `http://localhost:8080`。
