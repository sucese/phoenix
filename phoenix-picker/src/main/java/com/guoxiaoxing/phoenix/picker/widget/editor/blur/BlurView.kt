package com.guoxiaoxing.phoenix.picture.edit.widget.blur

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.guoxiaoxing.phoenix.picker.model.BlurSaveState
import com.guoxiaoxing.phoenix.picker.util.MatrixUtils
import com.guoxiaoxing.phoenix.picker.util.recycleBitmap
import com.guoxiaoxing.phoenix.picker.util.saveEntireLayer
import com.guoxiaoxing.phoenix.picture.edit.widget.hierarchy.BasePaintHierarchyView

/**
 * ## BlurView show to user
 *
 * Created by lxw
 */
class BlurView : BasePaintHierarchyView<BlurSaveState> {
    private var mGridMosaicCover: Bitmap? = null
    private var mBlurMosaicCover: Bitmap? = null
    private var mBlurMode: BlurMode? = null
    private var mLastBitmapId: Int = 0
    private lateinit var mMosaicPaint: Paint
    private lateinit var mMosaicPaintMode: Xfermode
    //initial
    var initializeMatrix: Matrix = Matrix()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun initSupportView(context: Context) {
        super.initSupportView(context)
        mMosaicPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mMosaicPaint.style = Paint.Style.STROKE
        mMosaicPaint.color = Color.BLACK
        mMosaicPaint.isAntiAlias = true
        mMosaicPaint.strokeJoin = Paint.Join.ROUND
        mMosaicPaint.strokeCap = Paint.Cap.ROUND
        mMosaicPaint.pathEffect = CornerPathEffect(10f)
        mMosaicPaint.strokeWidth = MatrixUtils.dp2px(context, 30f).toFloat()
        mMosaicPaintMode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recycleBitmap(mGridMosaicCover)
        recycleBitmap(mBlurMosaicCover)
    }

    private fun getMosaicCover(blurMode: BlurMode?): Bitmap? {
        return if (blurMode == null) null
        else if (blurMode == BlurMode.Grid) mGridMosaicCover
        else if (blurMode == BlurMode.Blur) mBlurMosaicCover
        else null
    }

    override fun interceptDrag(x: Float, y: Float): Boolean {
        return !validateRect.contains(x, y)
    }

    override fun drawDragPath(paintPath: Path) {
        super.drawDragPath(paintPath)
        displayCanvas?.let {
            if (drawMosaicLayer(it, mBlurMode, paintPath)) invalidate()
        }
    }

    private fun drawMosaicLayer(canvas: Canvas, mode: BlurMode?, paintPath: Path): Boolean {
        val cover = getMosaicCover(mode)
        cover ?: return false
        val count = canvas.saveEntireLayer()
        canvas.drawPath(paintPath, mMosaicPaint)
        mMosaicPaint.xfermode = mMosaicPaintMode
        canvas.drawBitmap(cover, initializeMatrix, mMosaicPaint)
        mMosaicPaint.xfermode = null
        canvas.restoreToCount(count)
        return true
    }

    override fun savePathOnFingerUp(paintPath: Path): BlurSaveState? {
        mBlurMode?.let {
            return BlurSaveState(it, paintPath)
        } ?: return null

    }

    override fun drawAllCachedState(canvas: Canvas) {
        for ((mode, path) in saveStateMap.values) {
            drawMosaicLayer(canvas, mode, path)
        }
    }

    fun setMosaicMode(blurMode: BlurMode, mosaicBitmap: Bitmap?) {
        mBlurMode = blurMode
        mosaicBitmap ?: return
        val bitmapId = mosaicBitmap.hashCode()
        val sameFromLast = mLastBitmapId == bitmapId
        if (blurMode == BlurMode.Grid) {
            if (sameFromLast) {
                mGridMosaicCover ?: let {
                    mGridMosaicCover = BlurUtils.getGridBlur(mosaicBitmap)
                }
            } else {
                recycleBitmap(mGridMosaicCover)
                mGridMosaicCover = BlurUtils.getGridBlur(mosaicBitmap)
            }
        } else if (blurMode == BlurMode.Blur) {
            if (sameFromLast) {
                mBlurMosaicCover ?: let {
                    mBlurMosaicCover = BlurUtils.getBlurMosaic(mosaicBitmap)
                }
            } else {
                recycleBitmap(mBlurMosaicCover)
                mBlurMosaicCover = BlurUtils.getBlurMosaic(mosaicBitmap)
            }
        }
        mLastBitmapId = bitmapId
    }

    fun setBitmap(mosaicBitmap: Bitmap) {
        mBlurMosaicCover = BlurUtils.getBlurMosaic(mosaicBitmap)
        mGridMosaicCover = BlurUtils.getGridBlur(mosaicBitmap)
    }
}