package com.guoxiaoxing.phoenix.picker.util

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Build
import android.view.*

object MatrixUtils {

    private var randomId = 0

    fun getMatrixValue(matrix: Matrix, whichValue: Int): Float {
        val matrixValues = FloatArray(9)
        matrix.getValues(matrixValues)
        return matrixValues[whichValue]
    }

    fun geMatrixScale(matrix: Matrix): Float {
        return Math.sqrt((Math.pow(getMatrixValue(matrix, Matrix.MSCALE_X).toDouble(), 2.0).toFloat() + Math.pow(getMatrixValue(matrix, Matrix.MSKEW_Y).toDouble(), 2.0).toFloat()).toDouble()).toFloat()
    }

    fun getMatrixTransX(matrix: Matrix): Float {
        return getMatrixValue(matrix, Matrix.MTRANS_X)
    }

    fun getMatrixTransY(matrix: Matrix): Float {
        return getMatrixValue(matrix, Matrix.MTRANS_Y)
    }

    fun getMatrixDegree(matrix: Matrix): Float {
        return -Math.round(Math.atan2(getMatrixValue(matrix, Matrix.MSKEW_X).toDouble(), getMatrixValue(matrix, Matrix.MSCALE_X).toDouble()) * (180 / Math.PI)).toFloat()
    }

