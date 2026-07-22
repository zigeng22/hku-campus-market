# HKU Campus Market 项目结构、实现原理与测试指南

> 文档用途：帮助组员快速理解项目文件、功能实现、数据流、Demo 数据和测试方法。
>
> 当前技术栈：Android Studio、Java 11、XML、Material Components、SQLiteOpenHelper、SharedPreferences。
>
> 当前数据范围：本地单设备数据库，不连接云端服务器。

---

## 1. 先回答最常见的 Demo 数据问题

### 1.1 不点击 Prepare demo data，能否看到预设商品？

不能。

登录页的 `Prepare demo data` 是创建本地 Demo 记录的入口。若在一台全新设备上不点击该按钮，而是直接注册新账号，则数据库中只有刚注册的账号，不会自动出现 4 个 Demo 账号、14 个 Demo 商品和预设 Offer。

这样设计的原因是：

1. 不污染正常注册用户的数据。
2. Demo 数据只在需要演示或测试时创建。
3. Release 构建默认隐藏该按钮，避免正式展示版本自动加入测试账号。

### 1.2 Demo 数据是否嵌入 App？

需要区分“创建规则”和“运行后的数据库记录”：

| 内容 | 是否随代码/GitHub 分发 | 位置 |
|---|---|---|
| 4 个账号、14 个商品、Offer 和成交场景的创建规则 | 是 | `DemoDataSeeder.java` |
| 14 张商品图片 | 是 | `app/src/main/assets/demo_products/` |
| 把图片复制到模拟器相册的逻辑 | 是 | `DemoImageInstaller.java` |
| 运行后真正生成的 users/items/offers 数据行 | 否 | 每台设备自己的 SQLite 数据库 |
| 当前登录用户 ID | 否 | 每台设备自己的 SharedPreferences |

因此可以理解为：**Demo 数据配方和素材嵌入了项目，但已经生成的数据库文件没有提交到 GitHub。**

### 1.3 队友拉取 GitHub 后是否能看到相同数据？

可以，但每位队友都需要在自己的设备上执行一次：

1. 拉取最新代码。
2. 用 Android Studio 打开 `Android_Studio_files`。
3. 运行 Debug 版本。
4. 在登录页点击 `Prepare demo data`。
5. 使用本文第 9 节的账号登录。

完成后，每台设备会独立拥有相同的基础 Demo 场景。某位队友后来新增、修改或删除的数据不会自动同步给其他队友，因为项目没有云端数据库。

### 1.4 关闭或重启 App 后数据会消失吗？

不会。SQLite 和 SharedPreferences 都保存在设备本地，普通关闭、强制停止、模拟器重启和重新打开 App 不会清空数据。

以下操作会清空或重新建立 App 数据：

- Android 设置中的 Clear storage / Clear app data。
- `adb shell pm clear com.example.a7506_project`。
- 卸载 App。
- 部分 instrumentation test 任务会安装和卸载测试 App。

---

## 2. 仓库一级目录

```text
E:\7506_project\
├─ Android_Studio_files\              Android Studio 工程，所有 App 代码在这里
├─ project_docs\                      开发计划、增强记录和本说明
├─ .github\                           GitHub 工作流或仓库配置
├─ COMP7506D_Group_Project_2026.pdf   课程项目要求原文
├─ HKU 交易平台 PRD.docx              小组产品需求文档
├─ README.md                          项目运行、Demo 和技术概览
├─ TASK_COMPLETION_REPORT.md          队友提交的历史任务完成报告
├─ AGENTS.md                          Agent 开发边界和项目规则
└─ .gitignore / .gitattributes        Git 文件规则
```

日常开发时，Android Studio 应打开：

```text
E:\7506_project\Android_Studio_files
```

不要把 PDF、PRD 或 `project_docs` 当成 Android module 导入。

---

## 3. Android 工程核心结构

