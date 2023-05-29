package com.goo.store.lib

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.appsflyer.AppsFlyerLib
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.EncryptUtils

/**
 *
 */
@SuppressLint("StaticFieldLeak")
object StoreManager {

    private var context: Context? = null
    private var appKey: String? = null
    private var decryptKey: String? = null

    private val api: ThirdApi by lazy {
        HttpClient.getInstance().createService(ThirdApi::class.java)
    }

    fun init(
        context: Context,
        baseUrl: String?,
        appKey: String?,
        trackKey: String?,
        decryptKey: String?
    ) {
        this.context = context.applicationContext
        this.appKey = appKey
        this.decryptKey = decryptKey
        AppsFlyerLib.getInstance().setDebugLog(BuildConfig.DEBUG)
        AppsFlyerLib.getInstance().init(trackKey ?: "", null, context.applicationContext)
        AppsFlyerLib.getInstance().start(context)
        HttpClient.getInstance().init( baseUrl)
        HttpClient.getInstance().setDebug(BuildConfig.DEBUG)
    }

    fun loadConfig() {
        api.loadConfig(appKey, "")
            .compose(HttpClient.switcher())
            .subscribe(ApiObserver({
                it?.version?.let { version ->
                    if (version > AppUtils.getAppVersionCode()) {
                        startBrowser(it.downurl)
                    } else if (it.needJump()) {
                        startBrowser(it.wapurl)
                    }
                }
            }) {

            })
    }

    fun openWeb(context: Context?, url: String?) {
        WebActivity.start(context, url ?: "")
    }

    fun decrypt(data: String?): String? {
        return try {
            if (data == null) return ""
            val result = EncryptUtils.decryptBase64AES(
                data.toByteArray(),
                decryptKey?.toByteArray(),
                "AES/ECB/PKCS5Padding",
                null
            )
            String(result)
        } catch (e: Exception) {
            e.printStackTrace()
            data
        }
    }

    private fun startBrowser(url: String?) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
                or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        context?.startActivity(intent)
    }
}