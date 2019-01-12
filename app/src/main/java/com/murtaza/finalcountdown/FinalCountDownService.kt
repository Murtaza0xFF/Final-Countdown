package com.murtaza.finalcountdown

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class FinalCountDownService : Service() {

    private val timer = BehaviorSubject.create<Long>()
    private val duration: Long = 2 * 60 * 1000
    private var countDownProgress: Long = 0
    private val intent = Intent(FinalCountDownService.intentIdentifier)
    private lateinit var disposable: Disposable
    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        disposable = timer
            .switchMap { time ->
                Observable.intervalRange(0, time, 0, 1, TimeUnit.MILLISECONDS)
                    .map<Any> { t -> time - t }
            }
            .doOnNext(({
                countDownProgress = it.toString().toLong()
            }))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(({
                intent.putExtra(FinalCountDownService.extraIdentifier, it.toString().toLong())
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }))
            .subscribe()

        timer.onNext(duration)
    }

    fun incrementTimer(milliSeconds: Long) {
        if (duration - countDownProgress < milliSeconds * 1000) {
            timer.onNext(duration)
        } else {
            timer.onNext(countDownProgress + milliSeconds)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    inner class LocalBinder : Binder() {
        fun getService(): FinalCountDownService = this@FinalCountDownService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    companion object {
        const val intentIdentifier = "FINAL_COUNTDOWN"
        const val extraIdentifier = "ITS_THE_FINAL_COUNTDOWN"
    }
}
