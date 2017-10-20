package com.guoxiaoxing.phoenix.picture.edit.widget.stick

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import com.guoxiaoxing.phoenix.picker.model.InputStickModel
import com.guoxiaoxing.phoenix.picker.model.StickSaveState
import com.guoxiaoxing.phoenix.picker.util.MatrixUtils
import com.guoxiaoxing.phoenix.picker.util.increase
import com.guoxiaoxing.phoenix.picker.util.schedule
import com.guoxiaoxing.phoenix.picture.edit.widget.hierarchy.BasePastingHierarchyView

class StickView : BasePastingHierarchyView<StickSaveState> {
    private var mFocusRectOffset = 0f

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun initSupportView(context: Context) {
        super.initSupportView(context)
        mFocusRectOffset = MatrixUtils.dp2px(context, 10f).toFloat()
    }

    fun onStickerPastingChanged(data: InputStickModel) {
        addStickerPasting(data.stickerIndex, data.sticker)
    }

    private fun addStickerPasting(stickerIndex: Int, sticker: Sticker) {
        genDisplayCanvas()
        val state = initStickerSaveState(stickerIndex, sticker)
        state ?: return
        saveStateMap.put(state.id, state)
        currentPastingState = state
        redrawAllCache()
        hideExtraValidateRect()
    }

    private fun initStickerSaveState(stickerIndex: Int, sticker: Sticker, matrix: Matrix = Matrix()): StickSaveState? {
        val bitmap = StickerUtils.getStickerBitmap(context, sticker, stickerIndex)
        bitmap ?: return null
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val initDisplayRect = RectF()
        var point = PointF(validateRect.centerX(), validateRect.centerY())
        point = MatrixUtils.mapInvertMatrixPoint(drawMatrix, point)
        initDisplayRect.schedule(point.x, point.y, width, height)
        val initTextRect = RectF()
        initTextRect.set(initDisplayRect)
        initDisplayRect.increase(mFocusRectOffset, mFocusRectOffset)
        return StickSaveState(sticker, stickerIndex, initDisplayRect, matrix)
    }

    override fun drawPastingState(state: StickSaveState, canvas: Canvas) {
        super.drawPastingState(state, canvas)
        val result = StickerUtils.getStickerBitmap(context, state.sticker, state.stickerIndex)
        result ?: return
        val resultStickerRect = RectF()
        val matrix = Matrix(state.displayMatrix)
        matrix.mapRect(resultStickerRect, state.initDisplayRect)
        canvas.drawBitmap(result, null, resultStickerRect, null)
    }

}