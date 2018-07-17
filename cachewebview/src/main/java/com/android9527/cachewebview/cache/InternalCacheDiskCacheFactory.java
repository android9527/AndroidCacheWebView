package com.android9527.cachewebview.cache;

import android.content.Context;

import java.io.File;

/**
 * Creates an {@link com.android9527.cachewebview.cache.diskcache.DiskLruCache} based disk cache in the internal
 * disk cache directory.
 */
public final class InternalCacheDiskCacheFactory extends DiskLruCacheFactory {

    public InternalCacheDiskCacheFactory(Context context) {
        this(context, DEFAULT_DISK_CACHE_DIR,
                DEFAULT_DISK_CACHE_SIZE);
    }

    public InternalCacheDiskCacheFactory(Context context, int diskCacheSize) {
        this(context, DEFAULT_DISK_CACHE_DIR, diskCacheSize);
    }

    public InternalCacheDiskCacheFactory(final Context context, final String diskCacheName,
                                         int diskCacheSize) {
        super(new CacheDirectoryGetter() {
            @Override
            public File getCacheDirectory() {
                File cacheDirectory = context.getCacheDir();
                if (cacheDirectory == null) {
                    return null;
                }
                if (diskCacheName != null) {
                    return new File(cacheDirectory, diskCacheName);
                }
                return cacheDirectory;
            }
        }, diskCacheSize);
    }
}
