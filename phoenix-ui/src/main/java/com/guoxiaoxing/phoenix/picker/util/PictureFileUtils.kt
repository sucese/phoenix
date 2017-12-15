package com.guoxiaoxing.phoenix.picker.util

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant
import java.io.*
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*

object PictureFileUtils {
    private val DEFAULT_CACHE_DIR = "picture_cache"

    val POSTFIX = ".JPEG"
    val POST_VIDEO = ".mp4"
    val POST_AUDIO = ".mp3"
    val APP_NAME = "Phoenix"
    val CAMERA_PATH = "/$APP_NAME/CameraImage/"
    val CAMERA_AUDIO_PATH = "/$APP_NAME/CameraAudio/"
    val CROP_PATH = "/$APP_NAME/CropImage/"

    fun createCameraFile(context: Context, type: Int, outputCameraPath: String): File {
        val path: String
        if (type == PhoenixConstant.TYPE_AUDIO) {
            path = if (!TextUtils.isEmpty(outputCameraPath))
                outputCameraPath
            else
                CAMERA_AUDIO_PATH
        } else {
            path = if (!TextUtils.isEmpty(outputCameraPath))
                outputCameraPath
            else
                CAMERA_PATH
        }
        return if (type == PhoenixConstant.TYPE_AUDIO)
            createMediaFile(context, path, type)
        else
            createMediaFile(context, path, type)
    }

    fun createCropFile(context: Context, type: Int): File {
        return createMediaFile(context, CROP_PATH, type)
    }

