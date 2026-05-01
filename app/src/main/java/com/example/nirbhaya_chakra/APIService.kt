package com.example.nirbhaya_chakra

import com.example.nirbhaya_chakra.Data.LocationRequest
import com.example.nirbhaya_chakra.Data.HotspotLocation
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface APIService {
    @POST("api/user-location")

    suspend fun sendLocation(
        @Body request: LocationRequest
    ): Response<Unit>
    @GET("api/all-locations") // 👈 confirm endpoint with teammate
    suspend fun getHotspots(): List<HotspotLocation>
}