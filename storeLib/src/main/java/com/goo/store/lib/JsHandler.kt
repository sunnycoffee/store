package com.goo.store.lib

import android.webkit.JavascriptInterface
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.blankj.utilcode.util.LogUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * JsHandler
 */
internal class JsHandler(private val context: WebActivity) {

    private val gson = Gson()
    private val eventType = object : TypeToken<Map<String, Any>>() {}.type

    @JavascriptInterface
    fun postMessage(eventName: String?) {
        postMessage(eventName, null)
    }

    @JavascriptInterface
    fun postMessage(eventName: String?, data: String?) {
        var event: Map<String, Any>? = null
        if (!data.isNullOrEmpty()) {
            event = gson.fromJson<Map<String, Any>>(data, eventType)
        }
        AppsFlyerLib.getInstance()
            .logEvent(context, eventName, event, object : AppsFlyerRequestListener {
                override fun onSuccess() {
                    LogUtils.d("logEvent onSuccess")
                }

                override fun onError(p0: Int, p1: String) {
                    LogUtils.d("logEvent onError:$p1")
                }
            })
    }
}