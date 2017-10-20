package com.guoxiaoxing.phoenix.picker.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.DisplayMetrics

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

object BitmapUtils {
    val MAX_SZIE = (1024 * 900).toLong()


    class Size(var width: Int, var height: Int)

    fun loadImageByPath(imagePath: String, reqWidth: Int,
                        reqHeight: Int): Bitmap {
        val file = File(imagePath)
        if (file.length() < MAX_SZIE) {
            return getSampledBitmap(imagePath, reqWidth, reqHeight)
        } else {// 压缩图片
            return getImageCompress(imagePath)
        }
    }

    fun getSampledBitmap(filePath: String, reqWidth: Int,
                         reqHeight: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        BitmapFactory.decodeFile(filePath, options)

        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math
                        .floor((height.toFloat() / reqHeight + 0.5f).toDouble()).toInt() // Math.round((float)height

            } else {
                inSampleSize = Math
                        .floor((width.toFloat() / reqWidth + 0.5f).toDouble()).toInt() // Math.round((float)width
            }
        }

        options.inSampleSize = inSampleSize
        options.inJustDecodeBounds = false
        try {
            BitmapFactory.decodeFile(filePath, options)
        } catch (error: OutOfMemoryError) {
            options.inSampleSize *= 2
        }

        return BitmapFactory.decodeFile(filePath, options)
    }


    // 按大小缩放
    fun getImageCompress(srcPath: String): Bitmap {
        val newOpts = BitmapFactory.Options()
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true
        var bitmap = BitmapFactory.decodeFile(srcPath, newOpts)// 此时返回bm为空

        newOpts.inJustDecodeBounds = false
        val w = newOpts.outWidth
        val h = newOpts.outHeight
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        val hh = 1280f// 这里设置高度为800f
        val ww = 720f// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        var be = 1// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (newOpts.outWidth / ww).toInt()
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (newOpts.outHeight / hh).toInt()
        }
        if (be <= 0)
            be = 1
        newOpts.inSampleSize = be// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts)
        return compressImage(bitmap)// 压缩好比例大小后再进行质量压缩
    }

    fun getImageSize(absPath: String): Size {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ALPHA_8
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(absPath, options)
        val size = Size(options.outWidth, options.outHeight)
        return size
    }

    // 图片质量压缩
    private fun compressImage(image: Bitmap): Bitmap {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        var options = 100

        while (baos.toByteArray().size / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset()// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos)// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10// 每次都减少10
        }
        val isBm = ByteArrayInputStream(baos.toByteArray())// 把压缩后的数据baos存放到ByteArrayInputStream中
        val bitmap = BitmapFactory.decodeStream(isBm, null, null)// 把ByteArrayInputStream数据生成图片
        return bitmap
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, context: Context): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        val manager = (context as Activity).windowManager

        val outMetrics = DisplayMetrics()
        manager.defaultDisplay.getMetrics(outMetrics)
        val screenWidht = outMetrics.widthPixels
        val ScreeHeiht = outMetrics.heightPixels
        if (height > ScreeHeiht || width > screenWidht) {
            val heightRatio = Math.round(height.toFloat() / ScreeHeiht.toFloat())
            val widthRatio = Math.round(width.toFloat() / screenWidht.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    fun getSmallBitmap(filePath: String, context: Context): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, context)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        try {
            BitmapFactory.decodeFile(filePath, options)
        } catch (error: OutOfMemoryError) {
            options.inSampleSize *= 2
        }

        return BitmapFactory.decodeFile(filePath, options)
    }
}
