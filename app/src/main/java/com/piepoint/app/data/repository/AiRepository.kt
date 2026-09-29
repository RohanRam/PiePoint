package com.piepoint.app.data.repository

import com.piepoint.app.data.model.*
import com.piepoint.app.data.remote.AiApiService
import kotlinx.coroutines.delay

/**
 * Common contract for PiePoint AI interaction.
 */
interface AiRepository {
    suspend fun chat(messages: List<ChatMessage>): Result<Pair<String, AiPizza?>>
    suspend fun createPizza(prompt: String): Result<AiPizza>
}

/**
 * Real repository connecting to the PiePoint Express backend using Retrofit.
 * Falls back gracefully to FakeAiRepository if the local backend server is not running,
 * ensuring seamless offline / UI testing.
 */
class RealAiRepository(
    private val apiService: AiApiService = AiApiService.create(),
    private val fallback: FakeAiRepository = FakeAiRepository()
) : AiRepository {

    override suspend fun chat(messages: List<ChatMessage>): Result<Pair<String, AiPizza?>> {
        return try {
            val dtos = messages.filter { it.content.isNotBlank() }.map {
                ChatMessageDto(
                    role = if (it.role == MessageRole.USER) "user" else "assistant",
                    content = it.content.trim()
                )
            }
            val response = apiService.chat(ChatRequest(dtos))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(Pair(body.reply, body.resultPizza))
            } else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    org.json.JSONObject(errorBody ?: "").optString("error", "Server returned HTTP ${response.code()}")
                } catch (_: Exception) {
                    "Server error (HTTP ${response.code()})"
                }
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            if (e is java.net.ConnectException || e is java.net.SocketTimeoutException || e is java.net.UnknownHostException) {
                fallback.chat(messages)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun createPizza(prompt: String): Result<AiPizza> {
        return try {
            val response = apiService.createPizza(CreatePizzaRequest(prompt.trim()))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.pizza)
            } else {
                val errorBody = response.errorBody()?.string()
                val message = try {
                    org.json.JSONObject(errorBody ?: "").optString("error", "Server returned HTTP ${response.code()}")
                } catch (_: Exception) {
                    "Server error (HTTP ${response.code()})"
                }
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            if (e is java.net.ConnectException || e is java.net.SocketTimeoutException || e is java.net.UnknownHostException) {
                fallback.createPizza(prompt)
            } else {
                Result.failure(e)
            }
        }
    }
}

/**
 * Fake AI repository with realistic sample pizzas and intent detection.
 * Used for testing and as offline fallback when the Node backend is not reachable.
 */
class FakeAiRepository : AiRepository {

    // ─── Sample pizzas with correct ₹ prices ────────────────────────────────

    private val spicyVegPizza = AiPizza(
        name = "Spicy Garden Blaze 🔥",
        crust = AiComponent("c1", "Classic", 0),
        sauce = AiComponent("s2", "Spicy Arrabbiata", 29),
        cheese = AiComponent("ch1", "Mozzarella", 0),
        toppings = listOf(
            AiTopping("t10", "Jalapeño", "🌶️", 39),
            AiTopping("t6", "Bell Pepper", "🫑", 39),
            AiTopping("t3", "Mushroom", "🍄", 49),
            AiTopping("t5", "Onion", "🧅", 29)
        ),
        size = AiSize("MEDIUM", "M", 10, 50),
        price = 384 // 149+50+0+29+0+39+39+49+29
    )

    private val budgetPizza = AiPizza(
        name = "Garden Fresh Delight 🌿",
        crust = AiComponent("c1", "Classic", 0),
        sauce = AiComponent("s1", "Classic Tomato", 0),
        cheese = AiComponent("ch1", "Mozzarella", 0),
        toppings = listOf(
            AiTopping("t5", "Onion", "🧅", 29),
            AiTopping("t7", "Tomato", "🍅", 29),
            AiTopping("t8", "Basil", "🌿", 29)
        ),
        size = AiSize("MEDIUM", "M", 10, 50),
        price = 286 // 149+50+0+0+0+29+29+29
    )

    private val cheesyPizza = AiPizza(
        name = "Cheesy Paradise 🧀",
        crust = AiComponent("c3", "Cheese Burst", 99),
        sauce = AiComponent("s3", "Garlic Cream", 39),
        cheese = AiComponent("ch4", "Four Cheese", 79),
        toppings = listOf(
            AiTopping("t1", "Cheese", "🧀", 49),
            AiTopping("t3", "Mushroom", "🍄", 49),
            AiTopping("t8", "Basil", "🌿", 29)
        ),
        size = AiSize("LARGE", "L", 12, 100),
        price = 593 // 149+100+99+39+79+49+49+29
    )

