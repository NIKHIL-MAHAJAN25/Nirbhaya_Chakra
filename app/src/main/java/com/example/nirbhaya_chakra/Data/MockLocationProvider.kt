package com.example.nirbhaya_chakra.Data



class MockLocationProvider {

    private val fakePath = listOf(

        // 🟢 Koramangala (SAFE)
        LocationData(12.9352, 77.6245, 1f),

        LocationData(12.9330, 77.6260, 1.2f),

        // 🟡 Moving
        LocationData(12.9310, 77.6275, 1.5f),

        LocationData(12.9275, 77.6300, 2.0f),

        // 🔴 Towards HSR (RISK)
        LocationData(12.9200, 77.6355, 2.5f),

        LocationData(12.9150, 77.6385, 3.0f),

        // 🔴 HSR (CRITICAL)
        LocationData(12.9116, 77.6412, 3.5f)
    )

    private var index = 0

    fun getNextLocation(): LocationData {
        val loc = fakePath[index]
        index = (index + 1) % fakePath.size
        return loc
    }
}