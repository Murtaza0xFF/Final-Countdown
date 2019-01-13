package com.murtaza.finalcountdown

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat

class FinalCountDownApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ContextCompat.startForegroundService(this, Intent(this, FinalCountDownService::class.java))
    }
}