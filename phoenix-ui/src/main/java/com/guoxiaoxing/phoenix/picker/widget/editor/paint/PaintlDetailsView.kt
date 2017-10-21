package com.guoxiaoxing.phoenix.picture.edit.widget.paint

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.picker.widget.editor.ColorSeekBar

/**
 * ## UI elements of scrawl view
 *
 * Created by lxw
 */
class PaintlDetailsView(ctx: Context) : FrameLayout(ctx) {
    var onRevokeListener: com.guoxiaoxing.phoenix.picture.edit.operation.OnRevokeListener? = null
    var onColorChangeListener: ColorSeekBar.OnColorChangeListener? = null

    init {
        LayoutInflater.from(ctx).inflate(R.layout.item_edit_paint, this, true)
        findViewById(R.id.ivRevoke).setOnClickListener {
            onRevokeListener?.revoke(com.guoxiaoxing.phoenix.picture.edit.operation.Operation.PaintOperation)
        }
        val ckb = findViewById(R.id.colorBarScrawl) as ColorSeekBar
        ckb.setOnColorChangeListener(object : ColorSeekBar.OnColorChangeListener {
            override fun onColorChangeListener(colorBarPosition: Int, alphaBarPosition: Int, color: Int) {
                onColorChangeListener?.onColorChangeListener(colorBarPosition, alphaBarPosition, color)
            }
        })
    }
}