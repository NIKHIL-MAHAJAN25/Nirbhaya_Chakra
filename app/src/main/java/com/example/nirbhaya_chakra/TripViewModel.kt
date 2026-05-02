package com.example.nirbhaya_chakra

import android.location.Location
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nirbhaya_chakra.Data.MapMode
import com.example.nirbhaya_chakra.Data.RiskData
import com.example.nirbhaya_chakra.Data.RiskLevel
import com.example.nirbhaya_chakra.Data.TripState
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TripViewModel : ViewModel() {

    private val _tripState = MutableStateFlow(TripState())
    val tripState: StateFlow<TripState> = _tripState

    private val _currentPosition = MutableStateFlow(LatLng(12.9352, 77.6245))
    val currentPosition: StateFlow<LatLng> = _currentPosition

    private var tripJob: Job? = null

    // Throttle + hotspot cache
    private var lastRiskUpdateTime = 0L
    private var lastHotspotCenter: LatLng? = null
    private var cachedHotspotScore = 0

    // ─── START TRIP ───
    fun startTrip(destinationName: String) {
        tripJob?.cancel()
        val route = PresetRoutes.destinations[destinationName] ?: return

        // Seed initial RiskData so null crashes never happen
        seedInitialRiskData(route.first())

        _tripState.value = TripState(
            mode = MapMode.TRIP,
            destination = route.last(),
            destinationLabel = destinationName,
            routePoints = route,
            trailPoints = listOf(route.first()),
            progress = 0f,
            isActive = true
        )

        tripJob = viewModelScope.launch {
            for (i in 0 until route.size - 1) {
                val from = route[i]
                val to = route[i + 1]
                val steps = 20
                val stepDelay = 300L

                for (step in 0..steps) {
                    val fraction = step.toFloat() / steps
                    val pos = LatLng(
                        from.latitude + (to.latitude - from.latitude) * fraction,
                        from.longitude + (to.longitude - from.longitude) * fraction
                    )

                    _currentPosition.value = pos
                    val currentState = _tripState.value
                    _tripState.value = currentState.copy(
                        trailPoints = currentState.trailPoints + pos,
                        progress = (i.toFloat() + fraction) / (route.size - 1)
                    )

                    updateRiskWithPosition(pos)
                    delay(stepDelay)
                }
            }
            _tripState.value = _tripState.value.copy(isActive = false, progress = 1f)
        }
    }

    // ─── FREE MODE ───
    fun startFreeMode() {
        tripJob?.cancel()
        seedInitialRiskData(_currentPosition.value)

        _tripState.value = TripState(
            mode = MapMode.FREE,
            isActive = true,
            trailPoints = listOf(_currentPosition.value)
        )

        tripJob = viewModelScope.launch {
            var lat = 12.9352
            var lng = 77.6245
            while (_tripState.value.isActive) {
                lat += (-0.0003..0.0003).random()
                lng += (-0.0003..0.0003).random()
                val pos = LatLng(lat, lng)
                _currentPosition.value = pos
                _tripState.value = _tripState.value.copy(
                    trailPoints = _tripState.value.trailPoints + pos
                )
                updateRiskWithPosition(pos)
                delay(3000)
            }
        }
    }

    // ─── STOP ───
    fun stopTrip() {
        tripJob?.cancel()
        _tripState.value = _tripState.value.copy(isActive = false)
    }

    // ─── INJECT DEVIATION (demo button) ───
    fun injectDeviation() {
        tripJob?.cancel()
        val deviation = PresetRoutes.DEVIATION_POINT
        _currentPosition.value = deviation
        _tripState.value = _tripState.value.copy(
            trailPoints = _tripState.value.trailPoints + deviation
        )
        val current = RiskRepository.riskData.value ?: return
        RiskRepository.updateRiskData(
            current.copy(
                lat = deviation.latitude,
                lng = deviation.longitude,
                riskScore = 85,
                level = RiskLevel.CRITICAL,
                isDeviatingRoute = true,
                reasons = listOf(
                    "Route deviation detected (380m off path)",
                    "Entering unfamiliar area",
                    "Late hour risk"
                )
            )
        )
    }

    // ─── SEED INITIAL DATA (prevents null crashes) ───
    private fun seedInitialRiskData(pos: LatLng) {
        if (RiskRepository.riskData.value != null) return
        RiskRepository.updateRiskData(
            RiskData(
                riskScore = 20,
                level = RiskLevel.SAFE,
                reasons = listOf("Monitoring active"),
                lat = pos.latitude,
                lng = pos.longitude,
                isDeviatingRoute = false,
                followerDetected = false
            )
        )
    }

    // ─── CORE RISK ENGINE ───
    private fun updateRiskWithPosition(pos: LatLng) {
        if (!shouldUpdateRisk()) return

        val current = RiskRepository.riskData.value ?: return

        // Time risk — works on all API levels
        val hour = java.util.Calendar.getInstance()
            .get(java.util.Calendar.HOUR_OF_DAY)

        val hotspotScore = getNearbyHotspotScore(pos)
        val timeRisk = getTimeRisk(hour)

        var finalRisk = hotspotScore * 0.5 + timeRisk
        if (current.isDeviatingRoute) finalRisk += 25
        if (current.followerDetected) finalRisk += 20

        val riskInt = finalRisk.toInt().coerceIn(0, 100)

        // Build dynamic reasons list
        val reasons = mutableListOf<String>()
        if (timeRisk >= 20) reasons.add("Late hour (${hour}:00)")
        if (hotspotScore > 50) reasons.add("Near danger zone (score: $hotspotScore)")
        if (current.isDeviatingRoute) reasons.add("Route deviation active")
        if (current.followerDetected) reasons.add("Possible follower detected")
        if (reasons.isEmpty()) reasons.add("All clear — monitoring active")

        RiskRepository.updateRiskData(
            current.copy(
                lat = pos.latitude,
                lng = pos.longitude,
                riskScore = riskInt,
                level = riskInt.toRiskLevel(),
                reasons = reasons
            )
        )
    }

    // ─── THROTTLE — max one risk update every 5s ───
    private fun shouldUpdateRisk(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastRiskUpdateTime < 5000) return false
        lastRiskUpdateTime = now
        return true
    }

    // ─── HOTSPOT PROXIMITY ───
    private fun getNearbyHotspotScore(pos: LatLng): Int {
        val radiusMeters = 200f
        lastHotspotCenter?.let { center ->
            if (distanceBetween(pos, center) < radiusMeters) {
                return cachedHotspotScore
            }
        }
        val hotspots = HotspotRepository.cachedHotspots
        val nearest = hotspots.minByOrNull {
            distanceBetween(pos, LatLng(it.lat, it.lng))
        }
        return if (nearest != null) {
            lastHotspotCenter = LatLng(nearest.lat, nearest.lng)
            cachedHotspotScore = nearest.score
            nearest.score
        } else 20
    }

    // ─── DISTANCE ───
    private fun distanceBetween(a: LatLng, b: LatLng): Float {
        val result = FloatArray(1)
        Location.distanceBetween(
            a.latitude, a.longitude,
            b.latitude, b.longitude,
            result
        )
        return result[0]
    }

    // ─── TIME RISK ───
    private fun getTimeRisk(hour: Int): Int = when (hour) {
        in 0..5   -> 30
        in 20..23 -> 20
        in 12..16 -> 10
        else      -> 5
    }

    private fun ClosedRange<Double>.random(): Double =
        start + Math.random() * (endInclusive - start)
}