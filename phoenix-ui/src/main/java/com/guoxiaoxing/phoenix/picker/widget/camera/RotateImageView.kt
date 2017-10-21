package com.guoxiaoxing.phoenix.picker.widget.camera

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class RotateImageView : View {

    private var dstRect: RectF? = null
    private var bitmap: Bitmap? = null
    private val mMatrix = Matrix()// 辅助计算矩形

    @get:Synchronized var scale: Float = 0.toFloat()
        private set// 缩放比率
    @get:Synchronized var rotateAngle: Int = 0
        private set

    private val wrapRect = RectF()// 图片包围矩形

    private lateinit var originImageRect: RectF
    private var srcRect: Rect? = null
    private var mThisWidth: Int = 0
    private var mThisHeight: Int = 0
    private var mTargetAspectRatio: Float = 0.toFloat()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }


    private fun init() {
        srcRect = Rect()
        dstRect = RectF()
        originImageRect = RectF()
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    fun addBit(bit: Bitmap) {
        bitmap = bit
        srcRect!!.set(0, 0, bitmap!!.width, bitmap!!.height)
        mTargetAspectRatio = bitmap!!.width / bitmap!!.height.toFloat()
        originImageRect!!.set(0f, 0f, bit.width.toFloat(), bit.height.toFloat())
        setupCropBounds()
        this.invalidate()
    }

    fun rotateImage(angle: Int) {
        rotateAngle = angle
        this.invalidate()
    }

    fun reset() {
        rotateAngle = 0
        scale = 1f
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            left = paddingLeft
            top = paddingTop
            right = width - paddingRight
            bottom = height - paddingBottom
            mThisWidth = right - left
            mThisHeight = bottom - top
            setupCropBounds()
            postInvalidate()
        }
    }

    fun setupCropBounds() {
        if (mThisWidth <= 0) {
            return
        }
        // 根据 width
        val height = (mThisWidth / mTargetAspectRatio).toInt()
        //如果算出来的高度 比控件的高度要大
        if (height > mThisHeight) {
            // apectRatio 比例下的 Width
            val width = (mThisHeight * mTargetAspectRatio).toInt()
            // now width < mthisWidth
            //
            val halfDiff = (mThisWidth - width) / 2
            dstRect!!.set((paddingLeft + halfDiff).toFloat(),
                    paddingTop.toFloat(),
                    (paddingLeft + width + halfDiff).toFloat(),
                    (paddingTop + mThisHeight).toFloat())
        } else {
            val halfDiff = (mThisHeight - height) / 2
            dstRect!!.set(paddingLeft.toFloat(),
                    (paddingTop + halfDiff).toFloat(),
                    (paddingLeft + mThisWidth).toFloat(),
                    (paddingTop + height + halfDiff).toFloat())
        }

    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (bitmap == null || dstRect!!.isEmpty)
            return
        calculateWrapBox()
        scale = 1f
        if (wrapRect.width() > width) {
            scale = width / wrapRect.width()
        }

        // 绘制图形
        canvas.save()
        canvas.scale(scale, scale, (canvas.width shr 1).toFloat(),
                (canvas.height shr 1).toFloat())

        canvas.rotate(rotateAngle.toFloat(), (canvas.width shr 1).toFloat(),
                (canvas.height shr 1).toFloat())
        canvas.drawBitmap(bitmap!!, null, dstRect!!, null)
        canvas.restore()
    }


    private fun calculateWrapBox() {
        wrapRect.set(dstRect)
        mMatrix.reset()// 重置矩阵为单位矩阵
        val centerX = width shr 1
        val centerY = height shr 1
        mMatrix.postRotate(rotateAngle.toFloat(), centerX.toFloat(), centerY.toFloat())// 旋转后的角度
        mMatrix.mapRect(wrapRect)
    }


    val imageNewRect: RectF
        get() {
            val m = Matrix()
            m.postRotate(this.rotateAngle.toFloat(), originImageRect!!.centerX(),
                    originImageRect!!.centerY())
            m.mapRect(originImageRect)
            return originImageRect
        }
}
