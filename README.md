
在线家教服务平台

基于 Spring Boot + 微信小程序的在线家教服务平台，实现学员与教员的需求匹配、预约接单、评价反馈等完整闭环服务。前端:https://github.com/zzwdtys/tutor-platform-weixinfront

 项目简介

本项目是一个课程设计作品，旨在解决传统家教市场中信息不对称、匹配效率低、流程不透明等问题。系统支持学员发布需求、搜索教员、发起预约并评价；教员发布简历、接单、确认授课；管理员进行用户、需求、简历、预约的全面管理，并提供数据可视化大屏辅助运营决策。
项目采用前后端分离架构，后端基于Spring Boot + MyBatis-Plus + MySQL，前端使用微信小程序原生框架，通过 JWT 实现身份认证与权限控制。

本项目为独立开发的课程项目，主要用于学习和演示目的，部分功能仍在完善中。

🛠️ 技术栈

| 层级 | 技术选型 |
|------|----------|
| 后端 | Spring Boot 2.7.18, MyBatis-Plus, JWT |
| 数据库 | MySQL 8.0, Redis |
| 前端 | 微信小程序原生框架 |
| 可视化 | ECharts |
| 工具 | Maven, Lombok, Git |

核心功能

用户端（学员）
- 微信一键登录 / 账号密码注册登录
- 个人资料编辑（头像上传、昵称、手机号等）
- 发布家教需求（科目、年级、地点、预算）
- 搜索/筛选教员（科目、年级、价格区间、排序）
- 查看教员详情（简历、评价）
- 发起预约（选择需求、期望时间）
- 评价教员（多维打分 + 文字评论 + 情感标签）

教员端
- 发布/更新简历（可授科目、年级、价格、区域、自我介绍）
- 查看与自己匹配的学员需求
- 处理预约请求（接受/拒绝）
- 确认授课（授课完成后触发状态变更）
- 查看收到的评价列表（含评分、标签）

管理端（管理员）
- 用户管理（启用/禁用账号）
- 需求管理（查看、修改状态）
- 简历管理（审核/下架）
- 预约管理（强制修改状态）
- 数据大屏（总用户数、需求趋势、科目热度、地域分布）

预约状态机

待接单 → 已接单 → 已授课 → 已完成
↓ ↓
已取消 已拒绝



📊 数据库设计

系统共包含 5 张核心表：

| 表名         | 说明                       | 关键字段                                   |
|-------------|---------------------------|--------------------------------------------|
| `user`      | 用户表                     | openid, role, nickname, avatar, username   |
| `demand`    | 需求表                     | user_id, subject, grade, budget_min/max    |
| `resume`    | 简历表（一人一份）          | user_id(unique), subjects, price, location |
| `appointment`| 预约表                    | demand_id, resume_id, status, appointment_time |
| `review`    | 评价表（一预约一条）        | appointment_id(unique), score, comment_text, sentiment, tags |

🏗️ 项目结构

<img width="444" height="451" alt="image" src="https://github.com/user-attachments/assets/674667b3-7099-4e91-b670-b2d73e72caf7" />


2. 配置数据库
创建 MySQL 数据库 tutor_db（UTF-8 字符集）。
执行 doc/schema.sql（如有）或根据 entity 类自动建表（开启 ddl-auto）。
修改 application.yml 中的数据库连接信息（用户名、密码）。

3. 运行后端
bash
mvn clean install
mvn spring-boot:run
后端启动后访问 http://localhost:8080

4. 运行微信小程序
使用微信开发者工具打开前端项目中的 weixinfront/ 目录。
修改 utils/request.js 中的 BASE_URL 为你的后端地址。
在 app.json 中配置你的小程序 AppID（如需真机调试）。

📸 项目截图
登录页面

<img width="576" height="1234" alt="image" src="https://github.com/user-attachments/assets/3b913e7c-b5a0-49ed-b610-94cf176b5544" />

首页

<img width="582" height="1223" alt="image" src="https://github.com/user-attachments/assets/f17c134c-927e-428d-a1bd-ac45227bd11f" />

预约列表

<img width="568" height="1218" alt="image" src="https://github.com/user-attachments/assets/4da7c6c9-0f79-4485-9218-dd71d83a220d" />

个人中心

<img width="578" height="1231" alt="image" src="https://github.com/user-attachments/assets/112bf4bf-78c7-47dc-b78f-2ef7fbac4821" />

管理员界面

<img width="578" height="1229" alt="image" src="https://github.com/user-attachments/assets/40f68bdf-1f6d-43f3-9443-0be71dcb0adb" />

管理数据可视化

<img width="563" height="1220" alt="image" src="https://github.com/user-attachments/assets/3cb73a6f-5066-4bb2-9e2c-ef84ae884ed4" />



🔧 主要接口一览
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/user/wxlogin` | POST | 微信登录 |
| `/api/demand/create` | POST | 发布需求 |
| `/api/appointment/accept/{id}` | POST | 接单 |
| `/api/admin/statistics` | GET | 大屏统计数据 |

完整接口文档请参阅[家教服务平台技术文档.pdf](https://github.com/user-attachments/files/29043803/default.pdf)


📈 未来计划
增加关注教员功能，支持学员收藏心仪的教师。
接入消息红点提示，预约状态变化时主动提醒用户。
实现学员与教员私信对话功能，方便沟通确认上课细节。
支持拒绝理由填写，提升用户体验。
优化头像上传至云存储，并增加文件大小校验。
考虑将后端云部署,实现外网访问


🤝 贡献指南
本项目为个人课程设计，暂不接受外部贡献。如有建议，欢迎提 Issue 或联系作者。


📧 联系方式
作者：zzwdtys
邮箱：zzwdtys@outlook.com
GitHub：https://github.com/zzwdtys
