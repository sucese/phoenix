package com.guoxiaoxing.phoenix.picker.ui

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import android.widget.Toast

import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.picker.SCPicker
import com.guoxiaoxing.phoenix.core.PhoenixOption
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant
import com.guoxiaoxing.phoenix.core.listener.OnPickerListener
import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.core.model.MimeType
import com.guoxiaoxing.phoenix.core.util.ReflectUtils
import com.guoxiaoxing.phoenix.picker.model.MediaFolder
import com.guoxiaoxing.phoenix.picker.util.AttrsUtils
import com.guoxiaoxing.phoenix.picker.util.DateUtils
import com.guoxiaoxing.phoenix.picker.util.DoubleUtils
import com.guoxiaoxing.phoenix.picker.util.PictureFileUtils
import com.guoxiaoxing.phoenix.picker.widget.dialog.PhoenixLoadingDialog

import java.io.File
import java.util.ArrayList

import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


open class BaseActivity : FragmentActivity() {

    protected lateinit var mContext: Context
    protected lateinit var option: PhoenixOption

    protected var spanCount: Int = 0
    protected var maxSelectNum: Int = 0
    protected var minSelectNum: Int = 0
    protected var compressQuality: Int = 0
    protected var selectionMode: Int = 0
    protected var fileType: Int = 0
    protected var videoSecond: Int = 0
    protected var compressMaxKB: Int = 0
    protected var compressWidth: Int = 0
    protected var compressHeight: Int = 0
    protected var aspect_ratio_x: Int = 0
    protected var aspect_ratio_y: Int = 0
    protected var recordVideoSecond: Int = 0
    protected var videoQuality: Int = 0
    protected var cropWidth: Int = 0
    protected var cropHeight: Int = 0
    protected var isGif: Boolean = false
    protected var enableCamera: Boolean = false
    protected var enablePreview: Boolean = false
    protected var enableCrop: Boolean = false
    protected var enableCompress: Boolean = false
    protected var enPreviewVideo: Boolean = false
    protected var checkNumMode: Boolean = false
    protected var openClickSound: Boolean = false
    protected var numComplete: Boolean = false
    protected var freeStyleCropEnabled: Boolean = false
    protected var circleDimmedLayer: Boolean = false
    protected var hideBottomControls: Boolean = false
    protected var rotateEnabled: Boolean = false
    protected var scaleEnabled: Boolean = false
    protected var previewEggs: Boolean = false
    protected var statusFont: Boolean = false
    protected var showCropFrame: Boolean = false
    protected var showCropGrid: Boolean = false
    protected var previewStatusFont: Boolean = false
    protected var enableUpload: Boolean = false

    protected var cameraPath: String = ""
    protected lateinit var outputCameraPath: String

    protected var originalPath: String = ""
    protected val dialog: PhoenixLoadingDialog by lazy { PhoenixLoadingDialog(mContext) }

    protected var enableDelete: Boolean = false
    protected var currentIndex: Int = 0
    protected lateinit var mediaList: MutableList<MediaEntity>
    protected lateinit var onPickerListener: OnPickerListener

    override fun onCreate(savedInstanceState: Bundle?) {
        option = SCPicker.with()
        when (option.theme) {
            PhoenixOption.THEME_DEFAULT -> setTheme(R.style.phoenix_style_default)
            PhoenixOption.THEME_RED -> setTheme(R.style.phoenix_style_red)
            PhoenixOption.THEME_ORANGE -> setTheme(R.style.phoenix_style_orange)
            PhoenixOption.THEME_BLUE -> setTheme(R.style.picker_style_blue)
        }
        super.onCreate(savedInstanceState)
        mContext = this
        setupConfig()
    }

