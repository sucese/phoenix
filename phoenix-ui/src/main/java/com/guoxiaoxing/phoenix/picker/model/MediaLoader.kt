package com.guoxiaoxing.phoenix.picker.model

import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.text.TextUtils
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant
import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.core.model.MimeType
import java.io.File
import java.util.*

class MediaLoader(private val activity: FragmentActivity, type: Int, private val isGif: Boolean, videoS: Long) {

    private var type = PhoenixConstant.TYPE_IMAGE
    private var videoS: Long = 0

    init {
        this.type = type
        this.videoS = videoS
    }

    fun loadAllMedia(imageLoadListener: LocalMediaLoadListener) {
        activity.supportLoaderManager.initLoader(type, null,
                object : LoaderManager.LoaderCallbacks<Cursor> {
                    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
                        val cursorLoader = when (id) {
                            PhoenixConstant.TYPE_ALL -> CursorLoader(
                                    activity,
                                    QUERY_URI,
                                    PROJECTION,
                                    SELECTION_ALL,
                                    null,
                                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC")
                            PhoenixConstant.TYPE_IMAGE -> CursorLoader(
                                    activity,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    PROJECTION, if (isGif) CONDITION_GIF else CONDITION,
                                    if (isGif) SELECT_GIF else SELECT,
                                    PROJECTION[0] + " DESC")
                            PhoenixConstant.TYPE_VIDEO -> CursorLoader(
                                    activity,
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                    PROJECTION, if (videoS > 0)
                                DURATION + " <= ? and "
                                        + DURATION + "> 0"
                            else
                                DURATION + "> 0", if (videoS > 0)
                                arrayOf(videoS.toString())
                            else
                                null, PROJECTION[0] + " DESC")
                            else ->
                                CursorLoader(
                                        activity, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                        PROJECTION, if (videoS > 0)
                                    DURATION + " <= ? and "
                                            + DURATION + ">" + AUDIO_DURATION
                                else
                                    DURATION + "> " + AUDIO_DURATION, if (videoS > 0)
                                    arrayOf(videoS.toString())
                                else
                                    null, PROJECTION[0] + " DESC")

                        }
                        return cursorLoader
                    }

                    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {

                        if (data == null) {
                            return
                        }

                        try {
                            val imageFolders = ArrayList<MediaFolder>()
//                            val allImageFolder = MediaFolder()
                            val allImageFolder = MediaFolder("", "", "", 0, 0, true, ArrayList())
                            val latelyImages = ArrayList<MediaEntity>()
                            val count = data.count
                            if (count > 0) {
                                data.moveToFirst()
                                do {
                                    val path = data.getString(data.getColumnIndexOrThrow(PROJECTION[1]))
                                    // 如原图路径不存在或者路径存在但文件不存在,就结束当前循环
                                    if (TextUtils.isEmpty(path) || !File(path).exists()) {
                                        continue
                                    }
                                    val mimeType = data.getString(data.getColumnIndexOrThrow(PROJECTION[4]))
                                    val duration = data.getLong(data.getColumnIndexOrThrow(PROJECTION[6]))
                                    var fileType = 0
                                    if (mimeType.startsWith(PhoenixConstant.AUDIO)) {
                                        fileType = MimeType.ofAudio()
                                    } else if (mimeType.startsWith(PhoenixConstant.IMAGE)) {
                                        fileType = MimeType.ofImage()
                                    } else if (mimeType.startsWith(PhoenixConstant.VIDEO)) {
                                        fileType = MimeType.ofVideo()
                                    }
                                    val size = data.getLong(data.getColumnIndexOrThrow(PROJECTION[5]))
                                    val width = data.getInt(data.getColumnIndexOrThrow(PROJECTION[7]))
                                    val height = data.getInt(data.getColumnIndexOrThrow(PROJECTION[8]))
                                    val latitude = data.getDouble(data.getColumnIndexOrThrow(PROJECTION[9]))
                                    val longitude = data.getDouble(data.getColumnIndexOrThrow(PROJECTION[10]))
                                    val image = MediaEntity.newBuilder()
                                            .localPath(path)
                                            .duration(duration)
                                            .fileType(fileType)
                                            .mimeType(mimeType)
                                            .size(size)
                                            .width(width)
                                            .height(height)
                                            .latitude(latitude)
                                            .longitude(longitude)
                                            .build()

                                    val folder = getImageFolder(path, imageFolders)
                                    val images = folder.images
                                    images.add(image)
                                    folder.imageNumber = folder.imageNumber + 1
                                    latelyImages.add(image)
                                    val imageNum = allImageFolder.imageNumber
                                    allImageFolder.imageNumber = imageNum + 1
                                } while (data.moveToNext())

                                if (latelyImages.size > 0) {
                                    sortFolder(imageFolders)
                                    imageFolders.add(0, allImageFolder)
                                    allImageFolder.firstImagePath = latelyImages[0].localPath
                                    val title = if (type == MimeType.ofAudio())
                                        activity.getString(R.string.picture_all_audio)
                                    else
                                        activity.getString(R.string.picture_camera_roll)
                                    allImageFolder.name = title
                                    allImageFolder.images = latelyImages
                                }
                                imageLoadListener.loadComplete(imageFolders)
                            } else {
                                // 如果没有相册
                                imageLoadListener.loadComplete(imageFolders)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                    override fun onLoaderReset(loader: Loader<Cursor>) {}
                })
    }

    private fun sortFolder(imageFolders: List<MediaFolder>) {
        // 文件夹按图片数量排序
        Collections.sort(imageFolders, Comparator<MediaFolder> { lhs, rhs ->
            if (lhs.images == null || rhs.images == null) {
                return@Comparator 0
            }
            val lsize = lhs.imageNumber
            val rsize = rhs.imageNumber
            if (lsize == rsize) 0 else if (lsize < rsize) 1 else -1
        })
    }

    private fun getImageFolder(path: String, imageFolders: MutableList<MediaFolder>): MediaFolder {
        val imageFile = File(path)
        val folderFile = imageFile.parentFile

        for (folder in imageFolders) {
            if (folder.name == folderFile.name) {
                return folder
            }
        }
        val newFolder = MediaFolder(folderFile.name, folderFile.absolutePath, path, 0, 0, true, ArrayList())
//        newFolder.name =folderFile.name
//        newFolder.path =folderFile.absolutePath
//        newFolder.firstImagePath =path
        imageFolders.add(newFolder)
        return newFolder
    }

    interface LocalMediaLoadListener {
        fun loadComplete(folders: MutableList<MediaFolder>)
    }

    companion object {

        private val QUERY_URI = MediaStore.Files.getContentUri("external")
        private val DURATION = "duration"

        /**
         * 过滤掉小于500毫秒的录音
         */
        private val AUDIO_DURATION = 500

        /**
         * 查询全部图片和视频，并且过滤掉已损坏图片和视频
         */
        private val SELECTION_ALL = (
                MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        + " AND "
                        + MediaStore.Files.FileColumns.SIZE + ">0")

        private val SELECTION_ALL_ARGS = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())

        private val PROJECTION = arrayOf(MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.LATITUDE,
                MediaStore.Images.Media.LONGITUDE)

        /**
         * 只查询图片条件
         */
        private val CONDITION_GIF =
                "(" + MediaStore.Images.Media.MIME_TYPE + "=? or " +
                        MediaStore.Images.Media.MIME_TYPE + "=?" + " or " +
                        MediaStore.Images.Media.MIME_TYPE + "=?" + " or " +
                        MediaStore.Images.Media.MIME_TYPE + "=?)" + " AND " +
                        MediaStore.MediaColumns.WIDTH +
                        ">0"

        private val SELECT_GIF = arrayOf("image/jpeg", "image/png", "image/gif", "image/webp")

        /**
         * 获取全部图片，但过滤掉gif
         */
        private val CONDITION =
                "(" + MediaStore.Images.Media.MIME_TYPE + "=? or " +
                        MediaStore.Images.Media.MIME_TYPE + "=?" + " or " +
                        MediaStore.Images.Media.MIME_TYPE + "=?)" + " AND " +
                        MediaStore.MediaColumns.WIDTH +
                        ">0"

        private val SELECT = arrayOf("image/jpeg", "image/png", "image/webp")

        /**
         * 获取全部图片和视频，但过滤掉gif图片
         */
        private val SELECTION_NOT_GIF =
                "(" + MediaStore.Images.Media.MIME_TYPE + "=?" +
                        " OR " +
                        MediaStore.Images.Media.MIME_TYPE + "=?" +
                        " OR " +
                        MediaStore.Images.Media.MIME_TYPE + "=?" +
                        " OR " +
                        MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)" +
                        " AND " +
                        MediaStore.MediaColumns.SIZE + ">0" +
                        " AND " +
                        MediaStore.MediaColumns.WIDTH + ">0"

        private val SELECTION_NOT_GIF_ARGS = arrayOf("image/jpeg", "image/png", "image/webp", MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())

        private val ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC"
    }
}
