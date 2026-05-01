package com.example.nirbhaya_chakra.Data

data class ScoreUpdateRequest(
    val riskScore: Int,
    val lat: Double,
    val lng: Double,
    val reasons: List<String>
)