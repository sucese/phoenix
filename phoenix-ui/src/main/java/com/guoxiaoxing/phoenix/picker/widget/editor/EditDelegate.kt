package com.guoxiaoxing.phoenix.picker.widget.editor

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.view.ViewGroup
import android.widget.ImageView
import com.guoxiaoxing.phoenix.picker.listener.GestureDetectorListener
import com.guoxiaoxing.phoenix.picker.listener.OnPhotoRectUpdateListener
import com.guoxiaoxing.phoenix.picker.util.MatrixUtils.callChildren
import com.guoxiaoxing.phoenix.picture.edit.widget.hierarchy.HierarchyTransformer

class EditDelegate(val rootNode: RootNode<ImageView>, val delegateParent: ViewGroup) : RootNode<ImageView>, HierarchyTransformer, OnPhotoRectUpdateListener {
    init {
        addGestureDetectorListener(this)
        addOnMatrixChangeListener(this)
    }

    override fun onPhotoRectUpdate(rect: RectF, matrix: Matrix) {
        callChildren(OnPhotoRectUpdateListener::class.java, delegateParent) {
            it.onPhotoRectUpdate(rect, matrix)
        }
    }

    override fun onDrag(dx: Float, dy: Float, x: Float, y: Float, rootLayer: Boolean) {
        callChildren(HierarchyTransformer::class.java, delegateParent) {
            it.onDrag(dx, dy, x, y, true)
        }
    }

    override fun onScale(scaleFactor: Float, focusX: Float, focusY: Float, rootLayer: Boolean) {
        callChildren(HierarchyTransformer::class.java, delegateParent) {
            it.onScale(scaleFactor, focusX, focusY, true)
        }
    }

    override fun resetEditorSupportMatrix(matrix: Matrix) {
        callChildren(HierarchyTransformer::class.java, delegateParent) {
            it.resetEditorSupportMatrix(matrix)
        }
    }

    override fun setRotationBy(degree: Float) {
        rootNode.setRotationBy(degree)
    }

    override fun resetMinScale(minScale: Float) = rootNode.resetMinScale(minScale)

    override fun resetMaxScale(maxScale: Float) = rootNode.resetMaxScale(maxScale)

    override fun setScale(scale: Float, animate: Boolean) {
        rootNode.setScale(scale, animate)
    }
    override fun addOnMatrixChangeListener(listener: OnPhotoRectUpdateListener) {
        rootNode.addOnMatrixChangeListener(listener)
    }

    override fun addGestureDetectorListener(listener: GestureDetectorListener) {
        rootNode.addGestureDetectorListener(listener)
    }

    override fun getSupportMatrix() = rootNode.getSupportMatrix()

    override fun getDisplayBitmap() = rootNode.getDisplayBitmap()

    override fun setSupportMatrix(matrix: Matrix) {
        rootNode.setSupportMatrix(matrix)
    }

    override fun setDisplayBitmap(bitmap: Bitmap) {
        rootNode.setDisplayBitmap(bitmap)
    }


    override fun getRooView(): ImageView {
        return rootNode.getRooView()
    }

    override fun getDisplayingRect() = rootNode.getDisplayingRect()

    override fun getDisplayMatrix() = rootNode.getDisplayMatrix()

    override fun getBaseLayoutMatrix() = rootNode.getBaseLayoutMatrix()

    override fun getOriginalRect() = rootNode.getOriginalRect()

}