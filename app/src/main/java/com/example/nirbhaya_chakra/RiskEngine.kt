package com.example.nirbhaya_chakra



import com.example.nirbhaya_chakra.Data.RiskLevel
import java.time.LocalTime

object RiskEngine {

    fun calculateRisk(
        hotspotScore: Int,
        isDeviating: Boolean,
        isFollowerDetected: Boolean,
        speedVariance: Float,
        hour: Int
    ): Pair<Int, RiskLevel> {

        var risk = 0.0

        // 📍 Location weight (40%)
        risk += hotspotScore * 0.4

        // 🧭 Route deviation (20%)
        if (isDeviating) risk += 20

        // 👣 Follower detection (20%)
        if (isFollowerDetected) risk += 20

        // 🚶 Movement anomaly (10%)
        risk += (speedVariance * 10)

        // 🌙 Time-based risk (10%)
        risk += when (hour) {
            in 0..5 -> 15   // late night
            in 20..23 -> 10 // evening
            else -> 2
        }

        val finalRisk = risk.toInt().coerceIn(0, 100)

        return finalRisk to finalRisk.toRiskLevel()
    }
}