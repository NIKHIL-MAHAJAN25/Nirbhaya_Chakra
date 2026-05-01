package com.example.nirbhaya_chakra.Data

import com.google.android.gms.maps.model.LatLng

// Two ways the map can operate
enum class MapMode {
    FREE,    // no destination, just track movement
    TRIP     // destination set, follow route path
}

// Holds trip state
data class TripState(
    val mode: MapMode = MapMode.FREE,
    val destination: LatLng? = null,
    val destinationLabel: String = "",
    val routePoints: List<LatLng> = emptyList(),  // path to follow in TRIP mode
    val trailPoints: List<LatLng> = emptyList(),   // breadcrumb trail behind user
    val progress: Float = 0f,                       // 0.0 → 1.0 for trip mode
    val isActive: Boolean = false
)