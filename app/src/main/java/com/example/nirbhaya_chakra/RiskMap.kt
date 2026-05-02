package com.example.nirbhaya_chakra

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.nirbhaya_chakra.Data.HotspotLocation
import com.example.nirbhaya_chakra.Data.MapMode
import com.example.nirbhaya_chakra.Data.TripState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.runtime.collectAsState

@Composable
 fun RiskMap(
    currentPosition: LatLng,
    tripState: TripState,
    isDeviating: Boolean = false,    // ← add this
    hotspots: List<HotspotLocation> = emptyList(),
    modifier: Modifier = Modifier
){
    // Camera follows user
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentPosition, 16f)
    }

    // Smoothly move camera to follow marker
    LaunchedEffect(currentPosition) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLng(currentPosition),
            300
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = false
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false
        )
    ) {

        // ──── Blue dot (user location) ────
        Marker(
            state = MarkerState(position = currentPosition),
            title = "You",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        )

        // ──── Trail behind user (blue breadcrumb line) ────
        if (tripState.trailPoints.size > 1) {
            Polyline(
                points = tripState.trailPoints,
                color = androidx.compose.ui.graphics.Color(0xFF4285F4), // Google blue
                width = 12f
            )
        }

        // ──── Route path ahead (grey, only in Trip mode) ────
        if (tripState.mode == MapMode.TRIP && tripState.routePoints.isNotEmpty()) {
            Polyline(
                points = tripState.routePoints,
                color = androidx.compose.ui.graphics.Color(0xFF9E9E9E), // grey path ahead
                width = 8f,
                pattern = listOf(
                    com.google.android.gms.maps.model.Dot(),
                    com.google.android.gms.maps.model.Gap(10f)
                )
            )
        }

        // ──── Destination marker (Trip mode only) ────
        if (tripState.mode == MapMode.TRIP && tripState.destination != null) {
            Marker(
                state = MarkerState(position = tripState.destination),
                title = tripState.destinationLabel,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        }

        // ──── Deviation marker (when off route) ────
        if (tripState.trailPoints.isNotEmpty()) {
            val lastPoint = tripState.trailPoints.last()
            val riskData = RiskRepository.riskData.collectAsState().value
            if (isDeviating) {
                Marker(
                    state = MarkerState(position = lastPoint),
                    title = "DEVIATION",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
            }
        }

        // ──── Hotspot danger zones ────
        hotspots.forEach { hotspot ->
            Circle(
                center = LatLng(hotspot.lat, hotspot.lng),
                radius = 200.0,
                fillColor = when {
                    hotspot.score > 70 -> androidx.compose.ui.graphics.Color(0x40E63946)
                    hotspot.score> 40 -> androidx.compose.ui.graphics.Color(0x40FFD166)
                    else -> androidx.compose.ui.graphics.Color(0x402D9E5F)
                },
                strokeColor = when {
                    hotspot.score > 70 -> androidx.compose.ui.graphics.Color(0xFFE63946)
                    hotspot.score > 40 -> androidx.compose.ui.graphics.Color(0xFFFFD166)
                    else -> androidx.compose.ui.graphics.Color(0xFF2D9E5F)
                },
                strokeWidth = 2f
            )
        }
    }
}