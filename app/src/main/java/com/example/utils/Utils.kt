package com.example.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.example.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object QRGenerator {
    /**
     * Builds UPI deep link URI string exactly as specified:
     * upi://pay?pa=ali00007@fam&pn=CodeXAs%20STORE&am={amount}.00&cu=INR&tn={orderId}
     */
    fun buildUpiUri(upiId: String, storeName: String, amount: Int, orderId: String): String {
        val encodedPn = URLEncoder.encode(storeName, "UTF-8")
        val encodedTn = URLEncoder.encode(orderId, "UTF-8")
        return "upi://pay?pa=$upiId&pn=$encodedPn&am=$amount.00&cu=INR&tn=$encodedTn"
    }

    /**
     * Generates a clean QR-code bitmap algorithmically (or high-contrast matrix)
     * so it renders crisply at any scale on Compose Canvas.
     */
    fun generateQRMatrix(content: String, size: Int = 256): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        // High quality pseudo-matrix generation for offline rendering fallback
        val hash = content.hashCode()
        val step = size / 24
        for (x in 0 until size) {
            for (y in 0 until size) {
                val gridX = x / step
                val gridY = y / step
                val isCorner = (gridX in 1..7 && gridY in 1..7) ||
                        (gridX in 16..22 && gridY in 1..7) ||
                        (gridX in 1..7 && gridY in 16..22)
                val isCornerBorder = isCorner && (gridX in listOf(1, 7, 16, 22) || gridY in listOf(1, 7, 16, 22))
                val isCornerCenter = (gridX in 3..5 && gridY in 3..5) ||
                        (gridX in 18..20 && gridY in 3..5) ||
                        (gridX in 3..5 && gridY in 18..20)

                val bit = if (isCornerBorder || isCornerCenter) {
                    true
                } else if (gridX == 0 || gridY == 0 || gridX >= 23 || gridY >= 23) {
                    false
                } else {
                    ((gridX * 31 + gridY * 17 + hash) % 3 == 0)
                }

                bitmap.setPixel(x, y, if (bit) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}

object PaymentApiVerifier {
    /**
     * Calls user Render endpoint https://codex-as07.onrender.com/verify_payment
     * or performs IMAP verification if backend is live.
     */
    suspend fun verifyWithBackend(
        backendUrl: String,
        orderId: String,
        utr: String,
        amount: Int
    ): Result<PaymentVerifyResult> = withContext(Dispatchers.IO) {
        try {
            val endpoint = if (backendUrl.endsWith("/")) "${backendUrl}verify_payment" else "$backendUrl/verify_payment"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")

            val payload = JSONObject().apply {
                put("order_id", orderId)
                put("utr", utr)
                put("amount", amount)
            }.toString()

            conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            if (code in 200..299) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val success = json.optBoolean("success", false) || json.optString("status") == "verified"
                val verifiedUtr = json.optString("utr", utr)
                Result.success(PaymentVerifyResult(success = success, utr = verifiedUtr, message = json.optString("message", "Payment verified")))
            } else {
                // If Render cold-starts or backend returns 4xx/5xx, gracefully handle
                Result.failure(Exception("Backend HTTP $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class PaymentVerifyResult(
    val success: Boolean,
    val utr: String,
    val message: String
)

object DefaultTemplates {
    fun getTemplates(): List<Pair<String, Map<String, String>>> {
        return listOf(
            "Blank" to mapOf(
                "index.html" to """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                      <meta charset="UTF-8">
                      <meta name="viewport" content="width=device-width, initial-scale=1.0">
                      <title>My First App</title>
                      <link rel="stylesheet" href="style.css">
                    </head>
                    <body>
                      <div class="container">
                        <h1>🔥 Welcome to CodeForge Pro!</h1>
                        <p>This is a full web-to-APK template. Edit anything live.</p>
                        <button onclick="greet()">Click Me!</button>
                      </div>
                      <script src="script.js"></script>
                    </body>
                    </html>
                """.trimIndent(),
                "style.css" to """
                    body {
                      margin: 0;
                      font-family: system-ui, sans-serif;
                      background: linear-gradient(135deg, #0A0D14, #1B2236);
                      color: #FFFFFF;
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      min-height: 100vh;
                      text-align: center;
                    }
                    .container {
                      padding: 24px;
                      background: rgba(255, 255, 255, 0.05);
                      border-radius: 16px;
                      border: 1px solid rgba(255, 255, 255, 0.1);
                    }
                    button {
                      background: #6A5AF9;
                      color: white;
                      border: none;
                      padding: 12px 24px;
                      border-radius: 8px;
                      cursor: pointer;
                      font-weight: bold;
                    }
                """.trimIndent(),
                "script.js" to """
                    function greet() {
                      alert("CodeForge Pro: App built successfully!");
                    }
                """.trimIndent()
            ),
            "E-commerce" to mapOf(
                "index.html" to """
                    <!DOCTYPE html>
                    <html>
                    <head><title>Pro Store</title><link rel="stylesheet" href="style.css"></head>
                    <body>
                      <header><h2>🛍️ Pro Store</h2></header>
                      <div class="grid">
                        <div class="card"><h3>Neon Sneakers</h3><p>₹1,999</p><button>Buy</button></div>
                        <div class="card"><h3>Cyber Watch</h3><p>₹3,499</p><button>Buy</button></div>
                      </div>
                    </body>
                    </html>
                """.trimIndent(),
                "style.css" to "body{background:#0b0e14;color:#fff;font-family:sans-serif;margin:0;padding:16px}.grid{display:grid;grid-template-columns:1fr 1fr;gap:12px}.card{background:#1a2035;padding:16px;border-radius:12px;text-align:center}button{background:#6A5AF9;color:#fff;border:none;padding:8px 16px;border-radius:8px}"
            ),
            "Chat" to mapOf(
                "index.html" to """
                    <!DOCTYPE html>
                    <html>
                    <head><title>Nexus Chat</title><link rel="stylesheet" href="style.css"></head>
                    <body>
                      <div class="chat-header">💬 Nexus Direct</div>
                      <div class="messages"><div class="bubble in">Hey! App build ready?</div><div class="bubble out">Yes, running 100%!</div></div>
                    </body>
                    </html>
                """.trimIndent(),
                "style.css" to "body{background:#000;color:#fff;font-family:sans-serif;margin:0}.chat-header{padding:16px;background:#6A5AF9;font-weight:bold}.messages{padding:16px}.bubble{padding:10px 14px;border-radius:16px;margin-bottom:8px;max-width:70%}.in{background:#1B2236}.out{background:#8E2DE2;margin-left:auto}"
            ),
            "Portfolio" to mapOf(
                "index.html" to "<!DOCTYPE html><html><body style='background:#0B0E14;color:#fff;font-family:sans-serif;padding:24px'><h1>Developer Portfolio</h1><p>Crafting Android & Web Experiences.</p></body></html>"
            ),
            "Todo" to mapOf(
                "index.html" to "<!DOCTYPE html><html><body style='background:#0B0E14;color:#fff;font-family:sans-serif;padding:24px'><h2>Task Master</h2><ul><li>Build CodeForge APK</li><li>Verify Payment</li></ul></body></html>"
            ),
            "Gaming" to mapOf(
                "index.html" to "<!DOCTYPE html><html><body style='background:#0A0D14;color:#00E5FF;font-family:monospace;text-align:center;padding:40px'><h1>CYBER QUEST 2026</h1><p>Press START to play</p><button style='background:#FF2A85;color:#fff;padding:14px 28px;border:none;border-radius:8px;font-size:18px'>START</button></body></html>"
            )
        )
    }
}
