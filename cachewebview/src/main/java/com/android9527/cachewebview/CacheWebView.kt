package com.android9527.cachewebview

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.android9527.cachewebview.bean.CacheValue

/**
 * Created by chenfeiyue on 2018/6/7.
 * Description ：CacheWebView
 */
open class CacheWebView : WebView {

    /**
     * 缓存是否开启
     */
    private var mIsEnableCache = true

    private var mCacheWebViewClient: CacheWebViewClient? = null

    companion object {
        const val DEFAULT_ENCODING = "UTF-8"
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        initCacheWebViewClient()
        initSettings()
    }

    private fun initCacheWebViewClient() {
        mCacheWebViewClient?.setEncoding(DEFAULT_ENCODING)
        mCacheWebViewClient?.setUserAgent(this.settings.userAgentString)
    }

    private fun initSettings() {
        val webSettings = this.settings

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            webSettings.pluginState = WebSettings.PluginState.ON
        }
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = false
        webSettings.displayZoomControls = false

        webSettings.setAppCacheEnabled(true)
        webSettings.databaseEnabled = true
        val cachePath = context.cacheDir.absolutePath
        webSettings.setAppCachePath(cachePath)

        webSettings.loadsImagesAutomatically = true

        /**
         * WebView默认缓存类型
         */
//        webSettings.cacheMode = if (YKNetworkUtil.isNetConnected()) {
//             WebSettings.LOAD_NO_CACHE
//        } else {
//            WebSettings.LOAD_CACHE_ELSE_NETWORK
//        }

        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
    }

    fun setEnableCache(enableCache: Boolean) {
        mIsEnableCache = enableCache
    }

    fun setEncoding(encoding: String?) {
        var encode = ""
        encode = if (encoding.isNullOrEmpty()) {
            DEFAULT_ENCODING
        } else {
            encoding!!
        }
        mCacheWebViewClient?.setEncoding(encode)
    }

    fun setUserAgent(userAgent: String?) {
        val webSettings = this.settings
        webSettings.userAgentString = userAgent
        mCacheWebViewClient?.setUserAgent(userAgent)
    }

    /**
     * setWebViewClient
     *
     * @param innerClient CacheWebViewClientWrapper
     */
    override fun setWebViewClient(innerClient: WebViewClient) {
        if (innerClient !is CacheWebViewClient) {
            throw IllegalArgumentException("WebViewClient must be extends CacheWebViewClient!")
        }
        mCacheWebViewClient = innerClient
        initCacheWebViewClient()
        mCacheWebViewClient?.setEnableCache(mIsEnableCache)
        super.setWebViewClient(mCacheWebViewClient)
    }

    /**
     *
     */
    override fun loadUrl(url: String?) {
        if (url != null && url.startsWith("http") && mCacheWebViewClient != null) {
            mCacheWebViewClient!!.addVisitUrl(url)
        }
        super.loadUrl(url)
    }

    override fun loadUrl(url: String, additionalHttpHeaders: MutableMap<String, String?>?) {
        if (mCacheWebViewClient == null) {
            super.loadUrl(url)
        } else {
            mCacheWebViewClient?.addVisitUrl(url)
            if (additionalHttpHeaders != null) {
                mCacheWebViewClient?.addHeaderMap(url, additionalHttpHeaders)
                super.loadUrl(url, additionalHttpHeaders)
            } else {
                super.loadUrl(url)
            }
        }
    }

    override fun destroy() {
        mCacheWebViewClient?.clearUrl()

        this.stopLoading()
        this.clearHistory()
        this.removeAllViews()

        val viewParent = this.parent

        if (viewParent == null) {
            super.destroy()
            return
        }
        val parent = viewParent as ViewGroup
        parent.removeView(this)
        super.destroy()
    }

    override fun goBack() {
        if (canGoBack()) {
            mCacheWebViewClient?.clearLastUrl()
            super.goBack()
        }
    }

    fun clearCache() {
        WebViewCacheManage.get(context).clearCache()
    }

    fun release() {
        WebViewCacheManage.get(context).release()
    }

    fun getCache(url: String?): CacheValue? {
        return WebViewCacheManage.get(context).getCache(url)
    }
}