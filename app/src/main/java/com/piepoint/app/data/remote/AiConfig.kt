package com.piepoint.app.data.remote

object AiConfig {
    /**
     * Base URL for PiePoint AI backend.
     * - Android Emulator: "http://10.0.2.2:3000" (points to host machine localhost:3000)
     * - Physical Device: "http://<YOUR_LAN_IP>:3000" (e.g. "http://192.168.1.48:3000")
     */
    var baseUrl: String = "http://10.0.2.2:3000"
}
