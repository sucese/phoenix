package com.guoxiaoxing.phoenix.picture.edit.widget.crop

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.guoxiaoxing.phoenix.picker.listener.CustomGestureDetector
import com.guoxiaoxing.phoenix.picker.listener.GestureDetectorListener
import com.guoxiaoxing.phoenix.picker.listener.OnPhotoRectUpdateListener
import com.guoxiaoxing.phoenix.picker.util.MatrixUtils
import com.guoxiaoxing.phoenix.picture.edit.layer.CropWindowHelper

class CropView : View, GestureDetectorListener, OnPhotoRectUpdateListener {

    private var mBackgroundColor = DEFAULT_BG_COLOR
    private var mGuidelineColor = DEFAULT_GUIDE_LINE_COLOR
    private var mBorderlineColor = DEFAULT_BORDER_LINE_COLOR
    private var mGuidelineStrokeWidth = DEFAULT_GUIDE_LINE_WIDTH
    private var mBorderlineWidth = DEFAULT_BORDER_LINE_WIDTH
    private var mBorderCornerLength = 0
    private var mBorderCornerOffset = 0
    private lateinit var mGuidelinePaint: Paint
    private lateinit var mBorderlinePaint: Paint
    private lateinit var mBorderCornerPaint: Paint
    private lateinit var mPaintTranslucent: Paint
    private val mViewRect: RectF = RectF()
    private lateinit var mScaleDragDetector: CustomGestureDetector
    private lateinit var mCropWindowHelper: CropWindowHelper
    private val mBgPath = Path()
    /*当前view绘制crop的区域。*/
    private var mDrawingRect: RectF = RectF()
    /*限制cropWindow 通过 mValidateBorderRect，max,min with,or height...*/
    private var mValidateBorderRect: RectF = RectF()
    private var mCropViewIsUpdated = false
    var onCropViewUpdatedListener: OnCropViewUpdatedListener? = null
    private var mLastRotateDegree = 0f

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(context)
    }

    private fun initView(context: Context) {
        //mDrawingRect = RectF(200f, 200f, 1000f, 1000f)//test
        val mCropTouchSlop = MatrixUtils.dp2px(context, 15f)
        //init cropWindowHelper.
        mCropWindowHelper = CropWindowHelper(mCropTouchSlop.toFloat())
        mCropWindowHelper.setEdge(mDrawingRect)
        //mValidateBorderRect = RectF(0f, 0f, 1080f, 1920f)//test
        val minCrop = MatrixUtils.dp2px(context, 60f)
        mCropWindowHelper.minCropWindowWidth = minCrop.toFloat()
        mCropWindowHelper.minCropWindowHeight = minCrop.toFloat()
        //touchEventSupport.
        mScaleDragDetector = CustomGestureDetector(context, this, false)
        //paint
        mBorderlinePaint = getBorderPaint(mBorderlineWidth, mBorderlineColor)
        mGuidelinePaint = getBorderPaint(mGuidelineStrokeWidth, mGuidelineColor)
        mBorderCornerPaint = getBorderPaint(mBorderlineWidth * 3, mBorderlineColor)
        //bgPath
        mPaintTranslucent = getBorderPaint(mGuidelineStrokeWidth, mBackgroundColor)
        mPaintTranslucent.style = Paint.Style.FILL
        //inner border
        mBorderCornerLength = MatrixUtils.dp2px(context, 20f)
        mBorderCornerOffset = MatrixUtils.dp2px(context, 3f)
    }

    override fun onDraw(canvas: Canvas) {
        if (mDrawingRect.width() <= 0) {
            return
        }
        //drawBgPath
        drawBgPath(canvas)
        //drawBorder
        canvas.drawRect(mDrawingRect, mBorderlinePaint)
        //Draw 2 vertical and 2 horizontal guidelines
        drawGuideLines(canvas)
        //drawCorners
        drawCorners(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action and MotionEvent.ACTION_MASK
        if (action == MotionEvent.ACTION_DOWN) {
            val intercept = mCropWindowHelper.interceptTouchEvent(event)
            if (!intercept) {
                return false
            }
        } else if (action == MotionEvent.ACTION_UP) {
            mCropWindowHelper.resetTouchEvent(event)
        }
        return mScaleDragDetector.onTouchEvent(event)
    }

    private fun getBorderPaint(thickness: Float, color: Int): Paint {
        val borderPaint = Paint()
        borderPaint.color = color
        borderPaint.strokeWidth = thickness
        borderPaint.style = Paint.Style.STROKE
        borderPaint.isAntiAlias = true
        return borderPaint
    }

    private fun drawBgPath(canvas: Canvas) {
        mBgPath.reset()
        mBgPath.addRect(mViewRect, Path.Direction.CW)
        mBgPath.addRect(mDrawingRect, Path.Direction.CCW)
        canvas.drawPath(mBgPath, mPaintTranslucent)
    }

    private fun drawGuideLines(canvas: Canvas) {
        val rect = mDrawingRect
        val oneThirdCropWidth = rect.width() / 3
        val oneThirdCropHeight = rect.height() / 3
        // Draw vertical guidelines.
        val x1 = rect.left + oneThirdCropWidth
        val x2 = rect.right - oneThirdCropWidth
        canvas.drawLine(x1, rect.top, x1, rect.bottom, mGuidelinePaint)
        canvas.drawLine(x2, rect.top, x2, rect.bottom, mGuidelinePaint)
        // Draw horizontal guidelines.
        val y1 = rect.top + oneThirdCropHeight
        val y2 = rect.bottom - oneThirdCropHeight
        canvas.drawLine(rect.left, y1, rect.right, y1, mGuidelinePaint)
        canvas.drawLine(rect.left, y2, rect.right, y2, mGuidelinePaint)

    }

    private fun drawCorners(canvas: Canvas) {
        val rect = mDrawingRect
        val cornerOffset = mBorderCornerOffset
        // Top left
        canvas.drawLine(rect.left + cornerOffset, rect.top + cornerOffset, rect.left + cornerOffset, rect.top + mBorderCornerLength + cornerOffset, mBorderCornerPaint)
        canvas.drawLine(rect.left + cornerOffset, rect.top + cornerOffset, rect.left + mBorderCornerLength + cornerOffset, rect.top + cornerOffset, mBorderCornerPaint)
        // Top right
        canvas.drawLine(rect.right - cornerOffset, rect.top + cornerOffset, rect.right - cornerOffset, rect.top + mBorderCornerLength + cornerOffset, mBorderCornerPaint)
        canvas.drawLine(rect.right - cornerOffset, rect.top + cornerOffset, rect.right - mBorderCornerLength - cornerOffset, rect.top + cornerOffset, mBorderCornerPaint)
        // Bottom left
        canvas.drawLine(rect.left + cornerOffset, rect.bottom - cornerOffset, rect.left + cornerOffset, rect.bottom - mBorderCornerLength - cornerOffset, mBorderCornerPaint)
        canvas.drawLine(rect.left + cornerOffset, rect.bottom - cornerOffset, rect.left + mBorderCornerLength + cornerOffset, rect.bottom - cornerOffset, mBorderCornerPaint)
        // Bottom left
        canvas.drawLine(rect.right - cornerOffset, rect.bottom - cornerOffset, rect.right - cornerOffset, rect.bottom - mBorderCornerLength - cornerOffset, mBorderCornerPaint)
        canvas.drawLine(rect.right - cornerOffset, rect.bottom - cornerOffset, rect.right - mBorderCornerLength - cornerOffset, rect.bottom - cornerOffset, mBorderCornerPaint)
    }

    override fun onDrag(dx: Float, dy: Float, x: Float, y: Float, rootLayer: Boolean) {
        val feedBack = mCropWindowHelper.onCropWindowDrag(dx, dy, mValidateBorderRect)
        if (feedBack) {
            mDrawingRect.set(mCropWindowHelper.getEdge())
            invalidate()
            notifyCropViewUpdated()
        }
    }

    override fun onScale(scaleFactor: Float, focusX: Float, focusY: Float, rootLayer: Boolean) {

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if(mViewRect.isEmpty){
            mViewRect.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
        }
        if(mValidateBorderRect.isEmpty){
            mValidateBorderRect.set(mViewRect)
        }
    }

    override fun onPhotoRectUpdate(rect: RectF, matrix: Matrix) {
        val validateBorder = rect
        if (validateBorder.left < mViewRect.left) {
            validateBorder.left = mViewRect.left
        }
        if (validateBorder.top < mViewRect.top) {
            validateBorder.top = mViewRect.top
        }
        if (validateBorder.right > mViewRect.right) {
            validateBorder.right = mViewRect.right
        }
        if (validateBorder.bottom > mViewRect.bottom) {
            validateBorder.bottom = mViewRect.bottom
        }
        rotateCropWindow(MatrixUtils.getMatrixDegree(matrix))//rotate.
        mValidateBorderRect.set(validateBorder)
        val boundsChanged = mCropWindowHelper.checkCropWindowBounds(mValidateBorderRect)
        if (boundsChanged) {
            mDrawingRect.set(mCropWindowHelper.getEdge())
            invalidate()
        }
        notifyCropViewUpdated()
    }

    private fun notifyCropViewUpdated() {
        mCropViewIsUpdated = true
        onCropViewUpdatedListener?.onCropViewUpdated()
    }

    private fun rotateCropWindow(rotateDegree: Float) {
        val degree = rotateDegree - mLastRotateDegree
        if (degree == 0f) {
            return
        }
        mLastRotateDegree = rotateDegree
        val matrix = Matrix()
        val result = RectF()
        matrix.postRotate(degree % 360, mDrawingRect.centerX(), mDrawingRect.centerY())
        matrix.postTranslate(mViewRect.centerX() - mDrawingRect.centerX(), mViewRect.centerY() - mDrawingRect.centerY())
        matrix.mapRect(result, mDrawingRect)
        //exchange
        if (degree % 90 == 0f) {
            val oldHeight = mCropWindowHelper.maxCropWindowHeight
            mCropWindowHelper.maxCropWindowHeight = mCropWindowHelper.maxCropWindowWidth
            mCropWindowHelper.maxCropWindowWidth = oldHeight
        }
        updateDrawingRect(result, true)
    }

    private fun updateDrawingRect(rect: RectF, notifyUpdated: Boolean) {
        mDrawingRect.set(rect)
        mCropWindowHelper.setEdge(mDrawingRect)
        invalidate()
        if (notifyUpdated) notifyCropViewUpdated()
    }

    fun setupDrawingRect(rect: RectF) {
        mValidateBorderRect.set(rect)
        updateDrawingRect(rect, false)
        mCropViewIsUpdated = false
    }

    fun updateCropMaxSize(maxWidth: Float, maxHeight: Float) {
        mCropWindowHelper.maxCropWindowHeight = maxHeight
        mCropWindowHelper.maxCropWindowWidth = maxWidth
    }

    fun clearDrawingRect() {
        setupDrawingRect(RectF())
    }

    fun getCropRect() = RectF(mDrawingRect)

    fun isCropWindowEdit() = mCropViewIsUpdated

    companion object {
        private val DEFAULT_BG_COLOR = Color.parseColor("#99000000")
        private val DEFAULT_GUIDE_LINE_COLOR = Color.WHITE
        private val DEFAULT_BORDER_LINE_COLOR = Color.WHITE
        private val DEFAULT_GUIDE_LINE_WIDTH = 2.0f
        private val DEFAULT_BORDER_LINE_WIDTH = 2.0f

    }

    interface OnCropViewUpdatedListener {
        fun onCropViewUpdated()
    }

}