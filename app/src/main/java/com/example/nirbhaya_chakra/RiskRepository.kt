package com.example.nirbhaya_chakra

import android.util.Log
import com.example.nirbhaya_chakra.Data.RiskData
import com.example.nirbhaya_chakra.Data.RiskLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object RiskRepository {

    // ✅ NEVER NULL
    private val _riskData = MutableStateFlow(
        RiskData(
            riskScore = 20,
            level = RiskLevel.SAFE,
            reasons = listOf("System initialized"),
            lat = 12.9352,
            lng = 77.6245,
            isDeviatingRoute = false,
            followerDetected = false
        )
    )

    val riskData: StateFlow<RiskData> = _riskData

    fun updateRiskData(data: RiskData) {
        Log.d("APP_DEBUG/REPO_INTERNAL", "🧠 Updating StateFlow: $data")
        _riskData.value = data
    }
}