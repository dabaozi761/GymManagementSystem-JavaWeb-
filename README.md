# 健身房管理系统

这是一个 Spring Boot + Vue 3 的前后端分离项目。本地使用不需要 Docker，也不需要 Nginx：分别启动后端和前端即可。

## 环境要求

- JDK 17 或 21
- MySQL 8.0.13+
- Node.js 22.12+

项目已经包含 Maven Wrapper，因此不需要单独安装 Maven。

## 最简单的启动方式

### 1. 初始化数据库

先启动 MySQL，然后导入 [sql/init.sql](sql/init.sql)。可以直接使用 Navicat、DataGrip 或 MySQL Workbench 打开并执行该文件。

> `init.sql` 会删除并重建项目数据表，只适合首次初始化。已有数据时不要重复执行。

打开 `src/main/resources/application-dev.yaml`，将数据库用户名和密码改成自己的 MySQL 配置：

```yaml
spring:
  datasource:
    username: root
    password: 你的密码
```

初始化脚本不会自动创建管理员。首次使用前，请在数据库中执行：

```sql
INSERT INTO admin (username, password, name, status)
VALUES ('admin', MD5('123456'), '管理员', 1);
```

这里的账号和密码只是示例，建议替换后再执行。

### 2. 启动后端

在项目根目录打开 PowerShell：

```powershell
.\mvnw.cmd spring-boot:run
```

后端启动后访问：

- 接口地址：<http://localhost:8080>
- 接口文档：<http://localhost:8080/doc.html>

macOS 或 Linux 使用：

```bash
./mvnw spring-boot:run
```

### 3. 启动前端

再打开一个终端，在项目根目录执行：

```powershell
cd frontend
npm ci
npm run dev
```

浏览器访问 <http://localhost:5173>，选择“管理员”，使用刚才创建的管理员账号登录。

`npm ci` 只需在首次启动或依赖变化后执行，以后通常只需要运行 `npm run dev`。

## 只构建、不启动

先停止正在运行的后端，再构建后端：

```powershell
.\mvnw.cmd clean package -DskipTests
```

生成的后端程序位于 `target/GymManagementSystem-0.0.1-SNAPSHOT.jar`。

在项目根目录一键构建前端：

```powershell
npm run build
```

该命令会自动安装前端依赖并执行正式构建，生成的静态文件位于 `frontend/dist`。

也可以进入前端目录手动执行：

```powershell
cd frontend
npm ci
npm run build
```

## 常见问题

- **后端提示数据库连接失败**：确认 MySQL 已启动，并检查 `application-dev.yaml` 中的端口、用户名和密码。
- **前端提示连接失败**：确认后端正在监听 `8080` 端口，再重新启动前端。
- **登录失败**：管理员登录时需要选择“管理员”；普通用户和会员选择“会员 / 用户”。
- **注册后不能报名**：先由管理员在会员管理中关联该用户账号，再让用户重新登录。
- **报错 `Unknown column 'member_no'`**：检查 `member` 表是否仍使用旧字段 `member_id`。确认后可执行 `ALTER TABLE member RENAME COLUMN member_id TO member_no;`。不要修改其他表中用于关联会员主键的 `member_id` 字段。

## 会员余额、流水与商城

会员卡类型统一为 `1 月卡 / 2 季卡 / 3 年卡 / 4 次卡`，全部使用会员卡上的余额。次卡可以独立核销次数，也可以使用余额购买商品。

已有数据库请先执行 [sql/upgrade_wallet_shop.sql](sql/upgrade_wallet_shop.sql)，然后重启后端和前端。该脚本保留已有数据，并增加余额流水、商品、订单三张表；不要对已有数据库重复执行 `init.sql`。

如果数据库仍有旧类型 `5` 的卡，请管理员按实际权益改为 1–4，并补齐有效期或次数；升级脚本保留旧卡和余额，不自动猜测权益。升级前的余额保留，历史充值和消费因缺少原始记录无法补造，新操作开始留流水。

管理员菜单新增充值流水、消费扣费记录、商品管理、购物与租用订单；会员菜单新增我的会员卡、我的充值记录、我的消费记录、健身商城、我的购物与租用。后端按登录身份限制数据归属，会员只能查看本人卡、流水和订单。

充值由管理员确认收款后操作。购物直接扣会员卡余额，储物柜和充电宝按件/天预付，支持 1–30 天租期，实际归还后由管理员确认归还并释放库存。商品下架不影响已有租用归还。饮料、蛋白粉和运动周边按件购买，到前台领取。本版采用单个商品直接结算，不接入在线支付、物流或租用设备硬件。

逐文件改动、接口和验证结果见 [改动报备](docs/会员余额与商城改动说明.md)。

## 项目目录

```text
GymManagementSystem/
├─ src/                 后端源码与配置
├─ sql/init.sql         数据库初始化脚本
├─ frontend/            Vue 前端
├─ package.json         前端一键构建命令
├─ pom.xml              后端 Maven 配置
└─ README.md            使用指南
```
