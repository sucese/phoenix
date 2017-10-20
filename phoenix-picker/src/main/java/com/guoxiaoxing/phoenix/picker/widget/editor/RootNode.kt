package com.guoxiaoxing.phoenix.picker.widget.editor

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import com.guoxiaoxing.phoenix.picker.listener.GestureDetectorListener
import com.guoxiaoxing.phoenix.picker.listener.OnPhotoRectUpdateListener

interface RootNode<out RootView> {

    fun addOnMatrixChangeListener(listener: OnPhotoRectUpdateListener)

    fun addGestureDetectorListener(listener: GestureDetectorListener)
    //setter
    fun setRotationBy(degree: Float)

    fun resetMinScale(minScale: Float)

    fun resetMaxScale(maxScale: Float)

    fun setScale(scale: Float, animate: Boolean)

    fun setSupportMatrix(matrix: Matrix)

    fun setDisplayBitmap(bitmap: Bitmap)

    fun getSupportMatrix(): Matrix

    fun getDisplayBitmap(): Bitmap?

    fun getRooView(): RootView

    fun getDisplayingRect(): RectF

    fun getDisplayMatrix(): Matrix

    fun getBaseLayoutMatrix(): Matrix

    fun getOriginalRect(): RectF?

}