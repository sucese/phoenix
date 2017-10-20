package com.guoxiaoxing.phoenix.picker.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.Log
import android.widget.Toast

fun Any.logD(message: String) {
    Log.d(this.javaClass.simpleName, message)
}

fun Any.logD1(message: String) {
    Log.d("ImageEditor", message)
}

fun Any.logE(error: Throwable, desc: String = "Error occur in ${this.javaClass.simpleName} scope") {
    Log.e(this.javaClass.simpleName, desc, error)
}

fun Any.logE1(error: Throwable, desc: String = "Error occur in ${this.javaClass.simpleName} scope") {
    Log.e("ImageEditor", desc, error)
}

fun Any.recycleBitmap(bitmap: Bitmap?) {
    bitmap?.let {
        if (!it.isRecycled) it.recycle()
    }
}

fun RectF.increase(dx: Float, dy: Float) {
    this.left -= dx
    this.top -= dx
    this.right += dy
    this.bottom += dy
}

fun RectF.setInt(left: Int, top: Int, right: Int, bottom: Int) {
    this.left = left.toFloat()
    this.top = top.toFloat()
    this.right = right.toFloat()
    this.bottom = bottom.toFloat()
}

fun RectF.schedule(centerX: Float, centerY: Float, width: Float, height: Float) {
    this.left = centerX - width / 2
    this.top = centerY - height / 2
    this.right = centerX + width / 2
    this.bottom = centerY + height / 2
}

fun Canvas.saveEntireLayer() = this.saveLayer(0f, 0f, this.width.toFloat(), this.height.toFloat(), null, android.graphics.Canvas.ALL_SAVE_FLAG)

fun Context.toastShort(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

