# OnePlus Notification Swipe Gesture Fix

An LSPosed/Xposed module that fixes broken swipe-to-expand on individual
notifications in the shade on ColorOS/OxygenOS devices.

## The bug

On affected ColorOS/OxygenOS builds, swiping down on a single notification
in the shade to expand it works correctly right after a reboot, then stops
working permanently the first time the device is locked — even after
unlocking again. No setting reverts this; only a reboot temporarily
restores it.

## Root cause

`com.android.systemui.ExpandHelper` (the AOSP class responsible for
notification expand/collapse touch gestures) has a boolean field `mEnabled`
that gates its entire touch-handling logic. SystemUI calls
`ExpandHelper.setEnabled(false)` as part of locking the device. The
matching `setEnabled(true)` call that should happen on unlock either never
fires, or doesn't reach the live `ExpandHelper` instance actually receiving
touches on this OEM build. The result: `mEnabled` stays stuck `false` after
the first lock/unlock cycle, silently disabling the swipe-to-expand gesture
for the rest of the boot session.

This was confirmed through direct field-state logging across multiple
lock/unlock cycles, rather than guessed from source inspection — see
commit history for the debugging trail if you're curious how this was
narrowed down.

## The fix

This module hooks `ExpandHelper.onInterceptTouchEvent` and forces
`mEnabled` back to `true` whenever it's found to be `false`, so the
gesture can never get permanently stuck disabled.

A secondary hook on `NotificationStackScrollLayout.onKeyguard()` (forcing
it to always report `false`) is also included as a defensive measure from
an earlier hypothesis. It isn't the root cause but is harmless to leave in
place.

## Installation

1. Requires a rooted device with [LSPosed](https://github.com/LSPosed/LSPosed) installed
2. Download the latest APK from [Releases](../../releases) (or build from source, see below)
3. Install the APK
4. Enable the module in LSPosed Manager, scope it to `com.android.systemui`
5. Reboot

## Building from source

```
git clone https://github.com/ffluryy/oneplus-notif-gesture-fix.git
cd oneplus-notif-gesture-fix
./gradlew assembleDebug
```
Requires the Android SDK (set `sdk.dir` in `local.properties` or set
`ANDROID_HOME`).

## Compatibility

Confirmed on a OnePlus/Oppo Reno-series device (CPH2723) running ColorOS
16. The underlying `ExpandHelper`/`mEnabled` mechanism is part of AOSP, so
this may also help on other ColorOS/OxygenOS builds exhibiting the same
symptom, though this hasn't been broadly tested. If you confirm it works
(or doesn't) on another device/build, please open an issue.

## Attribution

This project is a fork of
[AutoExpandNotifications](https://github.com/kvmy666/autoexpand) by
[kvmy666](https://github.com/kvmy666), which auto-expands notifications by
default rather than fixing the underlying gesture bug. This fork strips
that broader feature set down to a single targeted fix for the
swipe-to-expand regression described above.

## License

GPLv3, inherited from the upstream project. See [LICENSE](LICENSE).
