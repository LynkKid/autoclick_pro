package com.auto.click

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.auto.click.databinding.ActivityWebBinding
import com.google.android.gms.common.internal.ImagesContract

class WebActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebBinding
    private var url: String? = null
    private var customTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initToolbar()
        initWebView()
    }

    private fun initData() {
        val intent = intent
        val title = intent?.getStringExtra("title") ?: ""
        setTitle(title)

        url = intent?.getStringExtra(ImagesContract.URL)
        customTitle = intent?.getStringExtra("custom_title")
    }


    private fun initToolbar() {
        setSupportActionBar(binding.inToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.inToolbar.toolbar.title = getString(R.string.text_language)

        // Set navigation click listener
        binding.inToolbar.toolbar.setContentInsetStartWithNavigation(0)
//        binding.inToolbar.toolbar.setNavigationIcon(R.drawable.icon_close_black)
        binding.inToolbar.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initWebView() {
        val webView = binding.webView

        // Set WebChromeClient
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.webProgress.progress = newProgress
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)

                // Cập nhật title nếu chưa có title từ Intent
                if (this@WebActivity.title.isNullOrEmpty() || this@WebActivity.title == "") {
                    setTitle(title ?: "")
                }
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(view, callback)
                // Ẩn custom view nếu có
                callback?.onCustomViewHidden()
            }
        }

        // Set WebViewClient
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.webProgress.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.webProgress.visibility = View.GONE

                // Ẩn progress bar nếu là trang YouTube
                if (url?.contains("www.youtube.com") == true) {
                    binding.viewVo.visibility = View.GONE
                }
            }
        }

        // Cấu hình WebView
        webView.requestFocus(130)
        webView.settings.displayZoomControls = false

        // Xử lý đặc biệt cho YouTube
        if (url?.contains("www.youtube.com") == true) {
            binding.viewVo.visibility = View.VISIBLE

            // Tạo URL YouTube player
            val videoId = customTitle ?: ""
            val screenWidth = resources.displayMetrics.widthPixels
            val playerUrl =
                "http://ott.bangtv.tv/yt/play.html?v=$videoId&w=${screenWidth * 0.38}&h=300"

            // Cấu hình đặc biệt cho YouTube
            webView.settings.apply {
                setSupportZoom(false)
                builtInZoomControls = false
                textZoom = 0
                layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
                loadWithOverviewMode = true
            }

            // Set height cho WebView
            val layoutParams = webView.layoutParams
            layoutParams.height = (300 * resources.displayMetrics.density).toInt()
            webView.layoutParams = layoutParams

            // Load URL YouTube player
            webView.loadUrl(playerUrl)
        } else {
            // Load URL thông thường
            url?.let {
                webView.loadUrl(it)
                webView.requestFocus()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        fun start(context: android.content.Context, url: String, title: String? = null) {
            val intent = Intent(context, WebActivity::class.java).apply {
                putExtra(ImagesContract.URL, url)
                putExtra("title", title ?: "")
            }
            context.startActivity(intent)
        }
    }
}