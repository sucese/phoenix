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

class MediaLoader(private val activity: FragmentActivity, type: Int, private val isGif: Boolean
                  , videoFilterTime: Long, mediaFilterSize: Int) {

    private var type = PhoenixConstant.TYPE_IMAGE
    private var videoFilterTime: Long = 0
    private var mediaFilterSize: Int = 0

    init {
        this.type = type
        this.videoFilterTime = videoFilterTime * 1000
        this.mediaFilterSize = mediaFilterSize * 1000
    }

    fun loadAllMedia(imageLoadListener: LocalMediaLoadListener) {
        activity.supportLoaderManager.initLoader(type, null,
                object : LoaderManager.LoaderCallbacks<Cursor> {
                    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {

                        val durationCondition = if (videoFilterTime > 0) " AND " + DURATION + "<" + videoFilterTime.toString() else ""
                        val sizeCondition = if (mediaFilterSize > 0) " AND " + SIZE + "<" + mediaFilterSize.toString() else ""
                        return when (id) {
                            PhoenixConstant.TYPE_ALL ->
                                CursorLoader(
                                        activity,
                                        ALL_QUERY_URI,
                                        ALL_PROJECTION,
                                        ALL_SELECTION
                                                + durationCondition
                                                + sizeCondition,
                                        null,
                                        MediaStore.Files.FileColumns.DATE_ADDED + " DESC")
                            PhoenixConstant.TYPE_IMAGE -> CursorLoader(
                                    activity,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    IMAGE_PROJECTION,
                                    IMAGE_SELECTION
                                            + sizeCondition,
                                    IMAGE_SELECTION_ARGS,
                                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC")
                            PhoenixConstant.TYPE_VIDEO -> CursorLoader(
                                    activity,
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                    VIDEO_PROJECTION,
                                    VIDEO_SELECTION
                                            + durationCondition
                                            + sizeCondition,
                                    VIDEO_SELECTION_ARGS, MediaStore.Files.FileColumns.DATE_ADDED + " DESC")
                            PhoenixConstant.TYPE_AUDIO -> CursorLoader(
                                    activity,
                                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    AUDIO_PROJECTION,
                                    AUDIO_SELECTION
                                            + durationCondition
                                            + sizeCondition,
                                    AUDIO_SELECTION_ARGS, MediaStore.Files.FileColumns.DATE_ADDED + " DESC")
                            else ->
                                CursorLoader(
                                        activity,
                                        ALL_QUERY_URI,
                                        ALL_PROJECTION,
                                        ALL_SELECTION
                                                + durationCondition
                                                + sizeCondition,
                                        null,
                                        MediaStore.Files.FileColumns.DATE_ADDED + " DESC")

                        }
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
                                    val path = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[1]))
                                    // 如原图路径不存在或者路径存在但文件不存在,就结束当前循环
                                    if (TextUtils.isEmpty(path) || !File(path).exists()) {
                                        continue
                                    }
                                    val mimeType = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[4]))
                                    var fileType = 0
                                    var duration = 0L
                                    if (mimeType == null) continue;
                                    if (mimeType.startsWith(PhoenixConstant.AUDIO)) {
                                        fileType = MimeType.ofAudio()
                                    } else if (mimeType.startsWith(PhoenixConstant.IMAGE)) {
                                        fileType = MimeType.ofImage()
                                    } else if (mimeType.startsWith(PhoenixConstant.VIDEO)) {
                                        fileType = MimeType.ofVideo()
                                        duration = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[10]))

                                    }

                                    val size = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[5]))
                                    val width = data.getInt(data.getColumnIndexOrThrow(ALL_PROJECTION[6]))
                                    val height = data.getInt(data.getColumnIndexOrThrow(ALL_PROJECTION[7]))
                                    val latitude = data.getDouble(data.getColumnIndexOrThrow(ALL_PROJECTION[8]))
                                    val longitude = data.getDouble(data.getColumnIndexOrThrow(ALL_PROJECTION[9]))
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

        /**
         * 过滤掉小于500毫秒的录音
         */
        private val AUDIO_DURATION = 500

        private val ALL_QUERY_URI = MediaStore.Files.getContentUri("external")
        private val DURATION = "duration"
        private val SIZE = "_size"
        private val LATITUDE = "latitude"
        private val LONGITUDE = "longitude"

        /**
         * 全部媒体数据 - SELECTION_ARGS
         */
        private val ALL_SELECTION_ARGS = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())

        /**
         * 全部媒体数据 - PROJECTION
         */
        private val ALL_PROJECTION = arrayOf(MediaStore.Images.Media._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT,
                LATITUDE,
                LONGITUDE,
                DURATION)

        /**
         * 全部媒体数据 - SELECTION
         */
        private val ALL_SELECTION = (
                MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO
                        + " AND "
                        + MediaStore.Files.FileColumns.SIZE + ">0"
                        + " AND "
                        + DURATION + ">0")


        /**
         * 图片 - PROJECTION
         */
        private val IMAGE_PROJECTION = arrayOf(MediaStore.Images.Media._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT,
                LATITUDE,
                LONGITUDE)

        /**
         * 图片 - SELECTION
         */
        private val IMAGE_SELECTION = (
                MediaStore.Images.Media.MIME_TYPE + "=? or " +
                        MediaStore.Images.Media.MIME_TYPE + "=?"
                        + " or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?"
                        + " AND "
                        + MediaStore.MediaColumns.WIDTH +
                        ">0"
                )

        /**
         * 图片 - SELECTION_ARGS
         */
        private val IMAGE_SELECTION_ARGS = arrayOf("image/jpeg", "image/png", "image/webp")

        /**
         * 视频 - PROJECTION
         */
        private val VIDEO_PROJECTION = arrayOf(MediaStore.Images.Media._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT,
                LATITUDE,
                LONGITUDE,
                DURATION)

        /**
         * 视频 - SELECTION
         */
        private val VIDEO_SELECTION = (
                MediaStore.Images.Media.MIME_TYPE + "=?"
                        + " AND "
                        + MediaStore.MediaColumns.WIDTH + ">0"
                        + " AND "
                        + DURATION + ">0"
                )

        /**
         * 视频 - SELECTION_ARGS
         */
        private val VIDEO_SELECTION_ARGS = arrayOf("video/mp4")

        /**
         * 音频 - PROJECTION
         */
        private val AUDIO_PROJECTION = arrayOf(MediaStore.Images.Media._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE,
                DURATION)

        /**
         * 音频 - SELECTION
         */
        private val AUDIO_SELECTION = (
                MediaStore.Images.Media.MIME_TYPE + "=?"
                        + " AND "
                        + DURATION + ">0"
                )

        /**
         * 音频 - SELECTION_ARGS
         */
        private val AUDIO_SELECTION_ARGS = arrayOf("audio/wav")

        /**
         * 获取全部图片和视频，但过滤掉gif图片
         */
        private val SELECTION_NOT_GIF = (
                MediaStore.Images.Media.MIME_TYPE + "=?"
                        + " OR "
                        + MediaStore.Images.Media.MIME_TYPE + "=?"
                        + " OR "
                        + MediaStore.Images.Media.MIME_TYPE + "=?"
                        + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                        + " AND "
                        + MediaStore.MediaColumns.SIZE + ">0"
                        + " AND "
                        + MediaStore.MediaColumns.WIDTH + ">0"
                )

        private val SELECTION_NOT_GIF_ARGS = arrayOf("image/jpeg", "image/png", "image/webp", MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())

        private val ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC"
    }
}
