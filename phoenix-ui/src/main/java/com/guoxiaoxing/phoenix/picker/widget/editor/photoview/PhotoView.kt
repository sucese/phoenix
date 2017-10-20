package com.guoxiaoxing.phoenix.picture.edit.widget.photoview

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView
import com.guoxiaoxing.phoenix.picker.listener.GestureDetectorListener
import com.guoxiaoxing.phoenix.picker.listener.OnPhotoRectUpdateListener
import com.guoxiaoxing.phoenix.picker.widget.editor.RootNode

class PhotoView : ImageView, RootNode<ImageView> {

    lateinit var attacher: PhotoViewAttacher
        private set

    @JvmOverloads constructor(context: Context, attr: AttributeSet? = null, defStyle: Int = 0) : super(context, attr, defStyle) {
        init()
    }

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        attacher = PhotoViewAttacher(this)
        //We always pose as a Matrix scale type, though we can change to another scale type
        //via the attacher
        super.setScaleType(ScaleType.MATRIX)
    }

    override fun getScaleType(): ScaleType {
        return attacher.scaleType
    }

    override fun getImageMatrix(): Matrix {
        return attacher.imageMatrix
    }

    override fun setScaleType(scaleType: ScaleType) {
        attacher.scaleType = scaleType
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        // setImageBitmap calls through to this method
        attacher.update()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        attacher.update()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        attacher.update()
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = super.setFrame(l, t, r, b)
        if (changed) {
            attacher.update()
        }
        return changed
    }

    fun setRotationTo(rotationDegree: Float) {
        attacher.setRotationTo(rotationDegree)
    }

    override fun setRotationBy(degree: Float) {
        attacher.setRotationBy(degree)
    }

    val displayRect: RectF?
        get() = attacher.displayRect

    override fun getDisplayMatrix(): Matrix {
        val result = Matrix()
        attacher.getDisplayMatrix(result)
        return result
    }

    fun setDisplayMatrix(finalRectangle: Matrix): Boolean {
        return attacher.setDisplayMatrix(finalRectangle)
    }

    var minimumScale: Float
        get() = attacher.minimumScale
        set(minimumScale) {
            attacher.minimumScale = minimumScale
        }

    var mediumScale: Float
        get() = attacher.mediumScale
        set(mediumScale) {
            attacher.mediumScale = mediumScale
        }

    var maximumScale: Float
        get() = attacher.maximumScale
        set(maximumScale) {
            attacher.maximumScale = maximumScale
        }

    var scale: Float
        get() = attacher.scale
        set(scale) {
            attacher.scale = scale
        }

    override fun setScale(scale: Float, animate: Boolean) {
        attacher.setScale(scale, animate)
    }

    override fun resetMaxScale(maxScale: Float) {
        maximumScale = maxScale;
    }

    override fun resetMinScale(minScale: Float) {
        minimumScale = minScale;
    }

    fun setScaleAndTranslate(scale: Float, dx: Float, dy: Float) {
        attacher.setScaleAndTranslate(scale, dx, dy)
    }

    fun setAllowParentInterceptOnEdge(allow: Boolean) {
        attacher.setAllowParentInterceptOnEdge(allow)
    }

    fun setScaleLevels(minimumScale: Float, mediumScale: Float, maximumScale: Float) {
        attacher.setScaleLevels(minimumScale, mediumScale, maximumScale)
    }

    /*rootNode support*/
    override fun addOnMatrixChangeListener(listener: OnPhotoRectUpdateListener) {
        attacher.setOnMatrixChangeListener(listener)
    }

    override fun addGestureDetectorListener(listener: GestureDetectorListener) {
        attacher.setGestureDetectorListener(listener)
    }

    private fun getBitmap(): Bitmap? {
        var bm: Bitmap? = null
        val d = drawable
        if (d != null && d is BitmapDrawable) bm = d.bitmap
        return bm
    }

    override fun getSupportMatrix() = attacher.getSupportMatrix()

    override fun setSupportMatrix(matrix: Matrix) = attacher.setSupportMatrix(matrix)

    override fun getDisplayBitmap() = getBitmap()

    override fun setDisplayBitmap(bitmap: Bitmap) {
        setImageBitmap(bitmap)
    }

    override fun getRooView() = this

    override fun getDisplayingRect() = RectF(displayRect)

    override fun getBaseLayoutMatrix() = attacher.getBaseMatrix()

    override fun getOriginalRect() = attacher.getDisplayRect(getBaseLayoutMatrix())


}
