package com.guoxiaoxing.phoenix.picker.widget

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView

import com.guoxiaoxing.phoenix.R

class PhotoPopupWindow(context: Context) : PopupWindow(context), View.OnClickListener {
    private val picture_tv_photo: TextView
    private val picture_tv_video: TextView
    private val picture_tv_cancel: TextView
    private val ll_root: LinearLayout
    private val fl_content: FrameLayout
    private val animationIn: Animation
    private val animationOut: Animation
    private var isDismiss = false

    init {
        val inflate = LayoutInflater.from(context).inflate(R.layout.picture_camera_pop_layout, null)
        this.width = LinearLayout.LayoutParams.MATCH_PARENT
        this.height = LinearLayout.LayoutParams.MATCH_PARENT
        this.setBackgroundDrawable(ColorDrawable())
        this.isFocusable = true
        this.isOutsideTouchable = true
        this.update()
        this.setBackgroundDrawable(ColorDrawable())
        this.contentView = inflate
        animationIn = AnimationUtils.loadAnimation(context, R.anim.phoenix_up_in)
        animationOut = AnimationUtils.loadAnimation(context, R.anim.phoenix_down_out)
        ll_root = inflate.findViewById(R.id.ll_root) as LinearLayout
        fl_content = inflate.findViewById(R.id.fl_content) as FrameLayout
        picture_tv_photo = inflate.findViewById(R.id.picture_tv_photo) as TextView
        picture_tv_cancel = inflate.findViewById(R.id.picture_tv_cancel) as TextView
        picture_tv_video = inflate.findViewById(R.id.picture_tv_video) as TextView
        picture_tv_video.setOnClickListener(this)
        picture_tv_cancel.setOnClickListener(this)
        picture_tv_photo.setOnClickListener(this)
        fl_content.setOnClickListener(this)
    }

    override fun showAsDropDown(parent: View) {
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                val location = IntArray(2)
                parent.getLocationOnScreen(location)
                val x = location[0]
                val y = location[1] + parent.height
                this.showAtLocation(parent, Gravity.BOTTOM, x, y)
            } else {
                this.showAtLocation(parent, Gravity.BOTTOM, 0, 0)
            }

            isDismiss = false
            ll_root.startAnimation(animationIn)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun dismiss() {
        if (isDismiss) {
            return
        }
        isDismiss = true
        ll_root.startAnimation(animationOut)
        dismiss()
        animationOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                isDismiss = false
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                    dismiss4Pop()
                } else {
                    super@PhotoPopupWindow.dismiss()
                }
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    /**
     * 在android4.1.1和4.1.2版本关闭PopWindow
     */
    private fun dismiss4Pop() {
        Handler().post { super@PhotoPopupWindow.dismiss() }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.picture_tv_photo) {
            if (onItemClickListener != null) {
                onItemClickListener!!.onItemClick(0)
                super@PhotoPopupWindow.dismiss()
            }
        }
        if (id == R.id.picture_tv_video) {
            if (onItemClickListener != null) {
                onItemClickListener!!.onItemClick(1)
                super@PhotoPopupWindow.dismiss()
            }
        }
        dismiss()
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(positon: Int)
    }
}
