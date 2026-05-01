package com.example.nirbhaya_chakra.Data

data class RiskData(
    val riskScore: Int,          // 0-100
    val level: RiskLevel,           // "Safe" / "Moderate" / "High" / "Critical"
    val reasons: List<String>,   // ["Late hour", "Unfamiliar area", "HR spike"]
    val lat: Double,
    val lng: Double,
    val isDeviatingRoute: Boolean,
    val followerDetected: Boolean
)
enum class RiskLevel {
    SAFE, MODERATE, HIGH, CRITICAL
}