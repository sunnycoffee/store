package com.goo.store.lib

import com.goo.store.lib.data.AppInfo
import com.goo.store.lib.data.BaseResult
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 *
 */
internal interface ThirdApi {

    @POST("api/v1/app/3333")
    @FormUrlEncoded
    fun loadConfig(
        @Field("appkey") appKey: String?,
        @Field("device_number") deviceNumber: String?
    ): Observable<BaseResult<AppInfo>>
}