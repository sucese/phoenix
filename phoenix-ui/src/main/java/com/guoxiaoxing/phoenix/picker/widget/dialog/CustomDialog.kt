package com.guoxiaoxing.phoenix.picker.widget.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity

class CustomDialog(context: Context, width: Int, height: Int, layout: Int,
                   style: Int) : Dialog(context, style) {

    init {
        setContentView(layout)
        val window = window
        val params = window!!.attributes
        params.width = width
        params.height = height
        params.gravity = Gravity.CENTER
        window.attributes = params
    }

    fun getDensity(context: Context): Float {
        val resources = context.resources
        val dm = resources.displayMetrics
        return dm.density
    }

}
