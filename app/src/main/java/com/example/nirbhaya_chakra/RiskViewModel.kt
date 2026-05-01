package com.example.nirbhaya_chakra

import androidx.lifecycle.ViewModel
import com.example.nirbhaya_chakra.Data.RiskData
import kotlinx.coroutines.flow.StateFlow

class RiskViewModel : ViewModel() {

    val riskData: StateFlow<RiskData?> = RiskRepository.riskData
}