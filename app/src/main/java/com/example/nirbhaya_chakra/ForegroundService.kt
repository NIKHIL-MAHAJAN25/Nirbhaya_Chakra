package com.example.nirbhaya_chakra

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.nirbhaya_chakra.Data.RiskData
import com.example.nirbhaya_chakra.Data.RiskLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RiskForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.e("SERVICE_DEBUG", "onCreate called")

        startForegroundNow() // 🔥 MUST be here immediately
        CoroutineScope(Dispatchers.IO).launch {
            var score = 20
            while (true) {
                score = (score + (1..5).random()).coerceAtMost(100)
                RiskRepository.updateRiskData(
                    RiskData(
                        riskScore = score,
                        level = score.toRiskLevel(),
                        reasons = listOf("Late hour", "Unfamiliar area"),
                        lat = 12.9352,
                        lng = 77.6245,
                        isDeviatingRoute = false,
                        followerDetected = false
                    )
                )
                delay(2000)
            }
        }
    }

    private fun startForegroundNow() {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "risk_channel")
            .setContentTitle("SafeCircle Active")
            .setContentText("Tracking safety...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_MAX) // 🔥 important
            .setOngoing(true) // 🔥 important
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "risk_channel",
                "Risk Monitoring",
                NotificationManager.IMPORTANCE_HIGH // 🔥 MUST be HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}