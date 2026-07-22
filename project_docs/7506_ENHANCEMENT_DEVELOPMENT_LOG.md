# HKU Campus Market: Enhancement Development Log

> This document records post-P0 improvements starting from 2026-07-22. The master plan remains the source of truth for frozen contracts and course scope. New enhancement work should be assigned an `ENH-*` ID here and its status updated in this file.

## 1. Baseline

- Baseline branch: `main`
- Baseline commit: `30f8609` (`Merge pull request #11 from feat/demo-data-setup`)
- Working branch: `feat/enhancement-round-1`
- Technology constraints: Java 11, XML, AppCompat/Material, SQLiteOpenHelper, SharedPreferences
- Scope rule: prioritize demonstrable course-project flows; do not add remote services or production-only infrastructure.

## 2. Enhancement Round 1 Scope

| ID | Status | Requirement | Acceptance criteria |
|---|---|---|---|
| ENH-DOC-01 | DONE | Establish this enhancement log | Scope, decisions, status, verification and follow-up work are recorded here. |
| ENH-DEMO-01 | DONE | Expand demo accounts and marketplace data | 4 accounts; each owns at least 2 listings and has offers on other users' listings. |
| ENH-ASSET-01 | DONE | Add realistic demo product images | 8 product images are versioned in the Android project and mapped to demo listings. |
| ENH-MEDIA-01 | DONE | Install demo images into emulator gallery | Preparing demo data copies images to `Pictures/HKU Campus Market` on Android 10+ and stores reusable content URIs. |
| ENH-TRADE-01 | DONE | Reject a pending offer | Seller can reject a pending offer; the item remains active and the offer becomes `REJECTED`. |
| ENH-TRADE-02 | DONE | Show buyer contact for sold items | Sold detail shows confirmed buyer nickname and WhatsApp to the transaction participants. |
| ENH-ITEM-01 | DONE | Delete a sold listing | Seller can soft-delete their sold listing while transaction history remains stored. |
| ENH-NOTIFY-01 | DONE | Show pending-offer reminders | Home management control shows a red indicator, and management listings tab shows pending count. |
| ENH-PROFILE-01 | DONE | Update account data | Signed-in user can update nickname/WhatsApp and change password after entering the current password. |
| ENH-PROFILE-02 | DONE | Add profile/settings UI | Home exposes a profile control and a Java/XML settings screen for the account operations. |
| ENH-QA-01 | DONE | Regression and demo acceptance | 49 instrumentation tests, Debug builds, and targeted API 36 manual flows pass. |

### Enhancement Round 2 Scope

| ID | Status | Requirement | Acceptance criteria |
|---|---|---|---|
| ENH-DEMO-02 | DONE | Enrich the public demo catalog | Demo contains 14 listings across 4 accounts; the six new listings do not require preset offers. |
| ENH-ASSET-02 | DONE | Add six matching product images | Six new generated images are versioned, installed through MediaStore, and mapped to the new listings. |
| ENH-DOC-02 | DONE | Add project structure and teammate testing guide | Chinese guide explains files, layers, flows, database, demo behavior, accounts, test cases, and modification entry points. |
| ENH-QA-02 | DONE | Verify expanded catalog | Debug build and all 49 API 36 instrumentation tests pass with the 14-listing assertions. |

## 3. Implementation Decisions

1. No database schema migration is planned. Existing users, items, offers, and trade transaction columns cover this scope.
2. The red indicator means **pending offers requiring seller action**, not a persisted unread-message state. It disappears when no pending offer remains.
3. Sold-listing deletion is a soft delete. It hides the listing from seller management without deleting the confirmed trade row.
4. Demo images are project assets. The debug demo-data action copies them to MediaStore on Android 10+ so they also appear in the emulator gallery.
5. Demo preparation remains debug-only and idempotent. Existing normal user data must not be wiped.
6. Profile settings are local SQLite account settings. SharedPreferences continues to store only the signed-in user ID.

## 4. Contract Changes Authorized By This Round

The user's 2026-07-22 request explicitly authorizes the following additive contract changes:

- Repository methods for rejecting offers and updating account credentials/profile.
- A profile Activity/layout and home navigation control.
- Optional sold-buyer contact views and pending-offer indicator views.
- `softDeleteItem` may accept both `ACTIVE` and `SOLD` seller-owned items.

