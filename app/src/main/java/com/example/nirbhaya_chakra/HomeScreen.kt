package com.example.nirbhaya_chakra

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Text
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun HomeScreen(viewModel: RiskViewModel = viewModel()) {

    val riskData = viewModel.riskData.collectAsState()

    Text(
        text = "Risk: ${riskData.value?.riskScore ?: 0}"
    )
}