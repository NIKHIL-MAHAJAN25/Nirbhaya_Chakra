package com.example.nirbhaya_chakra

import android.util.Log
import com.example.nirbhaya_chakra.Data.HotspotLocation

object HotspotRepository {

    var cachedHotspots: List<HotspotLocation> = emptyList()

    suspend fun fetchHotspots(): List<HotspotLocation> {
        val data = RetrofitClient.api.getHotspots()
        cachedHotspots = data
        return data

    }
}