package com.guoxiaoxing.phoenix.picker.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue

object AttrsUtils {

    /**
     * get attrs color

     * @param mContext
     * *
     * @param attr
     * *
     * @return
     */
    fun getTypeValueColor(mContext: Context, attr: Int): Int {
        val typedValue = TypedValue()
        val attribute = intArrayOf(attr)
        val array = mContext.obtainStyledAttributes(typedValue.resourceId, attribute)
        val color = array.getColor(0, -1)
        array.recycle()
        return color
    }

    /**
     * attrs status color or black

     * @param mContext
     * *
     * @param attr
     * *
     * @return
     */
    fun getTypeValueBoolean(mContext: Context, attr: Int): Boolean {
        val typedValue = TypedValue()
        val attribute = intArrayOf(attr)
        val array = mContext.obtainStyledAttributes(typedValue.resourceId, attribute)
        val statusFont = array.getBoolean(0, false)
        array.recycle()
        return statusFont
    }

    /**
     * attrs PopupWindow down or up icon

     * @param mContext
     * *
     * @param attr
     * *
     * @return
     */
    fun getTypeValuePopWindowImg(mContext: Context, attr: Int): Drawable {
        val typedValue = TypedValue()
        val attribute = intArrayOf(attr)
        val array = mContext.obtainStyledAttributes(typedValue.resourceId, attribute)
        val drawable = array.getDrawable(0)
        array.recycle()
        return drawable
    }
}
