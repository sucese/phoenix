package com.guoxiaoxing.phoenix.picture.edit.layer

import android.graphics.RectF
import android.view.MotionEvent

/**
 * ## A helper clz for CropView's window rect moving
 *
 * Created by lxw
 */
class CropWindowHelper(var targetRadius: Float) {

    private var mEdges: RectF = RectF()
    private var mPressedCropType: Type? = null
    private val mFeedBackEdges: RectF = RectF()
    /*cropWindow,size*/
    var minCropWindowHeight: Float = 0f
    var maxCropWindowHeight: Float = Float.MAX_VALUE
    var minCropWindowWidth: Float = 0f
    var maxCropWindowWidth: Float = Float.MAX_VALUE

    fun setEdge(edge: RectF) = mEdges.set(edge)

    fun getEdge(): RectF {
        mFeedBackEdges.set(mEdges)
        return mFeedBackEdges
    }

    fun interceptTouchEvent(event: MotionEvent): Boolean {
        mPressedCropType = getPressedCropType(event.x, event.y, targetRadius)
        return mPressedCropType != null
    }

    fun resetTouchEvent(event: MotionEvent) {
        mPressedCropType = null
    }

    fun checkCropWindowBounds(bounds: RectF): Boolean {
        var offsetX = 0f
        var offsetY = 0f
        val tempRect = RectF(mEdges)
        if (mEdges.left < bounds.left) {
            offsetX = bounds.left - mEdges.left
            tempRect.left = bounds.left
        }
        if (mEdges.top < bounds.top) {
            offsetY = bounds.top - mEdges.top
            tempRect.top = bounds.top
        }
        if (mEdges.right > bounds.right) {
            offsetX = bounds.right - mEdges.right
            tempRect.right = bounds.right
        }
        if (mEdges.bottom > bounds.bottom) {
            offsetY = bounds.bottom - mEdges.bottom
            tempRect.bottom = bounds.bottom
        }
        mEdges.offset(offsetX, offsetY)
        if (!bounds.contains(mEdges)) {
            mEdges.set(tempRect)
        }
        return offsetX != 0f || offsetY != 0f
    }

    fun onCropWindowDrag(dx: Float, dy: Float, bound: RectF): Boolean {
        var hasDrag = false
        if (mPressedCropType == Type.CENTER) {
            hasDrag = moveCenter(mEdges, dx, dy, bound)
        } else {
            hasDrag = moveOtherCropType(mEdges, dx, dy, bound, mPressedCropType)
        }
        return hasDrag
    }

    private fun moveOtherCropType(rect: RectF, dx: Float, dy: Float, bounds: RectF, pressedCropType: Type?): Boolean {
        pressedCropType ?: return false
        when (pressedCropType) {
            Type.LEFT -> return moveLeft(rect, dx, bounds)
            Type.RIGHT -> return moveRight(rect, dx, bounds)
            Type.TOP -> return moveTop(rect, dy, bounds)
            Type.BOTTOM -> return moveBottom(rect, dy, bounds)
            Type.TOP_LEFT -> return moveTop(rect, dy, bounds) && moveLeft(rect, dx, bounds)
            Type.TOP_RIGHT -> return moveTop(rect, dy, bounds) && moveRight(rect, dx, bounds)
            Type.BOTTOM_LEFT -> return moveBottom(rect, dy, bounds) && moveLeft(rect, dx, bounds)
            Type.BOTTOM_RIGHT -> return moveBottom(rect, dy, bounds) && moveRight(rect, dx, bounds)
            else -> return false
        }
    }

    //region: move center,left,top,right,bottom constraint
    private fun moveCenter(rect: RectF, dx: Float, dy: Float, bounds: RectF): Boolean {
        var offsetX = dx
        var offsetY = dy
        if (rect.left + dx <= bounds.left) {
            offsetX = bounds.left - rect.left
        }
        if (rect.right + dx >= bounds.right) {
            offsetX = bounds.right - rect.right
        }
        if (rect.top + dy <= bounds.top) {
            offsetY = bounds.top - rect.top
        }
        if (rect.bottom + dy >= bounds.bottom) {
            offsetY = bounds.bottom - rect.bottom
        }
        rect.offset(offsetX, offsetY)
        return offsetX != 0f || offsetY != 0f
    }

    private fun moveLeft(rect: RectF, dx: Float, bounds: RectF): Boolean {
        var newLeft = rect.left + dx
        if (rect.right - newLeft > maxCropWindowWidth) {
            newLeft = rect.right - maxCropWindowWidth
        }
        if (rect.right - newLeft < minCropWindowWidth) {
            newLeft = rect.right - minCropWindowWidth
        }
        if (newLeft < bounds.left) {
            newLeft = bounds.left
        }
        val changed = rect.left != newLeft
        rect.left = newLeft
        return changed
    }

