package com.example.nirbhaya_chakra



import com.google.android.gms.maps.model.LatLng

// MOCK: Pre-defined Bangalore routes for demo
// Each route has waypoints (not just start/end) for realistic path following
object PresetRoutes {

    val KORAMANGALA_TO_HSR = listOf(
        LatLng(12.9352, 77.6245),  // Koramangala 5th Block
        LatLng(12.9330, 77.6260),  // Koramangala Sony Signal
        LatLng(12.9305, 77.6275),  // Near Jyoti Nivas
        LatLng(12.9280, 77.6290),  // Silk Board approach
        LatLng(12.9255, 77.6310),  // Silk Board junction
        LatLng(12.9230, 77.6325),  // HSR entry
        LatLng(12.9210, 77.6340),  // HSR Sector 1
        LatLng(12.9190, 77.6350),  // HSR Sector 2
        LatLng(12.9180, 77.6350),  // HSR Layout final
    )

    val KORAMANGALA_TO_INDIRANAGAR = listOf(
        LatLng(12.9352, 77.6245),  // Koramangala
        LatLng(12.9370, 77.6240),  // Jyoti Nivas
        LatLng(12.9400, 77.6230),  // EGL Tech Park
        LatLng(12.9430, 77.6220),  // Domlur
        LatLng(12.9460, 77.6210),  // Old Airport Road
        LatLng(12.9500, 77.6200),  // Domlur flyover
        LatLng(12.9550, 77.6190),  // CMH Road
        LatLng(12.9610, 77.6400),  // Indiranagar 100ft Road
        LatLng(12.9716, 77.6412),  // Indiranagar Metro
    )

    // Deviation point — 400m off normal path (injected mid-trip for demo)
    val DEVIATION_POINT = LatLng(12.9255, 77.6500)

    // All available preset destinations
    val destinations = mapOf(
        "HSR Layout" to KORAMANGALA_TO_HSR,
        "Indiranagar" to KORAMANGALA_TO_INDIRANAGAR
    )
}