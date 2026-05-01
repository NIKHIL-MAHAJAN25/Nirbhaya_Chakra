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
import com.example.nirbhaya_chakra.data.RiskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RiskForegroundService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO)

    // 🔥 Koramangala → HSR path
    private val fakePath = listOf(
        Triple(12.9352, 77.6245, 1f),   // Koramangala (safe)
        Triple(12.9330, 77.6260, 1.2f),
        Triple(12.9310, 77.6275, 1.5f),
        Triple(12.9275, 77.6300, 2.0f),
        Triple(12.9200, 77.6355, 2.5f),
        Triple(12.9150, 77.6385, 3.0f),
        Triple(12.9116, 77.6412, 3.5f)  // HSR (high risk)
    )

    private var index = 0

    override fun onCreate() {
        super.onCreate()
        Log.e("SERVICE_DEBUG", "onCreate called")

        startForegroundNow()

        startMockMovement()
    }

    private fun startMockMovement() {
        scope.launch {
            while (true) {
                delay(3000)

                val (lat, lng, speed) = fakePath[index]
                index = (index + 1) % fakePath.size

                val riskScore = when {
                    speed < 1.5f -> 20
                    speed < 2.5f -> 50
                    else -> 85
                }

                val level = riskScore.toRiskLevel()

                val data = RiskData(
                    riskScore = riskScore,
                    level = riskScore.toRiskLevel(), // ✅ FIXED
                    reasons = listOf("Movement pattern"),
                    lat = lat,
                    lng = lng,
                    isDeviatingRoute = riskScore > 70,
                    followerDetected = riskScore > 80
                )

                Log.d("MOCK_PATH", "Lat: $lat Lng: $lng Speed: $speed Risk: $riskScore")

                RiskRepository.updateRiskData(data)
            }
        }
    }

    private fun startForegroundNow() {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "risk_channel")
            .setContentTitle("SafeCircle Active")
            .setContentText("Tracking safety...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "risk_channel",
                "Risk Monitoring",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}