Existing public model fields, Intent extra names, state constants, and database columns remain unchanged.

## 5. Work Sequence

1. ENH-DOC-01, ENH-ASSET-01, ENH-DEMO-01, ENH-MEDIA-01
2. ENH-TRADE-01, ENH-TRADE-02, ENH-ITEM-01
3. ENH-NOTIFY-01
4. ENH-PROFILE-01, ENH-PROFILE-02
5. ENH-QA-01 and documentation closure

## 6. Change Log

### 2026-07-22 - Round 1 opened

- Confirmed PR #11 demo-data setup is present on `main`.
- Created `feat/enhancement-round-1`.
- Confirmed requested functions can use schema version 2 without a migration.
- Began generation of eight square product-listing images for bundled demo data.

### 2026-07-22 - Round 1 implemented

- Expanded the reusable seed to AliceDemo, BobDemo, CarolDemo, and DavidDemo; all use `demo123`, own at least two listings, and make offers on other users' items.
- Added `DemoImageInstaller`, which publishes bundled images into `Pictures/HKU Campus Market` on Android 10+, reuses existing MediaStore entries, and caches returned URIs to prevent repeat inserts during the same installation.
- Added Offer rejection, sold-item buyer contact, sold-listing soft deletion, home red-dot reminders, and a numeric My Listings badge.
- Added Account settings for nickname, WhatsApp, and password changes.
- Corrected Offer date display by converting SQLite seconds to Java milliseconds.
- Updated README and master-plan status for merged PR #11 and this enhancement round.

### 2026-07-22 - Round 2 demo catalog and team guide

- Expanded the catalog from 8 to 14 listings: Alice and Bob own 4 each; Carol and David own 3 each.
- Added Statistics Revision Notes, USB-C Multiport Hub, Over-Ear Headphones, Compact Desk Fan, Three-Tier Storage Trolley, and Bicycle Helmet.
- Kept the original Offer and confirmed-deal graph unchanged; the six new listings are catalog/search examples without required preset offers.
- Added `7506_PROJECT_STRUCTURE_AND_TESTING_GUIDE.md`, including the explicit rule that Git distributes Seeder/assets but not each device's generated SQLite database.
- Updated README and master-plan Demo descriptions to the 14-item baseline.

## 7. Demo Image Asset Manifest

All assets were generated with the built-in OpenAI image generation tool as square, realistic, second-hand marketplace product photos with no people, logos, watermark, price labels, or legible brand text.

| File | Subject |
|---|---|
| `java_textbook.png` | Red and white programming textbook |
| `scientific_calculator.png` | Dark scientific calculator and cover |
| `wireless_keyboard.png` | Compact white wireless keyboard |
| `desk_lamp.png` | Blue adjustable study lamp |
| `office_chair.png` | Black ergonomic mesh chair |
| `rice_cooker.png` | Compact white rice cooker |
| `monitor_stand.png` | Bamboo desktop monitor stand |
| `tennis_racket.png` | Red and black tennis racket with cover |
| `statistics_notes.png` | Statistics revision-note binder |
| `usb_c_hub.png` | Silver USB-C multiport hub |
| `headphones.png` | Black over-ear headphones and case |
| `desk_fan.png` | Mint-green compact desk fan |
| `storage_trolley.png` | White three-tier storage trolley |
| `bicycle_helmet.png` | Navy bicycle helmet |

Workspace path: `Android_Studio_files/app/src/main/assets/demo_products/`.

## 8. Verification Log

- `:app:compileDebugJavaWithJavac`: PASS.
- `:app:assembleDebug :app:assembleDebugAndroidTest`: PASS.
- `:app:connectedDebugAndroidTest`: PASS, 49/49 tests on `Medium_Phone(AVD) - API 36`.
- `:app:testDebugUnitTest`: PASS.
- `:app:lintDebug`: PASS.
- `git diff --check`: PASS before documentation closure.
- MediaStore query: PASS for the Round 1 assets; the same installer path now covers all fourteen named assets.
- Round 2 `:app:assembleDebug :app:connectedDebugAndroidTest`: PASS, 49/49 tests with 14-listing assertions.
- API 36 manual visual checks: PASS for login/demo preparation, image-rich home, three bottom actions, red dot, My Listings numeric badge, account settings, sold buyer contact/delete, and Offer Accept/Reject row.
- Database schema version remains 2; no migration was added.
