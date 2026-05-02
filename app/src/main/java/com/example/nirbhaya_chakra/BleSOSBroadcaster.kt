package com.example.nirbhaya_chakra

import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.nio.ByteBuffer
import java.util.UUID

object BleSOSBroadcaster {

    private val SAFECIRCLE_UUID = UUID.fromString("0000abcd-0000-1000-8000-00805f9b34fb")
    private val PARCEL_UUID = ParcelUuid(SAFECIRCLE_UUID)

    private var advertiser: BluetoothLeAdvertiser? = null
    private var scanner: BluetoothLeScanner? = null
    private var advertiseCallback: AdvertiseCallback? = null
    private var scanCallback: ScanCallback? = null
    private var isAdvertising = false
    private var isScanning = false

    // ─── BROADCAST SOS (when YOU need help) ───
    fun startSOSBroadcast(context: Context, lat: Double, lng: Double, userId: String) {
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter ?: return
            advertiser = adapter.bluetoothLeAdvertiser ?: return

            // Pack lat/lng into manufacturer data
            val data = ByteBuffer.allocate(17).apply {
                putDouble(lat)
                putDouble(lng)
                put(0x01) // SOS alert type
            }.array()

            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .setTimeout(0)
                .build()

            val advertiseData = AdvertiseData.Builder()
                .addServiceUuid(PARCEL_UUID)
                .addManufacturerData(0xFFFF, data)
                .setIncludeDeviceName(false)
                .build()

            advertiseCallback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                    isAdvertising = true
                    Log.d("BLE_SOS", "Broadcasting SOS at $lat, $lng")
                }

                override fun onStartFailure(errorCode: Int) {
                    Log.e("BLE_SOS", "Broadcast failed: $errorCode")
                }
            }

            advertiser?.startAdvertising(settings, advertiseData, advertiseCallback)

        } catch (e: SecurityException) {
            Log.e("BLE_SOS", "Permission denied: ${e.message}")
        }
    }

    // ─── STOP BROADCASTING ───
    fun stopBroadcast() {
        try {
            if (isAdvertising && advertiseCallback != null) {
                advertiser?.stopAdvertising(advertiseCallback)
                isAdvertising = false
                advertiseCallback = null
                Log.d("BLE_SOS", "Broadcast stopped")
            }
        } catch (e: SecurityException) {
            Log.e("BLE_SOS", "Stop failed: ${e.message}")
        }
    }

    // ─── LISTEN FOR NEARBY SOS (passive relay mode) ───
    fun startListening(context: Context, onSOSDetected: (lat: Double, lng: Double) -> Unit) {
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter ?: return
            scanner = adapter.bluetoothLeScanner ?: return

            val filter = ScanFilter.Builder()
                .setServiceUuid(PARCEL_UUID)
                .build()

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    result ?: return
                    val mfgData = result.scanRecord?.getManufacturerSpecificData(0xFFFF) ?: return

                    if (mfgData.size >= 17) {
                        val buffer = ByteBuffer.wrap(mfgData)
                        val lat = buffer.double
                        val lng = buffer.double
                        val alertType = buffer.get()

                        Log.d("BLE_SOS", "Received SOS from nearby: $lat, $lng")
                        onSOSDetected(lat, lng)
                    }
                }
            }

            scanner?.startScan(listOf(filter), settings, scanCallback)
            isScanning = true
            Log.d("BLE_SOS", "Listening for nearby SOS broadcasts")

        } catch (e: SecurityException) {
            Log.e("BLE_SOS", "Scan permission denied: ${e.message}")
        }
    }

    // ─── STOP LISTENING ───
    fun stopListening() {
        try {
            if (isScanning && scanCallback != null) {
                scanner?.stopScan(scanCallback)
                isScanning = false
                scanCallback = null
                Log.d("BLE_SOS", "Stopped listening")
            }
        } catch (e: SecurityException) {
            Log.e("BLE_SOS", "Stop scan failed: ${e.message}")
        }
    }
}