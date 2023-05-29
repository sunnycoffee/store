package com.goo.store.lib

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 *
 */
internal class EncryptionInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val response: Response = chain.proceed(request)
        return decrypt(response)
    }

    @Throws(IOException::class)
    private fun decrypt(response: Response): Response {
        val body = response.body
        if (body == null || response.code != 200) return response
        val contentType = body.contentType()
        val source = body.source()
        source.request(Long.MAX_VALUE)
        val buffer = source.buffer()
        val originalText = buffer.clone().readString(StandardCharsets.UTF_8)
        return try {
            val dataKey = "data"
            val originalObj = JSONObject(originalText)
            val cipherData = originalObj.optString(dataKey)
            originalObj.remove(dataKey)
            val data: String? = StoreManager.decrypt(cipherData)
            if (!data.isNullOrEmpty()) {
                val dataObj = JSONObject(data)
                originalObj.put(dataKey, dataObj)
            }
            response.newBuilder()
                .body(ResponseBody.create(contentType, originalObj.toString()))
                .build()
        } catch (e: Exception) {
            e.printStackTrace()
            response
        }
    }

}