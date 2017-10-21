package com.guoxiaoxing.phoenix.picker.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import android.net.Uri
import android.os.Environment
import android.view.WindowManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {

    private var fileContentUri: Uri? = null

    fun savePicture(context: Context, bitmap: Bitmap, isKownMedia: Boolean): Uri? {
        val mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
                .format(Date()) + System.currentTimeMillis()
        val fileName = "IMG_$timeStamp.png"
        val absPath = mediaStorageDir.path + File.separator + fileName
        val mediaFile = File(absPath)

        // Saving the bitmap
        try {
            val stream = FileOutputStream(mediaFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (exception: IOException) {
            exception.printStackTrace()
        }

        if (isKownMedia) {
            val mediaScannerIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            fileContentUri = Uri.fromFile(mediaFile)
            mediaScannerIntent.data = fileContentUri
            context.sendBroadcast(mediaScannerIntent)
        } else {
            fileContentUri = Uri.parse(absPath)
        }
        return fileContentUri
    }

    fun roatePicture(rotation: Int, bitmap: Bitmap): Bitmap {
        var bitmap = bitmap
        val oldBitmap = bitmap
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        bitmap = Bitmap.createBitmap(oldBitmap, 0, 0, oldBitmap.width, oldBitmap.height, matrix, false)
        oldBitmap.recycle()
        return bitmap
    }

    fun roatePicture(rotation: Int, data: ByteArray, context: Context): Bitmap {
        var bitmap = decodeSampledBitmapFromByte(context, data)
        val oldBitmap = bitmap
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        bitmap = Bitmap.createBitmap(oldBitmap, 0, 0, oldBitmap.width, oldBitmap.height, matrix, false)
        oldBitmap.recycle()
        return bitmap
    }

    val path: String
        get() {
            val isExit = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
            if (isExit) {
                val path = Environment.getExternalStorageDirectory()
                return path.toString()
            }
            return ""
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
