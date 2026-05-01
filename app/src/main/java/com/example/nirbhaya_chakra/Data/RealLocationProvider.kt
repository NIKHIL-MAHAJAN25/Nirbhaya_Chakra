package com.example.nirbhaya_chakra.Data

import android.content.Context

class RealLocationProvider(
    private val context: Context
) : LocationProvider {

    override fun getLocation(): LocationData {
        // You already have fused location logic
        return LocationData(12.9716, 77.5946, 1.2f) // replace later
    }
}