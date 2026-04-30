# Web 前端说明

## 和作业 PDF 的关系

作业 PDF 说系统可以有一个 nice user interface，例如：

> Web, application, etc.

我们现在选择的是 **Web**。前端由 HTML、CSS、JavaScript 写成，用户通过浏览器访问：

```text
http://localhost:8080
```

Java 程序负责启动一个轻量 HTTP server，既提供网页文件，也提供 API 给网页调用。

## 报告里怎么描述

可以写：

- Web front-end
- browser-based user interface
- HTML/CSS/JavaScript front-end
- Java back-end with a web UI
- lightweight Java HTTP server with JSON APIs

不要再写 Swing、desktop application 或 Java GUI。

## 主要前端文件

| 文件 | 作用 |
| --- | --- |
| `src/main/resources/web/index.html` | 页面结构，包括登录、tab、表单、表格、弹窗。 |
| `src/main/resources/web/style.css` | 页面样式和响应式布局。 |
| `src/main/resources/web/app.js` | 前端逻辑、API 调用、表格渲染、表单提交。 |
| `src/main/java/edu/ucd/comp2013j/lab/web/LabWebServer.java` | Java HTTP server 和 API handler。 |
| `src/main/java/edu/ucd/comp2013j/lab/web/FormData.java` | 解析浏览器提交的表单数据。 |
| `src/main/java/edu/ucd/comp2013j/lab/web/Json.java` | 生成简单 JSON 响应。 |

## 页面功能

### Login

API：

`POST /api/login`

功能：

- 用户输入 username/password；
- 后端验证；
- 登录成功后进入主页面。

### Equipment

API：

- `GET /api/equipment`
- `GET /api/equipment?q=keyword`
- `POST /api/maintenance/report`

功能：

- 查看设备；
- 搜索设备；
- 上报设备问题。

### Reservations

API：

- `GET /api/reservations?userId=...`
- `POST /api/reservations/create`
- `POST /api/reservations/decide`
- `POST /api/reservations/cancel`

功能：

- 创建预约；
- 查看预约；
- 老师/管理员审批；
- 取消预约。

### Maintenance

API：

- `GET /api/maintenance`
- `GET /api/users/technicians`
- `POST /api/maintenance/update`

功能：

- 查看维修工单；
- 分配技术员；
- 更新维修状态。

### Inventory

API：

- `GET /api/inventory`
- `POST /api/inventory/change`

功能：

- 查看库存；
- 修改库存；
- 显示低库存。

### Reports

API：

- `GET /api/reports/lab-usage`
- `GET /api/reports/equipment-status`

功能：

- 查看实验室使用报表；
- 查看设备状态统计。

## 前后端调用流程

浏览器不会直接连数据库。调用链是：

```text
HTML 表单 / JavaScript
  -> Java HTTP API
      -> Service class
          -> DAO class
              -> MySQL database
```

例如预约：

```text
用户填写预约表单
  -> app.js 使用 fetch() 发送 POST /api/reservations/create
      -> LabWebServer.handleCreateReservation()
          -> ReservationService.requestReservation()
              -> ReservationDao.create()
                  -> reservations 表
```

## 报告里可用的一段话

The system includes a web front-end implemented with HTML, CSS, and JavaScript. The front-end is served by a lightweight Java HTTP server and communicates with the back-end through API endpoints. The main web pages include login, equipment catalogue, reservation management, maintenance tickets, inventory, and reports. This satisfies the assignment's user interface requirement because users can operate the database system through a browser instead of using SQL directly.

## Report 截图建议

至少截这些图：

1. 登录页面；
2. 主页面；
3. Equipment 页面；
4. Reservation 表单；
5. 审批按钮；
6. Maintenance 更新弹窗；
7. Inventory 页面；
8. Reports 页面。

## 答辩可能问什么

### Q: 为什么用 Web 前端？

A: 因为我们学过 Web，HTML/CSS/JavaScript 更容易理解和解释。作业也明确说 UI 可以是 Web。

### Q: 有没有用复杂 MVC 框架？

A: 没有。我们用的是简单 HTML/CSS/JavaScript，加 Java 内置轻量 HTTP server。没有用 ORM，也没有用复杂 MVC 框架。

### Q: 前端怎么拿数据？

A: `app.js` 用 `fetch()` 调 Java API。Java 后端通过 service 和 DAO 操作 MySQL 数据库，然后返回 JSON 给浏览器。
