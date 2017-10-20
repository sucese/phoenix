package com.guoxiaoxing.phoenix.picker.util

import android.graphics.RectF
import android.view.View
import com.guoxiaoxing.phoenix.picker.model.SharableData


/**
 * The type alias
 * it coordinate with [com.guoxiaoxing.phoenix.picture.edit.widget.ActionBarAnimHelper]
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
typealias ShowOrHideDragCallback = (Boolean) -> Unit

typealias SetOrNotDragCallback = (Boolean) -> Unit

typealias DragViewRectCallback = (RectF) -> Unit

typealias OnLayerViewDoubleClick = (View, SharableData) -> Unit

typealias OnLayoutRectChange = (View, RectF) -> Unit

typealias ImageComposeCallback = (Boolean) -> Unit

typealias PreDrawSizeListener = (Int, Int) -> Unit

