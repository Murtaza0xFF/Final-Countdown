package com.murtaza.finalcountdown

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit


class FinalCountDownService : Service() {

    private val timer = BehaviorSubject.create<Long>()

    private var countDownProgress: Long = 0
    private val intent = Intent(FinalCountDownService.FINAL_COUNTDOWN_INTENT_IDENTIFIER)
    private lateinit var disposable: Disposable
    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("101", "TimerNotificationChannel")
            } else {
                ""
            }
        val notification = NotificationCompat.Builder(this, channelId)
            .setPriority(NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .build()
        startForeground(1, notification)
        disposable = timer
            .switchMap { time ->
                Observable.intervalRange(0, time, 0, 1, TimeUnit.MILLISECONDS)
                    .map<Any> { t -> time - t }
                    .doOnNext(({
                        countDownProgress = it.toString().toLong()
                        broadcastTime(it.toString().toLong())
                    }))
                    .doOnComplete {
                        broadcastTime(-1)
                    }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()

        timer.onNext(DURATION)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val channel = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }

    fun incrementTimer(milliSeconds: Long) {
        if (DURATION - countDownProgress < milliSeconds) {
            timer.onNext(DURATION)
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

    private fun broadcastTime(countDownProgress: Long) {
        intent.putExtra(FinalCountDownService.COUNT_DOWN_PROGRESS, countDownProgress)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {
        const val FINAL_COUNTDOWN_INTENT_IDENTIFIER = "FINAL_COUNTDOWN"
        const val COUNT_DOWN_PROGRESS = "ITS_THE_FINAL_COUNTDOWN"
        const val DURATION: Long = 2 * 60 * 1000
    }
}


