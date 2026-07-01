# OnePlus Notification Swipe Gesture Fix

An LSPosed module that fixes the broken swipe-to-expand notification gesture on ColorOS/OxygenOS.

On affected devices, swiping down on a notification to expand it works after a fresh reboot but stops working after the first lock/unlock cycle. This module fixes that.

## Requirements

- Rooted device with [LSPosed](https://github.com/LSPosed/LSPosed) installed
- Tested on CPH2723 (ColorOS 16.0.7.200), may work on other OnePlus/Oppo devices

## Installation

1. Download the APK from [Releases](../../releases)
2. Install the APK
3. Open LSPosed Manager, enable the module and scope it to `com.android.systemui`
4. Reboot

## Building from source

```
git clone https://github.com/ffluryy/oneplus-notif-gesture-fix.git
cd oneplus-notif-gesture-fix
./gradlew assembleDebug
```

## Credits

Forked from [AutoExpandNotifications](https://github.com/kvmy666/autoexpand) by [kvmy666](https://github.com/kvmy666). This fork strips it down to a single targeted fix for the swipe-to-expand regression rather than auto-expanding all notifications.

## License

[GPLv3](LICENSE)
