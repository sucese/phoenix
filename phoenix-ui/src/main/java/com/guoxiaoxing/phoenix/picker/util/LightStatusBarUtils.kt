package com.guoxiaoxing.phoenix.picker.util

import android.app.Activity
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.WindowManager

/**
 * author：luck
 * project：Phoenix
 * package：com.luck.picture.lib.tool
 * email：893855882@qq.com
 * data：2017/5/25
 */
object LightStatusBarUtils {

    fun setLightStatusBar(activity: Activity, dark: Boolean) {
        var dark = dark
        val availableRomType = RomUtils.lightStatausBarAvailableRomType
        when (availableRomType) {
            RomUtils.AvailableRomType.MIUI -> {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    dark = false
                }
                setMIUILightStatusBar(activity, dark)
            }

            RomUtils.AvailableRomType.FLYME -> {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    dark = false
                }
                setFlymeLightStatusBar(activity, dark)
            }

            RomUtils.AvailableRomType.ANDROID_NATIVE -> setAndroidNativeLightStatusBar(activity, dark)

            RomUtils.AvailableRomType.NA ->
                // N/A do nothing
                setAndroidNativeLightStatusBar(activity, dark)
        }
    }

    private fun setMIUILightStatusBar(activity: Activity, darkmode: Boolean): Boolean {
        val clazz = activity.window.javaClass
        try {
            var darkModeFlag = 0
            val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
            val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
            darkModeFlag = field.getInt(layoutParams)
            val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            extraFlagField.invoke(activity.window, if (darkmode) darkModeFlag else 0, darkModeFlag)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    private fun setFlymeLightStatusBar(activity: Activity?, dark: Boolean): Boolean {
        var result = false
        if (activity != null) {
            try {
                val lp = activity.window.attributes
                val darkFlag = WindowManager.LayoutParams::class.java
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java
                        .getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(lp)
                if (dark) {
                    value = value or bit
                } else {
                    value = value and bit.inv()
                }
                meizuFlags.setInt(lp, value)
                activity.window.attributes = lp
                result = true
            } catch (e: Exception) {
            }

        }
        return result
    }

    private fun setAndroidNativeLightStatusBar(activity: Activity, dark: Boolean) {
        val decor = activity.window.decorView
        if (dark) {
            val model = android.os.Build.MODEL // 手机型号
            if (!TextUtils.isEmpty(model) && model.startsWith("Le")) {
            } else {
                decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        } else {
            // We want to change tint color to white again.
            // You can also record the flags in advance so that you can turn UI phoenix_arrow_left completely if
            // you have set other flags before, such as translucent or full screen.
            decor.systemUiVisibility = 0
        }
    }

}
