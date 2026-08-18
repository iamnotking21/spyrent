# Spyrent child app

The Android app that runs on the child's device. Talks to the same `/api/v1`
endpoints as the web portal — no PHP, one bearer token.

## Build

```bash
cd mobile
./gradlew assembleDebug
```

Needs a JDK 17+ and an Android SDK. Put the SDK path in `mobile/local.properties`:

```
sdk.dir=C:/Users/you/AppData/Local/Android/Sdk
```

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

## Run it against a local server

The emulator reaches your machine at `10.0.2.2`, and the debug network config
allows cleartext for that host only:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell appops set com.spyrent.child android:get_usage_stats allow
```

Open the app, paste a pairing token from any child page, and set the server
field to `http://10.0.2.2:3000`.

## How it works

| File | Job |
|---|---|
| `Api.kt` | HTTP client for pair, policy, apps, events, heartbeat |
| `Store.kt` | Token, server URL, and the cached locked-package list |
| `Usage.kt` | Usage stats, foreground app, installed app inventory |
| `SyncWorker.kt` | Every 15 min: report usage, refresh policy, heartbeat |
| `BlockerService.kt` | Foreground service; shows the lock screen on a locked app |
| `LockActivity.kt` | The "done for today" screen |
| `SiteBlockerService.kt` | Accessibility service; blocks browsing to ruled-out domains |
| `BootReceiver.kt` | Restarts the service after a reboot |

Three permissions matter. `PACKAGE_USAGE_STATS` is granted in Settings, not by a
dialog — the app links straight to that screen. Notifications carry the ongoing
foreground-service notice.

## On being visible

The service notification is permanent and the app has a launcher icon. That is
deliberate: this is a house rule the child can see, not covert monitoring. It
reads screen time only — never message content, keystrokes, location, or audio.

## What site blocking can and cannot do

Android gives an app no view of another app's traffic without running a VPN. The
workable route is the address bar, which browsers publish to accessibility
services — so blocking covers the browsers listed in
`res/xml/accessibility_service.xml`, and only what is actually shown in the bar.
Incognito is covered. A browser we do not list, or an app with its own webview,
is not. This is a house rule that holds for ordinary use, not a content filter
that survives a determined teenager.

On a sideloaded build Android treats accessibility as a restricted setting, so
enabling it from a terminal during testing needs one extra step:

```bash
adb shell appops set com.spyrent.child ACCESS_RESTRICTED_SETTINGS allow
```

On a normally installed app the in-app button opens the right Settings screen.
