package android9527.android.cachewebview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.android9527.cachewebview.CacheWebView
import com.android9527.cachewebview.CacheWebViewClient

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mWebView: CacheWebView
    private lateinit var mBtnLoad: Button
    private lateinit var mBtnClearCache: Button
    private lateinit var mBtnGetCache: Button
    private lateinit var mBtnOpenCache: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initWebView()
    }

    fun initListener() {
        mBtnGetCache = findViewById(R.id.btn_get_cache)
        mBtnClearCache = findViewById(R.id.btn_clear_cache)
        mBtnOpenCache = findViewById(R.id.btn_open_cache)
        mBtnLoad = findViewById(R.id.btn_load)
        mBtnOpenCache.setOnClickListener(this)
        mBtnGetCache.setOnClickListener(this)
        mBtnClearCache.setOnClickListener(this)
        mBtnLoad.setOnClickListener(this)
    }

    private fun initWebView() {
        mWebView = findViewById(R.id.web_view)
        var webClient = CacheWebViewClient(this)
        mWebView.webViewClient = webClient
        mWebView.loadUrl("http://www.youku.com")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_load -> {

            }
            R.id.btn_get_cache -> {

            }
            R.id.btn_clear_cache -> {
                mWebView.clearCache()
            }
            R.id.btn_open_cache -> {
                mWebView.setEnableCache(true)
            }
        }
    }
}
