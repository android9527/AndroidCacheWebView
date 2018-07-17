package com.android9527.cachewebview;

import android.content.Context;

import com.android9527.cachewebview.cache.DiskCache;
import com.android9527.cachewebview.cache.InternalCacheDiskCacheFactory;
import com.android9527.cachewebview.cache.MemoryCache;
import com.android9527.cachewebview.cache.MemoryLruCache;
import com.android9527.cachewebview.cache.MemorySizeCalculator;

import java.util.concurrent.Executor;


/**
 * A builder class for setting default structural classes for WebViewCache to use.
 */
public final class WebViewCacheBuilder {
    private MemoryCache memoryCache;
    private Executor diskCacheExecutor;
    private DiskCache diskCache;
    private MemorySizeCalculator memorySizeCalculator;

    /**
     * Sets the {@link com.android9527.cachewebview.cache.MemoryCache} implementation to store
     *
     * @param memoryCache The cache to use.
     * @return This builder.
     */
    public WebViewCacheBuilder setMemoryCache(MemoryCache memoryCache) {
        this.memoryCache = memoryCache;
        return this;
    }

    /**
     * Sets the {@link com.android9527.cachewebview.cache.DiskCache.Factory} implementation to use
     * to construct the {@link com.android9527.cachewebview.cache.DiskCache} to use to store {@link
     *
     * @param diskCache The disk cache factory to use.
     * @return This builder.
     */
    public WebViewCacheBuilder setDiskCache(DiskCache diskCache) {
        this.diskCache = diskCache;
        return this;
    }


    /**
     * Sets the {@link java.util.concurrent.ExecutorService} implementation to use when retrieving
     * <p>
     * for thumbnail requests to work properly.
     *
     * @param service The ExecutorService to use.
     * @return This builder.
     * @see Executor
     */
    public WebViewCacheBuilder setDiskCacheExecutor(Executor service) {
        this.diskCacheExecutor = service;
        return this;
    }

    /**
     * Sets the {@link MemorySizeCalculator} to use to calculate maximum sizes for default
     *
     * @param builder The builder to use (will not be modified).
     * @return This builder.
     * @see #setMemorySizeCalculator(MemorySizeCalculator)
     */
    public WebViewCacheBuilder setMemorySizeCalculator(MemorySizeCalculator.Builder builder) {
        return setMemorySizeCalculator(builder.build());
    }

    /**
     * Sets the {@link MemorySizeCalculator} to use to calculate maximum sizes for default
     * <p>
     * <p>The given {@link MemorySizeCalculator} will not affect custom pools or caches provided
     *
     * @param calculator The calculator to use.
     * @return This builder.
     */
    public WebViewCacheBuilder setMemorySizeCalculator(MemorySizeCalculator calculator) {
        this.memorySizeCalculator = calculator;
        return this;
    }

    public WebViewCacheManage build(Context context) {

        if (diskCacheExecutor == null) {
            diskCacheExecutor = ExecutorServiceUtil.getExecutorService();
        }

        if (memorySizeCalculator == null) {
            memorySizeCalculator = new MemorySizeCalculator.Builder(context).build();
        }

        if (memoryCache == null) {
            memoryCache = new MemoryLruCache(memorySizeCalculator.getMemoryCacheSize());
        }

        if (diskCache == null) {
            diskCache = new InternalCacheDiskCacheFactory(context).build();
        }

        return new WebViewCacheManage(context, memoryCache, diskCache, diskCacheExecutor);
    }
}
