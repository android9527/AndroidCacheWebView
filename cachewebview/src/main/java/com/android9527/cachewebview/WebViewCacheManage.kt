package com.android9527.cachewebview

import android.annotation.SuppressLint
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.text.TextUtils
import android.webkit.WebResourceResponse
import com.android9527.cachewebview.bean.CacheKey
import com.android9527.cachewebview.bean.CacheValue
import com.android9527.cachewebview.bean.StreamCacheValue
import com.android9527.cachewebview.bean.UrlCacheKey
import com.android9527.cachewebview.cache.DiskCache
import com.android9527.cachewebview.cache.MemoryCache
import com.android9527.cachewebview.okhttp.Method
import com.android9527.cachewebview.okhttp.http
import com.android9527.cachewebview.util.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executor
import javax.net.ssl.HttpsURLConnection

/**
 * Created by chenfeiyue on 2018/6/4.
 * Description ：WebViewDiskCache
 */
class WebViewCacheManage : ComponentCallbacks2 {

    /**
     * android.content.ComponentCallbacks2
     */
    override fun onLowMemory() {
        mMemoryCache?.clear()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
    }

    override fun onTrimMemory(level: Int) {
        mMemoryCache?.trimMemory(level)
    }

    private var mDiskCache: DiskCache? = null
    private var mMemoryCache: MemoryCache? = null
    private var mDiskCacheExecutor: Executor? = null

    private var mContext: Context? = null

    companion object {

        val TAG: String = WebViewCacheManage::class.java.simpleName

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var mWebViewCache: WebViewCacheManage? = null

        /**
         * Get the singleton.
         *
         * @return the singleton
         */
        @JvmStatic
        operator fun get(context: Context): WebViewCacheManage {
            if (mWebViewCache == null) {
                synchronized(WebViewCacheManage::class.java) {
                    if (mWebViewCache == null) {
                        initWebViewCacheManage(context.applicationContext)
                    }
                }
            }
            return mWebViewCache!!
        }

        /**
         * val builder = WebViewCacheBuilder().setDiskCache().setMemoryCache()
         *
         * builder.build(context)
         * WebViewCacheManage.init(builder.build(context))
         */
        @JvmStatic
        fun init(webViewCacheManage: WebViewCacheManage) {
            mWebViewCache = webViewCacheManage
        }

        private fun initWebViewCacheManage(context: Context) {
            val builder = WebViewCacheBuilder()
            mWebViewCache = builder.build(context)
        }

        @JvmStatic
        fun tearDown() {
            mWebViewCache = null
        }
    }

    fun clearCache() {
        mDiskCache?.clear()
        mMemoryCache?.clear()
    }

    internal constructor(context: Context, memoryCache: MemoryCache?, diskCache: DiskCache?, diskExecutor: Executor) {
        mContext = context.applicationContext
        mContext?.registerComponentCallbacks(this)
        this.mMemoryCache = memoryCache
        this.mDiskCache = diskCache
        this.mDiskCacheExecutor = diskExecutor
    }

    fun release() {
        mContext?.unregisterComponentCallbacks(this)
        mDiskCache?.clear()
        mMemoryCache?.clear()
    }

