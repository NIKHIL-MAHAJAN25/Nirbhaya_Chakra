package com.example.nirbhaya_chakra.wear.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text


import kotlin.math.sqrt

// ──── Colors matching phone app ────
private val BgDark = Color(0xFF0F0F1A)
private val SafeColor = Color(0xFF2D9E5F)
private val ModerateColor = Color(0xFFFFD166)
private val HighColor = Color(0xFFFF8C42)
private val CriticalColor = Color(0xFFE63946)
private val TextMuted = Color(0xFF9E9E9E)

class WatchMainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var vibrator: Vibrator

    // Shake detection state
    private var lastShakeTime = 0L
    private var shakeCount = 0

    // Fall detection state
    private var lastMagnitude = 9.8f

    // Compose state holders
    private val _sosTriggered = mutableStateOf(false)
    private val _fallDetected = mutableStateOf(false)
    private val _shakeDetected = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init sensors
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        // Register accelerometer
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        setContent {
            WatchScreen(
                sosTriggered = _sosTriggered.value,
                fallDetected = _fallDetected.value,
                onSosDismiss = { _sosTriggered.value = false },
                onFallDismiss = { _fallDetected.value = false },
                onTapSos = { triggerSos() }
            )
        }
    }

    // ──── Accelerometer: shake + fall ────
    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)

        // SHAKE: 3 hard shakes in 2 seconds
        if (magnitude > 18f) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime < 2000) {
                shakeCount++
                if (shakeCount >= 3) {
                    triggerSos()
                    shakeCount = 0
                }
            } else {
                shakeCount = 1
            }
            lastShakeTime = now
        }

        // FALL: freefall (~0) followed by spike (>20)
        if (lastMagnitude < 3f && magnitude > 20f) {
            _fallDetected.value = true
            vibrator.vibrate(longArrayOf(0, 200, 100, 200, 100, 500), -1)
        }
        lastMagnitude = magnitude
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun triggerSos() {
        _sosTriggered.value = true
        // MOCK: In production, sends via DataLayer to phone
        vibrator.vibrate(longArrayOf(0, 100, 100, 100, 100, 400), -1)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}

@Composable
fun WatchScreen(
    sosTriggered: Boolean,
    fallDetected: Boolean,
    onSosDismiss: () -> Unit,
    onFallDismiss: () -> Unit,
    onTapSos: () -> Unit
) {
    // MOCK: Score and HR animate independently
    var score by remember { mutableStateOf(35) }
    var hr by remember { mutableStateOf(72) }
    var stressLabel by remember { mutableStateOf("Calm") }

    // MOCK: Simulate live data ticking
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(3000)
            score = (score + (-3..5).random()).coerceIn(0, 100)
            hr = (hr + (-2..3).random()).coerceIn(60, 140)
            stressLabel = when {
                hr > 110 -> "Stressed"
                hr > 90  -> "Elevated"
                else     -> "Calm"
            }
        }
    }

    val riskColor = when {
        score <= 30 -> SafeColor
        score <= 60 -> ModerateColor
        score <= 75 -> HighColor
        else -> CriticalColor
    }
    val animatedColor by animateColorAsState(riskColor, tween(600), label = "color")

    // Pulse when critical
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (score > 75) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            tween(700, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .clickable { onTapSos() },
        contentAlignment = Alignment.Center
    ) {

        // ──── SOS Overlay ────
        if (sosTriggered) {
            SosOverlay(onDismiss = onSosDismiss)
            return@Box
        }

        // ──── Fall Overlay ────
        if (fallDetected) {
            FallOverlay(onDismiss = onFallDismiss)
            return@Box
        }

        // ──── Main Display ────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Risk score
            Box(
                modifier = Modifier
                    .scale(pulse)
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(animatedColor, animatedColor.copy(alpha = 0.5f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$score",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        when {
                            score <= 30 -> "SAFE"
                            score <= 60 -> "MODERATE"
                            score <= 75 -> "HIGH"
                            else -> "CRITICAL"
                        },
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Heart rate
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = CriticalColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("$hr bpm", color = Color.White, fontSize = 14.sp)
            }

            Spacer(Modifier.height(4.dp))

            // Stress
            Text(
                when (stressLabel) {
                    "Stressed" -> "😰 Stressed"
                    "Elevated" -> "😐 Elevated"
                    else -> "😌 Calm"
                },
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(Modifier.height(6.dp))

            // Hint
            Text(
                "TAP or SHAKE for SOS",
                color = TextMuted.copy(alpha = 0.5f),
                fontSize = 8.sp
            )
        }
    }
}

@Composable
fun SosOverlay(onDismiss: () -> Unit) {
    // Auto dismiss after 3s
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        onDismiss()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(500),
            RepeatMode.Reverse
        ),
        label = "sosAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CriticalColor.copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "🚨",
                fontSize = 32.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "SOS SENT",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Alerting your circle...",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun FallOverlay(onDismiss: () -> Unit) {
    var countdown by remember { mutableStateOf(30) }

    // Countdown timer
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            kotlinx.coroutines.delay(1000)
            countdown--
        }
        // MOCK: Auto SOS after countdown
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HighColor)
            .clickable { onDismiss() }, // tap to cancel
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "FALL DETECTED",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "$countdown",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Tap to cancel",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
    }
}