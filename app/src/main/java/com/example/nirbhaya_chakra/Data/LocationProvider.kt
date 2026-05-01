package com.example.nirbhaya_chakra.Data

interface LocationProvider {
    fun getLocation(): LocationData
}

data class LocationData(
    val lat: Double,
    val lng: Double,
    val speed: Float
)