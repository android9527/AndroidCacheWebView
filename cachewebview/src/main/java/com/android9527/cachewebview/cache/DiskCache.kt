package com.android9527.cachewebview.cache

import com.android9527.cachewebview.bean.CacheKey
import com.android9527.cachewebview.bean.CacheValue
import java.io.InputStream

/**
 * Created by chenfeiyue on 2018/6/6.
 * Description ：
 * An interface for writing to and reading from a disk cache.
 */
interface DiskCache {
    /**
     * Get the cache for the value at the given key.
     *
     *
     *
     *  Note - This is potentially dangerous, someone may write a new value to the file at any
     * point in time and we won't know about it.
     *
     * @param key The key in the cache.
     * @return An InputStream representing the data at key at the time get is called.
     */
    operator fun get(key: CacheKey): CacheValue?

    /**
     * Write to a key in the cache. [InputStream] is used so that the cache implementation can
     * perform actions after the write finishes, like commit (via atomic file rename).
     *
     * @param key    The key to write to.
     * @param value An interface that will write data given an OutputStream for the key.
     */
    fun put(key: CacheKey, value: CacheValue?)

    /**
     * Remove the key and value from the cache.
     *
     * @param key The key to remove.
     */
    fun remove(key: CacheKey)

    /**
     * Clear the cache.
     */
    fun clear()


    /**
     * An interface for lazily creating a disk cache.
     */
    interface Factory {

        /**
         * Returns a new disk cache, or `null` if no disk cache could be created.
         */
        fun build(): DiskCache?

        companion object {

            /** 20 MB of cache.  */
            const val DEFAULT_DISK_CACHE_SIZE = 20 * 1024 * 1024
            const val DEFAULT_DISK_CACHE_DIR = "lru_cache_webview"
        }
    }

}