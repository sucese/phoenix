package com.guoxiaoxing.phoenix.picker.widget.videoview

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView

import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.picker.widget.dialog.PhoenixLoadingDialog
import kotlinx.android.synthetic.main.view_phoenix_video.view.*

class PhoenixVideoView : RelativeLayout {

    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var videoPos: Int = 0
    private var state = 0
    private var mVideoPath: String? = null
    private val isVerticalScreen = true

    private lateinit var mAudioManager: AudioManager
    private lateinit var loadingDialog: PhoenixLoadingDialog

    constructor(context: Context) : super(context, null) {}

    @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        this.mContext = context
        setupData()
        setupListener()
    }

    fun register(activity: Activity) {
        this.mActivity = activity
    }

    fun setVideoPath(path: String) {
        this.mVideoPath = path
        if (path.startsWith("http") || path.startsWith("https")) {
            video_view.setVideoURI(Uri.parse(path))
        } else {
            video_view.setVideoPath(mVideoPath)
        }
    }

    fun onPause() {
        videoPos = video_view.currentPosition
        video_view.stopPlayback()
        mHandler.removeMessages(UPDATE_PROGRESS)
    }

    fun onResume() {
        video_view.seekTo(videoPos)
        video_view.resume()
    }

    fun onDestory() {

    }

    fun seekTo(position: Int) {
        video_view.seekTo(position)
    }

    private fun setupData() {
        screenWidth = getScreenWidth(mContext)
        screenHeight = getScreenHeight(mContext)
        mAudioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        mContext.registerReceiver(MyVolumeReceiver(), filter)
        val currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    private fun setupListener() {

        //        btnScreen.setOnClickListener(new OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        //                if (isVerticalScreen) {
        //                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //                } else {
        //                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //                }
        //            }
        //        });

        iv_play.setOnClickListener {
            video_view.start()
            mHandler.sendEmptyMessage(UPDATE_PROGRESS)
            iv_play.visibility = View.GONE
            ll_controller.visibility = View.VISIBLE
            btn_controller.setImageResource(R.drawable.phoenix_video_pause)
        }

        btn_controller.setOnClickListener {
            if (video_view.isPlaying) {
                btn_controller.setImageResource(R.drawable.phoenix_video_play)
                video_view.pause()
                mHandler.removeMessages(UPDATE_PROGRESS)
                iv_play.visibility = View.VISIBLE
            } else {
                btn_controller.setImageResource(R.drawable.phoenix_video_pause)
                video_view.start()
                mHandler.sendEmptyMessage(UPDATE_PROGRESS)
                if (state == 0) state = 1
                iv_play.visibility = View.GONE
            }
        }

        video_view.setOnCompletionListener {
            btn_controller.setImageResource(R.drawable.phoenix_video_play)
            iv_play.visibility = View.VISIBLE
            ll_controller.visibility = View.GONE
        }

        video_view.setStateListener(object : InternalVideoView.StateListener {

            override fun changeVolumn(detlaY: Float) {
                val maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                val index = (detlaY / screenHeight * maxVolume.toFloat() * 3f).toInt()
                val volume = Math.max(0, currentVolume - index)
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
            }

            override fun changeBrightness(detlaX: Float) {
                val wml = mActivity.window.attributes
                var screenBrightness = wml.screenBrightness
                val index = detlaX / screenWidth.toFloat() / 3f
                screenBrightness += index
                if (screenBrightness > 1.0f) {
                    screenBrightness = 1.0f
                } else if (screenBrightness < 0.01f) {
                    screenBrightness = 0.01f
                }
                wml.screenBrightness = screenBrightness
                mActivity.window.attributes = wml
            }

            override fun hideHint() {

            }
        })

        seekbar_progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateTextViewFormat(tv_currentProgress, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // 暂停刷新
                mHandler.removeMessages(UPDATE_PROGRESS)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (state != 0) {
                    mHandler.sendEmptyMessage(UPDATE_PROGRESS)
                }
                video_view.seekTo(seekBar.progress)
            }
        })
    }

    /**
     * 屏幕状态改变

     * @param newConfig newConfig
     */
    public override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    /**
     * 格式化时间进度
     */
    private fun updateTextViewFormat(tv: TextView, m: Int) {

        val result: String
        // 毫秒转成秒
        val second = m / 1000
        val hour = second / 3600
        val minute = second % 3600 / 60
        val ss = second % 60

        if (hour != 0) {
            result = String.format("%02d:%02d:%02d", hour, minute, ss)
        } else {
            result = String.format("%02d:%02d", minute, ss)
        }
        tv.text = result
    }

    private val mHandler = object : Handler() {

        override fun handleMessage(msg: Message) {

            if (msg.what == UPDATE_PROGRESS) {

                // 获取当前时间
                val currentTime = video_view.currentPosition
                // 获取总时间
                val totalTime = video_view.duration - 100
                if (currentTime >= totalTime) {
                    video_view.pause()
                    video_view.seekTo(0)
                    seekbar_progress.progress = 0
                    btn_controller.setImageResource(R.drawable.phoenix_video_play)
                    updateTextViewFormat(tv_currentProgress, 0)
                    this.removeMessages(UPDATE_PROGRESS)
                } else {
                    seekbar_progress.max = totalTime
                    seekbar_progress.progress = currentTime
                    updateTextViewFormat(tv_currentProgress, currentTime)
                    updateTextViewFormat(tv_totalProgress, totalTime)
                    this.sendEmptyMessageDelayed(UPDATE_PROGRESS, 100)
                }
            }
        }
    }

    /**
     * 设置播放进度条样式

     * @param drawable drawable
     */
    fun setProgressBg(drawable: Drawable) {
        seekbar_progress.progressDrawable = drawable
    }

    fun getScreenWidth(context: Context): Int {

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.heightPixels
    }

    fun dipToPx(context: Context, dipValue: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue.toFloat(), context
                .resources.displayMetrics).toInt()
    }

    /**
     * show loading dialog
     */
    protected fun showLoadingDialog() {
        dismissLoadingDialog()
        loadingDialog = PhoenixLoadingDialog(context)
        loadingDialog.show()
    }

    /**
     * dismiss loading dialog
     */
    protected fun dismissLoadingDialog() {
        try {
            if (loadingDialog != null && loadingDialog.isShowing) {
                loadingDialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    internal inner class MyVolumeReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            //如果音量发生变化则更改seekbar的位置
            if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
                // 当前的媒体音量
                val currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            }
        }
    }

    companion object {

        private val UPDATE_PROGRESS = 1
    }
}
