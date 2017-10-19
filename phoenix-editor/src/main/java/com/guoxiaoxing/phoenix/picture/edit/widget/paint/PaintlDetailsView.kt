package com.guoxiaoxing.phoenix.picture.edit.widget.paint

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.guoxiaoxing.phoenix.picture.edit.R
import com.guoxiaoxing.phoenix.picture.edit.operation.OnRevokeListener
import com.guoxiaoxing.phoenix.picture.edit.operation.Operation
import com.guoxiaoxing.phoenix.picture.edit.widget.ColorSeekBar

/**
 * ## UI elements of scrawl view
 *
 * Created by lxw
 */
class PaintlDetailsView(ctx: Context) : FrameLayout(ctx) {
    var onRevokeListener: OnRevokeListener? = null
    var onColorChangeListener: ColorSeekBar.OnColorChangeListener? = null

    init {
        LayoutInflater.from(ctx).inflate(R.layout.scralw_func_details, this, true)
        findViewById(R.id.ivRevoke).setOnClickListener {
            onRevokeListener?.revoke(Operation.PaintOperation)
        }
        val ckb = findViewById(R.id.colorBarScrawl) as ColorSeekBar
        ckb.setOnColorChangeListener(object : ColorSeekBar.OnColorChangeListener {
            override fun onColorChangeListener(colorBarPosition: Int, alphaBarPosition: Int, color: Int) {
                onColorChangeListener?.onColorChangeListener(colorBarPosition, alphaBarPosition, color)
            }
        })
    }
}