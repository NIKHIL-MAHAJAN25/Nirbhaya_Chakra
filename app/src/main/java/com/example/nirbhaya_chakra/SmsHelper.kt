package com.example.nirbhaya_chakra

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat

object SmsHelper {

    private const val TRUSTED_CONTACT = "+917529073222"

    fun sendSosAlert(
        context: Context,
        lat: Double,
        lng: Double,
        userName: String = "Priya"
    ) {

        // 🔥 1. Permission check
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("SOS_SMS", "❌ SMS permission NOT granted")
            return
        }

        try {
            val message = """
🚨 EMERGENCY ALERT 🚨
$userName may be in danger!

📍 Location:
https://maps.google.com/?q=$lat,$lng

Sent via SafeCircle
            """.trimIndent()

            val smsManager = SmsManager.getDefault()

            smsManager.sendTextMessage(
                TRUSTED_CONTACT,
                null,
                message,
                null,
                null
            )

            Log.d("SOS_SMS", "✅ SMS sent successfully")

        } catch (e: Exception) {
            Log.e("SOS_SMS", "❌ SMS FAILED: ${e.message}")
        }
    }
}