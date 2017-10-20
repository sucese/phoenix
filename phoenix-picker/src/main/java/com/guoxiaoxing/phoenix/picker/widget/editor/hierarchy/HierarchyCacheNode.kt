package com.guoxiaoxing.phoenix.picture.edit.widget.hierarchy

import com.guoxiaoxing.phoenix.picker.model.HierarchyCache

/**
 * Base paintingLayerView  for [PaintView] and [BlurView]
 *  It's hold move path[paintPath] for user's finger move
 *
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
interface HierarchyCacheNode {

    fun getLayerTag(): String = this::class.java.simpleName

    fun restoreLayerData(input: MutableMap<String, HierarchyCache>)

    fun saveLayerData(output: MutableMap<String, HierarchyCache>)
}