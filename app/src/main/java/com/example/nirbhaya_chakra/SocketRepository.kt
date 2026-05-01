package com.example.nirbhaya_chakra

import android.util.Log
import okhttp3.*

class SocketRepository {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect() {
        val request = Request.Builder()
            .url("ws://10.81.45.44:3000") // 🔥 CHANGE TO YOUR PC IP
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("SOCKET", "Connected")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d("SOCKET", "Received: $text")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("SOCKET", "Error: ${t.message}")
            }
        })
    }

    fun sendLocation(lat: Double, lng: Double) {
        val json = """
            {
                "type": "location",
                "lat": $lat,
                "lng": $lng
            }
        """.trimIndent()

        webSocket?.send(json)
    }
}