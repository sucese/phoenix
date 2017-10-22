package com.guoxiaoxing.phoenix.picker.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.Toast
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.core.PhoenixOption
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant
import com.guoxiaoxing.phoenix.core.listener.OnPickerListener
import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.core.model.MimeType
import com.guoxiaoxing.phoenix.core.util.ReflectUtils
import com.guoxiaoxing.phoenix.picker.Phoenix
import com.guoxiaoxing.phoenix.picker.util.AttrsUtils
import com.guoxiaoxing.phoenix.picker.widget.dialog.PhoenixLoadingDialog
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

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
    protected var selectionMode: Int = 0
    protected var fileType: Int = 0
    protected var videoSecond: Int = 0
    protected var compressMaxKB: Int = 0
    protected var compressWidth: Int = 0
    protected var compressHeight: Int = 0
    protected var recordVideoSecond: Int = 0
    protected var checkNumberMode: Boolean = false
    protected var isGif: Boolean = false
    protected var enableCamera: Boolean = false
    protected var enablePreview: Boolean = false
    protected var enableCompress: Boolean = false
    protected var checkNumMode: Boolean = false
    protected var openClickSound: Boolean = false
    protected var numComplete: Boolean = false
    protected var previewEggs: Boolean = false
    protected var statusFont: Boolean = false
    protected var previewStatusFont: Boolean = false
    protected var savePath: String = ""

    protected var originalPath: String = ""
    protected val loadingDialog: PhoenixLoadingDialog by lazy { PhoenixLoadingDialog(mContext) }

    protected lateinit var mediaList: MutableList<MediaEntity>
    protected var onPickerListener: OnPickerListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = context
        option = Phoenix.with()
        setupConfig()
        onPickerListener = option.onPickerListener
    }

    /**
     * show loading loadingDialog
     */
    protected fun showLoadingDialog() {
        if (!activity.isFinishing) {
            dismissLoadingDialog()
            loadingDialog.show()
        }
    }

    /**
     * dismiss loading loadingDialog
     */
    protected fun dismissLoadingDialog() {
        try {
            if (loadingDialog.isShowing) {
                loadingDialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected fun processMedia(mediaList: List<MediaEntity>) {

        val enableCompress = option.isEnableCompress
        if (!enableCompress) {
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
        statusFont = AttrsUtils.getTypeValueBoolean(activity, R.attr.phoenix_status_font_color)
        previewStatusFont = AttrsUtils.getTypeValueBoolean(activity, R.attr.phoenix_preview_status_font_color)
        fileType = option.fileType
        mediaList = option.pickedMediaList
        if (mediaList == null) {
            mediaList = ArrayList<MediaEntity>()
        }
        selectionMode = option.pickMode
        if (selectionMode == PhoenixConstant.SINGLE) {
            mediaList = ArrayList<MediaEntity>()
        }
        spanCount = option.spanCount
        isGif = option.isEnableGif
        maxSelectNum = option.maxPickNumber
        minSelectNum = option.minPickNumber
        enablePreview = option.isEnablePreview
        checkNumberMode = option.isPickNumberMode
        openClickSound = option.isEnableClickSound
        videoSecond = option.videoSecond
        enableCompress = option.isEnableCompress
        numComplete = AttrsUtils.getTypeValueBoolean(activity, R.attr.phoenix_style_number_complete)
        compressMaxKB = option.compressMaxSize
        compressWidth = option.compressMaxWidth
        compressHeight = option.compressMaxHeight
        recordVideoSecond = option.recordVideoSecond
        previewEggs = option.isPreviewEggs
        savePath = option.savePath
        onPickerListener = option.onPickerListener
    }

    private fun onResult(mediaList: List<MediaEntity>) {
        onPickerListener?.onPickSuccess(mediaList)
        activity.finish()
    }
}