```text
Android_Studio_files/
├─ app/
│  ├─ src/main/
│  │  ├─ java/com/example/a7506_project/
│  │  │  ├─ contract/       全局状态、Intent key、数据库字段契约
│  │  │  ├─ data/           SQLite 和 Repository
│  │  │  ├─ model/          数据模型与操作结果
│  │  │  ├─ ui/             各页面 Activity 和 RecyclerView Adapter
│  │  │  ├─ util/           登录状态、校验、金额、图片、Demo 工具
│  │  │  └─ MainActivity.java
│  │  ├─ res/
│  │  │  ├─ layout/         页面、列表行和 Dialog XML
│  │  │  ├─ drawable/       图标、背景、状态形状
│  │  │  ├─ values/         文本、颜色、尺寸、数组、主题、样式
│  │  │  └─ menu/           Toolbar 菜单
│  │  ├─ assets/demo_products/ 14 张 Demo 商品图片
│  │  └─ AndroidManifest.xml
│  ├─ src/debug/            只对 Debug 生效的资源配置
│  ├─ src/test/             本地 JVM 测试
│  └─ src/androidTest/      需要模拟器/真机的仪器测试
├─ gradle/                  Gradle Wrapper
├─ build.gradle.kts         工程级构建配置
├─ settings.gradle.kts      Module 和仓库配置
└─ gradlew.bat              Windows Gradle 命令入口
```

---

## 4. 软件分层与连接方式

项目采用简化的三层结构：

```text
Activity / Adapter / XML UI
            ↓
      MarketRepository
            ↓
 MarketRepositoryImpl
            ↓
 DatabaseHelper / SQLite
```

### 4.1 UI 层

- Activity 读取输入、切换页面、显示 Toast/Dialog、刷新列表。
- Adapter 把模型绑定到 RecyclerView 行。
- XML 只负责布局和资源引用。
- UI 不直接执行 SQL。

### 4.2 Repository 层

`MarketRepository` 是所有数据操作的统一接口。Activity 只依赖接口，不关心 SQL 细节。

`MarketRepositoryImpl` 负责：

- 注册、登录、更新资料、修改密码。
- 商品新增、编辑、查询和软删除。
- 搜索首页商品。
- 创建、接受和拒绝 Offer。
- 原子确认成交。
- 查询卖家发布、买家参与和成交联系人。

### 4.3 SQLite 层

`DatabaseHelper` 继承 `SQLiteOpenHelper`，创建数据库表、索引和 v1 到 v2 的迁移。

`RepositoryProvider` 在 App 中提供同一个 Repository 实例：

```java
MarketRepository repo = RepositoryProvider.get(this);
```

### 4.4 为什么采用这种结构

- 三位编码成员可以按 UI、业务逻辑、数据库/测试分工。
- UI 文件不用理解 SQL。
- Repository 测试可以直接验证业务规则。
- 修改数据库查询时，不需要重写每个 Activity。

---

## 5. Java 文件与功能对应表

### 5.1 contract

| 文件 | 作用 |
|---|---|
| `AppContract.java` | 定义 Item/Offer 状态、Offer 类型、商品类别、Intent extra 和非法 ID。 |
| `DatabaseContract.java` | 定义数据库名、版本号、四张表及全部列名。 |

任何 Agent 修改状态字符串、Intent key、数据库列名前，都必须先检查主计划中的冻结契约。

### 5.2 data

| 文件 | 作用 |
|---|---|
| `DatabaseHelper.java` | 创建 users、items、offers、trade_transactions，建立索引和执行 schema migration。 |
| `MarketRepository.java` | UI 与数据层共同遵守的公开方法接口。 |
| `MarketRepositoryImpl.java` | 所有 SQLite 查询、写入、事务和权限检查。 |
| `RepositoryProvider.java` | 创建并复用 Repository 单例。 |

### 5.3 model

| 文件 | 作用 |
|---|---|
| `User.java` | 用户 ID、昵称、WhatsApp 和创建时间。 |
| `Item.java` | 完整商品详情。 |
| `ItemDraft.java` | 发布/编辑商品时提交的数据。 |
| `ItemCard.java` | 首页和 My Listings 所需的精简商品信息及 Pending Offer 数。 |
| `Offer.java` | Offer 基础模型。 |
| `OfferSummary.java` | 卖家 Offer 列表使用的买家、金额、类型、状态和时间。 |
| `TradeTransaction.java` | 已确认成交记录。 |
| `ParticipationSummary.java` | My Activity 使用的报价、成交状态和联系人。 |
| `SortOrder.java` | 商品排序方式。 |
| `model/result/*` | 注册、报价、接受 Offer 的成功/失败结果和错误代码。 |

