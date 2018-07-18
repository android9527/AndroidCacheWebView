package android9527.android.cachewebview

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.android9527.cachewebview.CacheWebView
import com.android9527.cachewebview.CacheWebViewClient


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mWebView: CacheWebView
    private lateinit var mBtnLoad: Button
    private lateinit var mBtnClearCache: Button
    private lateinit var mBtnGetCache: Button
    private lateinit var mBtnOpenCache: Button
    private lateinit var mImageView: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initWebView()
        initListener()
    }

    fun initListener() {
        mBtnGetCache = findViewById(R.id.btn_get_cache)
        mBtnClearCache = findViewById(R.id.btn_clear_cache)
        mBtnOpenCache = findViewById(R.id.btn_open_cache)
        mBtnLoad = findViewById(R.id.btn_load)

        mImageView = findViewById(R.id.image_view)
        mBtnOpenCache.setOnClickListener(this)
        mBtnGetCache.setOnClickListener(this)
        mBtnClearCache.setOnClickListener(this)
        mBtnLoad.setOnClickListener(this)
    }

    private fun initWebView() {
        mWebView = findViewById(R.id.web_view)
        val webClient = CacheWebViewClient(this)
        mWebView.webViewClient = webClient
        mWebView.loadUrl("https://static.youku.com/h5/html/share/images/h5_300x300.png")

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_load -> {
                mWebView.loadUrl("http://www.youku.com")
            }
            R.id.btn_get_cache -> {

                val cacheValue = mWebView.getCache("https://static.youku.com/h5/html/share/images/h5_300x300.png")
                        ?: return
                val bitmap = BitmapFactory.decodeStream(cacheValue.getInputStream())
                        ?: return
                mImageView.setImageBitmap(bitmap)
            }
            R.id.btn_clear_cache -> {
                mWebView.clearCache()
            }
            R.id.btn_open_cache -> {
                mWebView.setEnableCache(true)
            }
        }
    }

    override fun onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        mWebView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mWebView.onResume()
    }

    override fun onDestroy() {
        mWebView.loadUrl("about:blank")
        mWebView.destroy()
        super.onDestroy()
    }

}
