package com.guoxiaoxing.phoenix.picker.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.Toast

import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.picker.SCPicker
import com.guoxiaoxing.phoenix.core.PhoenixOption
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant
import com.guoxiaoxing.phoenix.core.listener.OnPickerListener
import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.core.model.MimeType
import com.guoxiaoxing.phoenix.core.util.ReflectUtils
import com.guoxiaoxing.phoenix.picker.util.AttrsUtils
import com.guoxiaoxing.phoenix.picker.widget.dialog.PhoenixLoadingDialog

import java.util.ArrayList

import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.

 * @author guoxiaoxing
 * *
 * @since 2017/8/12 下午3:19
 */
open class BaseFragment : Fragment() {

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
    protected var enableCameraModel: Boolean = false

    protected var cameraPath: String? = null
    protected lateinit var outputCameraPath: String

    protected var originalPath: String? = null
    protected var dialog: PhoenixLoadingDialog? = null
    protected var compressDialog: PhoenixLoadingDialog? = null

    protected var enableDelete: Boolean = false
    protected var currentIndex: Int = 0
    protected var mediaList: List<MediaEntity>? = null
    protected lateinit var onPickerListener: OnPickerListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = context
        option = SCPicker.with()
        setupConfig()
        onPickerListener = option.onPickerListener
    }

    /**
     * compressPicture loading dialog
     */
    protected fun showLoadingDialog() {
        if (!activity.isFinishing) {
            dismissLoadingDialog()
            compressDialog = PhoenixLoadingDialog(activity)
            compressDialog!!.show()
        }
    }

    /**
     * dismiss compressPicture dialog
     */
    protected fun dismissLoadingDialog() {
        try {
            if (!activity.isFinishing
                    && compressDialog != null
                    && compressDialog!!.isShowing) {
                compressDialog!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    protected fun processMedia(mediaList: List<MediaEntity>) {

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

    protected fun showToast(msg: String) {
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show()
    }

    /**
     * Close Activity
     */
    protected fun closeActivity() {
        activity.finish()
        activity.overridePendingTransition(0, R.anim.phoenix_activity_out)
    }

    /**
     * 获取配置参数
     */
    private fun setupConfig() {
        enableCamera = option.isEnableCamera
        outputCameraPath = option.outputCameraPath
        statusFont = AttrsUtils.getTypeValueBoolean(activity, R.attr.phoenix_status_font_color)
        previewStatusFont = AttrsUtils.getTypeValueBoolean(activity, R.attr.phoenix_preview_status_font_color)
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
        numComplete = AttrsUtils.getTypeValueBoolean(activity, R.attr.phoenix_style_number_complete)
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
        enableCameraModel = option.isEnableCameraModel
        hideBottomControls = option.isHideBottomControls

        enableUpload = option.isEnableUpload
        onPickerListener = option.onPickerListener
    }

    private fun onResult(mediaList: List<MediaEntity>) {
        onPickerListener.onPickSuccess(mediaList)
        activity.finish()
    }
}
