package com.example.nirbhaya_chakra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nirbhaya_chakra.Data.MapMode
import com.example.nirbhaya_chakra.Data.RiskData
import com.example.nirbhaya_chakra.PresetRoutes
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

    // Current animated position (smooth interpolated)
    private val _currentPosition = MutableStateFlow(LatLng(12.9352, 77.6245))
    val currentPosition: StateFlow<LatLng> = _currentPosition

    private var tripJob: Job? = null

    // ──── Start a preset trip ────
    fun startTrip(destinationName: String) {
        val route = PresetRoutes.destinations[destinationName] ?: return

        _tripState.value = TripState(
            mode = MapMode.TRIP,
            destination = route.last(),
            destinationLabel = destinationName,
            routePoints = route,
            trailPoints = listOf(route.first()),
            progress = 0f,
            isActive = true
        )

        // Animate through waypoints
        tripJob = viewModelScope.launch {
            for (i in 0 until route.size - 1) {
                val from = route[i]
                val to = route[i + 1]
                val steps = 20  // sub-steps between waypoints for smoothness
                val stepDelay = 150L  // ms between each sub-step

                for (step in 0..steps) {
                    val fraction = step.toFloat() / steps
                    val lat = from.latitude + (to.latitude - from.latitude) * fraction
                    val lng = from.longitude + (to.longitude - from.longitude) * fraction
                    val pos = LatLng(lat, lng)

                    _currentPosition.value = pos

                    // Add to trail
                    val currentState = _tripState.value
                    _tripState.value = currentState.copy(
                        trailPoints = currentState.trailPoints + pos,
                        progress = (i.toFloat() + fraction) / (route.size - 1)
                    )

                    // Update RiskRepository with new position
                    updateRiskWithPosition(pos, currentState.progress)

                    delay(stepDelay)
                }
            }

            // Trip complete
            _tripState.value = _tripState.value.copy(isActive = false, progress = 1f)
        }
    }

    // ──── Free mode — just tracks position ────
    fun startFreeMode() {
        _tripState.value = TripState(
            mode = MapMode.FREE,
            isActive = true,
            trailPoints = listOf(_currentPosition.value)
        )

        // MOCK: Wander randomly around Koramangala
        tripJob = viewModelScope.launch {
            var lat = 12.9352
            var lng = 77.6245
            while (_tripState.value.isActive) {
                // Small random movement
                lat += (-0.0003..0.0003).random()
                lng += (-0.0003..0.0003).random()
                val pos = LatLng(lat, lng)

                _currentPosition.value = pos
                _tripState.value = _tripState.value.copy(
                    trailPoints = _tripState.value.trailPoints + pos
                )

                updateRiskWithPosition(pos, 0f)
                delay(3000)
            }
        }
    }

    // ──── Stop any active trip ────
    fun stopTrip() {
        tripJob?.cancel()
        _tripState.value = _tripState.value.copy(isActive = false)
    }

    // ──── Inject deviation (demo button) ────
    fun injectDeviation() {
        _currentPosition.value = PresetRoutes.DEVIATION_POINT
        _tripState.value = _tripState.value.copy(
            trailPoints = _tripState.value.trailPoints + PresetRoutes.DEVIATION_POINT
        )
        // This will cause risk score to spike via deviation detection
        val current = RiskRepository.riskData.value ?: return
        RiskRepository.updateRiskData(
            current.copy(
                riskScore = (current.riskScore + 25).coerceAtMost(100),
                isDeviatingRoute = true,
                reasons = current.reasons + "Route deviation detected (380m off path)"
            )
        )
    }

    private fun updateRiskWithPosition(pos: LatLng, progress: Float) {
        val current = RiskRepository.riskData.value ?: RiskData(
            riskScore = 20,
            level = 20.toRiskLevel(),
            reasons = listOf("Trip started"),
            lat = pos.latitude,
            lng = pos.longitude,
            isDeviatingRoute = false,
            followerDetected = false
        )
        RiskRepository.updateRiskData(
            current.copy(
                lat = pos.latitude,
                lng = pos.longitude
            )
        )
    }

    private fun ClosedRange<Double>.random(): Double {
        return start + Math.random() * (endInclusive - start)
    }
}