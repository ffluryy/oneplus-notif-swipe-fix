# OnePlus Notification Swipe Gesture Fix

An LSPosed module that fixes the broken swipe-to-expand notification gesture on ColorOS/OxygenOS.

On affected devices, swiping down on a notification to expand it works after a fresh reboot but stops working after the first lock/unlock cycle. This module fixes that.

It hooks onInterceptTouchEvent (the method that runs every time a touch/swipe on the notification shade begins) and checks if mEnabled is already false at that moment. If it is, it forces it back to true before the method runs normally.

## Requirements

- Rooted device with [LSPosed](https://github.com/LSPosed/LSPosed) installed
- OOS 16.0.7 device (June update)

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

## Disclaimer

This module modifies system UI internals. Use at your own risk. Tested on Oneplus 13T PKX1100 converted to OOS (CPH2723_16.0.7.200) only — behaviour on other devices or firmware versions is not guaranteed.

---

*Developed with assistance from [Claude](https://claude.ai) (Anthropic). Root cause identified through iterative runtime instrumentation.*

## License

[GPLv3](LICENSE)
