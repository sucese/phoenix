package com.guoxiaoxing.phoenix.picture.edit.widget.hierarchy

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.support.v4.util.ArrayMap
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.guoxiaoxing.phoenix.picker.listener.CustomGestureDetector
import com.guoxiaoxing.phoenix.picker.listener.OnPhotoRectUpdateListener
import com.guoxiaoxing.phoenix.picker.model.HierarchyCache
import com.guoxiaoxing.phoenix.picker.model.HierarchyEditResult
import com.guoxiaoxing.phoenix.picker.model.SaveStateMarker
import com.guoxiaoxing.phoenix.picker.util.recycleBitmap
import com.guoxiaoxing.phoenix.picker.util.setInt

/**
 * Base Layer for all function layer draw,redraw ,undo and restore
 *  Flowing properties is very important:
 *  1. [drawMatrix]
 *  2. [saveStateMap]
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
abstract class BaseHierarchyView<T : SaveStateMarker> : View, HierarchyTransformer
        , OnPhotoRectUpdateListener, HierarchyCacheNode {
    /*support matrix for drawing layerView*/
    open val drawMatrix: Matrix
        get() {
            val matrix = Matrix()
            matrix.set(supportMatrix)
            matrix.postConcat(rootLayerMatrix)
            return matrix
        }
    val supportMatrix = Matrix()
    val rootLayerMatrix = Matrix()
    val validateRect = RectF()
    /*support drawing*/
    var displayBitmap: Bitmap? = null
    var displayCanvas: Canvas? = null
    /*saveState Info*/
    var saveStateMap = ArrayMap<String, T>()
    /*gesture*/
    lateinit var gestureDetector: CustomGestureDetector
    val adInterpolator = AccelerateDecelerateInterpolator()
    /*paint*/
    lateinit var maskPaint: Paint
    /*operation*/
    open var isLayerInEditMode = false
    var unitMatrix: Matrix = Matrix()
    var viewIsLayout = false

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

    fun initView(context: Context) {
        gestureDetector = CustomGestureDetector(context, this)
        //maskPaint
        maskPaint = Paint()
        maskPaint.style = Paint.Style.FILL
        maskPaint.isAntiAlias = true
        maskPaint.color = Color.BLACK
        initSupportView(context)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recycleBitmap(displayBitmap)
        displayCanvas = null
    }

    /**
     * 2 part
     *  1. draw layer bitmap
     *  2. drawMask view to cover data out of root layer
     */
    override fun onDraw(canvas: Canvas) {
//        displayCanvas?.let {
//            drawMask(it)
//        }
        //drawDisplay
        displayBitmap?.let {
            if (clipRect()) {
                canvas.save()
                canvas.clipRect(validateRect)
                canvas.drawBitmap(it, drawMatrix, null)
                canvas.restore()
            } else {
                canvas.drawBitmap(it, drawMatrix, null)
            }
        }
        //drawExtra
        canvas.save()
        canvas.matrix = drawMatrix
        drawMask(canvas)
        canvas.matrix = unitMatrix
        canvas.restore()
    }

    open fun clipRect() = true

    open fun drawMask(canvas: Canvas) {
//        val layerRect = RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat())
//        val diffs = MatrixUtils.diffRect(layerRect, validateRect)
//        for (rect in diffs) {
//            canvas.drawRect(MatrixUtils.mapInvertMatrixRect(drawMatrix, rect), maskPaint)
//        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (validateRect.isEmpty) {
            validateRect.setInt(left, top, right, bottom)
        }
        viewIsLayout = true
    }

    override fun onPhotoRectUpdate(rect: RectF, matrix: Matrix) {
        validateRect.set(rect)
        rootLayerMatrix.set(matrix)
        redrawOnPhotoRectUpdate()
    }

    fun genDisplayCanvas() {
        displayBitmap ?: let {
            displayBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            displayCanvas = Canvas(displayBitmap)
        }
    }

    override fun resetEditorSupportMatrix(matrix: Matrix) {
        supportMatrix.set(matrix)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isLayerInEditMode) {
            return checkInterceptedOnTouchEvent(event) && gestureDetector.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }

    override fun onDrag(dx: Float, dy: Float, x: Float, y: Float, rootLayer: Boolean) {

    }

    override fun onScale(scaleFactor: Float, focusX: Float, focusY: Float, rootLayer: Boolean) {
    }

    /*OverBound translate dx and dy */
    inner class OverBoundRunnable(val dx: Float, val dy: Float) : Runnable {
        val mStartTime = System.currentTimeMillis()
        val mZoomDuration = 300
        var mLastDiffX = 0f
        var mLastDiffY = 0f

        override fun run() {
            val t = interpolate()
            val ddx = t * dx - mLastDiffX
            val ddy = t * dy - mLastDiffY
            onDrag(-ddx, -ddy, -1f, -1f, false)
            mLastDiffX = t * dx
            mLastDiffY = t * dy
            if (t < 1f) {
                ViewCompat.postOnAnimation(this@BaseHierarchyView, this)
            }
        }

        private fun interpolate(): Float {
            var t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration
            t = Math.min(1f, t)
            t = adInterpolator.getInterpolation(t)
            return t
        }

    }

    /**
     * open fun for intercept touch event or not.
     * if intercept this layer will handle it,other wise do nothing
     */
    open fun checkInterceptedOnTouchEvent(event: MotionEvent): Boolean {
        return true
    }

    open fun onStartCompose() {

    }

    open fun redrawOnPhotoRectUpdate() {
        invalidate()
    }

    fun redrawAllCache() {
        if (!saveStateMap.isEmpty) {
            genDisplayCanvas()
        }
        displayCanvas?.let {
            it.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            drawAllCachedState(it)
        }
        postInvalidate()
    }

    /**
     * invalidate all cached data
     */
    abstract fun drawAllCachedState(canvas: Canvas)

    open fun initSupportView(context: Context) {

    }

    /**
     * ui element undo clicked
     */
    open fun revoke() {

    }
    //region of save and restore data

    open fun getEditorResult() = HierarchyEditResult(supportMatrix, displayBitmap)

    //cache layer data.
    override fun saveLayerData(output: MutableMap<String, HierarchyCache>) {
        output.put(getLayerTag(), HierarchyCache(ArrayMap<String, T>(saveStateMap)))
    }

    override fun restoreLayerData(input: MutableMap<String, HierarchyCache>) {
        val lastCache = input[getLayerTag()]
        lastCache?.let {
            val restore = lastCache.hierarchyCache as ArrayMap<String, T>
            for (key in restore.keys) {
                val value = restore[key]
                value?.let {
                    saveStateMap.put(key, it.deepCopy() as T)
                }
            }
            if (viewIsLayout) {
                redrawAllCache()
            } else {
                addOnLayoutChangeListener(OnLayerLayoutListener())
            }
        }

    }


    inner class OnLayerLayoutListener : OnLayoutChangeListener {
        override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            redrawAllCache()
            removeOnLayoutChangeListener(this)
        }

    }
}