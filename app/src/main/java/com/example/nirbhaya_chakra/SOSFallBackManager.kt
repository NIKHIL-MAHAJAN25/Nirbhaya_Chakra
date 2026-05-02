package com.example.nirbhaya_chakra



import android.content.Context
import android.util.Log
import com.example.nirbhaya_chakra.Data.LocationRequest
import com.example.nirbhaya_chakra.RetrofitClient
import com.example.nirbhaya_chakra.BleSOSBroadcaster
import com.example.nirbhaya_chakra.utils.HotspotSOSBroadcaster
import kotlinx.coroutines.*

object SOSFallbackManager {

    // ─── MAIN SOS TRIGGER — cascading fallback ───
    fun triggerSOS(
        context: Context,
        lat: Double,
        lng: Double,
        userId: String,
        userName: String,
        scope: CoroutineScope
    ) {
        scope.launch {

            // ── LAYER 1: Try cloud (Retrofit) ──
            Log.d("SOS_FLOW", "Layer 1: Trying cloud...")
            val cloudSuccess = tryCloud(lat, lng, userId)

            if (cloudSuccess) {
                Log.d("SOS_FLOW", "Layer 1 SUCCESS — alert sent via cloud")
                return@launch
            }

            // ── LAYER 2: BLE broadcast ──
            Log.d("SOS_FLOW", "Layer 1 FAILED — Layer 2: BLE broadcast")
            BleSOSBroadcaster.startSOSBroadcast(context, lat, lng, userId)

            // Wait 60s for a relay
            delay(60_000)

            // ── LAYER 3: Hotspot fallback ──
            Log.d("SOS_FLOW", "Layer 2 timeout — Layer 3: Hotspot")
            HotspotSOSBroadcaster.enableSOSHotspot(context, lat, lng, userId)

            // Wait 90s for connection
            delay(90_000)

            // ── LAYER 4: SMS (always works) ──
            Log.d("SOS_FLOW", "Layer 3 timeout — Layer 4: SMS fallback")
            SmsHelper.sendSosAlert(context, lat, lng, userName)

            Log.d("SOS_FLOW", "All layers exhausted — SMS sent as last resort")
        }
    }

    // ─── Try sending alert via Retrofit ───
    private suspend fun tryCloud(lat: Double, lng: Double, userId: String): Boolean {
        return try {
            val response = RetrofitClient.api.sendLocation(
                LocationRequest(lat = lat, lng = lng, riskScore = 100)
            )
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("SOS_FLOW", "Cloud failed: ${e.message}")
            false
        }
    }
}