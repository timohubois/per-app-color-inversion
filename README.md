# Per-App Color Inversion

Enables Android color inversion while selected apps are in the foreground.

## Requirements

- Android 12 or later (API 31)
- ADB access for granting `WRITE_SECURE_SETTINGS`
- Accessibility service enabled by the user

Built against Android 15 (API 35). Tested on Android 12 on the Mudita Kompakt.

## Behavior

- Reacts to foreground-window changes; no polling
- Stores the selected package names locally
- Does not retrieve window content
- Has no network or storage permission

## Build

```sh
nix develop --command ./gradlew --no-daemon assembleDebug
```

## Install

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm grant io.github.timohubois.perappcolorinversion android.permission.WRITE_SECURE_SETTINGS
```

Enable **Per-app color inversion** under Android's accessibility settings, then select apps in **Per-App Color Inversion**.

## Obtainium

Add this repository URL to Obtainium:

```text
https://github.com/timohubois/per-app-color-inversion
```

## Reset inversion

```sh
adb shell settings put secure accessibility_display_inversion_enabled 0
```
