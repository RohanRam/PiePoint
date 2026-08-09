package com.piepoint.app.data.model

data class Topping(
    val id: String,
    val name: String,
    val emoji: String,
    val price: Double,
    val imageRes: Int? = null
)

data class Pizza(
    val id: String,
    val name: String,
    val description: String,
    val basePrice: Double,
    val imageRes: Int,
    val category: String,
    val availableToppings: List<Topping>,
    val rating: Float = 4.5f,
    val reviewCount: Int = 120
)

data class CartItem(
    val id: String,
    val pizza: Pizza,
    val selectedSize: PizzaSize,
    val selectedToppings: List<Topping>,
    var quantity: Int = 1
) {
    val totalPrice: Double
        get() = (pizza.basePrice + selectedSize.priceModifier + selectedToppings.sumOf { it.price }) * quantity
}

enum class PizzaSize(val label: String, val priceModifier: Double, val inches: Int) {
    SMALL("S", 0.0, 8),
    MEDIUM("M", 2.5, 10),
    LARGE("L", 5.0, 12)
}

data class Order(
    val id: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val status: OrderStatus,
    val createdAt: Long,
    val deliveryAddress: String,
    val customerName: String
) {
    val itemCount: Int get() = items.sumOf { it.quantity }
}

enum class OrderStatus(val label: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    PREPARING("Preparing"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled")
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val avatarRes: Int? = null
)

data class PizzaCategory(
    val id: String,
    val name: String,
    val emoji: String
)
