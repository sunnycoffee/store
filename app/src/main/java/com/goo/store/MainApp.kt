package com.goo.store

import android.app.Application
import com.goo.store.lib.StoreManager

/**
 * myApplication
 */
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        StoreManager.init(
            this,
            BuildConfig.BASE_URL,
            BuildConfig.APP_KEY,
            BuildConfig.TRACK_KEY,
            BuildConfig.DECRYPT_KEY
        )
    }
}