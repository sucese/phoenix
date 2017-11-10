package com.guoxiaoxing.phoenix.picker.widget.videoview

import android.annotation.SuppressLint
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
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.picker.widget.dialog.PhoenixLoadingDialog

class PhoenixVideoView : RelativeLayout {

    private lateinit var videoView: InternalVideoView
    private lateinit var seekbarProgress: SeekBar
    private lateinit var ivPlay: ImageView
    private lateinit var tvCurrentProgress: TextView
    private lateinit var tvTotalProgress: TextView
    private lateinit var ivCenterPlay: ImageView

    private lateinit var llController: LinearLayout
    private lateinit var flContainer: FrameLayout
    private lateinit var mAudioManager: AudioManager
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private lateinit var mContext: Context
    private lateinit var videoLayout: View
    private lateinit var mActivity: Activity
    private var videoPos: Int = 0
    private var state = 0
    private var mVideoPath: String? = null
    private lateinit var loadingDialog: PhoenixLoadingDialog

    private val volumeReceiver by lazy { VolumeReceiver() }

    constructor(context: Context) : super(context, null)

    @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        this.mContext = context
        setupView()
        setupData()
        setupListener()
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if(hasWindowFocus){
            onResume()
        }else{
            onPause()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onDestory()
    }

    fun register(activity: Activity) {
        this.mActivity = activity
    }

    fun setVideoPath(path: String) {
        this.mVideoPath = path
        if (path.startsWith("http") || path.startsWith("https")) {
            videoView.setVideoURI(Uri.parse(path))
        } else {
            videoView.setVideoPath(mVideoPath)
        }
    }

    fun onPause() {
        videoPos = videoView.currentPosition
        videoView.stopPlayback()
        mHandler.removeMessages(UPDATE_PROGRESS)
        ivPlay.setImageResource(R.drawable.phoenix_video_play_center)
    }

    fun onResume() {
        videoView.seekTo(videoPos)
        videoView.resume()
    }

    fun onDestory() {
        videoView.stopPlayback()
        mContext.unregisterReceiver(volumeReceiver)
    }

    fun seekTo(position: Int) {
        videoView.seekTo(position)
    }

    private fun setupView() {
        videoLayout = LayoutInflater.from(mContext).inflate(R.layout.view_phoenix_video, this, true)
        videoView = videoLayout.findViewById(R.id.video_view) as InternalVideoView
        seekbarProgress = videoLayout.findViewById(R.id.seekbar_progress) as SeekBar
        ivPlay = videoLayout.findViewById(R.id.iv_play) as ImageView
        tvCurrentProgress = videoLayout.findViewById(R.id.tv_currentProgress) as TextView
        tvTotalProgress = videoLayout.findViewById(R.id.tv_totalProgress) as TextView
        llController = videoLayout.findViewById(R.id.ll_controller) as LinearLayout
        flContainer = videoLayout.findViewById(R.id.rl_container) as FrameLayout
        ivCenterPlay = findViewById(R.id.iv_center_play) as ImageView
    }

    private fun setupData() {
        screenWidth = getScreenWidth(mContext)
        screenHeight = getScreenHeight(mContext)
        mAudioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        mContext.registerReceiver(volumeReceiver, filter)
    }

    private fun setupListener() {
        ivCenterPlay.setOnClickListener {
            videoView.start()
            mHandler.sendEmptyMessage(UPDATE_PROGRESS)
            ivCenterPlay.visibility = View.GONE
            llController.visibility = View.VISIBLE
            ivPlay.setImageResource(R.drawable.phoenix_video_pause)
        }

        ivPlay.setOnClickListener {
            if (videoView.isPlaying) {
                ivPlay.setImageResource(R.drawable.phoenix_video_play_center)
                videoView.pause()
                mHandler.removeMessages(UPDATE_PROGRESS)
                ivCenterPlay.visibility = View.VISIBLE
            } else {
                ivPlay.setImageResource(R.drawable.phoenix_video_pause)
                videoView.start()
                mHandler.sendEmptyMessage(UPDATE_PROGRESS)
                if (state == 0) state = 1
                ivCenterPlay.visibility = View.GONE
            }
        }

        videoView.setOnCompletionListener {
            ivPlay.setImageResource(R.drawable.phoenix_video_play_center)
            ivCenterPlay.visibility = View.VISIBLE
            llController.visibility = View.GONE
        }

        videoView.setStateListener(object : InternalVideoView.StateListener {

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

        seekbarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateTextViewFormat(tvCurrentProgress, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // 暂停刷新
                mHandler.removeMessages(UPDATE_PROGRESS)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (state != 0) {
                    mHandler.sendEmptyMessage(UPDATE_PROGRESS)
                }
                videoView.seekTo(seekBar.progress)
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

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == UPDATE_PROGRESS) {

                // 获取当前时间
                val currentTime = videoView.currentPosition
                // 获取总时间
                val totalTime = videoView.duration - 100
                if (currentTime >= totalTime) {
                    videoView.pause()
                    videoView.seekTo(0)
                    seekbarProgress.progress = 0
                    ivPlay.setImageResource(R.drawable.phoenix_video_play_center)
                    updateTextViewFormat(tvCurrentProgress, 0)
                    this.removeMessages(UPDATE_PROGRESS)
                } else {
                    seekbarProgress.max = totalTime
                    seekbarProgress.progress = currentTime
                    updateTextViewFormat(tvCurrentProgress, currentTime)
                    updateTextViewFormat(tvTotalProgress, totalTime)
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
        seekbarProgress.progressDrawable = drawable
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
     * show loading loadingDialog
     */
    protected fun showLoadingDialog() {
        dismissLoadingDialog()
        loadingDialog = PhoenixLoadingDialog(context)
        loadingDialog.show()
    }

    /**
     * dismiss loading loadingDialog
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

    internal inner class VolumeReceiver : BroadcastReceiver() {
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
