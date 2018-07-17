package com.android9527.cachewebview.cache

import com.android9527.cachewebview.bean.CacheKey
import com.android9527.cachewebview.bean.CacheValue

/**
 * An interface for adding and removing resources from an in memory cache.
 */
interface MemoryCache {

    /**
     * Returns the sum of the sizes of all the contents of the cache in bytes.
     */
    fun getCurrentSize(): Int

    /**
     * Returns the current maximum size in bytes of the cache.
     */
    fun getMaxSize(): Int

    /**
     * Removes the value for the given key and returns it if present or null otherwise.
     *
     * @param key The key.
     */
    fun remove(key: CacheKey): CacheValue?

    /**
     * Get the cache for the value at the given key.
     *
     *
     *
     *  Note - This is potentially dangerous, someone may write a new value to the file at any
     * point in time and we won't know about it.
     *
     * @param key The key in the cache.
     * @return An CacheValue representing the data at key at the time get is called.
     */
    fun get(key: CacheKey): CacheValue?

    /**
     * Add bitmap to the cache with the given key.
     *
     * @param key      The key to retrieve the bitmap.
     * @param value The value to store.
     * @return The old value of key (null if key is not in map).
     */
    fun put(key: CacheKey, value: CacheValue?): CacheValue?

    /**
     * Evict all items from the memory cache.
     */
    fun clear()

    /**
     * Trim the memory cache to the appropriate level. Typically called on the callback onTrimMemory.
     *
     * @param level This integer represents a trim level as specified in [              ].
     */
    fun trimMemory(level: Int)
}
