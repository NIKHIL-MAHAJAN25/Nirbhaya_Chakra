package com.example.nirbhaya_chakra

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nirbhaya_chakra.Data.RiskLevel

// ──────────────── COLOR PALETTE ────────────────
private val BgDark       = Color(0xFF0F0F1A)
private val SurfaceCard  = Color(0xFF1A1A2E)
private val SurfaceDeep  = Color(0xFF16162A)
private val TextPrimary  = Color(0xFFE0E0E0)
private val TextMuted    = Color(0xFF9E9E9E)
private val SafeColor    = Color(0xFF2D9E5F)
private val ModerateColor= Color(0xFFFFD166)
private val HighColor    = Color(0xFFFF8C42)
private val CriticalColor= Color(0xFFE63946)

@Composable
fun HomeScreen(viewModel: RiskViewModel = viewModel()) {

    val data by viewModel.riskData.collectAsState()
    val risk = data?.riskScore ?: 0
    val level = data?.level ?: RiskLevel.SAFE

    // Dynamic color based on risk level
    val targetColor = when (level) {
        RiskLevel.SAFE     -> SafeColor
        RiskLevel.MODERATE -> ModerateColor
        RiskLevel.HIGH     -> HighColor
        RiskLevel.CRITICAL -> CriticalColor
    }
    val animatedColor by animateColorAsState(targetColor, tween(800), label = "riskColor")

    // Pulse animation when Critical
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (level == RiskLevel.CRITICAL) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        containerColor = BgDark,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* TODO: trigger SOS */ },
                containerColor = CriticalColor,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .shadow(12.dp, RoundedCornerShape(28.dp))
            ) {
                Icon(Icons.Default.Warning, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("EMERGENCY SOS", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BgDark)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            // ──────────── HEADER ────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Hello, Priya",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Text(
                        "SafeCircle Active",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ──────────── RISK SCORE CIRCLE ────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(animatedColor.copy(alpha = 0.15f))
                )
                // Mid ring
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(animatedColor.copy(alpha = 0.25f))
                )
                // Inner solid
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(animatedColor, animatedColor.copy(alpha = 0.7f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$risk",
                            color = Color.White,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            level.name,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ──────────── EXPLAINABILITY CARD ────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = animatedColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "WHY THIS SCORE",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                    val reasons = data?.reasons ?: listOf("All systems normal")
                    reasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(animatedColor)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                reason,
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ──────────── MAP CARD ────────────
            val tripViewModel: TripViewModel = viewModel()
            val tripState by tripViewModel.tripState.collectAsState()
            val currentPos by tripViewModel.currentPosition.collectAsState()

// Replace existing RiskMap call with:
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                RiskMap(
                    currentPosition = currentPos,
                    tripState = tripState,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(10.dp))

// Trip mode buttons
            if (!tripState.isActive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Free mode button
                    Button(
                        onClick = { tripViewModel.startFreeMode() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A2E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Free Mode", color = TextPrimary, fontSize = 12.sp)
                    }

                    // Preset trip buttons
                    Button(
                        onClick = { tripViewModel.startTrip("HSR Layout") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A2E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("→ HSR", color = TextPrimary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { tripViewModel.startTrip("Indiranagar") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A2E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("→ Indira", color = TextPrimary, fontSize = 12.sp)
                    }
                }
            } else {
                // Active trip controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { tripViewModel.injectDeviation() },
                        colors = ButtonDefaults.buttonColors(containerColor = HighColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Inject Deviation", color = Color.White, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { tripViewModel.stopTrip() },
                        colors = ButtonDefaults.buttonColors(containerColor = CriticalColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stop Trip", color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ──────────── INSIGHT METRICS ────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InsightCard(
                    icon = Icons.Default.Warning,
                    title = "SPEED",
                    value = "Normal",
                    accent = SafeColor,
                    modifier = Modifier.weight(1f)
                )
                InsightCard(
                    icon = Icons.Default.Place,
                    title = "ROUTE",
                    value = if (data?.isDeviatingRoute == true) "Deviation" else "On Track",
                    accent = if (data?.isDeviatingRoute == true) HighColor else SafeColor,
                    modifier = Modifier.weight(1f)
                )
                InsightCard(
                    icon = Icons.Default.Warning,
                    title = "FOLLOWER",
                    value = if (data?.followerDetected == true) "Detected" else "None",
                    accent = if (data?.followerDetected == true) CriticalColor else SafeColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(80.dp)) // space for FAB
        }
    }
}

@Composable
fun InsightCard(
    icon: ImageVector,
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(title, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}