package com.github.ffluryy.oneplusnotifgesturefix

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Fixes broken swipe-to-expand on individual notifications in the shade,
 * which stops working after the device is locked once and never recovers
 * until reboot.
 *
 * Root cause (confirmed via instrumented testing across multiple lock/unlock
 * cycles): com.android.systemui.ExpandHelper has a boolean field mEnabled
 * that gates its entire touch-handling logic. SystemUI calls
 * ExpandHelper.setEnabled(false) as part of locking the device (confirmed:
 * fires reliably on every SCREEN_OFF). The matching setEnabled(true) call
 * that should happen on unlock either never fires, or doesn't reach the
 * live ExpandHelper instance actually receiving touches -- so mEnabled
 * stays stuck false after the first lock/unlock cycle, silently disabling
 * the swipe-to-expand gesture for the rest of the boot session.
 *
 * Fix: force mEnabled back to true on every touch interception, so the
 * gesture can never get stuck disabled.
 *
 * (NotificationStackScrollLayout.onKeyguard() is also forced false as a
 * secondary safety net -- this was an earlier theory that turned out not
 * to be the real cause, but it's harmless to leave in place.)
 */
class MainHook : IXposedHookLoadPackage {

    private fun logAttach(name: String, e: Throwable?) {
        if (e == null) XposedBridge.log("AE-FIX hook attached OK: $name")
        else XposedBridge.log("AE-FIX hook FAILED to attach: $name -> $e")
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.android.systemui") return

        val stackClass = "com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout"
        val expandHelperClass = "com.android.systemui.ExpandHelper"

        // marker so the app can show "module active"
        try {
            XposedHelpers.findAndHookMethod(
                "android.app.Application", lpparam.classLoader, "onCreate",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val app = param.thisObject as android.app.Application
                            android.provider.Settings.Global.putString(
                                app.contentResolver, "autoexpand_active",
                                System.currentTimeMillis().toString()
                            )
                        } catch (_: Throwable) {}
                    }
                }
            )
        } catch (_: Throwable) {}

        // -------- THE REAL FIX --------
        // Force ExpandHelper.mEnabled back to true on every touch, so it can
        // never stay stuck false after a lock/unlock cycle.
        try {
            XposedHelpers.findAndHookMethod(
                expandHelperClass, lpparam.classLoader,
                "onInterceptTouchEvent", android.view.MotionEvent::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val enabled = XposedHelpers.getBooleanField(param.thisObject, "mEnabled")
                            if (!enabled) {
                                XposedBridge.log("AE-FIX mEnabled was false -> forcing true")
                                XposedHelpers.setBooleanField(param.thisObject, "mEnabled", true)
                            }
                        } catch (e: Throwable) {
                            XposedBridge.log("AE-FIX mEnabled field access failed: $e")
                        }
                    }
                }
            )
            logAttach("ExpandHelper.onInterceptTouchEvent (mEnabled fix)", null)
        } catch (e: Throwable) {
            logAttach("ExpandHelper.onInterceptTouchEvent (mEnabled fix)", e)
        }

        // -------- secondary safety net (not the root cause, but harmless) --------
        try {
            XposedHelpers.findAndHookMethod(
                stackClass, lpparam.classLoader,
                "onKeyguard",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = false
                    }
                }
            )
            logAttach("NotificationStackScrollLayout.onKeyguard() (secondary)", null)
        } catch (e: Throwable) {
            logAttach("NotificationStackScrollLayout.onKeyguard() (secondary)", e)
        }
    }
}
