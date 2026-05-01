package com.example.nirbhaya_chakra

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import com.google.android.gms.maps.model.LatLng

// Smoothly animates marker between two GPS points over durationMs
// Instead of hopping, marker glides like real Google Maps blue dot
object SmoothLocationAnimator {

    fun animate(
        from: LatLng,
        to: LatLng,
        durationMs: Long = 3000,
        onUpdate: (LatLng) -> Unit
    ) {
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMs
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                val fraction = animation.animatedFraction
                // Linear interpolation between two points
                val lat = from.latitude + (to.latitude - from.latitude) * fraction
                val lng = from.longitude + (to.longitude - from.longitude) * fraction
                onUpdate(LatLng(lat, lng))
            }
        }
        animator.start()
    }
}