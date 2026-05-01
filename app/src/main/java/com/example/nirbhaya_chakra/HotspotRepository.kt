package com.example.nirbhaya_chakra

import android.util.Log
import com.example.nirbhaya_chakra.Data.HotspotLocation

object HotspotRepository {

    suspend fun fetchHotspots(): List<HotspotLocation> {
        return try {
            val response = RetrofitClient.api.getHotspots()
            Log.d("APP_DEBUG/API", "🔥 Hotspots fetched: ${response.size}")
            response
        } catch (e: Exception) {
            Log.e("APP_DEBUG/API", "💥 Error: ${e.message}", e)
            emptyList()
        }
    }
}