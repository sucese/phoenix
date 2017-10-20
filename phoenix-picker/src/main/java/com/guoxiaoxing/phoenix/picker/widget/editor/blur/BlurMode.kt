package com.guoxiaoxing.phoenix.picture.edit.widget.blur

import com.guoxiaoxing.phoenix.R

/**
 * ## Mosaic mode supported  in current version
 *
 * Created by lxw
 */
enum class BlurMode {
    Grid {
        override fun getModeBgResource() = R.drawable.phoenix_selector_edit_image_traditional_mosaic
    },
    Blur {
        override fun getModeBgResource() = R.drawable.phoenix_selector_edit_image_brush_mosaic
    };

    abstract fun getModeBgResource(): Int
}