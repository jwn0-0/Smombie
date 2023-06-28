package com.example.smombie.controller

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.smombie.R

class AnalysisService : LifecycleService() {
    private val binder = LocalBinder()

    private var _isRunning: MutableLiveData<Boolean> = MutableLiveData()
    val isRunning: LiveData<Boolean> get() = _isRunning

    private val notification = createForegroundNotification()

    private val analyzerController = AnalyzerController(
        this,
        AnalyzerController.ANALYZER_CAMERA or
                AnalyzerController.ANALYZER_MIC or
                AnalyzerController.ANALYZER_GYRO
    )

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopService()
            return START_NOT_STICKY
        }

        _isRunning.value = true
        startForeground(1, notification)
        analyzerController.run()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopService() {
        _isRunning.value = false
        analyzerController.terminate()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createForegroundNotification(): Notification {
        val stopIntent =
            Intent(this, AnalysisService::class.java).apply {
                action = ACTION_STOP
            }

        val stopPendingIntent: PendingIntent =
            PendingIntent.getService(
                applicationContext, 0, stopIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val stopAction: Notification.Action = Notification.Action
            .Builder(null, "stop", stopPendingIntent).build()

        return Notification.Builder(this, getString(R.string.CHANNEL_ID))
            .setContentTitle(getString(R.string.app_name))
            .setContentText("${getString(R.string.app_name)} is working")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(stopAction)
            .setOngoing(false)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }

    inner class LocalBinder : Binder() {
        fun getService(): AnalysisService = this@AnalysisService
    }

    companion object {
        private const val TAG = "AnalysisService"
        const val ACTION_STOP = "Analyzing.stop"
    }
}