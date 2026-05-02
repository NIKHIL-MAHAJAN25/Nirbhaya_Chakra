package com.example.nirbhaya_chakra.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import java.lang.reflect.Method

object HotspotSOSBroadcaster {

    @RequiresPermission(Manifest.permission.NEARBY_WIFI_DEVICES)
    fun enableSOSHotspot(context: Context, lat: Double, lng: Double, userId: String) {
        try {
            val ssid = "SC_SOS_${String.format("%.4f", lat)}_${String.format("%.4f", lng)}"

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("HOTSPOT_SOS", "WiFi permission not granted")
                return
            }

            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                wifiManager.startLocalOnlyHotspot(
                    object : WifiManager.LocalOnlyHotspotCallback() {
                        override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                            Log.d("HOTSPOT_SOS", "Hotspot started: $ssid")
                        }

                        override fun onStopped() {
                            Log.d("HOTSPOT_SOS", "Hotspot stopped")
                        }

                        override fun onFailed(reason: Int) {
                            Log.e("HOTSPOT_SOS", "Hotspot failed: $reason")
                        }
                    }, null
                )
            } else {
                enableHotspotLegacy(context, ssid)
            }

        } catch (e: Exception) {
            Log.e("HOTSPOT_SOS", "Failed: ${e.message}")
        }
    }

    @Suppress("DEPRECATION")
    private fun enableHotspotLegacy(context: Context, ssid: String) {
        try {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager

            wifiManager.isWifiEnabled = false

            val wifiConfig = Class.forName("android.net.wifi.WifiConfiguration").newInstance()
            val ssidField = wifiConfig.javaClass.getField("SSID")
            ssidField.set(wifiConfig, ssid)

            val method: Method = wifiManager.javaClass.getMethod(
                "setWifiApEnabled",
                wifiConfig.javaClass,
                Boolean::class.javaPrimitiveType
            )
            method.invoke(wifiManager, wifiConfig, true)

            Log.d("HOTSPOT_SOS", "Legacy hotspot enabled: $ssid")

        } catch (e: Exception) {
            Log.e("HOTSPOT_SOS", "Legacy hotspot failed: ${e.message}")
        }
    }

    @Suppress("DEPRECATION")
    fun scanForSOSHotspots(context: Context, onFound: (lat: Double, lng: Double) -> Unit) {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("HOTSPOT_SOS", "WiFi scan permission not granted")
                return
            }

            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager

            val results = wifiManager.scanResults
            results.forEach { result ->
                val ssid = result.SSID
                if (ssid.startsWith("SC_SOS_")) {
                    val parts = ssid.removePrefix("SC_SOS_").split("_")
                    if (parts.size == 2) {
                        val lat = parts[0].toDoubleOrNull()
                        val lng = parts[1].toDoubleOrNull()
                        if (lat != null && lng != null) {
                            Log.d("HOTSPOT_SOS", "Found SOS hotspot at $lat, $lng")
                            onFound(lat, lng)
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("HOTSPOT_SOS", "Scan failed: ${e.message}")
        }
    }
}