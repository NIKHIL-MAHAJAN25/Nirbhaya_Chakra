package com.example.nirbhaya_chakra

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nirbhaya_chakra.Data.HotspotLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val _hotspots = MutableStateFlow<List<HotspotLocation>>(emptyList())
    val hotspots: StateFlow<List<HotspotLocation>> = _hotspots

    init {
        loadHotspots() // 🔥 AUTO FETCH
    }

    private fun loadHotspots() {
        viewModelScope.launch {
            val data = HotspotRepository.fetchHotspots()
            Log.d("MAP_VM", "Hotspots loaded: ${data.size}")
            _hotspots.value = data
        }
    }
}