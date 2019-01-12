package com.murtaza.finalcountdown

import android.content.*
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Integer.parseInt


class MainActivity : AppCompatActivity() {

    private lateinit var finalCountDownService: FinalCountDownService
    private var bound: Boolean = false

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            msToTime(intent.getLongExtra(FinalCountDownService.extraIdentifier, 0))
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as FinalCountDownService.LocalBinder
            finalCountDownService = binder.getService()
            bound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startService(Intent(this, FinalCountDownService::class.java))
        textview.setOnClickListener {
            finalCountDownService.incrementTimer(10 * 1000)
        }
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
            .registerReceiver(broadcastReceiver, IntentFilter(FinalCountDownService.intentIdentifier))
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

    private fun msToTime(duration: Long) {
        val milliseconds = parseInt(((duration % 1000) / 100).toString())
        val seconds = parseInt(((duration / 1000) % 60).toString())
        val minutes = parseInt(((duration / (1000 * 60)) % 60).toString())

        textview.text = String.format("%02d:%02d:%02d", minutes, seconds, milliseconds)
    }
}

