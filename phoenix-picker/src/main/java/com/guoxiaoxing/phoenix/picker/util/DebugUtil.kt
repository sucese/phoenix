package com.guoxiaoxing.phoenix.picker.util

import android.util.Log

object DebugUtil {
    val TAG = "com.luck.picture.lib"

    val DEBUG = false

    fun debug(tag: String, msg: String) {
        if (DEBUG) {
            Log.d(tag, msg)
        }
    }

    fun debug(msg: String) {
        if (DEBUG) {
            Log.d(TAG, msg)
        }
    }

    fun v(msg: String) {
        if (DEBUG) {
            Log.v(TAG, msg)
        }
    }

    fun v(tag: String, msg: String) {
        if (DEBUG) {
            Log.v(tag, msg)
        }
    }

    fun log(msg: String) {
        if (DEBUG) {
            Log.d(TAG, msg)
        }
    }

    fun log(tag: String, msg: String) {
        if (DEBUG) {
            Log.d(tag, msg)
        }
    }

    fun i(tag: String, msg: String) {
        if (DEBUG) {
            Log.i(tag, msg)
        }
    }

    fun i(msg: String) {
        if (DEBUG) {
            Log.i(TAG, msg)
        }
    }


    fun error(tag: String, error: String) {

        if (DEBUG) {

            Log.e(tag, error)
        }
    }

    fun error(error: String) {

        if (DEBUG) {

            Log.e(TAG, error)
        }
    }

}
