package com.android9527.cachewebview

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.net.URL
import java.util.*

/**
 * Created by chenfeiyue on 2018/6/7.
 * Description ：CacheWebViewClient
 */
open class CacheWebViewClient(context: Context) : WebViewClient() {
    /**
     * 缓存是否开启
     */
    private var mIsEnableCache = true
    /**
     * 阻塞图片加载
     */
    private var mIsBlockImageLoad = false
    private var mEncoding = ""
    private val mVisitVectorUrl: Vector<String?> = Vector()
    private val mHeaderMaps: HashMap<String, MutableMap<String, String?>?> = HashMap()
    private var mUserAgent: String? = ""

    private val webViewCacheManage: WebViewCacheManage = WebViewCacheManage.get(context)

    fun setBlockNetworkImage(isBlock: Boolean) {
        mIsBlockImageLoad = isBlock
    }

    fun setEnableCache(enableCache: Boolean) {
        mIsEnableCache = enableCache
    }

    fun setUserAgent(agent: String?) {
        mUserAgent = agent
    }

    fun getUserAgent(): String? {
        return mUserAgent
    }

    fun addHeaderMap(url: String, additionalHttpHeaders: MutableMap<String, String?>?) {
        if (additionalHttpHeaders != null) {
            mHeaderMaps.put(url, additionalHttpHeaders)
        }
    }

    fun getHeader(url: String?): MutableMap<String, String?>? {
        return mHeaderMaps[url]
    }

    fun getEncoding(): String {
        return mEncoding
    }

    fun setEncoding(encoding: String) {
        mEncoding = encoding
    }

    fun addVisitUrl(url: String) {
        if (!mVisitVectorUrl.contains(url)) {
            mVisitVectorUrl.add(url)
        }
    }

    fun clearLastUrl() {
        if (mVisitVectorUrl.size > 0) {
            mVisitVectorUrl.removeAt(mVisitVectorUrl.size - 1)
        }
    }

    fun clearUrl() {
        mVisitVectorUrl.clear()
        mHeaderMaps.clear()
    }

    /**
     * getOriginUrl
     */
    fun getOriginUrl(): String? {
        if (mVisitVectorUrl.isEmpty()) {
            return ""
        }
        try {
            val lastElement = mVisitVectorUrl.lastElement()
            val url = URL(lastElement)
            val port = url.port
            return url.protocol + "://" + url.host + if (port == -1) "" else ":" + port
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * getRefererUrl
     */
    fun getRefererUrl(): String? {
        if (mVisitVectorUrl.isEmpty()) {
            return ""
        }
        try {
            if (mVisitVectorUrl.size > 0) {
                return mVisitVectorUrl[mVisitVectorUrl.size - 1]
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * onPageStarted
     */
    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        if (mIsBlockImageLoad) {
            val webSettings = view.settings
            webSettings.blockNetworkImage = true
        }
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        if (mIsBlockImageLoad) {
            val webSettings = view.settings
            webSettings.blockNetworkImage = false
        }
        super.onPageFinished(view, url)
    }

    override fun shouldInterceptRequest(view: WebView, url: String?): WebResourceResponse? {
        return if (!mIsEnableCache) {
            super.shouldInterceptRequest(view, url)
        } else webViewCacheManage.getWebResourceResponse(this, url)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest?): WebResourceResponse? {

        return if (!mIsEnableCache) {
            super.shouldInterceptRequest(view, request)
        } else webViewCacheManage.getWebResourceResponse(this, request?.url?.toString())
    }

    /**
     * 清除缓存
     */
    fun clearCache() {
        webViewCacheManage.clearCache()
    }

}