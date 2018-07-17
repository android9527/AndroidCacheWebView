/*
 * Copyright (c) 2013. Bump Technologies Inc. All Rights Reserved.
 */

package com.android9527.cachewebview.cache

import android.util.Log
import com.android9527.cachewebview.bean.CacheKey
import com.android9527.cachewebview.bean.CacheValue
import com.android9527.cachewebview.bean.StreamCacheValue
import com.android9527.cachewebview.cache.diskcache.DiskLruCache
import com.android9527.cachewebview.util.inputStream2Str
import com.android9527.cachewebview.util.map2Str
import com.android9527.cachewebview.util.str2Map
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream

/**
 * The default DiskCache implementation. There must be no more than one active instance for a given
 * directory at a time.
 *
 * @see .get
 */
class DiskLruCacheWrapper : DiskCache {
    private val mDirectory: File
    private val mMaxSize: Long

    constructor(directory: File, maxSize: Long) {
        mDirectory = directory
        mMaxSize = maxSize
    }

    private var diskLruCache: DiskLruCache? = null

    @Synchronized
    @Throws(IOException::class)
    private fun getDiskCache(): DiskLruCache? {
        if (diskLruCache == null) {
            diskLruCache = DiskLruCache.open(mDirectory, APP_VERSION, VALUE_COUNT, mMaxSize)
        }
        return diskLruCache
    }

    override fun get(key: CacheKey): StreamCacheValue? {
        val cacheKey = key.getCacheKey()
        try {
            val diskCache = getDiskCache() ?: return null
            if (diskCache.isClosed) {
                return null
            }
            val snapshot: DiskLruCache.Snapshot?
            snapshot = diskCache.get(cacheKey)
            if (snapshot != null) {
                val inputStream = snapshot.getInputStream(CONTENT)
                val result = StreamCacheValue()
                result.stream = inputStream
                val headerStream = snapshot.getInputStream(HEADERS)
                val headerStr = inputStream2Str(headerStream)
                result.headerMap = str2Map(headerStr)
                return result
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }


    /**
     * 加入内存
     */
    override fun put(key: CacheKey, value: CacheValue?) {
        // We want to make sure that puts block so that data is available when put completes. We may
        // actually not write any data if we find that data is written by the time we acquire the lock.

        if (value?.getInputStream() == null) {
            return
        }

        var editor: DiskLruCache.Editor? = null
        var contentOutputStream : OutputStream? = null
        var headerOutputStream : OutputStream? = null
        try {
            val diskCache = getDiskCache() ?: return
            if (diskCache.isClosed) {
                return
            }

            // TODO 判断是否已经存在
            val cacheKey = key.getCacheKey()
            editor = diskCache.edit(cacheKey)

            if (editor == null) {
                throw IllegalStateException("Had two simultaneous puts for: " + key)
            }

            // 保存内容
            val inputStream = value.getInputStream()

            contentOutputStream = BufferedOutputStream(editor.newOutputStream(CONTENT))
            val buffer = ByteArray(1024)
            var len = inputStream!!.read(buffer)
            while (len != -1) {
                contentOutputStream.write(buffer, 0, len)
                len = inputStream.read(buffer)
            }
            contentOutputStream.flush()

            // 保存Header
            val headerJson = map2Str(value.getHeader())
            headerOutputStream = BufferedOutputStream(editor.newOutputStream(HEADERS))
            headerOutputStream.write(headerJson?.toByteArray())
            headerOutputStream.flush()

            diskCache.flush()
            editor.commit()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                editor?.abortUnlessCommitted()
                contentOutputStream?.close()
                headerOutputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun remove(key: CacheKey) {
        val cacheKey = key.getCacheKey()
        try {
            getDiskCache()?.remove(cacheKey)
        } catch (e: IOException) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unable to delete from disk cache", e)
            }
        }
    }

    @Synchronized
    override fun clear() {
        try {
            getDiskCache()?.delete()
            resetDiskCache()
        } catch (e: IOException) {
           e.printStackTrace()
        }

    }

    @Synchronized
    private fun resetDiskCache() {
        diskLruCache = null
    }

    companion object {
        private const val TAG = "DiskLruCacheWrapper"

        private const val CONTENT = 0
        private const val HEADERS = 1

        private const val APP_VERSION = 1
        private const val VALUE_COUNT = 2
        private var wrapper: DiskCache? = null

        /**
         * Get a DiskCache in the given directory and size. If a disk cache has already been created with
         * a different directory and/or size, it will be returned instead and the new arguments will be
         * ignored.
         *
         * @param directory The directory for the disk cache
         * @param maxSize   The max size for the disk cache
         * @return The new disk cache with the given arguments, or the current cache if one already exists
         */
        @Synchronized @JvmStatic
        operator fun get(directory: File, maxSize: Long): DiskCache? {
            // TODO calling twice with different arguments makes it return the cache for the same
            // directory, it's public!
            if (wrapper == null) {
                wrapper = DiskLruCacheWrapper(directory, maxSize)
            }
            return wrapper
        }
    }
}
