package com.murtaza.finalcountdown

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Integer.parseInt


class MainActivity : AppCompatActivity() {

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            msToTime(intent.getLongExtra(FinalCountDownService.extraIdentifier, 0))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startService(Intent(this, FinalCountDownService::class.java))
//        textview.setOnClickListener {
//            timer.onNext(countDownProgress + 10 * 1000)
//        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiver, IntentFilter (FinalCountDownService.intentIdentifier));
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }

    private fun msToTime(duration: Long) {
        val milliseconds = parseInt(((duration % 1000) / 100).toString())
        val seconds = parseInt(((duration / 1000) % 60).toString())
        val minutes = parseInt(((duration / (1000 * 60)) % 60).toString())

        textview.text = String.format("%02d:%02d:%02d", minutes, seconds, milliseconds)
    }
}

