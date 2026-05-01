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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class RiskForegroundService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        Log.d("APP_DEBUG/SERVICE", "Service Started")
        startForegroundNow()

        // Observe RiskRepository — whenever TripViewModel updates it,
        // service sends to backend
        scope.launch {
            RiskRepository.riskData.collectLatest { data ->
                data ?: return@collectLatest

                Log.d("APP_DEBUG/SERVICE", "Score: ${data.riskScore} | Lat: ${data.lat}")

                // Send to backend (fails silently if offline)
                try {
                    val response = RetrofitClient.api.sendLocation(
                        LocationRequest(
                            lat = data.lat,
                            lng = data.lng,
                            riskScore = data.riskScore
                        )
                    )
                    if (response.isSuccessful) {
                        Log.d("APP_DEBUG/API", "Sent to server")
                    } else {
                        Log.e("APP_DEBUG/API", "Failed: ${response.code()}")
                    }
                } catch (e: Exception) {
                    // TODO: Queue in Room for offline retry
                    Log.e("APP_DEBUG/API", "Offline: ${e.message}")
                }
            }
        }
    }

    private fun startForegroundNow() {
        val channelId = "risk_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Risk Monitoring",
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
        scope.cancel()
    }
}