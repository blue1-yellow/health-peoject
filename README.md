# 健康伴侣

健康管理 Web 项目，包含 React 前端和 Spring Boot 后端。仓库采用单体结构，队友可以在同一个仓库中修改前端、后端和文档。

## 目录

- frontend/：React + Vite
- backend/：Spring Boot + MyBatis

## 本地运行

需要 Java 17+、Node.js、npm 和 MySQL。先在 MySQL 中创建 health_db 数据库，并按项目数据库结构初始化表。

在 PowerShell 中设置本机数据库变量（请替换为自己的值）：

    $env:DB_URL = "jdbc:mysql://localhost:3306/health_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
    $env:DB_USERNAME = "root"
    $env:DB_PASSWORD = "你的本机数据库密码"
    $env:JWT_SECRET = "至少32位的随机字符串"

启动后端：

    cd backend
    .\mvnw.cmd spring-boot:run

另开终端启动前端：

    cd frontend
    npm ci
    npm run dev

浏览器打开 http://localhost:5175。前端默认向 http://localhost:8080 请求后端。

## 个人资料

登录后点击左下角「我的账号」可修改头像、名字、性别、身高（cm）、体重（kg）和可选年龄。头像在浏览器居中裁剪为256×256后随资料保存；支持不超过5MB的JPEG、PNG、WebP。登录用户名不变，名字用于页面显示。

接口为 `GET /user/profile` 和 `PUT /user/profile`，沿用登录token，仅操作当前账号。第一次访问时自动创建独立的 `user_profile` 表，不修改原用户表；数据库账号需有该库的建表权限。部署时请重启后端加载新接口。

身高和体重用于计算BMI。成年用户另填年龄及男/女性别后，使用Deurenberg成人公式估算体脂率：`1.20 × BMI + 0.23 × 年龄 − 10.8 × 性别系数 − 5.4`（男1、女0）。缺少信息或未满18岁时不提供估算体脂率。该结果不是实测值或医学诊断，孕期、运动员等人群可能有较大误差。

## 团队协作流程

请从 GitHub 克隆仓库，并在自己的分支开发：

    git clone <私有仓库地址>
    git checkout -b feature/your-change

完成后提交并推送分支，再通过 Pull Request 合并。不要向仓库提交 .env、数据库密码、API Key、JWT 私钥、个人健康数据或本机测试账号。

## AI 服务

真实模型配置为可选项。未配置时 AI 请求会提示服务尚未配置。豆包配置说明见 backend/AI_SETUP.md；密钥只通过后端环境变量提供，不要写入前端。

## 检查

    cd backend
    .\mvnw.cmd test
    cd ..\frontend
    npm ci
    npm run build
