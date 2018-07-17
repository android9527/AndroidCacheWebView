package com.android9527.cachewebview.cache

import android.os.Build
import com.android9527.cachewebview.bean.CacheKey
import com.android9527.cachewebview.bean.CacheValue
import com.android9527.cachewebview.cache.memorycache.LruCache

/**
 * Created by chenfeiyue on 2018/6/5.
 * Description ：MemoryLruCache
 */
class MemoryLruCache : LruCache<CacheKey, CacheValue?>, MemoryCache {

    override fun trimMemory(level: Int) {
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            // Nearing middle of list of cached background apps
            // Evict our entire bitmap cache
            clearMemory()
        } else if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            // Entering list of cached background apps
            // Evict oldest half of our bitmap cache
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                trimToSize(currentSize / 2)
            }
        }
    }

    constructor(maxSize: Int) : super(maxSize) {

    }

    /**
     * 内存获取对应缓存
     */
    override fun get(key: CacheKey): CacheValue? {
        var cacheValue = super.get(key)
        try {
            // 重置输入流
            cacheValue?.getInputStream()?.reset()
        } catch (e: Exception) {
            e.printStackTrace()
            cacheValue = null
        }
        return cacheValue
    }


    /**
     * 单个文件占用内存大小
     */
    override fun getSize(item: CacheValue?): Int {
        if (item == null) {
            return 0
        }
        return item.getContentSize()
    }

    override fun clear() {
        super<LruCache>.clearMemory()
    }

}