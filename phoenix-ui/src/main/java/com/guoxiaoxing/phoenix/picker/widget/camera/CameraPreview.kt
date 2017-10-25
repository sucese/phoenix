package com.guoxiaoxing.phoenix.picker.widget.camera

import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.SurfaceView
import java.util.*

class CameraPreview : SurfaceView {

    private val TAG = this.javaClass.simpleName
    private var mScaleGestureDetetor: ScaleGestureDetector? = null
    private var mCarmera: Camera? = null
    private var mMaxZoom: Int = 0
    private var mIsZoomSupport: Boolean = false
    private var mScaleFactor = 1
    private lateinit var mFocusArea: Camera.Area
    private var mFoucusAreas: MutableList<Camera.Area>? = null
    private var mIsFocus: Boolean = false
    private var mIsFocusReady: Boolean = false
    private var mLastTouchX: Float = 0.toFloat()
    private var mLastTouchY: Float = 0.toFloat()
    private var mActivePointId = INVALID_POINTER_ID

    constructor(context: Context) : super(context) {
        setup(context)
    }

    private fun setup(context: Context) {
        mScaleGestureDetetor = ScaleGestureDetector(context, ScaleListener())
        mFocusArea = Camera.Area(Rect(), 1000)
        mFoucusAreas = ArrayList<Camera.Area>()
        mFoucusAreas!!.add(mFocusArea)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setup(context)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var height = MeasureSpec.getSize(heightMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        if (isPortrait) {
            if (width > height * ASPECT_RATIO) {
                width = (height * ASPECT_RATIO + 0.5).toInt()
            } else {
                height = (width / ASPECT_RATIO + 0.5).toInt()
            }
        } else {
            if (height > width * ASPECT_RATIO) {
                height = (width * ASPECT_RATIO + 0.5).toInt()
            } else {
                width = (height / ASPECT_RATIO + 0.5).toInt()
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mScaleGestureDetetor!!.onTouchEvent(event)
        val action = event.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                Log.v(TAG, "down")
                mIsFocus = true
                mLastTouchX = event.x
                mLastTouchY = event.y
                mActivePointId = event.getPointerId(0)
            }
            MotionEvent.ACTION_UP -> {
                Log.v(TAG, "up_" + mIsFocus + "_" + mIsFocusReady)
                // atuo focus
                if (mIsFocus && mIsFocusReady) {
                    //                    handleFocus(mCarmera.getParameters());
                }
                mActivePointId = INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                Log.v(TAG, "action_pointer_down")
                mCarmera!!.cancelAutoFocus()
                mIsFocus = false
            }
            MotionEvent.ACTION_CANCEL -> {
                Log.v(TAG, "action_cancel")
                mActivePointId = INVALID_POINTER_ID
            }
        }
        return true
    }

    fun setCarmera(mCarmera: Camera?) {
        this.mCarmera = mCarmera
        if (mCarmera != null) {
            val params = mCarmera.parameters
            mIsZoomSupport = params.isZoomSupported
            if (mIsZoomSupport) {
                mMaxZoom = params.maxZoom
            }
        }

    }

    //    private void handleFocus(Camera.Parameters parameters) {
    //        float x = mLastTouchX;
    //        float y = mLastTouchY;
    //        if (!setFocusBound(x, y)) return;
    //        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
    //        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
    //            parameters.setFocusAreas(mFoucusAreas);
    //            //自动聚焦
    //            parameters.setFlashMode(Camera.Parameters.FOCUS_MODE_AUTO);
    //            mCarmera.setParameters(parameters);
    //            mCarmera.autoFocus(new Camera.AutoFocusCallback() {
    //                @Override
    //                public void onAutoFocus(boolean success, Camera enableCamera) {
    //
    //                }
    //            });
    //        }
    //    }

    private fun handlerZoom(params: Camera.Parameters) {
        var zoom = params.zoom
        if (mScaleFactor == ZOOM_IN) {
            if (zoom < mMaxZoom) zoom += ZOOM_DELTE
        } else if (mScaleFactor == ZOOM_OUT) {
            if (zoom > 0) zoom -= ZOOM_DELTE
        }
        params.zoom = zoom
        mCarmera!!.parameters = params

    }

    private fun setFocusBound(x: Float, y: Float): Boolean {
        val left = (x - FOCUS_SQR_SIZE / 2).toInt()
        val right = (x + FOCUS_SQR_SIZE / 2).toInt()
        val top = (y - FOCUS_SQR_SIZE / 2).toInt()
        val bottom = (y + FOCUS_SQR_SIZE / 2).toInt()
        if (left < FOCUS_MIN_BOUND || left > FOCUS_MAX_BOUND) return false
        if (right < FOCUS_MIN_BOUND || right > FOCUS_MAX_BOUND) return false
        if (top < FOCUS_MIN_BOUND || top > FOCUS_MAX_BOUND) return false
        if (bottom < FOCUS_MIN_BOUND || top > FOCUS_MAX_BOUND) return false
        mFocusArea!!.rect.set(left, right, top, bottom)
        return true
    }

    fun setmIsFocusReady(mIsFocusReady: Boolean) {
        this.mIsFocusReady = mIsFocusReady
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor = detector.scaleFactor.toInt()
            handlerZoom(mCarmera!!.parameters)
            return true
        }
    }

    companion object {
        private val INVALID_POINTER_ID = -1
        private val ASPECT_RATIO = 3.0 / 4.0

        private val ZOOM_OUT = 0
        private val ZOOM_IN = 1
        private val ZOOM_DELTE = 1

        private val FOCUS_SQR_SIZE = 100
        private val FOCUS_MAX_BOUND = 1000
        private val FOCUS_MIN_BOUND = -FOCUS_MAX_BOUND
    }
}
