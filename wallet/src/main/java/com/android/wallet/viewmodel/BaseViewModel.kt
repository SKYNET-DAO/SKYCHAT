package com.android.wallet.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.Disposable
import java.util.logging.Logger

open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    protected val error = MutableLiveData<ErrorEnvelope>()
    protected val progress = MutableLiveData<Boolean>()
    protected var disposable: Disposable? = null

    override fun onCleared() {
        cancel()
        com.orhanobut.logger.Logger.e("----${javaClass.simpleName}---onCleared()--->")
    }

    protected fun cancel() {
        if (disposable != null && !disposable!!.isDisposed) {
            disposable!!.dispose()

        }
    }

    fun error(): LiveData<ErrorEnvelope> {
        return error
    }

    fun progress(): LiveData<Boolean> {
        return progress
    }
    protected fun onError(throwable: Throwable) {
        if (throwable is ServiceException) {
            error.postValue(throwable.error)
        } else {
            error.postValue(ErrorEnvelope(Constants.ErrorCode.UNKNOWN, throwable.message, throwable))
            // TODO: Add dialog with offer send error log to developers: notify about error.
            Log.d("SESSION", "Err", throwable)
        }
    }

}
