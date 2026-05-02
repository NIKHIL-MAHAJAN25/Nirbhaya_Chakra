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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.sample

class RiskForegroundService : Service() {

    companion object {
        const val ACTION_STOP_SERVICE = "STOP_RISK_SERVICE"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        Log.d("APP_DEBUG/SERVICE", "Service Started")
        startForegroundNow()
        BleSOSBroadcaster.startListening(this) { lat, lng ->
            Log.d("BLE_RELAY", "Someone nearby needs help at $lat, $lng")
            // Relay to backend on their behalf
            scope.launch {
                try {
                    RetrofitClient.api.sendLocation(
                        LocationRequest(lat = lat, lng = lng, riskScore = 100)
                    )
                    Log.d("BLE_RELAY", "Relayed SOS to server")
                } catch (e: Exception) {
                    Log.e("BLE_RELAY", "Relay failed: ${e.message}")
                }
            }
        }

        // Observe RiskRepository — whenever TripViewModel updates it,
        // service sends to backend
        scope.launch {
            RiskRepository.riskData
                .debounce(5000) // 🔥 send every 5 sec max
                .distinctUntilChanged() // 🔥 avoid duplicate spam
                .collectLatest { data ->
                    data ?: return@collectLatest
                    try {
                        Log.d("APP_DEBUG/API", "📡 Sending: ${data.lat}, ${data.lng}")

                        val response = RetrofitClient.api.sendLocation(
                            LocationRequest(
                                lat = data.lat,
                                lng = data.lng,
                                riskScore = data.riskScore
                            )
                        )

                        Log.d("APP_DEBUG/API", "✅ Success: ${response.code()}")

                    } catch (e: Exception) {
                        Log.e("APP_DEBUG/API", "❌ API FAILED", e)
                    }
                }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
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