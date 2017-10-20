package com.guoxiaoxing.phoenix.picture.edit.widget.text

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.guoxiaoxing.phoenix.picker.model.InputTextModel
import com.guoxiaoxing.phoenix.picker.model.TextPastingSaveState
import com.guoxiaoxing.phoenix.picker.util.MatrixUtils
import com.guoxiaoxing.phoenix.picker.util.increase
import com.guoxiaoxing.phoenix.picker.util.schedule
import com.guoxiaoxing.phoenix.picture.edit.widget.hierarchy.BasePastingHierarchyView

class TextPastingView : BasePastingHierarchyView<TextPastingSaveState> {
    private var mFocusRectOffset = 0f
    private lateinit var mTextPaint: Paint
    private lateinit var mTempTextPaint: Paint

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun initSupportView(context: Context) {
        super.initSupportView(context)
        mFocusRectOffset = MatrixUtils.dp2px(context, 10f).toFloat()
        //textPaint
        mTextPaint = Paint()
        mTextPaint.textSize = MatrixUtils.sp2px(context, 25f).toFloat()
        mTextPaint.isAntiAlias = true
        mTempTextPaint = MatrixUtils.copyPaint(mTextPaint)

    }

    fun onTextPastingChanged(model: InputTextModel) {
        if (model.text == null || model.text.isBlank() || model.color == null) return
        addTextPasting(model.id, model.text, model.color)
    }

    private fun addTextPasting(id: String?, text: String, color: Int) {
        genDisplayCanvas()
        //old matrix info
        var displayMatrix = Matrix()
        id?.let {
            val result = saveStateMap[id]
            if (result != null) {
                displayMatrix = result.displayMatrix
            }
        }
        val state = initTextPastingSaveState(text, color, displayMatrix)
        id?.let {
            state.id = it
        }
        saveStateMap.put(state.id, state)
        currentPastingState = state
        redrawAllCache()
        //hideBorder...
        hideExtraValidateRect()
    }

    private fun initTextPastingSaveState(text: String, color: Int, matrix: Matrix = Matrix()): TextPastingSaveState {
        mTextPaint.color = color
        val width = mTextPaint.measureText(text)
        val height = mTextPaint.descent() - mTextPaint.ascent()
        val initDisplayRect = RectF()
        var point = PointF(validateRect.centerX(), validateRect.centerY())
        point = MatrixUtils.mapInvertMatrixPoint(drawMatrix, point)
        initDisplayRect.schedule(point.x, point.y, width, height)
        val initTextRect = RectF()
        initTextRect.set(initDisplayRect)
        initDisplayRect.increase(mFocusRectOffset, mFocusRectOffset)
        return TextPastingSaveState(text, color, initTextRect, initDisplayRect, matrix)
    }


    override fun drawPastingState(state: TextPastingSaveState, canvas: Canvas) {
        super.drawPastingState(state, canvas)
        val resultTextRect = RectF()
        val matrix = Matrix(state.displayMatrix)
        matrix.mapRect(resultTextRect, state.initTextRect)
        mTempTextPaint.textSize = mTextPaint.textSize * MatrixUtils.geMatrixScale(matrix)
        mTempTextPaint.color = state.textColor
        val result = PointF(resultTextRect.left, resultTextRect.bottom - mTempTextPaint.descent())
        canvas.drawText(state.text, result.x, result.y, mTempTextPaint)
    }

    override fun onPastingDoubleClick(state: TextPastingSaveState) {
        super.onPastingDoubleClick(state)
        onLayerViewDoubleClick?.invoke(this, InputTextModel(state.id, state.text, state.textColor))
    }

}