### 5.4 util

| 文件 | 作用 |
|---|---|
| `SessionManager.java` | SharedPreferences 保存当前 userId；不保存密码。 |
| `PasswordHasher.java` | PBKDF2 加盐哈希和密码验证。 |
| `Validators.java` | 昵称、密码、WhatsApp、商品名和价格校验。 |
| `MoneyFormatter.java` | HKD 字符串与整数 cents 互转。 |
| `CategoryFormatter.java` | 数据库类别代码转换成 UI 文本。 |
| `TradeDisplayFormatter.java` | Offer/成交状态显示文本和颜色。 |
| `ImageUriLoader.java` | 加载 content URI，失败时显示占位图。 |
| `DemoDataSeeder.java` | 幂等创建 Demo 用户、商品、Offer 和成交。 |
| `DemoImageInstaller.java` | 将 assets 图片复制进 Android MediaStore 相册并返回 content URI。 |

### 5.5 ui/auth

| 文件 | 作用 |
|---|---|
| `LoginActivity.java` | 登录、准备 Demo 数据、跳转注册页。 |
| `SignUpActivity.java` | 注册表单、字段错误和成功后返回登录。 |

### 5.6 ui/home

| 文件 | 作用 |
|---|---|
| `HomeActivity.java` | 当前用户标题、搜索、商品列表、发布/管理/账户入口和 Offer 红点。 |
| `ItemAdapter.java` | 首页商品卡片图片、名称、价格、类别和卖家绑定。 |

### 5.7 ui/item

| 文件 | 作用 |
|---|---|
| `PostEditItemActivity.java` | 发布/编辑表单、金额转换、相册选择和图片 URI 持久化。 |
| `ItemDetailActivity.java` | 商品详情、买家报价、卖家编辑/删除/查看 Offer、Sold 买家联系人。 |

### 5.8 ui/management

| 文件 | 作用 |
|---|---|
| `ManagementActivity.java` | My Listings / My Activity Tab、待处理 Offer 数字徽标。 |
| `ListingAdapter.java` | 卖家商品状态和 Offer 数。 |
| `ParticipationAdapter.java` | 买家报价/成交状态和确认后 WhatsApp。 |
| `OfferReviewActivity.java` | 校验卖家身份、接受或拒绝 Offer。 |
| `OfferAdapter.java` | Offer 行、金额、日期、状态和 Accept/Reject 按钮。 |

### 5.9 ui/profile

| 文件 | 作用 |
|---|---|
| `ProfileActivity.java` | 修改昵称、WhatsApp 和密码；密码修改要求当前密码。 |

### 5.10 启动入口

`MainActivity.java` 不显示实际页面，只负责路由：

1. 从 SessionManager 读取当前 userId。
2. 用 Repository 确认该用户仍存在。
3. 有效 session 进入 Home。
4. 无效 session 进入 Login。

---

## 6. XML 和资源文件对应表

### 6.1 页面布局

| XML | 对应页面 |
|---|---|
| `activity_login.xml` | 登录和 Prepare demo data。 |
| `activity_sign_up.xml` | 注册。 |
| `activity_home.xml` | 首页搜索、商品列表和三个底部按钮。 |
| `activity_post_edit_item.xml` | 发布/编辑商品。 |
| `activity_item_detail.xml` | 商品详情、联系人和操作区。 |
| `activity_management.xml` | My Listings / My Activity。 |
| `activity_offer_review.xml` | Offer 列表。 |
| `activity_profile.xml` | 账户设置。 |
| `activity_main.xml` | 早期入口占位；实际启动由 MainActivity 路由。 |

### 6.2 可复用列表和 Dialog

| XML | 作用 |
|---|---|
| `row_item.xml` | 首页商品卡片。 |
| `row_listing.xml` | My Listings 商品行。 |
| `row_offer.xml` | Offer 行。 |
| `row_participation.xml` | My Activity 行。 |
| `dialog_make_offer.xml` | 输入自定义 Offer 金额。 |

### 6.3 values 资源

