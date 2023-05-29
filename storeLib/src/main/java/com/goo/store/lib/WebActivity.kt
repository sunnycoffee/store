package com.goo.store.lib

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * web
 */
internal class WebActivity : Activity() {

    private var mWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        mWebView = findViewById(R.id.webView)

        initSettings()
        mWebView?.webViewClient = WebViewClient()
        mWebView?.webChromeClient = chromeClient
        mWebView?.loadUrl(intent.getStringExtra(URL) ?: "")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initSettings() {
        mWebView?.settings?.apply {
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            defaultTextEncodingName = "UTF-8"
            domStorageEnabled = true
        }
        mWebView?.addJavascriptInterface(JsHandler(this), "jsBridge")
    }

    private val chromeClient: WebChromeClient = object : WebChromeClient() {
    }

    companion object {

        private const val URL = "url"

        fun start(context: Context?, url: String) {
            val starter = Intent(context, WebActivity::class.java)
            starter.putExtra(URL, url)
            context?.startActivity(starter)
        }

    }
}