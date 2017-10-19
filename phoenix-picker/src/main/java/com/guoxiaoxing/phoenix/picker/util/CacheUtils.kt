package com.guoxiaoxing.phoenix.picker.util

import android.graphics.Bitmap
import android.support.v4.util.LruCache

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.

 * @author guoxiaoxing
 * *
 * @since 2017/10/16 上午10:08
 */
object CacheUtils {

    private val lruCache: LruCache<String, Bitmap>

    init {
        val maxMemory = (Runtime.getRuntime().totalMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        lruCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String?, bitmap: Bitmap?): Int {
                return bitmap!!.rowBytes * bitmap.height / 1024
            }
        }
    }

    operator fun get(key: String): Bitmap {
        return lruCache.get(key)
    }

    fun put(key: String, bitmap: Bitmap) {
        lruCache.put(key, bitmap)
    }

    fun remove(key: String) {
        lruCache.remove(key)
    }
}