| 文件 | 作用 |
|---|---|
| `strings.xml` | 通用文本和基础错误。 |
| `strings_auth.xml` | 登录、注册和 Demo 文本。 |
| `strings_items.xml` | 首页、商品发布和详情文本。 |
| `strings_trade.xml` | Offer、成交和管理中心文本。 |
| `strings_profile.xml` | 账户设置文本。 |
| `colors.xml` | 品牌色、文字色、状态色。 |
| `dimens.xml` | 间距、图片高度、Toolbar 高度等。 |
| `styles.xml` | Card、Button、TextInput 和文字样式。 |
| `themes.xml` | App 主题。 |
| `arrays.xml` | 商品类别等选项。 |
| `bools.xml` | 默认关闭 Demo 设置入口。 |

`src/debug/res/values/bools.xml` 把 `enable_demo_setup` 覆盖为 `true`，所以按钮只在 Debug 版本显示。

---

## 7. 数据库结构

数据库文件名：

```text
hku_campus_market.db
```

当前版本：`2`。

### 7.1 users

| 字段 | 说明 |
|---|---|
| `_id` | 用户主键。 |
| `nickname` | 唯一昵称，忽略大小写。 |
| `password_hash` | PBKDF2 密码哈希。 |
| `password_salt` | 每个账号独立 salt。 |
| `whatsapp` | 成交后展示的联系号码。 |
| `created_at` | 创建时间。 |

### 7.2 items

| 字段 | 说明 |
|---|---|
| `_id` | 商品主键。 |
| `seller_id` | 卖家 userId。 |
| `name` / `description` | 商品文本。 |
| `price_cents` | 整数分，例如 HK$120.00 存为 12000。 |
| `image_uri` | 相册或 MediaStore content URI。 |
| `category` | BOOKS、ELECTRONICS、FURNITURE、DAILY_GOODS、OTHERS。 |
| `status` | ACTIVE、SOLD、DELETED。 |
| `created_at` / `updated_at` | 时间。 |

### 7.3 offers

| 字段 | 说明 |
|---|---|
| `_id` | Offer 主键。 |
| `item_id` / `buyer_id` | 商品和买家。 |
| `amount_cents` | Offer 金额。 |
| `type` | NEGOTIATED 或 BUY_NOW。 |
| `status` | PENDING、ACCEPTED、REJECTED。 |
| `created_at` / `updated_at` | 时间。 |

同一买家对同一商品最多只有一个 Pending Offer。

### 7.4 trade_transactions

| 字段 | 说明 |
|---|---|
| `_id` | 成交主键。 |
| `item_id` | 成交商品，一件商品最多一条成交记录。 |
| `seller_id` / `buyer_id` | 成交双方。 |
| `offer_id` | 被接受的 Offer。 |
| `final_price_cents` | 最终成交价。 |
| `created_at` | 成交时间。 |

接受 Offer 在单个 SQLite transaction 中完成：

1. 目标 Offer 变为 ACCEPTED。
2. 同商品其他 Pending Offer 变为 REJECTED。
3. 商品变为 SOLD。
4. 写入 trade_transactions。
5. 任一步失败则整体回滚。

---

## 8. 主要用户流程如何连接

### 8.1 注册与登录

```text
SignUpActivity
  -> Validators
  -> MarketRepository.registerUser
  -> PasswordHasher
  -> users

LoginActivity
  -> MarketRepository.authenticate
  -> SessionManager.login(userId)
  -> HomeActivity
```

### 8.2 发布与搜索

```text
PostEditItemActivity
  -> ItemDraft
  -> MarketRepository.createItem/updateItem
  -> items

HomeActivity 输入搜索词
  -> searchActiveItems
  -> name/description LIKE 查询
  -> ItemAdapter
```

首页只显示 ACTIVE 商品，SOLD 和 DELETED 不会出现在公开列表。

### 8.3 Offer 与成交

```text
ItemDetailActivity
  -> placeOffer
  -> OfferReviewActivity
  -> acceptOffer / rejectOffer
  -> ManagementActivity 刷新
```

### 8.4 联系方式隐私

- Pending/Rejected 阶段不向买家公开卖家 WhatsApp。
- 成交后，My Activity 显示卖家 WhatsApp。
- Sold 商品详情向成交双方显示买家昵称和 WhatsApp。

### 8.5 待处理提醒

