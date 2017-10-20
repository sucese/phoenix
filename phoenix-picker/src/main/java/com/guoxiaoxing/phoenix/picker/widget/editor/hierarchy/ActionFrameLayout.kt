package com.guoxiaoxing.phoenix.picture.edit.widget.hierarchy

import android.annotation.TargetApi
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * Root layer for handle action event
 * it coordinate with [com.guoxiaoxing.phoenix.picture.edit.widget.ActionBarAnimHelper]
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
class ActionFrameLayout : FrameLayout {
    var actionListener: ActionListener? = null

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

    /**
     * always dispatch action for listener
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {
            actionListener?.actionUp()
        } else if (ev.action == MotionEvent.ACTION_MOVE) {
            actionListener?.actionMove()
        }
        return super.dispatchTouchEvent(ev)
    }

    interface ActionListener {
        fun actionUp()
        fun actionMove()
    }
}