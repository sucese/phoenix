package com.guoxiaoxing.phoenix.picture.edit.widget.blur

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.picker.util.MatrixUtils

class BlurDetailView(ctx: Context, onMosaicChangeListener: OnMosaicChangeListener?) : FrameLayout(ctx) {
    constructor(ctx: Context) : this(ctx, null)

    var onMosaicChangeListener: OnMosaicChangeListener? = null
    var onRevokeListener: com.guoxiaoxing.phoenix.picture.edit.operation.OnRevokeListener? = null

    init {
        this.onMosaicChangeListener = onMosaicChangeListener
        LayoutInflater.from(ctx).inflate(R.layout.item_edit_blur, this, true)
        val rootFunc = findViewById<View>(R.id.llMosaicDetails) as LinearLayout
        val values = BlurMode.values()
        for (index in 0 until values.size) {
            val mode = values[index]
            if (mode.getModeBgResource() <= 0) {
                continue
            }
            val item = LayoutInflater.from(context).inflate(R.layout.item_edit_blur_detail, rootFunc, false)
            val ivFuncDesc = item.findViewById(R.id.ivMosaicDesc) as ImageView
            ivFuncDesc.setImageResource(mode.getModeBgResource())
            item.tag = mode
            rootFunc.addView(item)
            item.setOnClickListener {
                onMosaicClick(mode, index, item, rootFunc)
            }
            if (index == 0) {
                item.isSelected = true
                onMosaicClick(mode, 0, item, rootFunc)
            }
        }
        findViewById<View>(R.id.ivRevoke).setOnClickListener {
            onRevokeListener?.revoke(com.guoxiaoxing.phoenix.picture.edit.operation.Operation.BlurOperation)
        }
    }

    private fun onMosaicClick(blurMode: BlurMode, position: Int, clickView: View, rootView: ViewGroup) {
        MatrixUtils.changeSelectedStatus(rootView, position)
        onMosaicChangeListener?.onChange(blurMode)
    }

    interface OnMosaicChangeListener {
        fun onChange(blurMode: BlurMode)
    }

}