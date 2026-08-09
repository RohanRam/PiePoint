package com.piepoint.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.piepoint.app.data.model.*
import com.piepoint.app.data.repository.PizzaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartViewModel(
    private val repository: PizzaRepository = PizzaRepository()
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val subtotal: Double
        get() = _cartItems.value.sumOf { it.totalPrice }

    val deliveryFee: Double
        get() = if (_cartItems.value.isEmpty()) 0.0 else repository.deliveryFee

    val total: Double
        get() = subtotal + deliveryFee

    val itemCount: Int
        get() = _cartItems.value.sumOf { it.quantity }

    fun addToCart(pizza: Pizza, size: PizzaSize, toppings: List<Topping>) {
        val existingItem = _cartItems.value.find { item ->
            item.pizza.id == pizza.id &&
                    item.selectedSize == size &&
                    item.selectedToppings.map { it.id }.sorted() == toppings.map { it.id }.sorted()
        }
        if (existingItem != null) {
            _cartItems.update { items ->
                items.map { if (it.id == existingItem.id) it.copy(quantity = it.quantity + 1) else it }
            }
        } else {
            val newItem = CartItem(
                id = java.util.UUID.randomUUID().toString(),
                pizza = pizza,
                selectedSize = size,
                selectedToppings = toppings,
                quantity = 1
            )
            _cartItems.update { it + newItem }
        }
    }

    fun increaseQuantity(itemId: String) {
        _cartItems.update { items ->
            items.map { if (it.id == itemId) it.copy(quantity = it.quantity + 1) else it }
        }
    }

    fun decreaseQuantity(itemId: String) {
        _cartItems.update { items ->
            val item = items.find { it.id == itemId } ?: return@update items
            if (item.quantity <= 1) items.filter { it.id != itemId }
            else items.map { if (it.id == itemId) it.copy(quantity = it.quantity - 1) else it }
        }
    }

    fun removeItem(itemId: String) {
        _cartItems.update { it.filter { item -> item.id != itemId } }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun placeOrder(customerName: String, address: String): Order {
        val order = Order(
            id = "ORD-${System.currentTimeMillis()}",
            items = _cartItems.value.toList(),
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            total = total,
            status = OrderStatus.CONFIRMED,
            createdAt = System.currentTimeMillis(),
            deliveryAddress = address,
            customerName = customerName
        )
        repository.placeOrder(order)
        clearCart()
        return order
    }
}