- Home 管理按钮红点表示至少有一个 Pending Offer。
- My Listings Tab Badge 显示 Pending Offer 总数。
- 接受或拒绝后，页面 `onResume()` / reload 方法重新查询并更新提醒。

### 8.6 账户设置

```text
Home Profile FAB
  -> ProfileActivity
  -> updateUserProfile / changePassword
  -> users
```

昵称修改后回到 Home，Toolbar 会在 `onResume()` 重新显示新昵称。

---

## 9. Demo 账号

所有 Demo 账号密码均为：

```text
demo123
```

| 昵称 | WhatsApp | 初始发布数 | 主要测试用途 |
|---|---:|---:|---|
| `AliceDemo` | `91234567` | 4 | Sold 联系人、收到 Offer、作为买家报价、账户设置。 |
| `BobDemo` | `92345678` | 4 | 已确认成交买家、多个 Pending 参与记录、作为卖家。 |
| `CarolDemo` | `93456789` | 3 | 家具/生活用品卖家、跨账号 Offer。 |
| `DavidDemo` | `94567890` | 3 | 运动用品卖家、对其他商品报价。 |

登录页点击准备按钮后会自动把登录表单填为 AliceDemo。

---

## 10. 14 个 Demo 商品

| 卖家 | 商品 | 价格 | 类别 | 初始状态/用途 |
|---|---|---:|---|---|
| AliceDemo | Java Programming Textbook | HK$120 | Books | ACTIVE；Bob 有 HK$100 Pending Offer。 |
| AliceDemo | Scientific Calculator | HK$80 | Electronics | SOLD；Bob 为确认买家。 |
| AliceDemo | Statistics Revision Notes | HK$65 | Books | ACTIVE；无预设 Offer，用于丰富搜索。 |
| AliceDemo | USB-C Multiport Hub | HK$130 | Electronics | ACTIVE；无预设 Offer。 |
| BobDemo | Wireless Keyboard | HK$150 | Electronics | ACTIVE；Alice 有 Pending Offer。 |
| BobDemo | Blue Study Desk Lamp | HK$90 | Daily Goods | ACTIVE；Carol 有 Pending Offer。 |
| BobDemo | Over-Ear Headphones | HK$220 | Electronics | ACTIVE；无预设 Offer。 |
| BobDemo | Compact Desk Fan | HK$70 | Daily Goods | ACTIVE；无预设 Offer。 |
| CarolDemo | Ergonomic Mesh Chair | HK$320 | Furniture | ACTIVE；David 有 Pending Offer。 |
| CarolDemo | Compact Rice Cooker | HK$180 | Daily Goods | ACTIVE；Bob 有 Pending Offer。 |
| CarolDemo | Three-Tier Storage Trolley | HK$160 | Furniture | ACTIVE；无预设 Offer。 |
| DavidDemo | Bamboo Monitor Stand | HK$110 | Furniture | ACTIVE；Carol 有 Pending Offer。 |
| DavidDemo | Tennis Racket with Cover | HK$140 | Others | ACTIVE；Alice 有 Pending Offer。 |
| DavidDemo | Bicycle Helmet | HK$100 | Others | ACTIVE；无预设 Offer。 |

首页公开列表初始可见 13 个 ACTIVE 商品，Scientific Calculator 只在 Alice 的 My Listings 和成交相关页面中显示。

---

## 11. Demo 创建逻辑

点击按钮后：

1. `LoginActivity.prepareDemoData()` 调用 `DemoDataSeeder.prepare()`。
2. Seeder 确保 4 个账号存在。
3. `DemoImageInstaller` 把 14 张 assets 图片写入 `Pictures/HKU Campus Market`。
4. MediaStore 返回的 content URI 写入 items.image_uri。
5. Seeder 按商品名检查记录，不存在才创建，因此同一次安装内反复点击不会重复插入同名商品。
6. Seeder 创建预设 Pending Offer 和 Alice/Bob confirmed deal。
7. 表单填入 AliceDemo / demo123。

若已经执行过上一版 8 商品 Seeder，更新 App 后再点击一次按钮，会补充新增的 6 个商品，不需要清除原数据。

Android 卸载 App 时 SQLite 会被删除，但系统相册中的图片可能继续保留。重新安装后 Android 可能给同名图片增加 `(1)` 后缀，不影响 App 使用。

---

