package com.guoxiaoxing.phoenix.picker.widget.videoview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.VideoView

class InternalVideoView @JvmOverloads constructor(mContext: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : VideoView(mContext, attrs, defStyleAttr), View.OnTouchListener {

    private var lastX: Float = 0.toFloat()
    private var lastY: Float = 0.toFloat()
    private val thresold = 30
    private var mStateListener: StateListener? = null

    interface StateListener {
        fun changeVolume(detlaY: Float)
        fun changeBrightness(detlaX: Float)
        fun hideHint()
    }

    fun setStateListener(stateListener: StateListener) {
        this.mStateListener = stateListener
    }

    init {
        setOnTouchListener(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = View.getDefaultSize(1920, widthMeasureSpec)
        val height = View.getDefaultSize(1080, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val detlaX = event.x - lastX
                val detlaY = event.y - lastY

                if (Math.abs(detlaX) < thresold && Math.abs(detlaY) > thresold) {
                    //左侧上下滑动调节音量
                    mStateListener!!.changeVolume(detlaY)
                    //TODO 右侧上下滑动调节亮度
                    //                    mStateListener.changeBrightness(detlaX);
                }

                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_UP -> mStateListener!!.hideHint()
        }
        return true
    }
}
