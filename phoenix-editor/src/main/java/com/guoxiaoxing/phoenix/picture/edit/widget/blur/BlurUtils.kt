package com.guoxiaoxing.phoenix.picture.edit.widget.blur

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

object BlurUtils {

    fun getGridBlur(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val radius = 20
        val mosaicBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mosaicBitmap)
        val horCount = Math.ceil((width / radius.toFloat()).toDouble()).toInt()
        val verCount = Math.ceil((height / radius.toFloat()).toDouble()).toInt()
        val paint = Paint()
        paint.isAntiAlias = true
        for (horIndex in 0..horCount - 1) {
            for (verIndex in 0..verCount - 1) {
                val l = radius * horIndex
                val t = radius * verIndex
                var r = l + radius
                if (r > width) {
                    r = width
                }
                var b = t + radius
                if (b > height) {
                    b = height
                }
                val color = bitmap.getPixel(l, t)
                val rect = Rect(l, t, r, b)
                paint.color = color
                canvas.drawRect(rect, paint)
            }
        }
        return mosaicBitmap
    }

    fun getBlurMosaic(bitmap: Bitmap): Bitmap {
        val iterations = 1
        val radius = 8
        val width = bitmap.width
        val height = bitmap.height
        val inPixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        val blured = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.getPixels(inPixels, 0, width, 0, 0, width, height)
        for (i in 0..iterations - 1) {
            blur(inPixels, outPixels, width, height, radius)
            blur(outPixels, inPixels, height, width, radius)
        }
        blured.setPixels(inPixels, 0, width, 0, 0, width, height)
        return blured
    }

    private fun blur(input: IntArray, out: IntArray, width: Int, height: Int, radius: Int) {
        val widthMinus = width - 1
        val tableSize = 2 * radius + 1
        val divide = IntArray(256 * tableSize)
        for (index in 0..256 * tableSize - 1) {
            divide[index] = index / tableSize
        }
        var inIndex = 0
        for (y in 0..height - 1) {
            var outIndex = y
            var ta = 0
            var tr = 0
            var tg = 0
            var tb = 0
            for (i in -radius..radius) {
                val rgb = input[inIndex + clamp(i, 0, width - 1)]
                ta += rgb shr 24 and 0xff
                tr += rgb shr 16 and 0xff
                tg += rgb shr 8 and 0xff
                tb += rgb and 0xff
            }
            for (x in 0..width - 1) {
                out[outIndex] = divide[ta] shl 24 or (divide[tr] shl 16) or (divide[tg] shl 8) or divide[tb]

                var i1 = x + radius + 1
                if (i1 > widthMinus)
                    i1 = widthMinus
                var i2 = x - radius
                if (i2 < 0)
                    i2 = 0
                val rgb1 = input[inIndex + i1]
                val rgb2 = input[inIndex + i2]

                ta += (rgb1 shr 24 and 0xff) - (rgb2 shr 24 and 0xff)
                tr += (rgb1 and 0xff0000) - (rgb2 and 0xff0000) shr 16
                tg += (rgb1 and 0xff00) - (rgb2 and 0xff00) shr 8
                tb += (rgb1 and 0xff) - (rgb2 and 0xff)
                outIndex += height
            }
            inIndex += width
        }
    }

    private fun clamp(x: Int, a: Int, b: Int): Int {
        return if (x < a) a else if (x > b) b else x
    }
}