## 12. 队友首次运行步骤

1. 在 GitHub 获取最新 `main`。
2. Android Studio 选择 **Open**。
3. 打开 `Android_Studio_files`，不要打开仓库一级目录作为 Android 工程。
4. 等待 Gradle Sync 完成。
5. 选择 `app` 配置和 API 24+ 模拟器；建议使用当前团队统一的 API 36 Medium Phone。
6. 点击 Run。
7. 登录页点击 `Prepare demo data`。
8. 用 AliceDemo 登录检查首页。
9. 再依次测试 BobDemo、CarolDemo、DavidDemo。

若看不到 Demo 按钮：

- 确认 Build Variant 是 `debug`。
- 执行 Build > Clean Project，然后重新 Run。
- 检查 `src/debug/res/values/bools.xml` 中 `enable_demo_setup=true`。

---

## 13. 推荐人工测试清单

### 13.1 Demo 和首页

- 点击 Prepare 后 4 个账号均可登录。
- Alice 首页显示来自不同卖家的 13 个 ACTIVE 商品。
- 商品图片正常显示。
- 搜索 `book`、`USB`、`chair`、`fan` 能筛选到对应商品。
- 搜索不存在的词显示空状态。

### 13.2 商品

- 新发布商品可以选择系统相册中的 Demo 图片。
- 发布后返回首页能看到新商品。
- 卖家可以编辑 ACTIVE 商品。
- 卖家可以软删除 ACTIVE/SOLD 商品。

### 13.3 Offer

- Bob 登录后可在 Java Programming Textbook 的 My Activity 看到 Pending。
- Alice 的管理按钮有红点，My Listings 标签有数字。
- Alice 打开教材 Offer，可以 Reject 或 Accept。
- Reject 后商品仍为 ACTIVE。
- Accept 后商品变 SOLD，其他 Pending 自动 Rejected。

### 13.4 成交联系人

- Alice 的 Scientific Calculator 显示 Buyer: BobDemo 和 `92345678`。
- Bob 的 My Activity 显示确认成交和 Alice WhatsApp。
- 无关账号无法从公开首页进入 SOLD 商品。

### 13.5 账户设置

- 修改昵称后回首页标题更新。
- 修改 WhatsApp 后重新打开账户页仍保留。
- 错误当前密码不能修改密码。
- 正确修改后旧密码登录失败，新密码登录成功。

### 13.6 数据持久化

- Force Stop 后重新打开仍保持数据。
- 模拟器重启后商品和账号仍存在。
- Clear storage 后回到空数据库，需要重新 Prepare。

---

## 14. 自动测试文件

| 测试 | 覆盖内容 |
|---|---|
| `DatabaseSchemaV2Test` | 表、约束、索引和迁移。 |
| `MarketRepositoryAuthTest` | 注册、登录、重复昵称、资料和密码修改。 |
| `MarketRepositoryItemCrudTest` | 商品新增、编辑、owner 权限、软删除。 |
| `MarketRepositoryItemSearchTest` | ACTIVE 搜索、类别和排序。 |
| `MarketRepositoryOfferRulesTest` | Offer 类型、金额、重复报价、拒绝。 |
| `MarketRepositoryAtomicTradeTest` | 接受 Offer 的原子事务和回滚。 |
| `MarketRepositoryListingSummaryTest` | My Listings 状态和 Offer 数。 |
| `MarketRepositoryTradePrivacyTest` | My Activity 和成交联系人隐私。 |
| `DemoDataSeederTest` | 4 账号、14 商品、图片和重复执行。 |
| `ImageUriLoaderTest` | 图片 URI 正常/失效回退。 |
| `MainActivitySessionRoutingTest` | 登录 session 启动路由。 |
| `ItemDetailOfferFlowTest` | 详情页报价流程。 |
| `ParticipationAdapterTest` | 参与记录 Adapter 和联系人显示。 |

---

## 15. 构建和测试命令

在 PowerShell 中：

```powershell
cd E:\7506_project\Android_Studio_files
```

构建 Debug APK：

```powershell
.\gradlew.bat :app:assembleDebug
```

运行本地单元测试：

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

运行 Lint：

```powershell
.\gradlew.bat :app:lintDebug
```

模拟器启动后运行仪器测试：

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Debug APK 输出位置：

