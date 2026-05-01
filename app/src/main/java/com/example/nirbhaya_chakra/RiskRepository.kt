package com.example.nirbhaya_chakra

import android.util.Log
import com.example.nirbhaya_chakra.Data.RiskData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object RiskRepository {

    private val _riskData = MutableStateFlow<RiskData?>(null)
    val riskData: StateFlow<RiskData?> = _riskData

    fun updateRiskData(data: RiskData) {
        Log.d("APP_DEBUG/REPO_INTERNAL", "🧠 Updating StateFlow: $data")
        _riskData.value = data
    }
}