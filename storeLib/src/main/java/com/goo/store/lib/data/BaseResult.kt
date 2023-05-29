package com.goo.store.lib.data

/**
 *
 */
internal class BaseResult<T> {

    val code: Int = 0
    val msg: String? = null
    val data: T? = null
    val now: Long = 0

    fun isOk(): Boolean {
        return code == 0
    }

}

internal data class AppInfo(
    val name: String? = null,
    val wapurl: String? = null,
    val iswap: Int? = null,
    val splash: String? = null,
    val version: Int? = 0,
    val downurl: String? = null,
    val appsflyer_id: String? = null
) {
    fun needJump(): Boolean {
        return iswap == 1
    }
}