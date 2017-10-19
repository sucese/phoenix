package com.guoxiaoxing.phoenix.picker.util

import android.os.Build
import android.text.TextUtils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class RomUtils {

    internal object AvailableRomType {
        val MIUI = 1
        val FLYME = 2
        val ANDROID_NATIVE = 3
        val NA = 4
    }

    companion object {

        val isLightStatusBarAvailable: Boolean
            get() {
                if (isMIUIV6OrAbove || isFlymeV4OrAbove || isAndroidMOrAbove) {
                    return true
                }
                return false
            }

        val lightStatausBarAvailableRomType: Int
            get() {
                if (isMIUIV6OrAbove) {
                    return AvailableRomType.MIUI
                }

                if (isFlymeV4OrAbove) {
                    return AvailableRomType.FLYME
                }

                if (isAndroidMOrAbove) {
                    return AvailableRomType.ANDROID_NATIVE
                }

                return AvailableRomType.NA
            }

        //Flyme V4的displayId格式为 [Flyme OS 4.x.x.xA]
        //Flyme V5的displayId格式为 [Flyme 5.x.x.x beta]
        private //版本号4以上，形如4.x.
        val isFlymeV4OrAbove: Boolean
            get() {
                val displayId = Build.DISPLAY
                if (!TextUtils.isEmpty(displayId) && displayId.contains("Flyme")) {
                    val displayIdArray = displayId.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (temp in displayIdArray) {
                        if (temp.matches("^[4-9]\\.(\\d+\\.)+\\S*".toRegex())) {
                            return true
                        }
                    }
                }
                return false
            }

        //MIUI V6对应的versionCode是4
        //MIUI V7对应的versionCode是5
        private val isMIUIV6OrAbove: Boolean
            get() {
                val miuiVersionCodeStr = getSystemProperty("ro.miui.ui.version.code")
                if (!TextUtils.isEmpty(miuiVersionCodeStr)) {
                    try {
                        val miuiVersionCode = Integer.parseInt(miuiVersionCodeStr)
                        if (miuiVersionCode >= 4) {
                            return true
                        }
                    } catch (e: Exception) {
                    }

                }
                return false
            }

        //Android Api 23以上
        private val isAndroidMOrAbove: Boolean
            get() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return true
                }
                return false
            }

        private fun getSystemProperty(propName: String): String? {
            val line: String
            var input: BufferedReader? = null
            try {
                val p = Runtime.getRuntime().exec("getprop " + propName)
                input = BufferedReader(InputStreamReader(p.inputStream), 1024)
                line = input.readLine()
                input.close()
            } catch (ex: IOException) {
                return null
            } finally {
                if (input != null) {
                    try {
                        input.close()
                    } catch (e: IOException) {
                    }

                }
            }
            return line
        }
    }
}
