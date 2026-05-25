# Member E 指南 - Web 前端、测试和文档

## 你负责什么

你负责 Web 前端、角色入口、学生注册、model classes、登录支持、测试和文档整理。

## 你应该看的文件

- `src/main/resources/web/index.html`
- `src/main/resources/web/style.css`
- `src/main/resources/web/app.js`
- `src/main/java/edu/ucd/comp2013j/lab/web/LabWebServer.java`
- `src/main/java/edu/ucd/comp2013j/lab/web/FormData.java`
- `src/main/java/edu/ucd/comp2013j/lab/web/Json.java`
- `src/main/java/edu/ucd/comp2013j/lab/model/*.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/UserDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/AuthService.java`
- `src/test/java/edu/ucd/comp2013j/lab/SystemServiceTest.java`
- `docs/07-test-document.md`
- `docs/09-report-outline.md`
- `docs/10-video-script.md`

## 你必须理解的点

前端由三部分组成：

- `index.html`：页面结构、tab、表单、表格、弹窗；
- `style.css`：颜色、布局、按钮、表格、响应式；
- `app.js`：用 `fetch()` 调 API、渲染表格、处理表单提交。

最近前端还承担了几个交互细节：

- 登录页提供学生自助注册；
- 登录后根据角色显示不同 tab；
- 学生注册必须选择所属实验室；
- 预约设备选择区只显示学生关联实验室里的设备；
- 管理员才看到设备管理和新增耗材入口。

Java Web server 在 `LabWebServer.java`。它做两件事：

1. 提供静态网页文件；
2. 提供 API，比如 `/api/login`、`/api/equipment`、`/api/reservations/create`。

浏览器不会直接访问数据库。调用链是：

```text
HTML/JavaScript
  -> Java HTTP API
      -> Service
          -> DAO
              -> MySQL Database
```

自动化测试覆盖：

- 正确/错误登录；
- 学生注册；
- 预约冲突；
- 库存不能为负；
- 正常库存更新；
- 新增耗材种类；
- 报修后设备状态变化。

## 你要自己手动做的事

- 运行 `mvn test`。
- 运行 `DB_PASSWORD='你的MySQL密码' mvn exec:java`。
- 打开 `http://localhost:8080`。
- 测试登录和所有 tab。
- 测试学生注册弹窗。
- 测试不同角色看到的入口不同。
- 测试预约设备选择的展开/收起。
- 截 Web 页面图。
- 检查视频脚本。
- 确认 AI 使用说明是诚实的。

## 答辩可能问你

### Q: 为什么用 Web 前端？

A: 因为我们学过 Web，HTML/CSS/JavaScript 更容易理解和解释。作业也给了 Web 作为 UI 例子。

### Q: 有没有用复杂 Web 框架？

A: 没有。我们用简单 HTML/CSS/JavaScript 和 Java 内置轻量 HTTP server。没有 ORM，也没有复杂 MVC 框架。

### Q: 前端怎么调用后端？

A: `app.js` 用 `fetch()` 调 API。Java server 接到请求后调用 service，service 调 DAO，DAO 用 JDBC 操作数据库。

### Q: 为什么学生注册只注册成学生？

A: 因为管理员和技术员有更高权限，真实系统中不应该让用户随便自助注册成这些角色。

### Q: model class 有什么用？

A: model class 把数据库结果变成 Java 对象，代码比直接到处传 `ResultSet` 更清楚。

### Q: 测试写了什么？

A: 测试登录、预约冲突、库存验证、库存更新和维修状态变化。

### Q: 你准备了哪些文档？

A: 需求、分工、UML、数据库设计、接口/API、开发计划、测试文档、report 大纲、视频脚本和组员指南。