    private fun moveTop(rect: RectF, dy: Float, bounds: RectF): Boolean {
        var newTop = rect.top + dy
        if (rect.bottom - newTop > maxCropWindowHeight) {
            newTop = rect.bottom - maxCropWindowHeight
        }
        if (rect.bottom - newTop < minCropWindowHeight) {
            newTop = rect.bottom - minCropWindowHeight
        }
        if (newTop < bounds.top) {
            newTop = bounds.top
        }
        val changed = rect.top != newTop
        rect.top = newTop
        return changed
    }

    private fun moveRight(rect: RectF, dx: Float, bounds: RectF): Boolean {
        var newRight = rect.right + dx
        if (newRight - rect.left > maxCropWindowWidth) {
            newRight = rect.left + maxCropWindowWidth
        }
        if (newRight - rect.left < minCropWindowWidth) {
            newRight = rect.left + minCropWindowWidth
        }
        if (newRight > bounds.right) {
            newRight = bounds.right
        }
        val changed = rect.right != newRight
        rect.right = newRight
        return changed
    }

    private fun moveBottom(rect: RectF, dy: Float, bounds: RectF): Boolean {
        var newBottom = rect.bottom + dy
        if (newBottom - rect.top > maxCropWindowHeight) {
            newBottom = rect.top + maxCropWindowHeight
        }
        if (newBottom - rect.top < minCropWindowHeight) {
            newBottom = rect.top + minCropWindowHeight
        }
        if (newBottom > bounds.bottom) {
            newBottom = bounds.bottom
        }
        val changed = rect.bottom != newBottom
        rect.bottom = newBottom
        return changed
    }

    //region: press crop type.

    private fun getPressedCropType(x: Float, y: Float, targetRadius: Float): Type? {
        var moveType: Type? = null
        if (isInCornerTargetZone(x, y, mEdges.left, mEdges.top, targetRadius)) {
            moveType = Type.TOP_LEFT
        } else if (isInCornerTargetZone(x, y, mEdges.right, mEdges.top, targetRadius)) {
            moveType = Type.TOP_RIGHT
        } else if (isInCornerTargetZone(x, y, mEdges.left, mEdges.bottom, targetRadius)) {
            moveType = Type.BOTTOM_LEFT
        } else if (isInCornerTargetZone(x, y, mEdges.right, mEdges.bottom, targetRadius)) {
            moveType = Type.BOTTOM_RIGHT
        } else if (isInHorizontalTargetZone(x, y, mEdges.left, mEdges.right, mEdges.top, targetRadius)) {
            moveType = Type.TOP
        } else if (isInHorizontalTargetZone(x, y, mEdges.left, mEdges.right, mEdges.bottom, targetRadius)) {
            moveType = Type.BOTTOM
        } else if (isInVerticalTargetZone(x, y, mEdges.left, mEdges.top, mEdges.bottom, targetRadius)) {
            moveType = Type.LEFT
        } else if (isInVerticalTargetZone(x, y, mEdges.right, mEdges.top, mEdges.bottom, targetRadius)) {
            moveType = Type.RIGHT
        } else if (isInCenterTargetZone(x, y, mEdges.left, mEdges.top, mEdges.right, mEdges.bottom)) {
            moveType = Type.CENTER
        }
        return moveType
    }

    private fun isInCornerTargetZone(x: Float, y: Float, handleX: Float, handleY: Float, targetRadius: Float): Boolean {
        return Math.abs(x - handleX) <= targetRadius && Math.abs(y - handleY) <= targetRadius
    }

    private fun isInHorizontalTargetZone(x: Float, y: Float, handleXStart: Float, handleXEnd: Float, handleY: Float, targetRadius: Float): Boolean {
        return x > handleXStart && x < handleXEnd && Math.abs(y - handleY) <= targetRadius
    }

    private fun isInCenterTargetZone(x: Float, y: Float, left: Float, top: Float, right: Float, bottom: Float): Boolean {
        return x > left && x < right && y > top && y < bottom
    }

    private fun isInVerticalTargetZone(x: Float, y: Float, handleX: Float, handleYStart: Float, handleYEnd: Float, targetRadius: Float): Boolean {
        return Math.abs(x - handleX) <= targetRadius && y > handleYStart && y < handleYEnd
    }

    enum class Type {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        LEFT,
        TOP,
        RIGHT,
        BOTTOM,
        CENTER
    }
}