```text
Android_Studio_files/app/build/outputs/apk/debug/app-debug.apk
```

Lint 报告：

```text
Android_Studio_files/app/build/reports/lint-results-debug.html
```

设备测试报告：

```text
Android_Studio_files/app/build/reports/androidTests/connected/debug/
```

---

## 16. 常见修改应该改哪里

| 目标 | 主要文件 |
|---|---|
| 增加 Demo 用户/商品/Offer | `DemoDataSeeder.java`、`DemoDataSeederTest.java`、Demo 图片目录。 |
| 修改首页布局 | `activity_home.xml`、`HomeActivity.java`、`ItemAdapter.java`、`row_item.xml`。 |
| 修改商品字段或发布流程 | `PostEditItemActivity.java`、`ItemDraft.java`、items schema、相关测试。 |
| 修改详情页买卖双方操作 | `ItemDetailActivity.java`、`activity_item_detail.xml`。 |
| 修改 Offer 规则 | `MarketRepository.java/Impl.java`、`OfferReviewActivity.java`、Offer 测试。 |
| 修改管理中心 | `ManagementActivity.java`、三个 management Adapter 和 row XML。 |
| 修改账户资料 | `ProfileActivity.java`、`activity_profile.xml`、Auth 测试。 |
| 修改主题颜色/间距 | `colors.xml`、`dimens.xml`、`styles.xml`、`themes.xml`。 |
| 修改状态或 Intent key | 先更新主计划契约，再改 `AppContract.java` 和所有调用方。 |
| 修改数据库表 | 先设计 migration，再改 `DatabaseContract.java`、`DatabaseHelper.java` 和 schema 测试。 |

---

## 17. 多人和 Agent 协作规则

1. 每次工作前阅读 `7506_MASTER_DEVELOPMENT_PLAN.md`、本文件和最新增强记录。
2. 从最新 `main` 创建一个能覆盖本轮完整功能的分支。
3. UI Agent 不直接写 SQL；调用 MarketRepository。
4. 数据 Agent 修改接口时，同时更新实现、调用方和测试。
5. 不随意修改冻结 View ID、状态常量、Intent key 和模型字段。
6. 不提交本机 `local.properties`、数据库文件、Gradle cache 或 build 输出。
7. 合并前至少执行 assembleDebug、相关测试和 `git diff --check`。
8. PR 描述写清任务 ID、主要文件、人工验收步骤和契约是否变化。

---

## 18. 当前已经达到的效果

- 账号注册、登录、退出和本地 session。
- 账户昵称、WhatsApp、密码修改。
- 商品发布、编辑、相册图片、软删除和关键词搜索。
- Negotiated Offer 和 Buy Now 请求。
- 卖家查看、接受、拒绝 Offer。
- 原子成交、Sold 状态和成交联系人。
- My Listings、My Activity、Pending Offer 红点和数量 Badge。
- 4 个可复用测试账号、14 个商品、14 张图片和多账号交易场景。
- SQLite 本地持久化和 Repository 层自动测试。

---

## 19. 当前明确不做的范围

这是课程小组项目，不以商用发布为目标。当前不包含：

- 云端数据库和跨设备同步。
- HKU 官方身份认证。
- 在线支付。
- App 内聊天。
- 地图和实时定位。
- 推送通知。
- 推荐算法。
- 管理员后台。

这些限制不会影响课程 Demo 的核心 Alice/Bob/Carol/David 买卖流程。

---

## 20. 文档之间的关系

| 文档 | 作用 |
|---|---|
| `README.md` | 最短的安装、运行和项目介绍。 |
| `7506_MASTER_DEVELOPMENT_PLAN.md` | 唯一主计划、任务顺序、冻结契约和验收要求。 |
| `7506_ENHANCEMENT_DEVELOPMENT_LOG.md` | P0 后每轮完善的任务状态和验证记录。 |
| `7506_PROJECT_STRUCTURE_AND_TESTING_GUIDE.md` | 文件结构、实现原理、Demo 账号和组员测试手册。 |
| `TASK_COMPLETION_REPORT.md` | 历史任务提交说明，仅作参考，不替代主计划。 |

新成员第一次阅读建议顺序：README → 本文件 → Master Plan → Enhancement Log。
