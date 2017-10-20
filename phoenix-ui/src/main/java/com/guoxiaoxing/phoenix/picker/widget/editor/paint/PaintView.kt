package com.guoxiaoxing.phoenix.picture.edit.widget.paint

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import com.guoxiaoxing.phoenix.picker.model.PaintSaveState
import com.guoxiaoxing.phoenix.picker.util.MatrixUtils
import com.guoxiaoxing.phoenix.picture.edit.widget.hierarchy.BasePaintHierarchyView

/**
 * ## PaintView show to user
 *
 * Created by lxw
 */
class PaintView : BasePaintHierarchyView<PaintSaveState> {
    private lateinit var mDrawPaint: Paint

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun initSupportView(context: Context) {
        super.initSupportView(context)
        mDrawPaint = Paint()
        mDrawPaint.isAntiAlias = true
        mDrawPaint.color = Color.RED
        mDrawPaint.strokeJoin = Paint.Join.ROUND
        mDrawPaint.strokeCap = Paint.Cap.ROUND
        mDrawPaint.style = Paint.Style.STROKE
        mDrawPaint.strokeWidth = MatrixUtils.dp2px(context, 3f).toFloat()
    }

    override fun drawDragPath(paintPath: Path) {
        super.drawDragPath(paintPath)
        displayCanvas?.drawPath(paintPath, mDrawPaint)
        invalidate()
    }

    override fun savePathOnFingerUp(paintPath: Path) = PaintSaveState(MatrixUtils.copyPaint(mDrawPaint), paintPath)

    override fun drawAllCachedState(canvas: Canvas) {
        for ((paint, path) in saveStateMap.values) {
            canvas.drawPath(path, paint)
        }
    }

    fun setPaintColor(color: Int) {
        mDrawPaint.color = color
    }
}