    /**
     * 获取WebResourceResponse
     * MemoryCache / DiskCache / Http
     */
    fun getWebResourceResponse(client: CacheWebViewClient, url: String?): WebResourceResponse? {
        val url = url ?: return null
        if (mDiskCache == null) {
            return null
        }

        if (!url.startsWith("http")) {
            return null
        }
        val extension = getFileExtensionFromUrl(url)
        val mimeType = getMimeTypeFromExtension(extension)

        if (TextUtils.isEmpty(extension)) {
            return null
        }

        if (CacheExtensionConfig.isMedia(extension)) {
            return null
        }
        if (!CacheExtensionConfig.canCache(extension)) {
            return null
        }

        if (CacheExtensionConfig.isHtml(extension)) {
            return null
        }

        val urlKey = UrlCacheKey(url)

        val cacheValue = getCacheInputStream(urlKey)

        if (cacheValue?.getInputStream() != null && cacheValue.getHeader() != null) {
            val webResourceResponse = WebResourceResponse(mimeType, client.getEncoding(), cacheValue.getInputStream())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webResourceResponse.responseHeaders = cacheValue.getHeader()
            }
            return webResourceResponse
        }

//        val inputStream = httpRequest(client, urlKey)
        val inputStream = okHttpClient3Request(client, urlKey)
        if (inputStream?.innerInputStream != null) {
            val webResourceResponse = WebResourceResponse(mimeType, client.getEncoding(), inputStream.innerInputStream)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webResourceResponse.responseHeaders = inputStream.headers
            }
            return webResourceResponse
        }
        return null
    }

    /**
     * 使用OkHttpClient3请求资源
     */
    private fun okHttpClient3Request(client: CacheWebViewClient, key: CacheKey): WebResourceInputStream? {

        YKLogUtil.e(TAG, "---start download " + key.getUrl())
        var resourceInputStream: WebResourceInputStream? = null
        http {
            url = key.getUrl()!!
            method = Method.GET
            asyncExec = false
            header = client.getHeader(key.getUrl())
            header?.put("Origin", client.getOriginUrl())
            header?.put("Referer", client.getRefererUrl())
            header?.put("User-Agent", client.getUserAgent())

            onSuccess { response ->
                YKLogUtil.e(TAG, "---download success " + key.getUrl())
                resourceInputStream = onRequestSuccess(key, response?.body()?.byteStream(), response?.headers()?.toMultimap())
            }

            onError { e ->
                YKLogUtil.e(TAG, "---download failed " + key.getUrl())
                e.printStackTrace()
            }
        }
        return resourceInputStream
    }

    /**
     * 使用Http网络请求下载资源
     */
    private fun httpRequest(client: CacheWebViewClient, key: CacheKey): WebResourceInputStream? {

        try {
            YKLogUtil.e("WebViewCache", "start download" + key.getUrl() + "\n" + Thread.currentThread().name)
            val urlRequest = URL(key.getUrl())

            val httpURLConnection = urlRequest.openConnection() as HttpURLConnection
            httpURLConnection.requestMethod = "GET"
            httpURLConnection.useCaches = false
            httpURLConnection.connectTimeout = 5000
            httpURLConnection.readTimeout = 10000

            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }


            val header = client.getHeader(key.getUrl())
            if (header != null) {
                for (entry in header.entries) {
                    httpURLConnection.setRequestProperty(entry.key, entry.value)
                }
            }

            httpURLConnection.setRequestProperty("Origin", client.getOriginUrl())
            httpURLConnection.setRequestProperty("Referer", client.getRefererUrl())
            httpURLConnection.setRequestProperty("User-Agent", client.getUserAgent())

            httpURLConnection.connect()
            val responseCode = httpURLConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = httpURLConnection.inputStream
                return onRequestSuccess(key, inputStream, httpURLConnection.headerFields)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 网络资源请求成功
     */
    private fun onRequestSuccess(key: CacheKey, inputStream: InputStream?, maps: Map<String, List<String>>?): WebResourceInputStream? {
        if (inputStream == null) {
            return null
        }
        val outputStream = inputStream.copyToOutputStream()
        val responseHeaders = getResponseHeader(maps)

        cacheResource(key, outputStream, responseHeaders)

        return WebResourceInputStream(outputStream?.copy2InputStream(), responseHeaders)
    }

    /**
     * 缓存资源
     */
    private fun cacheResource(key: CacheKey, outputStream: ByteArrayOutputStream?, responseHeaders: Map<String, String?>?) {
        // DiskLruCache
        mDiskCacheExecutor?.execute({
            mDiskCache?.put(key, StreamCacheValue.copy(responseHeaders, outputStream))
            save2MemoryCache(key, StreamCacheValue.copy(responseHeaders, outputStream))
        })
    }

    /**
     * 保存到内存
     * @param key key
     * @param value value
     */
    private fun save2MemoryCache(key: CacheKey, value: CacheValue?) {
        mMemoryCache?.put(key, value)
    }

    /**
     * 获取内存缓存
     * @param key key
     */
    private fun getMemoryCache(key: CacheKey): CacheValue? {
        val cacheValue: CacheValue? = mMemoryCache?.get(key)
        if (cacheValue != null) {
            YKLogUtil.d(TAG, "from memory cache " + key.getUrl())
        }
        return cacheValue
    }

    /**
     * 获取磁盘缓存
     * @param key key
     */
    private fun getDiskCache(key: CacheKey): CacheValue? {
        val cacheValue: CacheValue? = mDiskCache?.get(key)
        if (cacheValue != null && cacheValue is StreamCacheValue) {
            YKLogUtil.d(TAG, "from disk cache " + key.getUrl())
            save2MemoryCache(key, StreamCacheValue.copyOf(cacheValue))
        }
        return cacheValue
    }

    /**
     * 从缓存中获取CacheValue
     * @param key key
     */
    private fun getCacheInputStream(key: CacheKey): CacheValue? {
        // 获取内存缓存
        var cacheValue = getMemoryCache(key)
        if (cacheValue == null) {
            // 获取Disk缓存
            cacheValue = getDiskCache(key)
        }
        return cacheValue
    }

    /**
     * 获取缓存
     */
    fun getCache(url: String?):CacheValue? {
        val url = url ?: return null
        val urlKey = UrlCacheKey(url)
        return getCacheInputStream(urlKey)
    }

}