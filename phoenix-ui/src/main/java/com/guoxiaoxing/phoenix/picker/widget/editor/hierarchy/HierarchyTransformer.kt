package com.guoxiaoxing.phoenix.picture.edit.widget.hierarchy

import android.graphics.Matrix
import com.guoxiaoxing.phoenix.picker.listener.GestureDetectorListener

/**
 * Important for layer's gesture data handling marker
 * @see RootEditorDelegate
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
interface HierarchyTransformer : GestureDetectorListener {

    fun resetEditorSupportMatrix(matrix: Matrix)
}