    /**
     * 获取配置参数
     */
    private fun setupConfig() {
        enableCamera = option.isEnableCamera
        outputCameraPath = option.outputCameraPath
        statusFont = AttrsUtils.getTypeValueBoolean(this, R.attr.phoenix_status_font_color)
        previewStatusFont = AttrsUtils.getTypeValueBoolean(this, R.attr.phoenix_preview_status_font_color)
        fileType = option.fileType
        enableDelete = option.isEnableDelete
        currentIndex = option.currentIndex
        mediaList = option.mediaList
        if (mediaList == null) {
            mediaList = ArrayList<MediaEntity>()
        }
        selectionMode = option.selectionMode
        if (selectionMode == PhoenixConstant.SINGLE) {
            mediaList = ArrayList<MediaEntity>()
        }
        spanCount = option.imageSpanCount
        isGif = option.isEnableGif
        freeStyleCropEnabled = option.isFreeStyleCropEnabled
        maxSelectNum = option.maxSelectNum
        minSelectNum = option.minSelectNum
        enablePreview = option.isEnablePreview
        enPreviewVideo = option.isEnPreviewVideo
        checkNumMode = option.isCheckNumMode
        openClickSound = option.isOpenClickSound
        videoSecond = option.videoSecond
        enableCrop = option.isEnableCrop
        enableCompress = option.isEnableCompress
        compressQuality = option.cropCompressQuality
        numComplete = AttrsUtils.getTypeValueBoolean(this, R.attr.phoenix_style_number_complete)
        compressMaxKB = option.compressMaxSize
        compressWidth = option.compressMaxWidth
        compressHeight = option.compressMaxHeight
        recordVideoSecond = option.recordVideoSecond
        videoQuality = option.videoQuality
        cropWidth = option.cropWidth
        cropHeight = option.cropHeight
        aspect_ratio_x = option.aspect_ratio_x
        aspect_ratio_y = option.aspect_ratio_y
        circleDimmedLayer = option.isCircleDimmedLayer
        showCropFrame = option.isShowCropFrame
        showCropGrid = option.isShowCropGrid
        rotateEnabled = option.isRotateEnabled
        scaleEnabled = option.isScaleEnabled
        previewEggs = option.isPreviewEggs
        hideBottomControls = option.isHideBottomControls

        enableUpload = option.isEnableUpload
        onPickerListener = option.onPickerListener
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(PhoenixConstant.BUNDLE_CAMERA_PATH, cameraPath)
        outState.putString(PhoenixConstant.BUNDLE_ORIGINAL_PATH, originalPath)
    }

