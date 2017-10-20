package com.guoxiaoxing.phoenix.picker.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import android.view.WindowManager

object PictureUtils {

    private fun computeSize(inputWidth: Int, inputHeight: Int): Int {
        val sampleSize: Int
        var srcWidth = if (inputWidth % 2 == 1) inputWidth + 1 else inputWidth
        var srcHeight = if (inputHeight % 2 == 1) inputHeight + 1 else inputHeight
        srcWidth = if (srcWidth > srcHeight) srcHeight else srcWidth
        srcHeight = if (srcWidth > srcHeight) srcWidth else srcHeight
        val scale = srcWidth * 1.0 / srcHeight
        if (scale <= 1 && scale > 0.5625) {
            if (srcHeight < 1664) {
                sampleSize = 1
            } else if (srcHeight in 1666 until 4990) {
                sampleSize = 2
            } else if (srcHeight in 4990 until 10240) {
                sampleSize = 4
            } else {
                sampleSize = if (srcHeight / 1280 == 0) 1 else srcHeight / 1280
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            sampleSize = if (srcHeight / 1280 == 0) 1 else srcHeight / 1280
        } else {
            sampleSize = Math.ceil(srcHeight / (1280.0 / scale)).toInt()
        }
        return sampleSize
    }

    fun getImageBitmap(filePath: String): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        val outWidth = options.outWidth
        val outHeight = options.outHeight
        options.inSampleSize = computeSize(outWidth, outHeight) *2
        options.inJustDecodeBounds = false
        logD1("options.inSampleSize=${options.inSampleSize}")
        return BitmapFactory.decodeFile(filePath, options)
    }

    fun roatePicture(rotation: Int, data: ByteArray, context: Context): Bitmap {
        var bitmap = decodeSampledBitmapFromByte(context, data)
        val oldBitmap = bitmap
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        bitmap = Bitmap.createBitmap(oldBitmap, 0, 0, oldBitmap.getWidth(), oldBitmap.getHeight(), matrix, false)
        oldBitmap.recycle()
        return bitmap
    }

    fun decodeSampledBitmapFromByte(context: Context, bitmapBytes: ByteArray): Bitmap {
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

        val reqWidth: Int
        val reqHeight: Int
        val point = Point()
        display.getSize(point)
        reqWidth = point.x
        reqHeight = point.y

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inMutable = true
        options.inBitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.size, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Load & resize the image to be 1/inSampleSize dimensions
        // Use when you do not want to scale the image with a inSampleSize that is a power of 2
        options.inScaled = true
        options.inDensity = options.outWidth
        options.inTargetDensity = reqWidth * options.inSampleSize

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false // If set to true, the decoder will return null (no bitmap), but the out... fields will still be set, allowing the caller to query the bitmap without having to allocate the memory for its pixels.
        options.inPurgeable = true         // Tell to gc that whether it needs free memory, the Bitmap can be cleared
        options.inInputShareable = true    // Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
        return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.size, options)
    }

    /***
     * compute scale ratio

     * @param reqWidth 这里的reqwidth reqHeight 需要压缩到的尺寸
     */
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val initialInSampleSize = computeInitialSampleSize(options, reqWidth, reqHeight)

        var roundedInSampleSize: Int
        if (initialInSampleSize <= 8) {
            roundedInSampleSize = 1
            while (roundedInSampleSize < initialInSampleSize) {
                // Shift one bit to left
                roundedInSampleSize = roundedInSampleSize shl 1 // roundedSampleSize = roundSample * 2
            }
        } else {
            roundedInSampleSize = (initialInSampleSize + 7) / 8 * 8
        }
        return roundedInSampleSize
    }

    private fun computeInitialSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight.toDouble()
        val width = options.outWidth.toDouble()

        val maxNumOfPixels = (reqWidth * reqHeight).toLong()
        val minSideLength = Math.min(reqHeight, reqWidth)

        val lowerBound = if (maxNumOfPixels < 0)
            1
        else
            Math.ceil(Math.sqrt(width * height / maxNumOfPixels)).toInt()
        val upperBound = if (minSideLength < 0)
            128
        else
            Math.min(Math.floor(width / minSideLength),
                    Math.floor(height / minSideLength)).toInt()

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound
        }

        if (maxNumOfPixels < 0 && minSideLength < 0) {
            return 1
        } else if (minSideLength < 0) {
            return lowerBound
        } else {
            return upperBound
        }
    }
}