package com.guoxiaoxing.phoenix.picker.ui.camera

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Toast
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.picker.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.android.synthetic.main.include_camera_bottom_tool.*
import kotlinx.android.synthetic.main.include_camera_hint.*
import kotlinx.android.synthetic.main.include_camera_top_tool.*
import java.io.IOException
import java.util.*

class CameraFragment : BaseFragment(), SurfaceHolder.Callback, Camera.PictureCallback, View.OnClickListener
        , SensorEventListener {

    private lateinit var confirmDialog: ConfirmDialog

    private var mCameraID: Int = 0
    private lateinit var mFlashMode: String
    private lateinit var mCamera: Camera
    private lateinit var mSurfaceHolder: SurfaceHolder
    private var mIsSafeToTakePhoto: Boolean = false
    private lateinit var mOrientationListener: CameraOrientationListener
    private lateinit var mSensorManager: SensorManager
    private lateinit var mAccelerometer: Sensor

    private lateinit var handler: Handler
    private var isPopTips = false
    private var maxPictureNumber = -1
    private val map = HashMap<Int, CarHint>()
    private lateinit var mCameraParameter: CameraParameter
    private var curDegree: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK
        mCameraParameter = CameraParameter()

        for (carHint in CarHint.values()) {
            map.put(carHint.index, carHint)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContext = context
        return inflater?.inflate(R.layout.fragment_camera, container, false)
    }

    fun setIsSafeToTakePhoto(isSafeToTakePhoto: Boolean) {
        this.mIsSafeToTakePhoto = isSafeToTakePhoto
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        confirmDialog = ConfirmDialog(activity)
        maxPictureNumber = option.maxPickNumber
        try {
            handler = @SuppressLint("HandlerLeak")
            object : Handler() {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    val what = msg.what
                    when (what) {
                        1 -> {
                            var todegree = msg.obj as Int
                            if (todegree == 270) {
                                todegree = 90
                            } else if (todegree == 90) {
                                todegree = -90
                            }
                            camera_fr_hint.rotation = todegree.toFloat()
                            camera_iv_model.rotation = todegree.toFloat()
                        }
                    }
                }
            }
            //start oritation listener
            mOrientationListener.enable()
            camera_preview.holder.addCallback(this)
            val observer = camera_preview.viewTreeObserver
            observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mCameraParameter.mPreviewWidth = camera_preview.width
                    mCameraParameter.mPreviewHeight = camera_preview.height
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        camera_preview.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    } else {
                        camera_preview.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    }
                }
            })
            setupListener()
        } catch (e: Exception) {
            Log.v("StickerView", "oncreateViewError")
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

    fun setupListener() {
        camera_tv_compelete.setOnClickListener(this)
        camera_iv_take_picture.setOnClickListener(this)
        camera_tv_cancel.setOnClickListener(this)
        camera_iv_flash.setOnClickListener(this)
        camera_tv_model.setOnClickListener(this)
        camera_iv_rotate.setOnClickListener(this)

        mSensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun takePicture() {
        val index = 0
        if (index < maxPictureNumber && maxPictureNumber > 0 || maxPictureNumber == 0) {
            if (mIsSafeToTakePhoto) {
                setIsSafeToTakePhoto(false)
                mOrientationListener.rememberOrientation()
                val shutterCallback: Camera.ShutterCallback? = null
                val raw: Camera.PictureCallback? = null
                val postView: Camera.PictureCallback? = null
                mCamera.takePicture(shutterCallback, raw, postView, this)
            }
        } else {
            if (mContext != null) {
                Toast.makeText(mContext, "最多可以拍摄" + maxPictureNumber + "张照片", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mSurfaceHolder = holder
        getCamera(mCameraID)
        if (mCamera == null) {
            if (!confirmDialog.isShowing)
                confirmDialog.show()
            return
        }
        startCameraPreview()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {


    }

    override fun onPictureTaken(data: ByteArray, camera: Camera) {
        val rotation = photoRotation
        fragmentManager.beginTransaction().replace(R.id.fragment_root, CameraEditFragment.newInstance(data, rotation, mCameraParameter.crateCopy()))
                .addToBackStack(null).commitAllowingStateLoss()
        setIsSafeToTakePhoto(true)
    }

    val photoRotation: Int
        get() {
            val rotation: Int
            val orientation = mOrientationListener.rememberedNormalOrientation
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(mCameraID, info)
            rotation = (info.orientation + orientation) % 360
            return rotation
        }

    override fun onClick(v: View) {
        val resId = v.id
        if (resId == R.id.camera_iv_take_picture) {
            takePicture()
        } else if (resId == R.id.camera_tv_cancel) {
            closeActivity()
        } else if (resId == R.id.camera_tv_compelete) {
            val onPickerListener = option.onPickerListener
            onPickerListener?.onPickSuccess(CameraActivity.cameraList)
            activity.finish()
        } else if (resId == R.id.camera_iv_flash) {
            setFlashView()
            setupCamera()
        } else if (resId == R.id.camera_tv_model) {
            if (TextUtils.equals(camera_tv_model.text, getString(R.string.text_hide_model))) {
                camera_tv_model.text = getString(R.string.text_show_model)
                camera_iv_model.visibility = View.GONE
            } else {
                camera_tv_model.text = getString(R.string.text_hide_model)
                camera_iv_model.visibility = View.VISIBLE
            }
        } else if (resId == R.id.camera_iv_rotate) {
            switchCamera()
        }
    }

    /**
     * open enableCamera preview
     */
    private fun startCameraPreview(): Boolean {
        val flag = detemineDisPlayOrientation()
        if (flag) {
            try {
                if (mCamera != null) {
                    setupCamera()
                    mCamera.setPreviewDisplay(mSurfaceHolder)
                    mCamera.startPreview()
                    setIsSafeToTakePhoto(true)
                    setCameraFocusReady(true)
                }
                return true
            } catch (e: IOException) {
                if (!confirmDialog.isShowing) {
                    confirmDialog.show()
                }
                return false
            }

        } else {
            if (!confirmDialog.isShowing) {
                confirmDialog.show()
            }
        }
        return false
    }

    private fun getCamera(cameraID: Int): Boolean {
        try {
            mCamera = Camera.open(cameraID)
            camera_preview.setCarmera(mCamera)
        } catch (e: Exception) {
            Log.e(TAG, "打不开 id = " + cameraID + "的相机")
            return false
        }

        return mCamera != null
    }

    private fun restartCameraPreview() {
        try {
            if (mCamera != null) {
                stopCameraPreview()
                mCamera.release()
            }
            getCamera(mCameraID)
            startCameraPreview()
        } catch (e: Exception) {
            Log.v(TAG, "restartCameraPreView:" + e.message)
        }

    }

    private fun stopCameraPreview() {
        setIsSafeToTakePhoto(false)
        setCameraFocusReady(false)
        if (mCamera != null)
            mCamera.stopPreview()
        camera_preview.setCarmera(null)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mOrientationListener = CameraOrientationListener(context)
    }

    /**
     * set enableCamera preview orientation
     */
    private fun detemineDisPlayOrientation(): Boolean {
        val cameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(mCameraID, cameraInfo)
        val displayOrientation = 90
        mCameraParameter.setmDisplayOrientation(displayOrientation)
        return try {
            mCamera.setDisplayOrientation(mCameraParameter.getmDisplayOrientation())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.v(TAG, "deteminieDisplayOrientation error")
            false
        }

    }

    private fun setCameraFocusReady(isFocusReady: Boolean) {
        if (this.camera_preview != null) {
            camera_preview.setmIsFocusReady(isFocusReady)
        }
    }


    override fun onStop() {
        super.onStop()
        mOrientationListener.disable()
        //释放相机资源
        try {
            stopCameraPreview()
            mCamera.release()
        } catch (e: Exception) {
            Log.v(TAG, "onstop:" + e.message)
        }

    }

    override fun onResume() {
        super.onResume()
        restartCameraPreview()
        mOrientationListener.enable()
    }

    override fun onSensorChanged(event: SensorEvent) {

    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    private inner class CameraOrientationListener(context: Context) : OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {

        private var mCurrentNormalizedOrientation: Int = 0
        private var mRememberedNormalOrientation: Int = 0


        override fun onOrientationChanged(orientation: Int) {
            if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation)

                if (!isPopTips && (mCurrentNormalizedOrientation == 0 || mCurrentNormalizedOrientation == 90)) {
                    showToast()
                    isPopTips = true
                }
                if (mCurrentNormalizedOrientation % 90 == 0) {
                    if (curDegree != mCurrentNormalizedOrientation) {
                        val message = handler.obtainMessage()
                        message.what = 1
                        curDegree = mCurrentNormalizedOrientation
                        message.obj = curDegree
                        handler.sendMessage(message)
                    }
                }
            }
        }

        fun showToast() {
            if (mContext != null) {
                val view = LayoutInflater.from(mContext).inflate(R.layout.item_toast, null)
                val toast = Toast(mContext)
                toast.view = view
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.duration = Toast.LENGTH_SHORT
                toast.show()
            }
        }


        private fun normalize(degrees: Int): Int {
            if (degrees > 315 || degrees <= 45) {
                return 0
            }

            if (degrees in 46..135) {
                return 90
            }

            if (degrees in 136..225) {
                return 180
            }

            if (degrees in 226..315) {
                return 270
            }

            throw RuntimeException("抱歉 没有检测出手机的方向")
        }

        fun rememberOrientation() {
            mRememberedNormalOrientation = mCurrentNormalizedOrientation
        }

        val rememberedNormalOrientation: Int
            get() {
                rememberOrientation()
                return mRememberedNormalOrientation
            }
    }

    fun setupFlashView(context: Context) {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            camera_iv_flash.visibility = View.GONE
        } else {
            try {
                camera_iv_flash.visibility = View.VISIBLE
                if (mCamera.parameters.flashMode == Camera.Parameters.FLASH_MODE_OFF) {
                    mFlashMode = Camera.Parameters.FLASH_MODE_OFF
                    camera_iv_flash.setImageResource(R.drawable.phoenix_splash_close)
                } else {
                    mFlashMode = Camera.Parameters.FLASH_MODE_ON
                    camera_iv_flash.setImageResource(R.drawable.phoenix_splash_open)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun setFlashView() {
        if (camera_iv_flash.visibility == View.VISIBLE) {
            try {
                if (TextUtils.equals(mFlashMode, Camera.Parameters.FLASH_MODE_OFF)) {
                    mFlashMode = Camera.Parameters.FLASH_MODE_ON
                    mCamera.parameters.flashMode = mFlashMode
                    camera_iv_flash.setImageResource(R.drawable.phoenix_splash_open)
                } else {
                    mFlashMode = Camera.Parameters.FLASH_MODE_OFF
                    mCamera.parameters.flashMode = mFlashMode
                    camera_iv_flash.setImageResource(R.drawable.phoenix_splash_close)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun setupCamera() {
        try {
            if (mCamera != null) {
                val parameters = mCamera.parameters
                if (parameters != null) {
                    val bestPreviewSize = detemiBestPreViewSize(parameters)
                    val bestPictureSize = detemiBestPictureSize(parameters)

                    parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height)
                    parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height)
                    if (parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                    }
                    val flashModes = parameters.supportedFlashModes
                    if (flashModes != null && flashModes.contains(mFlashMode)) {
                        parameters.flashMode = mFlashMode
                    }
                    mCamera.parameters = parameters
                    setupFlashView(activity)
                }
            }
        } catch (e: Exception) {
            //getParameters 有的会报 getParameters failed (empty parameters)
            //还没有找到原因
            e.printStackTrace()
        }

    }

    private fun detemiBestPreViewSize(parameters: Camera.Parameters): Camera.Size {
        return detemiBestSize(parameters.supportedPreviewSizes)
    }

    private fun detemiBestPictureSize(parameters: Camera.Parameters): Camera.Size {
        return detemiBestSize(parameters.supportedPictureSizes)
    }

    private fun detemiBestSize(sizes: List<Camera.Size>): Camera.Size {
        var bestSize: Camera.Size? = null
        var size: Camera.Size
        val numOfSizes = sizes.size
        for (i in 0..numOfSizes - 1) {
            size = sizes[i]
            val isDesireRatio = size.width / 4 == size.height / 3
            val isBeetterSize = bestSize == null || size.width > bestSize.width
            if (isDesireRatio && isBeetterSize) {
                bestSize = size
            }

        }
        if (bestSize == null) {
            return sizes[sizes.size - 1]
        }
        return bestSize
    }

    private fun switchCamera() {
        if (mCameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK
            restartCameraPreview()
        } else {
            mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT
            restartCameraPreview()
        }
    }

    companion object {
        val TAG = CameraFragment::class.java.simpleName

        fun newInstance(): CameraFragment {
            return CameraFragment()
        }
    }
}
