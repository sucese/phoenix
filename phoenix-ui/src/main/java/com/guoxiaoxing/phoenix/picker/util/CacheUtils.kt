package com.guoxiaoxing.phoenix.picker.util

import android.support.v4.util.LruCache
import com.guoxiaoxing.phoenix.picker.model.HierarchyCache

/**
 * Layer data cache for save layer data and restore
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
object CacheUtils {

    private val mHierarchyCache = LruCache<String, MutableMap<String, HierarchyCache>>(5)

    fun getCache(editorId: String): MutableMap<String, HierarchyCache> {
        var cache = mHierarchyCache[editorId]
        if (cache == null) {
            cache = mutableMapOf()
            mHierarchyCache.put(editorId, cache)
        }
        return cache
    }

    fun setCache(editorId: String, data: MutableMap<String, HierarchyCache>?) {
        data?.let {
            mHierarchyCache.put(editorId, it)
        }
    }
}