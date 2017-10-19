package com.guoxiaoxing.phoenix.picker.ui.camera

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

import com.guoxiaoxing.phoenix.R

class ConfirmDialog @JvmOverloads constructor(private val mContext: Context, width: Int = 0, height: Int = 0) : Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar) {
    private var ll_confirm_content: LinearLayout? = null

    init {
        initView()
    }

    private fun initView() {
        setContentView(R.layout.dialog_confirm_takepic_library)
        ll_confirm_content = findViewById(R.id.ll_confirm_content) as LinearLayout
    }

    fun setLeftButton(label: String, l: View.OnClickListener): ConfirmDialog {
        val tv_left = findViewById(R.id.tv_left_action) as TextView
        tv_left.text = label
        tv_left.setOnClickListener(l)
        tv_left.visibility = View.VISIBLE
        checkDivider()
        return this
    }

    fun setLeftButton(stringResId: Int, l: View.OnClickListener): ConfirmDialog {
        return setLeftButton(mContext.getString(stringResId), l)
    }


    fun setRightButton(label: String, l: View.OnClickListener): ConfirmDialog {
        val tv_right = findViewById(R.id.tv_right_action) as TextView
        tv_right.text = label
        tv_right.setOnClickListener(l)
        tv_right.visibility = View.VISIBLE
        checkDivider()
        return this
    }

    fun setRightButton(stringResId: Int, l: View.OnClickListener): ConfirmDialog {
        return setRightButton(mContext.getString(stringResId), l)
    }

    private fun checkDivider() {
        val tv_right = findViewById(R.id.tv_right_action) as TextView
        val tv_left = findViewById(R.id.tv_left_action) as TextView
        val divider = findViewById(R.id.divider)
        if (tv_right.visibility == View.VISIBLE && tv_left.visibility == View.VISIBLE) {
            divider.visibility = View.VISIBLE
        } else {
            divider.visibility = View.GONE
        }
    }

    fun setMessage(msg: String): ConfirmDialog {
        val tv_message = findViewById(R.id.tv_message) as TextView
        tv_message.gravity = Gravity.CENTER_HORIZONTAL
        if (tv_message != null) {
            tv_message.text = msg
        }
        return this
    }

    fun setMessageAlignCenter(msg: String): ConfirmDialog {
        val tv_message = findViewById(R.id.tv_message) as TextView
        tv_message.gravity = Gravity.CENTER
        if (tv_message != null) {
            tv_message.text = msg
        }
        return this
    }

    fun setMessageAlignLeft(msg: String): ConfirmDialog {
        val tv_message = findViewById(R.id.tv_message) as TextView
        tv_message.gravity = Gravity.LEFT
        if (tv_message != null) {
            tv_message.text = msg
        }
        return this
    }

    fun setConfirmContent(view: View): ConfirmDialog {
        ll_confirm_content!!.removeAllViews()
        ll_confirm_content!!.addView(view)
        return this
    }

    fun setConfirmTitle(message: String): ConfirmDialog {
        val ll_title = findViewById(R.id.ll_confirm_title)
        val tv_title = findViewById(R.id.tv_confirm_title) as TextView
        ll_title.visibility = View.VISIBLE
        tv_title.text = message
        return this
    }

    override fun dismiss() {
        if (onDismissListener != null)
            onDismissListener!!.onDismiss()
        super.dismiss()
    }


    private var onDismissListener: OnDismissListener? = null

    fun setOnDismissListener(onDismissListener: OnDismissListener) {
        this.onDismissListener = onDismissListener
    }

    interface OnDismissListener {
        fun onDismiss()
    }


    var onRightBtnPressedListener: OnRightBtnPressedListener? = null

    interface OnRightBtnPressedListener {
        fun onPressed()
    }
}
