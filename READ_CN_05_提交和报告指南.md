# 提交和报告指南

## 作业要求提交什么

作业要求分开提交：

- PDF report；
- 视频；
- 代码和数据 zip。

注意：PDF 和视频不要压进代码 zip 里。

## 代码 zip 怎么生成

在项目根目录运行：

```bash
./package-source.sh
```

会生成：

```text
lab-equipment-system-source.zip
```

最终可以重命名成：

```text
GroupXX_LabEquipmentSystem_CodeAndData.zip
```

## 最终 Report 怎么写

可以从这个模板开始：

`docs/templates/final-report-template.md`

Report 应该包括：

- System Description；
- Requirements；
- System Design；
- UML diagrams；
- ER diagram；
- Database Design；
- CREATE TABLE 语句或 schema appendix；
- Implementation；
- Web UI 截图；
- Testing；
- Team Member Contribution；
- Self-assessment records；
- AI Usage Statement。

## Web UI 截图清单

Report 里要证明系统有前端，不只是数据库脚本。

建议截图：

- 登录页面；
- 主页面；
- Equipment tab；
- Reservation tab 和新建预约表单；
- 审批按钮；
- Maintenance 页面和更新弹窗；
- Inventory 页面；
- Reports 页面。

## 视频怎么录

参考：

- `docs/10-video-script.md`
- `docs/templates/video-narration-template.md`

推荐视频结构：

1. 项目背景和问题；
2. 用户角色；
3. 数据库设计；
4. 预约流程演示；
5. 维修流程演示；
6. 库存和报表演示；
7. 测试和总结。

## 双周汇报

参考：

- `docs/08b-biweekly-reports-template.md`
- `docs/templates/biweekly-email-templates.md`

提交前一定要：

- 改组号；
- 改姓名；
- 按真实情况改内容；
- 把最终 self-assessment 作为 appendix 放进 report。

## AI 使用说明

参考：

- `docs/08-ai-usage-statement.md`
- `docs/templates/ai-statement-options.md`

推荐写法：

AI assistants were used during this project mainly to help draft documentation, organise report content, prepare diagram descriptions, and support parts of code generation and review. All AI-assisted materials were checked and edited by the team. The team ran automated tests and manually reviewed the main workflows.

不要写不真实的话，比如“AI 只用于文档，代码完全没用 AI”。

## 提交前检查

运行：

```bash
mvn test
DB_PASSWORD='你的MySQL密码' mvn exec:java
```

然后浏览器打开：

```text
http://localhost:8080
```

检查：

- 组号正确；
- 姓名和学号正确；
- 分工表符合实际；
- Web UI 截图放进 report；
- AI 使用说明放进 report；
- PDF、视频、zip 是三个独立文件；
- 每个组员都读过自己的中文指南。

如果要跑完整 MySQL 自动化测试，也需要带密码：

```bash
DB_PASSWORD='你的MySQL密码' mvn test
```

不带 `DB_PASSWORD` 时，项目仍会编译，但数据库集成测试会跳过，因为密码不应该写进源码。
