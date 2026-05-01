package com.example.nirbhaya_chakra

import com.example.nirbhaya_chakra.Data.RiskLevel

fun Int.toRiskLevel() = when {
    this <= 30 -> RiskLevel.SAFE
    this <= 60 -> RiskLevel.MODERATE
    this <= 75 -> RiskLevel.HIGH
    else       -> RiskLevel.CRITICAL
}