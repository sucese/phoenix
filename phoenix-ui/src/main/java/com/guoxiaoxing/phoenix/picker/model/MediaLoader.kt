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
import com.guoxiaoxing.phoenix.picker.util.DebugUtil
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
                                    IMAGE_PROJECTION, if (isGif) CONDITION_GIF else CONDITION,
                                    if (isGif) SELECT_GIF else SELECT,
                                    IMAGE_PROJECTION[0] + " DESC")
                            PhoenixConstant.TYPE_VIDEO -> CursorLoader(
                                    activity,
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                    VIDEO_PROJECTION, if (videoS > 0)
                                DURATION + " <= ? and "
                                        + DURATION + "> 0"
                            else
                                DURATION + "> 0", if (videoS > 0)
                                arrayOf(videoS.toString())
                            else
                                null, VIDEO_PROJECTION[0] + " DESC")
                            else ->
                                CursorLoader(
                                        activity, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                        AUDIO_PROJECTION, if (videoS > 0)
                                    DURATION + " <= ? and "
                                            + DURATION + ">" + AUDIO_DURATION
                                else
                                    DURATION + "> " + AUDIO_DURATION, if (videoS > 0)
                                    arrayOf(videoS.toString())
                                else
                                    null, AUDIO_PROJECTION[0] + " DESC")

                        }
                        return cursorLoader
                    }

                    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
                        try {
                            val imageFolders = ArrayList<MediaFolder>()
//                            val allImageFolder = MediaFolder()
                            val allImageFolder = MediaFolder("", "", "", 0, 0, true, ArrayList())
                            val latelyImages = ArrayList<MediaEntity>()
                            if (data != null) {
                                val count = data.count
                                if (count > 0) {
                                    data.moveToFirst()
                                    do {
                                        val path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]))
                                        // 如原图路径不存在或者路径存在但文件不存在,就结束当前循环
                                        if (TextUtils.isEmpty(path) || !File(path).exists()) {
                                            continue
                                        }
                                        val mimeType = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[6]))
                                        val eqImg = mimeType.startsWith(PhoenixConstant.IMAGE)
                                        val duration = if (eqImg)
                                            0
                                        else
                                            data.getInt(data.getColumnIndexOrThrow(VIDEO_PROJECTION[7]))
                                        val w = if (eqImg)
                                            data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]))
                                        else
                                            0
                                        val h = if (eqImg)
                                            data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[5]))
                                        else
                                            0
                                        DebugUtil.i("media mime type:", mimeType)

                                        var fileType = 0
                                        if (mimeType.startsWith(PhoenixConstant.AUDIO)) {
                                            fileType = MimeType.ofAudio()
                                        } else if (mimeType.startsWith(PhoenixConstant.IMAGE)) {
                                            fileType = MimeType.ofImage()
                                        } else if (mimeType.startsWith(PhoenixConstant.VIDEO)) {
                                            fileType = MimeType.ofVideo()
                                        }

                                        val image = MediaEntity.newBuilder()
                                                .localPath(path)
                                                .duration(duration.toLong())
                                                .fileType(fileType)
                                                .mimeType(mimeType)
                                                .width(w)
                                                .height(h)
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
         * 图片
         */
        private val IMAGE_PROJECTION = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.SIZE)

        /**
         * 视频
         */
        private val VIDEO_PROJECTION = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATE_ADDED, MediaStore.Video.Media.WIDTH, MediaStore.Video.Media.HEIGHT, MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.DURATION)

        private val PROJECTION = arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DATE_ADDED, MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.SIZE, DURATION, MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.WIDTH, MediaStore.MediaColumns.HEIGHT)

        /**
         * 音频
         */
        private val AUDIO_PROJECTION = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.IS_MUSIC, MediaStore.Audio.Media.IS_PODCAST, MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.DURATION)

        /**
         * 查询全部图片和视频，并且过滤掉已损坏图片和视频
         */
        private val SELECTION_ALL = (
                MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)


        private val SELECTION_ALL_ARGS = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(), MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())

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
