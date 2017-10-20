package com.guoxiaoxing.phoenix.picker.util

import android.content.Context
import android.util.TypedValue

/**
 * Created by wangwei on 16/5/9.
 */
object DpUtils {

    fun dp2px(dip: Int, context: Context): Int {
        val resources = context.resources
        val px = Math
                .round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        dip.toFloat(), resources.displayMetrics))
        return px
    }
}
