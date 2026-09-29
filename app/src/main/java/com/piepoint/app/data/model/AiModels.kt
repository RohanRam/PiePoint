package com.piepoint.app.data.model

import java.util.UUID

// ─── AI Pizza (mirrors backend /create-pizza response) ──────────────────────

data class AiPizza(
    val name: String,
    val crust: AiComponent,
    val sauce: AiComponent,
    val cheese: AiComponent,
    val toppings: List<AiTopping>,
    val size: AiSize,
    val price: Int // in ₹, always computed server-side
)

data class AiComponent(
    val id: String,
    val name: String,
    val price: Int
)

data class AiTopping(
    val id: String,
    val name: String,
    val emoji: String,
    val price: Int
)

data class AiSize(
    val id: String,
    val label: String,
    val inches: Int,
    val priceModifier: Int
)

// ─── Chat ───────────────────────────────────────────────────────────────────

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val pizza: AiPizza? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageRole { USER, ASSISTANT }

// ─── UI State ───────────────────────────────────────────────────────────────

data class AiUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ─── Prefill bridge (AiScreen → PizzaBuilder) ──────────────────────────────
// Lightweight singleton to pass AI pizza data to the builder without
// serializing through nav arguments. Consumed once on builder entry.

object AiPizzaPrefill {
    var pendingPizza: AiPizza? = null
}

// ─── Network DTOs (used by Retrofit in Phase 3) ────────────────────────────

data class CreatePizzaRequest(val prompt: String)

data class CreatePizzaResponse(
    val pizza: AiPizza,
    val warnings: List<String>? = null
)

data class ChatRequest(val messages: List<ChatMessageDto>)

data class ChatMessageDto(val role: String, val content: String)

data class ChatResponse(
    val reply: String,
    val suggestedPizza: AiPizza? = null,
    val pizza: AiPizza? = null
) {
    val resultPizza: AiPizza? get() = suggestedPizza ?: pizza
}
