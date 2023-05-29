package com.goo.store.lib

import com.goo.store.lib.data.BaseResult
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * 默认处理
 *
 * @author KongFei
 */
internal class ApiObserver<T> : Observer<BaseResult<T>> {

    private var onSuccess: ((T?) -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null

    constructor (onSuccess: ((T?) -> Unit)?, onError: ((Throwable) -> Unit)?) {
        this.onSuccess = onSuccess
        this.onError = onError
    }

    override fun onSubscribe(d: Disposable) {

    }

    override fun onNext(result: BaseResult<T>) {
        if (result.isOk()) {
            onSuccess?.invoke(result.data)
        } else {
            onError?.invoke(newAppException(result))
        }
    }


    override fun onError(e: Throwable) {
        onError?.invoke(e)
    }

    override fun onComplete() {

    }

    companion object {

        @Throws(Exception::class)
        fun <T> getDataObservable(result: BaseResult<T>): Observable<T> {
            return if (result.isOk())
                Observable.just(result.data)
            else
                throw newAppException(result)
        }

        @Throws(Exception::class)
        fun <T> getData(result: BaseResult<T>): T? {
            return if (result.isOk())
                result.data
            else
                throw newAppException(result)
        }

        fun <T> check(result: BaseResult<T>) {
            if (!result.isOk()) throw newAppException(result)
        }


        private fun <T> newAppException(result: BaseResult<T>) =
            Exception(result.msg)
    }
}