    protected fun startActivity(clz: Class<*>, bundle: Bundle) {
        if (!DoubleUtils.isFastDoubleClick) {
            val intent = Intent()
            intent.setClass(this, clz)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    protected fun startActivity(clz: Class<*>, bundle: Bundle, requestCode: Int) {
        if (!DoubleUtils.isFastDoubleClick) {
            val intent = Intent()
            intent.setClass(this, clz)
            intent.putExtras(bundle)
            startActivityForResult(intent, requestCode)
        }
    }

    protected open fun showToast(msg: String) {
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show()
    }

    /**
     * show loading dialog
     */
    protected fun showLoadingDialog() {
        if (!isFinishing) {
            dismissLoadingDialog()
            dialog.show()
        }
    }

    /**
     * dismiss loading dialog
     */
    protected fun dismissLoadingDialog() {
        try {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    protected fun processMedia(mediaList: MutableList<MediaEntity>) {

        val enableCompress = option.isEnableCompress
        val enableUpload = option.isEnableUpload

        if (!enableCompress && !enableUpload) {
            onResult(mediaList)
        }

        //压缩图片
        val compressPictureProcessor = ReflectUtils.loadProcessor(ReflectUtils.PictureCompressProcessor)
        //压缩视频
        val compressVideoProcessor = ReflectUtils.loadProcessor(ReflectUtils.VideoCompressProcessor)

        val result = ArrayList<MediaEntity>(mediaList.size)

        Observable.create(ObservableOnSubscribe<MediaEntity> { e ->
            for (mediaEntity in mediaList) {
                //压缩
                if (enableCompress) {
                    if (mediaEntity.fileType == MimeType.ofImage()) {
                        compressPictureProcessor?.syncProcess(mContext, mediaEntity, option)
                    } else if (mediaEntity.fileType == MimeType.ofVideo()) {
                        compressVideoProcessor?.syncProcess(mContext, mediaEntity, option)
                    }

                }
                e.onNext(mediaEntity)
            }
            e.onComplete()
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<MediaEntity> {
                    override fun onSubscribe(d: Disposable) {
                        showLoadingDialog()
                    }

                    override fun onNext(mediaEntity: MediaEntity) {
                        result.add(mediaEntity)
                    }

                    override fun onError(e: Throwable) {
                        dismissLoadingDialog()
                    }

                    override fun onComplete() {
                        dismissLoadingDialog()
                        onResult(result)
                    }
                })

    }

    /**
     * 判断拍照 图片是否旋转
     * @param degree degree
     * *
     * @param file   file
     */
    protected fun rotateImage(degree: Int, file: File) {
        if (degree > 0) {
            // 针对相片有旋转问题的处理方式
            try {
                val opts = BitmapFactory.Options()//获取缩略图显示到屏幕上
                opts.inSampleSize = 2
                val bitmap = BitmapFactory.decodeFile(file.absolutePath, opts)
                val bmp = PictureFileUtils.rotaingImageView(degree, bitmap)
                PictureFileUtils.saveBitmapFile(bmp, file)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


    /**
     * compressPicture or callback
     * @param result
     */
    protected fun handlerResult(result: MutableList<MediaEntity>) {
        onResult(result)
    }


    /**
     * 如果没有任何相册，先创建一个最近相册出来
     * @param folders
     */
    protected fun createNewFolder(folders: MutableList<MediaFolder>) {
        if (folders.size == 0) {
            // 没有相册 先创建一个最近相册出来
//            val newFolder = MediaFolder()
            val newFolder = MediaFolder("", "", "", 0, 0, true, ArrayList())
            val folderName = if (fileType == MimeType.ofAudio())
                getString(R.string.picture_all_audio)
            else
                getString(R.string.picture_camera_roll)
            newFolder.name = folderName
            newFolder.path = ""
            newFolder.firstImagePath = ""
            folders.add(newFolder)
        }
    }

    /**
     * 将图片插入到相机文件夹中
     * @param path         path
     * *
     * @param imageFolders imageFolders
     * *
     * @return MediaFolder
     */
    protected fun getImageFolder(path: String, imageFolders: MutableList<MediaFolder>): MediaFolder {
        val imageFile = File(path)
        val folderFile = imageFile.parentFile

        for (folder in imageFolders) {
            if (folder.name == folderFile.name) {
                return folder
            }
        }
        val newFolder = MediaFolder("", "", "", 0, 0, true, ArrayList())
//        val newFolder = MediaFolder()
        newFolder.name = folderFile.name
        newFolder.path = folderFile.absolutePath
        newFolder.firstImagePath = path
        imageFolders.add(newFolder)
        return newFolder
    }

    /**
     * return image result
     * @param images images
     */
    protected fun onResult(images: MutableList<MediaEntity>) {
        dismissLoadingDialog()
        if (enableCamera
                && selectionMode == PhoenixConstant.MULTIPLE
                && mediaList != null) {
            images.addAll(mediaList)
        }
        onPickerListener.onPickSuccess(images)
        closeActivity()
    }

    /**
     * Close Activity
     */
    protected open fun closeActivity() {
        finish()
        overridePendingTransition(0, R.anim.phoenix_activity_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoadingDialog()
        dismissLoadingDialog()
    }


    /**
     * 获取DCIM文件下最新一条拍照记录
     * @return
     */
    protected fun getLastImageId(eqVideo: Boolean): Int {
        try {
            //selection: 指定查询条件
            val absolutePath = PictureFileUtils.dcimCameraPath
            val ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC"
            val selection = if (eqVideo)
                MediaStore.Video.Media.DATA + " like ?"
            else
                MediaStore.Images.Media.DATA + " like ?"
            //定义selectionArgs：
            val selectionArgs = arrayOf(absolutePath + "%")
            val imageCursor = this.contentResolver.query(if (eqVideo)
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                    selection, selectionArgs, ORDER_BY)
            if (imageCursor.moveToFirst()) {
                val id = imageCursor.getInt(if (eqVideo)
                    imageCursor.getColumnIndex(MediaStore.Video.Media._ID)
                else
                    imageCursor.getColumnIndex(MediaStore.Images.Media._ID))
                val date = imageCursor.getLong(if (eqVideo)
                    imageCursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                else
                    imageCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED))
                val duration = DateUtils.dateDiffer(date)
                imageCursor.close()
                // DCIM文件下最近时间30s以内的图片，可以判定是最新生成的重复照片
                return if (duration <= 30) id else -1
            } else {
                return -1
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }

    }

    /**
     * 删除部分手机 拍照在DCIM也生成一张的问题
     * @param id
     * *
     * @param eqVideo
     */
    protected fun removeImage(id: Int, eqVideo: Boolean) {
        try {
            val cr = contentResolver
            val uri = if (eqVideo)
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val selection = if (eqVideo)
                MediaStore.Video.Media._ID + "=?"
            else
                MediaStore.Images.Media._ID + "=?"
            cr.delete(uri,
                    selection,
                    arrayOf(java.lang.Long.toString(id.toLong())))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 录音
     * @param data
     */
    protected fun isAudio(data: Intent?) {
        if (data != null && fileType == MimeType.ofAudio()) {
            try {
                val uri = data.data
                val audioPath: String
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    audioPath = uri.path
                } else {
                    audioPath = getAudioFilePathFromUri(uri)
                }
                PictureFileUtils.copyAudioFile(audioPath, cameraPath)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 获取刚录取的音频文件
     * @param uri uri
     * *
     * @return return
     */
    protected fun getAudioFilePathFromUri(uri: Uri): String {
        var path = ""
        try {
            val cursor = contentResolver
                    .query(uri, null, null, null, null)
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)
            path = cursor.getString(index)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return path
    }

    companion object {

        private val TAG = "BaseActivity"
    }
}