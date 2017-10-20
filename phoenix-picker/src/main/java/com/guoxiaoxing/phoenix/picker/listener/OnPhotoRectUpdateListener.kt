package com.guoxiaoxing.phoenix.picker.listener

import android.graphics.Matrix
import android.graphics.RectF

/**

 *
 * Created by lxw
 */

/**
 * Root layer's matrix changed callback
 * it's very important to  coordinate with other layer view
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
interface OnPhotoRectUpdateListener {

    fun onPhotoRectUpdate(rect: RectF, matrix: Matrix)
}