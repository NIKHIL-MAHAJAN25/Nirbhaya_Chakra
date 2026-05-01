package com.example.nirbhaya_chakra

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.nirbhaya_chakra.Data.LocationRequest
import com.example.nirbhaya_chakra.Data.RiskData
import com.example.nirbhaya_chakra.RiskRepository
import com.example.nirbhaya_chakra.APIService
import kotlinx.coroutines.*

class RiskForegroundService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val socketRepository = SocketRepository()

    private val fakePath = listOf(
        Triple(12.9352, 77.6245, 1f),
        Triple(12.9330, 77.6260, 1.2f),
        Triple(12.9310, 77.6275, 1.5f),
        Triple(12.9275, 77.6300, 2.0f),
        Triple(12.9200, 77.6355, 2.5f),
        Triple(12.9150, 77.6385, 3.0f),
        Triple(12.9116, 77.6412, 3.5f)
    )

    private var index = 0

    override fun onCreate() {
        super.onCreate()

        Log.d("APP_DEBUG/SERVICE", "🚀 Service Started")

        startForegroundNow()


        Log.d("APP_DEBUG/SOCKET", "🔌 Attempting socket connection")

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

                val data = RiskData(
                    riskScore = riskScore,
                    level = riskScore.toRiskLevel(),
                    reasons = listOf("Movement pattern"),
                    lat = lat,
                    lng = lng,
                    isDeviatingRoute = riskScore > 70,
                    followerDetected = riskScore > 80
                )

                // 🔥 STEP 1: SERVICE LOG
                Log.d("APP_DEBUG/SERVICE", """
                    📍 New Location Generated
                    Lat: $lat
                    Lng: $lng
                    Speed: $speed
                    Risk: $riskScore
                """.trimIndent())

                // 🔥 STEP 2: REPOSITORY UPDATE
                RiskRepository.updateRiskData(data)
                Log.d("APP_DEBUG/REPO", "📦 Data pushed to repository")

                // 🔥 STEP 3: SOCKET SEND
                scope.launch {
                    try {
                        val response = RetrofitClient.api.sendLocation(
                            LocationRequest(
                                lat = lat,
                                lng = lng,
                                riskScore = riskScore
                            )
                        )

                        if (response.isSuccessful) {
                            Log.d("APP_DEBUG/API", "✅ Sent successfully to server")
                        } else {
                            Log.e("APP_DEBUG/API", "❌ Failed: ${response.code()}")
                        }

                    } catch (e: Exception) {
                        Log.e("APP_DEBUG/API", "💥 Error: ${e.message}")
                    }
                }
            }
        }
    }

    private fun startForegroundNow() {
        val channelId = "risk_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Risk Monitoring",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SafeCircle Active")
            .setContentText("Tracking safety...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d("APP_DEBUG/SERVICE", "❌ Service Destroyed")
        scope.cancel()
    }
}