    private val defaultPizza = AiPizza(
        name = "PiePoint Special ✨",
        crust = AiComponent("c2", "Thin & Crispy", 0),
        sauce = AiComponent("s1", "Classic Tomato", 0),
        cheese = AiComponent("ch2", "Cheddar", 39),
        toppings = listOf(
            AiTopping("t2", "Pepperoni", "🍖", 69),
            AiTopping("t4", "Olive", "🫒", 39),
            AiTopping("t7", "Tomato", "🍅", 29)
        ),
        size = AiSize("MEDIUM", "M", 10, 50),
        price = 375 // 149+50+0+0+39+69+39+29
    )

    // ─── Public API ─────────────────────────────────────────────────────────

    override suspend fun chat(messages: List<ChatMessage>): Result<Pair<String, AiPizza?>> {
        delay(1200) // Simulated latency

        val lastMessage = messages.lastOrNull { it.role == MessageRole.USER }
            ?.content?.lowercase() ?: ""

        return Result.success(
            when {
                // Pizza-building intents
                "spicy" in lastMessage && ("veg" in lastMessage || "vegetarian" in lastMessage) ->
                    "Here's a fiery vegetarian creation for you! 🔥🌿" to spicyVegPizza

                "under" in lastMessage && ("300" in lastMessage || "₹300" in lastMessage) ->
                    "A tasty pizza under ₹300 — fresh and budget-friendly! 💰" to budgetPizza

                "under" in lastMessage && ("400" in lastMessage || "₹400" in lastMessage) ->
                    "Here's a flavour-packed pizza within your budget! 🎯" to spicyVegPizza

                "chees" in lastMessage || "cheesy" in lastMessage ->
                    "Maximum cheese overload coming right up! 🧀🧀🧀" to cheesyPizza

                "build" in lastMessage || "create" in lastMessage || "make" in lastMessage
                    || lastMessage.startsWith("✨") ->
                    "I've crafted something special just for you! 🍕" to defaultPizza

                // Information intents
                "popular" in lastMessage || "best" in lastMessage || "recommend" in lastMessage ->
                    "Our top picks right now:\n\n" +
                    "🍕 **Pepperoni Supreme** — ₹249 (4.9★)\n" +
                    "🍕 **Four Cheese** — ₹349 (4.9★)\n" +
                    "🍕 **Margherita Classic** — ₹199 (4.8★)\n\n" +
                    "Want me to build you something similar?" to null

                "menu" in lastMessage || "have" in lastMessage || "options" in lastMessage ->
                    "Here's what we've got:\n\n" +
                    "🫓 **5 crusts** — Classic to Stuffed Crust\n" +
                    "🫙 **5 sauces** — Tomato to Pesto\n" +
                    "🧀 **4 cheeses** — Mozzarella to Four Cheese\n" +
                    "🥬 **10 toppings** — from ₹29 each\n" +
                    "📏 **3 sizes** — S (8\"), M (10\"), L (12\")\n\n" +
                    "Tell me what you're craving and I'll build the perfect pizza!" to null

                "price" in lastMessage || "cost" in lastMessage || "how much" in lastMessage ->
                    "Our custom pizzas start at ₹149 (base price). Add your favourite crust, sauce, cheese, and toppings!\n\n" +
                    "A loaded medium pizza is typically ₹300–450. Want me to build one within a specific budget?" to null

                "hi" in lastMessage || "hello" in lastMessage || "hey" in lastMessage ->
                    "Hey there! 👋 Welcome to PiePoint AI!\n\nI can:\n" +
                    "• 🍕 Build your dream pizza from scratch\n" +
                    "• 📋 Tell you about our menu\n" +
                    "• 💡 Recommend based on your mood\n\n" +
                    "What sounds good?" to null

                else ->
                    "I'd love to help! I can build you a custom pizza or tell you about our menu. " +
                    "Try saying something like \"spicy vegetarian pizza\" or \"what's popular?\" 🍕" to null
            }
        )
    }

    override suspend fun createPizza(prompt: String): Result<AiPizza> {
        delay(1000)
        val p = prompt.lowercase()
        return Result.success(
            when {
                "spicy" in p -> spicyVegPizza
                "under" in p && "300" in p -> budgetPizza
                "chees" in p -> cheesyPizza
                else -> defaultPizza
            }
        )
    }
}
