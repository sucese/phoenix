package com.guoxiaoxing.phoenix.picker.ui.camera

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Camera
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.picker.ui.BaseFragment
import com.guoxiaoxing.phoenix.picker.widget.camera.CameraPreView

import java.io.IOException
import java.util.HashMap

class CameraFragment : BaseFragment(), SurfaceHolder.Callback, Camera.PictureCallback, View.OnClickListener
        , SensorEventListener {

    private var ivFlash: ImageView? = null
    private var ivTakePic: ImageView? = null
    private var tvCancel: TextView? = null
    private var tvFinish: TextView? = null
    private var tvModel: TextView? = null
    private var ivModel: ImageView? = null
    private var frCameraHint: FrameLayout? = null
    private var tvCameraHint: TextView? = null
    private var ivCameraHint: ImageView? = null
    private var ivCameraRotate: ImageView? = null
    private var confirmDialog: ConfirmDialog? = null

    private var mCameraID: Int = 0
    private var mFlashMode: String? = null
    private var mCamera: Camera? = null
    private var mCameraPreview: CameraPreView? = null
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mIsSafeToTakePhoto: Boolean = false
    private var mOrientationListener: CameraOrientationListener? = null
    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null

    private var handler: Handler? = null
    private var isPopTips = false
    private var maxPictureNumber = -1
    private val map = HashMap<Int, CarHint>()

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
        return inflater!!.inflate(R.layout.fragment_camera, container, false)
    }

    fun setIsSafeToTakePhoto(isSafeToTakePhoto: Boolean) {
        this.mIsSafeToTakePhoto = isSafeToTakePhoto
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        confirmDialog = ConfirmDialog(activity)
        maxPictureNumber = option.maxSelectNum
        try {
            frCameraHint = view!!.findViewById(R.id.camera_fr_hint) as FrameLayout
            handler = object : Handler() {
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
                            frCameraHint!!.rotation = todegree.toFloat()
                            ivModel!!.rotation = todegree.toFloat()
                        }
                    }
                }
            }
            //start oritation listener
            mOrientationListener!!.enable()
            mCameraPreview = view.findViewById(R.id.camera_preview) as CameraPreView
            mCameraPreview!!.holder.addCallback(this)
            val observer = mCameraPreview!!.viewTreeObserver
            observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mCameraParameter!!.mPreviewWidth = mCameraPreview!!.width
                    mCameraParameter!!.mPreviewHeight = mCameraPreview!!.height
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mCameraPreview!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    } else {
                        mCameraPreview!!.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    }
                }
            })

            ivFlash = view.findViewById(R.id.camera_iv_flash) as ImageView
            ivTakePic = view.findViewById(R.id.camera_iv_take_picture) as ImageView
            tvCancel = view.findViewById(R.id.camera_tv_cancel) as TextView
            tvFinish = view.findViewById(R.id.camera_tv_compelete) as TextView
            tvModel = view.findViewById(R.id.camera_tv_model) as TextView
            ivModel = view.findViewById(R.id.camera_iv_model) as ImageView
            tvCameraHint = view.findViewById(R.id.camera_tv_hint) as TextView
            ivCameraHint = view.findViewById(R.id.camera_iv_hint) as ImageView
            frCameraHint = view.findViewById(R.id.camera_fr_hint) as FrameLayout
            ivCameraRotate = view.findViewById(R.id.camera_iv_rotate) as ImageView

            if (option.isEnableCameraHint) {
                frCameraHint!!.visibility = View.VISIBLE
            }

            if (option.isEnableCameraModel) {
                ivModel!!.visibility = View.VISIBLE
                tvModel!!.visibility = View.VISIBLE
            }

            setupCameraHint()
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
        mSensorManager!!.unregisterListener(this)
    }

    fun setupListener() {
        tvFinish!!.setOnClickListener(this)
        ivTakePic!!.setOnClickListener(this)
        tvCancel!!.setOnClickListener(this)
        ivFlash!!.setOnClickListener(this)
        tvModel!!.setOnClickListener(this)
        ivCameraRotate!!.setOnClickListener(this)

        mSensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun setupCameraHint() {
        val index = CameraActivity.cameraList.size
        if (option.isEnableCameraHint) {
            if (index < 12 && index >= 0) {
                val carHint = map[index]
                if(carHint == null){
                    return
                }
                ivCameraHint!!.setImageResource(carHint.res)
                tvCameraHint!!.text = carHint.text
                frCameraHint!!.visibility = View.VISIBLE
                frCameraHint!!.visibility = View.VISIBLE
            } else {
                frCameraHint!!.visibility = View.GONE
                frCameraHint!!.visibility = View.GONE
            }
        } else {
            frCameraHint!!.visibility = View.GONE
            frCameraHint!!.visibility = View.GONE
        }
    }

    private fun takePicture() {
        val index = 0
        if (index < maxPictureNumber && maxPictureNumber > 0 || maxPictureNumber == 0) {
            if (mIsSafeToTakePhoto) {
                setIsSafeToTakePhoto(false)
                mOrientationListener!!.rememberOrientation()
                val shutterCallback: Camera.ShutterCallback? = null
                val raw: Camera.PictureCallback? = null
                val postView: Camera.PictureCallback? = null
                mCamera!!.takePicture(shutterCallback, raw, postView, this)
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
            if (!confirmDialog!!.isShowing)
                confirmDialog!!.show()
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
        fragmentManager.beginTransaction().replace(R.id.fragment_root, CameraEditFragment.newInstance(data, rotation, mCameraParameter!!.crateCopy()))
                .addToBackStack(null).commitAllowingStateLoss()
        setIsSafeToTakePhoto(true)
    }

    val photoRotation: Int
        get() {
            val rotation: Int
            val orientation = mOrientationListener!!.rememberedNormalOrientation
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
            if (TextUtils.equals(tvModel!!.text, getString(R.string.text_hide_model))) {
                tvModel!!.text = getString(R.string.text_show_model)
                ivModel!!.visibility = View.GONE
            } else {
                tvModel!!.text = getString(R.string.text_hide_model)
                ivModel!!.visibility = View.VISIBLE
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
                    mCamera!!.setPreviewDisplay(mSurfaceHolder)
                    mCamera!!.startPreview()
                    setIsSafeToTakePhoto(true)
                    setCameraFocusReady(true)
                }
                return true
            } catch (e: IOException) {
                if (!confirmDialog!!.isShowing) {
                    confirmDialog!!.show()
                }
                return false
            }

        } else {
            if (!confirmDialog!!.isShowing) {
                confirmDialog!!.show()
            }
        }
        return false
    }

    private fun getCamera(cameraID: Int): Boolean {
        try {
            mCamera = Camera.open(cameraID)
            mCameraPreview!!.setCarmera(mCamera)
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
                mCamera!!.release()
                mCamera = null
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
            mCamera!!.stopPreview()
        mCameraPreview!!.setCarmera(null)
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
        val displayOrientation = cameraInfo.orientation
        mCameraParameter!!.setmDisplayOrientation(displayOrientation)
        try {
            mCamera!!.setDisplayOrientation(mCameraParameter!!.getmDisplayOrientation())
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.v(TAG, "deteminieDisplayOrientation error")
            return false
        }

    }

    private fun setCameraFocusReady(isFocusReady: Boolean) {
        if (this.mCameraPreview != null) {
            mCameraPreview!!.setmIsFocusReady(isFocusReady)
        }
    }


    override fun onStop() {
        super.onStop()
        mOrientationListener!!.disable()
        //释放相机资源
        try {
            stopCameraPreview()
            mCamera!!.release()
            mCamera = null
        } catch (e: Exception) {
            Log.v(TAG, "onstop:" + e.message)
        }

    }

    override fun onResume() {
        super.onResume()
        restartCameraPreview()
        mOrientationListener!!.enable()
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
                        val message = handler!!.obtainMessage()
                        message.what = 1
                        curDegree = mCurrentNormalizedOrientation
                        message.obj = curDegree
                        handler!!.sendMessage(message)
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

            if (degrees > 45 && degrees <= 135) {
                return 90
            }

            if (degrees > 135 && degrees <= 225) {
                return 180
            }

            if (degrees > 225 && degrees <= 315) {
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
            ivFlash!!.visibility = View.GONE
        } else {
            try {
                ivFlash!!.visibility = View.VISIBLE
                if (mCamera!!.parameters.flashMode == Camera.Parameters.FLASH_MODE_OFF) {
                    mFlashMode = Camera.Parameters.FLASH_MODE_OFF
                    ivFlash!!.setImageResource(R.drawable.phoenix_splash_close)
                } else {
                    mFlashMode = Camera.Parameters.FLASH_MODE_ON
                    ivFlash!!.setImageResource(R.drawable.phoenix_splash_open)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun setFlashView() {
        if (ivFlash!!.visibility == View.VISIBLE) {
            try {
                if (TextUtils.equals(mFlashMode, Camera.Parameters.FLASH_MODE_OFF)) {
                    mFlashMode = Camera.Parameters.FLASH_MODE_ON
                    mCamera!!.parameters.flashMode = mFlashMode
                    ivFlash!!.setImageResource(R.drawable.phoenix_splash_open)
                } else {
                    mFlashMode = Camera.Parameters.FLASH_MODE_OFF
                    mCamera!!.parameters.flashMode = mFlashMode
                    ivFlash!!.setImageResource(R.drawable.phoenix_splash_close)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun setupCamera() {
        try {
            if (mCamera != null) {
                val parameters = mCamera!!.parameters
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
                    mCamera!!.parameters = parameters
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
        private var mCameraParameter: CameraParameter? = null
        private var curDegree: Int = 0


        fun newInstance(): CameraFragment {
            return CameraFragment()
        }
    }
}
