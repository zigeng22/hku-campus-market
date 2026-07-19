# COMP7506 任务完成报告 — 2026/07/19

> **以下全部由 finekiss 独立完成**
> GitHub: finekiss | Email: finekiss.dream@gmail.com
> Commit: `24c9050` (已推送到 main)

---

## finekiss 已完成任务

### Phase 1 — Auth 系统 ✅
| Task ID | 内容 | 文件 |
|---------|------|------|
| AUTH-01 | users 表 + PasswordHasher (PBKDF2WithHmacSHA1) | DatabaseHelper.java, PasswordHasher.java |
| AUTH-02 | SessionManager + MainActivity 路由 | SessionManager.java, MainActivity.java |
| AUTH-03 | Registration with validation | SignUpActivity.java, Validators.java |
| AUTH-04 | Login/logout flow | LoginActivity.java, HomeActivity.java |
| UI-01 | Theme already in place | themes.xml (Phase 0) |
| UI-02 | Login/SignUp XML (已有，Phase 0) | activity_login.xml, activity_sign_up.xml |

### Phase 2 — Items & Home ✅
| Task ID | 内容 | 文件 |
|---------|------|------|
| ITEM-01 | items 表 + 索引 | DatabaseHelper.java |
| ITEM-02 | CRUD with ownership checks | MarketRepositoryImpl.java |
| ITEM-03 | Search/filter → ItemCard DTOs | MarketRepositoryImpl.java |
| ITEM-04 | HomeActivity + ItemAdapter | HomeActivity.java, ItemAdapter.java |
| ITEM-05 | PostEditItemActivity + MoneyFormatter | PostEditItemActivity.java, MoneyFormatter.java |
| ITEM-06 | Photo picker with persistable URI | PostEditItemActivity.java |
| ITEM-07 | ItemDetailActivity with buyer/seller switching | ItemDetailActivity.java |

### Phase 3 — Offers & Management ✅
| Task ID | 内容 | 文件 |
|---------|------|------|
| TRADE-01 | offers + trade_transactions 表 | DatabaseHelper.java |
| TRADE-02 | placeOffer / Buy Now rules | MarketRepositoryImpl.java |
| TRADE-03 | Offer dialog → ItemDetail | ItemDetailActivity.java |
| TRADE-04 | OfferReviewActivity | OfferReviewActivity.java, OfferAdapter.java |
| TRADE-05 | Atomic acceptOffer transaction (6 steps) | MarketRepositoryImpl.java |
| TRADE-06 | My Listings data + interaction | ManagementActivity.java, ListingAdapter.java |
| TRADE-07 | My Activity + WhatsApp visibility | ManagementActivity.java, ParticipationAdapter.java |
| TRADE-08 | ManagementActivity tab switching | ManagementActivity.java |

### Phase 4 — Integration ✅
| Task ID | 内容 | 文件 |
|---------|------|------|
| INT-01 | Alice/Bob E2E flow | Verified on device |
| INT-02 | onResume data refresh | All Activities |
| INT-03 | Error handling with Toast/Snackbar | All Activities |

---

## ⚠️ 待他人完成
| 模块 | 说明 |
|------|------|
| 前端样式 | XML 颜色、间距、卡片圆角、strings |
| 单元测试 | PasswordHasherTest, ValidatorTest, RepositoryTest |
| Phase 5 文档 | README, Final Report, Video, Attribution |

---

## finekiss 新建文件
```
data/DatabaseHelper.java          — SQLite 建表、4表、外键、索引
data/MarketRepositoryImpl.java    — 16个接口全部实现 + 原子化acceptOffer事务
data/RepositoryProvider.java      — 线程安全单例
util/PasswordHasher.java          — PBKDF2WithHmacSHA1 密码哈希
util/Validators.java              — 5类输入校验
util/MoneyFormatter.java          — 分↔港元互转
ui/home/ItemAdapter.java          — RecyclerView 适配器
ui/management/ListingAdapter.java — My Listings 适配器
ui/management/OfferAdapter.java   — 出价列表适配器
ui/management/ParticipationAdapter.java — My Activity 适配器
```

## finekiss 重写文件
```
ui/auth/LoginActivity.java        — 真实登录逻辑
ui/auth/SignUpActivity.java       — 真实注册+客户端校验
ui/home/HomeActivity.java         — 搜索+列表+登出
ui/item/PostEditItemActivity.java — 发帖/编辑+图片选择
ui/item/ItemDetailActivity.java   — 详情+买家/卖家角色切换
ui/management/OfferReviewActivity.java — 卖家查看/接受出价
ui/management/ManagementActivity.java  — Tab: My Listings + My Activity
model/ItemCard.java               — 新增 status + offerCount 字段
```

## finekiss 新增资源
```
res/menu/toolbar_home.xml         — Logout 菜单
res/layout/activity_home.xml      — 添加 menu 引用
res/layout/activity_sign_up.xml   — 添加 ProgressBar
```

---

> **以上全部由 finekiss 独立完成**
