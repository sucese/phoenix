package com.guoxiaoxing.phoenix.picture.edit.widget.hierarchy

import android.annotation.TargetApi
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * ## Underlay  view for intercept action event pass to layer
 *  A logic useful for [CropView]
 *
 * Created by lxw
 */
class HierarchyComposite : FrameLayout {
    var handleEvent = true

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(context)
    }

    private fun initView(context: Context) {

    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (handleEvent) {
            return super.dispatchTouchEvent(ev)
        } else {
            return false
        }
    }
}