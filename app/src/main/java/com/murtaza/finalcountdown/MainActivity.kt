package com.murtaza.finalcountdown

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Integer.parseInt


class MainActivity : AppCompatActivity() {

    private lateinit var finalCountDownService: FinalCountDownService

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val timeRemaining = intent.getLongExtra(FinalCountDownService.COUNT_DOWN_PROGRESS, 0)
            if (timeRemaining == -1L) {
                hideTimer()
                return
            }
            if (add.visibility != VISIBLE) {
                add.visibility = VISIBLE
            }
            msToTime(timeRemaining)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as FinalCountDownService.LocalBinder
            finalCountDownService = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        add.setOnClickListener {
            finalCountDownService.incrementTimer(10 * 1000)
        }
    }

    private fun msToTime(duration: Long) {
        val milliseconds = parseInt(((duration % 1000) / 100).toString())
        val seconds = parseInt(((duration / 1000) % 60).toString())
        val minutes = parseInt(((duration / (1000 * 60)) % 60).toString())

        textview.text = String.format("%02d:%02d:%01d", minutes, seconds, milliseconds)
    }

    private fun hideTimer() {
        textview.text = "DONE"
        add.visibility = GONE
    }

    override fun onStart() {
        super.onStart()
        Intent(this, FinalCountDownService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }


    override fun onResume() {
        super.onResume()
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter(FinalCountDownService.FINAL_COUNTDOWN_INTENT_IDENTIFIER))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(broadcastReceiver)
    }


    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
    }
}

