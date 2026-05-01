package com.example.nirbhaya_chakra

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.ui.graphics.Color

@Composable
fun RiskMap(
    lat: Double,
    lng: Double,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel()
) {

    val hotspots by viewModel.hotspots.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(lat, lng),
            14f
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {

        // 🔵 USER LOCATION
        Marker(
            state = MarkerState(position = LatLng(lat, lng)),
            title = "You"
        )

        // 🔥 HOTSPOTS → CIRCLES
        hotspots.forEach { hotspot ->

            val color = when {
                hotspot.score > 70 -> Color.Red
                hotspot.score > 40 -> Color.Yellow
                else -> Color.Green
            }

            Circle(
                center = LatLng(hotspot.lat, hotspot.lng),
                radius = 200.0,
                fillColor = color.copy(alpha = 0.3f), // ✅ Compose way
                strokeColor = color,
                strokeWidth = 2f
            )
        }
    }
}