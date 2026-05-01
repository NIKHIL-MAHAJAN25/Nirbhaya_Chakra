package com.example.nirbhaya_chakra.Data

data class AlertRequest(
    val riskScore: Int,
    val lat: Double,
    val lng: Double,
    val reasons: List<String>
)