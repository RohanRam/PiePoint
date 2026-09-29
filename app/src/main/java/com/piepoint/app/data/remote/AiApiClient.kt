package com.piepoint.app.data.remote

import com.piepoint.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object AiNetworkConfig {
    // 10.0.2.2 connects to host machine localhost:3000 from Android emulator
    var baseUrl: String = "http://10.0.2.2:3000"
}

class AiApiClient(
    private val getBaseUrl: () -> String = { AiNetworkConfig.baseUrl }
) {

    /**
     * Call POST /chat on the PiePoint AI backend.
     */
    suspend fun chat(messages: List<ChatMessage>): Result<Pair<String, AiPizza?>> = withContext(Dispatchers.IO) {
        try {
            val endpoint = "${getBaseUrl()}/chat"
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                connectTimeout = 15_000
                readTimeout = 35_000
                doOutput = true
                doInput = true
            }

            // Build request payload
            val jsonMessages = JSONArray()
            for (msg in messages) {
                if (msg.content.isNotBlank()) {
                    val msgObj = JSONObject().apply {
                        put("role", if (msg.role == MessageRole.USER) "user" else "assistant")
                        put("content", msg.content.trim())
                    }
                    jsonMessages.put(msgObj)
                }
            }

            val requestBody = JSONObject().apply {
                put("messages", jsonMessages)
            }

            // Write request
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { it.readText() }
            } else {
                val errorStream = connection.errorStream ?: connection.inputStream
                val errText = BufferedReader(InputStreamReader(errorStream, Charsets.UTF_8)).use { it.readText() }
                val errorMsg = try {
                    JSONObject(errText).optString("error", "Server returned HTTP $responseCode")
                } catch (_: Exception) {
                    "Server error (HTTP $responseCode)"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val responseJson = JSONObject(responseText)
            val reply = responseJson.optString("reply", "Here's what I found for you! 🍕")
            val pizzaJson = responseJson.optJSONObject("pizza")
            val pizza = pizzaJson?.let { parseAiPizza(it) }

            Result.success(Pair(reply, pizza))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Call POST /create-pizza on the PiePoint AI backend.
     */
    suspend fun createPizza(prompt: String): Result<AiPizza> = withContext(Dispatchers.IO) {
        try {
            val endpoint = "${getBaseUrl()}/create-pizza"
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                connectTimeout = 15_000
                readTimeout = 30_000
                doOutput = true
                doInput = true
            }

            val requestBody = JSONObject().apply {
                put("prompt", prompt.trim())
            }

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { it.readText() }
            } else {
                val errorStream = connection.errorStream ?: connection.inputStream
                val errText = BufferedReader(InputStreamReader(errorStream, Charsets.UTF_8)).use { it.readText() }
                val errorMsg = try {
                    JSONObject(errText).optString("error", "Server returned HTTP $responseCode")
                } catch (_: Exception) {
                    "Server error (HTTP $responseCode)"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val responseJson = JSONObject(responseText)
            val pizzaJson = responseJson.getJSONObject("pizza")
            val pizza = parseAiPizza(pizzaJson)

            Result.success(pizza)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseAiPizza(json: JSONObject): AiPizza {
        val crustObj = json.getJSONObject("crust")
        val sauceObj = json.getJSONObject("sauce")
        val cheeseObj = json.getJSONObject("cheese")
        val sizeObj = json.getJSONObject("size")
        val toppingsArray = json.getJSONArray("toppings")

        val toppingsList = mutableListOf<AiTopping>()
        for (i in 0 until toppingsArray.length()) {
            val tObj = toppingsArray.getJSONObject(i)
            toppingsList.add(
                AiTopping(
                    id = tObj.optString("id", "t1"),
                    name = tObj.optString("name", "Topping"),
                    emoji = tObj.optString("emoji", "🍕"),
                    price = tObj.optInt("price", 0)
                )
            )
        }

        return AiPizza(
            name = json.optString("name", "Custom AI Pizza"),
            crust = AiComponent(
                id = crustObj.optString("id", "c1"),
                name = crustObj.optString("name", "Classic"),
                price = crustObj.optInt("price", 0)
            ),
            sauce = AiComponent(
                id = sauceObj.optString("id", "s1"),
                name = sauceObj.optString("name", "Classic Tomato"),
                price = sauceObj.optInt("price", 0)
            ),
            cheese = AiComponent(
                id = cheeseObj.optString("id", "ch1"),
                name = cheeseObj.optString("name", "Mozzarella"),
                price = cheeseObj.optInt("price", 0)
            ),
            toppings = toppingsList,
            size = AiSize(
                id = sizeObj.optString("id", "MEDIUM"),
                label = sizeObj.optString("label", "M"),
                inches = sizeObj.optInt("inches", 10),
                priceModifier = sizeObj.optInt("priceModifier", 0)
            ),
            price = json.optInt("price", 149)
        )
    }
}
