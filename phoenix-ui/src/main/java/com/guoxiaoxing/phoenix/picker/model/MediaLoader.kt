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
                                    ALL_QUERY_URI,
                                    ALL_PROJECTION,
                                    ALL_SELECTION,
                                    null,
                                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC")
                            PhoenixConstant.TYPE_IMAGE -> CursorLoader(
                                    activity,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    IMAGE_PROJECTION, if (isGif) IMAGE_WITH_GIF_SELECTION else IMAGE_SELECTION,
                                    if (isGif) IMAGE_WITH_GIF_SELECTION_ARGS else IMAGE_SELECTION_ARGS,
                                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC")
                            PhoenixConstant.TYPE_VIDEO -> CursorLoader(
                                    activity,
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                    VIDEO_PROJECTION,
                                    VIDEO_SELECTION,
                                    VIDEO_SELECTION_ARGS, MediaStore.Files.FileColumns.DATE_ADDED + " DESC")
                            else ->
                                CursorLoader(
                                        activity, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                        ALL_PROJECTION, if (videoS > 0)
                                    DURATION + " <= ? and "
                                            + DURATION + ">" + AUDIO_DURATION
                                else
                                    DURATION + "> " + AUDIO_DURATION, if (videoS > 0)
                                    arrayOf(videoS.toString())
                                else
                                    null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC")

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
                                    val path = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[1]))
                                    // 如原图路径不存在或者路径存在但文件不存在,就结束当前循环
                                    if (TextUtils.isEmpty(path) || !File(path).exists()) {
                                        continue
                                    }
                                    val mimeType = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[4]))
                                    var fileType = 0
                                    var duration = 0L
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
        private val LATITUDE = "latitude"
        private val LONGITUDE = "longitude"

        private val ALL_SELECTION = (
                MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        + " AND "
                        + MediaStore.Files.FileColumns.SIZE + ">0")

        private val ALL_SELECTION_ARGS = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())

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
         * 只查询图片条件
         */
        private val IMAGE_WITH_GIF_SELECTION =
                "(" + MediaStore.Images.Media.MIME_TYPE + "=? or " +
                        MediaStore.Images.Media.MIME_TYPE + "=?" + " or " +
                        MediaStore.Images.Media.MIME_TYPE + "=?" + " or " +
                        MediaStore.Images.Media.MIME_TYPE + "=?)" + " AND " +
                        MediaStore.MediaColumns.WIDTH +
                        ">0"

        private val IMAGE_WITH_GIF_SELECTION_ARGS = arrayOf("image/jpeg", "image/png", "image/gif", "image/webp")

        /**
         * 获取全部图片
         */
        private val IMAGE_SELECTION =
                "(" + MediaStore.Images.Media.MIME_TYPE + "=? or " +
                        MediaStore.Images.Media.MIME_TYPE + "=?" + " or " +
                        MediaStore.Images.Media.MIME_TYPE + "=?)" + " AND " +
                        MediaStore.MediaColumns.WIDTH +
                        ">0"
        /**
         * 获取全部图片
         */
        private val IMAGE_SELECTION_ARGS = arrayOf("image/jpeg", "image/png", "image/webp")

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
         * 获取全部视频
         */
        private val VIDEO_SELECTION =
                "(" + MediaStore.Images.Media.MIME_TYPE + "=?)" + " AND " +
                        MediaStore.MediaColumns.WIDTH +
                        ">0"
        /**
         * 获取全部视频
         */
        private val VIDEO_SELECTION_ARGS = arrayOf("video/mp4")


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