    fun dp2px(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun sp2px(context: Context, sp: Float): Int {
        val scale = context.resources.displayMetrics.scaledDensity
        return (sp * scale + 0.5f).toInt()
    }

    fun changeSelectedStatus(viewGroup: ViewGroup, position: Int) {
        for (index in 0 until viewGroup.childCount) {
            viewGroup.getChildAt(index).isSelected = index == position
        }
    }

    fun mapInvertMatrixPoint(matrix: Matrix, point: PointF): PointF {
        val invert = Matrix()
        matrix.invert(invert)
        val src = floatArrayOf(point.x, point.y)
        val dst = FloatArray(2)
        invert.mapPoints(dst, src)
        return PointF(dst[0], dst[1])
    }

    fun mapMatrixPoint(matrix: Matrix, point: PointF): PointF {
        val src = floatArrayOf(point.x, point.y)
        val dst = FloatArray(2)
        matrix.mapPoints(dst, src)
        return PointF(dst[0], dst[1])
    }

    fun mapInvertMatrixRect(matrix: Matrix, rect: RectF): RectF {
        val invert = Matrix()
        matrix.invert(invert)
        val dst = RectF()
        invert.mapRect(dst, rect)
        return dst
    }

    fun mapInvertMatrixTranslate(matrix: Matrix, dx: Float, dy: Float): FloatArray {
        val tempMatrix = Matrix()
        val invertMatrix = Matrix()
        tempMatrix.set(matrix)
        tempMatrix.invert(invertMatrix)
        val startX = getMatrixTransX(invertMatrix)
        val startY = getMatrixTransY(invertMatrix)
        tempMatrix.postTranslate(dx, dy)
        invertMatrix.reset()
        tempMatrix.invert(invertMatrix)
        return floatArrayOf(startX - getMatrixTransX(invertMatrix), startY - getMatrixTransY(invertMatrix))
    }

    fun mapInvertMatrixScale(matrix: Matrix, scaleX: Float, scaleY: Float): FloatArray {
        val tempMatrix = Matrix()
        val invertMatrix = Matrix()
        tempMatrix.set(matrix)
        tempMatrix.invert(invertMatrix)
        val startScaleX = geMatrixScale(invertMatrix)
        val startScaleY = geMatrixScale(invertMatrix)
        tempMatrix.postScale(scaleX, scaleY)
        invertMatrix.reset()
        tempMatrix.invert(invertMatrix)
        return floatArrayOf(startScaleX / geMatrixScale(invertMatrix), startScaleY / geMatrixScale(invertMatrix))
    }

    fun getResourceColor(context: Context, resId: Int) = context.resources.getColor(resId)

    fun getResourceString(context: Context, resId: Int) = context.resources.getString(resId)


    fun getStatusBarHeight(context: Context): Int {
        var statusBarHeight = -1
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
        }
        if (statusBarHeight <= 0) {
            try {
                val clazz = Class.forName("com.android.internal.R\$dimen")
                val obj = clazz.newInstance()
                val height = Integer.parseInt(clazz.getField("status_bar_height").get(obj).toString())
                statusBarHeight = context.resources.getDimensionPixelSize(height)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (statusBarHeight < 0) {
            statusBarHeight = dp2px(context, 20f)
        }
        return statusBarHeight
    }

    /*set status bar in fullScreen...*/
    fun hideStatusBar(activity: Activity) {
        fullScreen(true, activity)
    }

    fun showStatusBar(activity: Activity) {
        fullScreen(false, activity)
    }

    private fun fullScreen(enable: Boolean, activity: Activity) {
        if (Build.VERSION.SDK_INT >= 19) {
            val flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            val decorView = activity.window.decorView
            var systemUiVisibility = decorView.systemUiVisibility
            if (enable) {
                systemUiVisibility = systemUiVisibility or flags
            } else {
                systemUiVisibility = systemUiVisibility and flags.inv()
            }
            decorView.systemUiVisibility = systemUiVisibility
        }
    }

    fun getNavigationBarHeight(context: Context): Int {
        val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
        val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
        if (!hasMenuKey && !hasBackKey) {
            val resources = context.resources
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            //获取NavigationBar的高度
            val height = resources.getDimensionPixelSize(resourceId)
            return height
        } else {
            return 0
        }
    }

    fun diffRect(bigger: RectF, smaller: RectF): Array<RectF> {
        val leftRect = RectF()
        val topRect = RectF()
        val rightRect = RectF()
        val bottomRect = RectF()
        var diffLeft = smaller.left - bigger.left
        diffLeft = if (diffLeft <= 0) 0f else diffLeft
        var diffRight = bigger.right - smaller.right
        diffRight = if (diffRight <= 0) 0f else diffRight
        var diffTop = smaller.top - bigger.top
        diffTop = if (diffTop <= 0) 0f else diffTop
        var diffBottom = bigger.bottom - smaller.bottom
        diffBottom = if (diffBottom <= 0) 0f else diffBottom
        leftRect.set(bigger.left, bigger.top, bigger.left + diffLeft, bigger.bottom)
        rightRect.set(bigger.right - diffRight, bigger.top, bigger.right, bigger.bottom)
        topRect.set(leftRect.right, bigger.top, rightRect.left, bigger.top + diffTop)
        bottomRect.set(leftRect.right, bigger.bottom - diffBottom, rightRect.left, bigger.bottom)
        return arrayOf(leftRect, topRect, rightRect, bottomRect)
    }

    fun copyPaint(copyPaint: Paint): Paint {
        val paint = Paint()
        paint.color = copyPaint.color
        paint.isAntiAlias = copyPaint.isAntiAlias
        paint.strokeJoin = copyPaint.strokeJoin
        paint.strokeCap = copyPaint.strokeCap
        paint.style = copyPaint.style
        paint.strokeWidth = copyPaint.strokeWidth
        return paint
    }

    // fun randomId() = UUID.randomUUID().toString()
    fun randomId(): String {
        randomId++
        return randomId.toString()
    }

    fun getInvertMatrix(matrix: Matrix): Matrix {
        val invert = Matrix()
        matrix.invert(invert)
        return invert
    }

    fun getWindowSize(context: Context): Point {
        val result = Point()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getSize(result)
        return result
    }

    /*invoke childView's method...*/
    fun <T> callChildren(clazz: Class<T>, parent: ViewGroup, transform: (T) -> Unit) {
        (0 until parent.childCount).forEach {
            val layer = parent.getChildAt(it)
            if (clazz.isInstance(layer)) {
                transform(layer as T)
            } else if (layer is ViewGroup) {
                callChildren(clazz, layer, transform)
            }
        }
    }
}