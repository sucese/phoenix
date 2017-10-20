package com.guoxiaoxing.phoenix.picker.listener

import android.content.Context
import android.view.View
import com.guoxiaoxing.phoenix.picker.util.ActionBarAnimUtils
import com.guoxiaoxing.phoenix.picker.widget.editor.EditDelegate
import com.guoxiaoxing.phoenix.picture.edit.widget.crop.CropHelper
import com.guoxiaoxing.phoenix.picture.edit.widget.hierarchy.HierarchyComposite

/**
 * UI element provider
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
interface LayerViewProvider {

    fun findLayerByEditorMode(operation: com.guoxiaoxing.phoenix.picture.edit.operation.Operation): View?

    fun getActivityContext(): Context

    fun getFuncAndActionBarAnimHelper(): ActionBarAnimUtils

    fun getCropHelper(): CropHelper

    fun getRootEditorDelegate(): EditDelegate

    fun getLayerCompositeView(): HierarchyComposite

    fun getSetupEditorId(): String

    fun getResultEditorId(): String

    fun getEditorSizeInfo(): Pair<Int, Int>

    fun getScreenSizeInfo(): Pair<Int, Int>
}