    private fun createMediaFile(context: Context, parentPath: String, type: Int): File {
        val state = Environment.getExternalStorageState()
        val rootDir = if (state == Environment.MEDIA_MOUNTED)
            Environment.getExternalStorageDirectory()
        else
            context.cacheDir

        val folderDir = File(rootDir.absolutePath + parentPath)
        if (!folderDir.exists() && folderDir.mkdirs()) {

        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
        val fileName = APP_NAME + "_" + timeStamp + ""
        val tmpFile: File
        when (type) {
            PhoenixConstant.TYPE_IMAGE -> tmpFile = File(folderDir, fileName + POSTFIX)
            PhoenixConstant.TYPE_VIDEO -> tmpFile = File(folderDir, fileName + POST_VIDEO)
            PhoenixConstant.TYPE_AUDIO -> tmpFile = File(folderDir, fileName + POST_AUDIO)
            else -> tmpFile = File(folderDir, fileName + POSTFIX)
        }
        return tmpFile
    }


    /**
     * TAG for log messages.
     */
    internal val TAG = "PictureFileUtils"

    /**
     * @param uri The Uri to check.
     * *
     * @return Whether the Uri authority is ExternalStorageProvider.
     * *
     * @author paulburke
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * *
     * @return Whether the Uri authority is DownloadsProvider.
     * *
     * @author paulburke
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * *
     * @return Whether the Uri authority is MediaProvider.
     * *
     * @author paulburke
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * *
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.

     * @param context       The mContext.
     * *
     * @param uri           The Uri to query.
     * *
     * @param selection     (Optional) Filter used in the query.
     * *
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * *
     * @return The value of the _data column, which is typically a file path.
     * *
     * @author paulburke
     */
    fun getDataColumn(context: Context, uri: Uri, selection: String?,
                      selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } catch (ex: IllegalArgumentException) {
            Log.i(TAG, String.format(Locale.getDefault(), "getDataColumn: _data - [%s]", ex.message))
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
        return null
    }

    fun getPhotoCacheDir(context: Context, file: File): File {
        val cacheDir = context.cacheDir
        val file_name = file.name
        if (cacheDir != null) {
            val mCacheDir = File(cacheDir, DEFAULT_CACHE_DIR)
            if (!mCacheDir.mkdirs() && (!mCacheDir.exists() || !mCacheDir.isDirectory)) {
                return file
            } else {
                var fileName = ""
                if (file_name.endsWith(".webp")) {
                    fileName = System.currentTimeMillis().toString() + ".webp"
                } else {
                    fileName = System.currentTimeMillis().toString() + ".png"
                }
                return File(mCacheDir, fileName)
            }
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, "default disk cache dir is null")
        }
        return file
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br></br>
     * <br></br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.

     * @param context The mContext.
     * *
     * @param uri     The Uri to query.
     * *
     * @author paulburke
     */
    @SuppressLint("NewApi")
    fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)!!)

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                val contentUri: Uri
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }else{
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.lastPathSegment
            }

            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }

    /**
     * Copies one file into the other with the given paths.
     * In the event that the paths are the same, trying to copy one file to the other
     * will cause both files to become null.
     * Simply skipping this step if the paths are identical.
     */
    @Throws(IOException::class)
    fun copyFile(pathFrom: String, pathTo: String) {
        if (pathFrom.equals(pathTo, ignoreCase = true)) {
            return
        }

        var outputChannel: FileChannel? = null
        var inputChannel: FileChannel? = null
        try {
            inputChannel = FileInputStream(File(pathFrom)).channel
            outputChannel = FileOutputStream(File(pathTo)).channel
            inputChannel!!.transferTo(0, inputChannel.size(), outputChannel)
            inputChannel.close()
        } finally {
            if (inputChannel != null) inputChannel.close()
            if (outputChannel != null) outputChannel.close()
        }
    }

    /**
     * Copies one file into the other with the given paths.
     * In the event that the paths are the same, trying to copy one file to the other
     * will cause both files to become null.
     * Simply skipping this step if the paths are identical.
     */
    @Throws(IOException::class)
    fun copyAudioFile(pathFrom: String, pathTo: String) {
        if (pathFrom.equals(pathTo, ignoreCase = true)) {
            return
        }

        var outputChannel: FileChannel? = null
        var inputChannel: FileChannel? = null
        try {
            inputChannel = FileInputStream(File(pathFrom)).channel
            outputChannel = FileOutputStream(File(pathTo)).channel
            inputChannel!!.transferTo(0, inputChannel.size(), outputChannel)
            inputChannel.close()
        } finally {
            if (inputChannel != null) inputChannel.close()
            if (outputChannel != null) outputChannel.close()
            PictureFileUtils.deleteFile(pathFrom)
        }
    }

    /**
     * 读取图片属性：旋转的角度

     * @param path 图片绝对路径
     * *
     * @return degree旋转的角度
     */
    fun readPictureDegree(path: String): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return degree
    }

    /*
     * 旋转图片
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    fun rotaingImageView(angle: Int, bitmap: Bitmap): Bitmap {
        //旋转图片 动作
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        println("angle2=" + angle)
        // 创建新的图片
        val resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.width, bitmap.height, matrix, true)
        return resizedBitmap
    }

    fun saveBitmapFile(bitmap: Bitmap, file: File) {
        try {
            val bos = BufferedOutputStream(FileOutputStream(file))
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bos.flush()
            bos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * 转换图片成圆形

     * @param bitmap 传入Bitmap对象
     * *
     * @return
     */
    fun toRoundBitmap(bitmap: Bitmap): Bitmap {
        var width = bitmap.width
        var height = bitmap.height
        val roundPx: Float
        val left: Float
        val top: Float
        val right: Float
        val bottom: Float
        val dst_left: Float
        val dst_top: Float
        val dst_right: Float
        val dst_bottom: Float
        if (width <= height) {
            roundPx = (width / 2).toFloat()

            left = 0f
            top = 0f
            right = width.toFloat()
            bottom = width.toFloat()

            height = width

            dst_left = 0f
            dst_top = 0f
            dst_right = width.toFloat()
            dst_bottom = width.toFloat()
        } else {
            roundPx = (height / 2).toFloat()

            val clip = ((width - height) / 2).toFloat()

            left = clip
            right = width - clip
            top = 0f
            bottom = height.toFloat()
            width = height

            dst_left = 0f
            dst_top = 0f
            dst_right = height.toFloat()
            dst_bottom = height.toFloat()
        }

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint()
        val src = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        val dst = Rect(dst_left.toInt(), dst_top.toInt(), dst_right.toInt(), dst_bottom.toInt())
        val rectF = RectF(dst)

        paint.isAntiAlias = true// 设置画笔无锯齿

        canvas.drawARGB(0, 0, 0, 0) // 填充整个Canvas

        // 以下有两种方法画圆,drawRounRect和drawCircle
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。
        // canvas.drawCircle(roundPx, roundPx, roundPx, paint);

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452
        canvas.drawBitmap(bitmap, src, dst, paint) // 以Mode.SRC_IN模式合并bitmap和已经draw了的Circle

        return output
    }

    /**
     * 创建文件夹

     * @param filename
     * *
     * @return
     */
    fun createDir(context: Context, filename: String, directory_path: String): String {
        val state = Environment.getExternalStorageState()
        val rootDir = if (state == Environment.MEDIA_MOUNTED) Environment.getExternalStorageDirectory() else context.cacheDir
        var path: File? = null
        if (!TextUtils.isEmpty(directory_path)) {
            // 自定义保存目录
            path = File(rootDir.absolutePath + directory_path)
        } else {
            path = File(rootDir.absolutePath + "/Phoenix")
        }
        if (!path.exists())
        // 若不存在，创建目录，可以在应用启动的时候创建
            path.mkdirs()

        return path.toString() + "/" + filename
    }


    /**
     * image is Damage

     * @param path
     * *
     * @return
     */
    fun isDamage(path: String): Int {
        var options: BitmapFactory.Options? = null
        if (options == null) options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options) //filePath代表图片路径
        if (options.mCancel || options.outWidth == -1
                || options.outHeight == -1) {
            //表示图片已损毁
            return -1
        }
        return 0
    }

    /**
     * 获取某目录下所有文件路径

     * @param dir
     */
    fun getDirFiles(dir: String): List<String> {
        val scanner5Directory = File(dir)
        val list = ArrayList<String>()
        if (scanner5Directory.isDirectory) {
            for (file in scanner5Directory.listFiles()) {
                val path = file.absolutePath
                if (path.endsWith(".jpg") || path.endsWith(".jpeg")
                        || path.endsWith(".png") || path.endsWith(".gif")
                        || path.endsWith(".webp")) {
                    list.add(path)
                }
            }
        }
        return list
    }

    val dcimCameraPath: String
        get() {
            val absolutePath: String
            try {
                absolutePath = "%" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/Camera"
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }

            return absolutePath
        }

    /**
     * set empty Phoenix Cache

     * @param mContext
     */
    fun deleteCacheDirFile(mContext: Context) {
        val cutDir = mContext.cacheDir
        val compressDir = File(mContext.cacheDir.toString() + "/picture_cache")
        val lubanDir = File(mContext.cacheDir.toString() + "/luban_disk_cache")
        if (cutDir != null) {
            val files = cutDir.listFiles()
            for (file in files) {
                if (file.isFile)
                    file.delete()
            }
        }

        if (compressDir != null) {
            val files = compressDir.listFiles()
            if (files != null)
                for (file in files) {
                    if (file.isFile)
                        file.delete()
                }
        }

        if (lubanDir != null) {
            val files = lubanDir.listFiles()
            if (files != null)
                for (file in files) {
                    if (file.isFile)
                        file.delete()
                }
        }
        DebugUtil.i(TAG, "Cache delete success!")
    }

    /**
     * delete file

     * @param path
     */
    fun deleteFile(path: String) {
        try {
            if (!TextUtils.isEmpty(path)) {
                val file = File(path)
                file?.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
