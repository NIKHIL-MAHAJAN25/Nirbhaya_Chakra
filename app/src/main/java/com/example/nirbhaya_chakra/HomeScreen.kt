package com.example.nirbhaya_chakra

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nirbhaya_chakra.Data.RiskLevel


@Composable
fun HomeScreen(viewModel: RiskViewModel = viewModel()) {

    val data by viewModel.riskData.collectAsState()

    val risk = data?.riskScore ?: 0
    val level = data?.level ?: RiskLevel.SAFE

    val color = when (level) {
        RiskLevel.SAFE -> Color(0xFF2ECC71)
        RiskLevel.MODERATE -> Color(0xFFF1C40F)
        RiskLevel.HIGH -> Color(0xFFE67E22)
        RiskLevel.CRITICAL -> Color(0xFFE74C3C)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* trigger SOS */ },
                containerColor = Color.Red
            ) {
                Text("SOS")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // 🔥 HEADER
            Text(
                text = "SafeCircle Active",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🔥 RISK CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = color),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Risk Score: $risk", fontSize = 22.sp, color = Color.White)
                    Text("Level: $level", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔥 MAP SECTION (placeholder for now)
            RiskMap(
                lat = data?.lat ?: 12.9352,
                lng = data?.lng ?: 77.6245,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔥 INSIGHTS ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InsightItem("Speed", "Normal")
                InsightItem("Route", "On Track")
                InsightItem("Follower", "No")
            }
        }


    }

}
@Composable
fun InsightItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontWeight = FontWeight.Bold)
        Text(value)
    }
}