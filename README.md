# HKU Campus Market

HKU Campus Market is a local Android marketplace prototype for the COMP7506D group project. Students can register, publish second-hand items, search listings, make offers, confirm deals, and review their selling or buying activity.

## Core Features

- Local account registration, login, logout, and persistent session
- Item publishing, editing, soft deletion, image selection, and search
- Negotiated offers and Buy Now requests
- Seller offer review and atomic deal confirmation
- My Listings and My Activity views
- Counterparty WhatsApp revealed only after a confirmed deal
- Debug-only reusable Alice/Bob demo scenario

## Technology

- Java 11 language level and XML layouts
- Android SDK 36, minimum SDK 24
- AndroidX AppCompat, Material Components, ConstraintLayout, and RecyclerView
- `SQLiteOpenHelper` through `MarketRepository`
- SharedPreferences for the current session only
- JUnit and Espresso instrumentation tests

The checked build uses Gradle 9.4.1, Android Gradle Plugin 9.2.1, and JDK 17 as the Gradle launcher.

## Open And Run

1. Open `Android_Studio_files` in Android Studio.
2. Wait for Gradle sync to finish.
3. Select the `app` run configuration and an API 24+ emulator or device.
4. Run the app.

Command-line build on Windows:

```powershell
cd Android_Studio_files
.\gradlew.bat assembleDebug
```

The Debug APK is generated at:

```text
Android_Studio_files/app/build/outputs/apk/debug/app-debug.apk
```

## Demo Accounts

The Debug build shows **Prepare demo data** on the Login page. The Release build hides this button and never inserts demo data automatically.

Click the button once to create:

| Account | Password | Role |
|---|---|---|
| `AliceDemo` | `demo123` | Seller |
| `BobDemo` | `demo123` | Buyer |

Prepared records include:

- An active Java textbook with Bob's HK$100 Pending offer
- A sold scientific calculator with a confirmed Alice/Bob deal
- Bob's My Activity showing both Pending and Deal confirmed states

The login form is filled with Alice after preparation. To rebuild the original scenario, clear the App Data and press **Prepare demo data** again.

## Local Data

All business data is stored only on the current device in the app-private SQLite database:

```text
databases/hku_campus_market.db
```

It contains `users`, `items`, `offers`, and `trade_transactions`. Passwords are stored as salted PBKDF2 hashes. Selected images remain in their document provider; SQLite stores only their persistent URI.

The current `userId` is stored separately in:

```text
shared_prefs/market_session.xml
```

Closing the app, force-stopping it, restarting the device, or installing an update with the same application ID preserves this data. Clearing App Data, running `adb shell pm clear`, uninstalling the app, or some instrumentation-test tasks removes it. There is no cloud synchronization, so each device has an independent database.

## Project Structure

```text
Android_Studio_files/app/src/main/
  java/com/example/a7506_project/
    contract/       Shared constants and database contract
    data/           SQLite helper and repository implementation
    model/          UI and domain data models
    ui/             Authentication, home, item, and management screens
    util/           Session, formatting, image, password, and demo helpers
  res/              XML layouts, drawables, menus, themes, and strings
project_docs/
  7506_MASTER_DEVELOPMENT_PLAN.md
```

Activities never execute SQL directly. They call `MarketRepository`, and mutable pages reload data in `onResume()`.

## Verification

```powershell
cd Android_Studio_files
.\gradlew.bat assembleDebug assembleRelease testDebugUnitTest lintDebug
.\gradlew.bat connectedDebugAndroidTest
```

`connectedDebugAndroidTest` requires a running emulator or device. It may uninstall the test app after completion, so prepare demo data after automated testing rather than before it.

## Scope And Limitations

- Local single-device teaching prototype, not a production marketplace
- No Firebase, remote API, cloud backup, payment, chat, map, push notification, recommendation, or admin system
- No real HKU identity verification
- WhatsApp is displayed as contact information; the app does not send messages
- Image availability depends on the persisted document URI

The authoritative requirements, task status, frozen contracts, testing matrix, and team workflow are in [`project_docs/7506_MASTER_DEVELOPMENT_PLAN.md`](project_docs/7506_MASTER_DEVELOPMENT_PLAN.md).

## Dependencies And Assets

The project uses AndroidX, Google Material Components, JUnit, and Espresso through their official Maven packages. No third-party marketplace source project was copied into the implementation. Project-specific UI layouts and visual assets are maintained in this repository.
