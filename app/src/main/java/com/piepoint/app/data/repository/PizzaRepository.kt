package com.piepoint.app.data.repository

import com.piepoint.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PizzaRepository {

    private val _pizzas = MutableStateFlow(MockDataProvider.pizzas)
    val pizzas: StateFlow<List<Pizza>> = _pizzas.asStateFlow()

    private val _categories = MutableStateFlow(MockDataProvider.categories)
    val categories: StateFlow<List<PizzaCategory>> = _categories.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _user = MutableStateFlow(MockDataProvider.mockUser)
    val user: StateFlow<User> = _user.asStateFlow()

    fun getPizzaById(id: String): Pizza? = MockDataProvider.pizzas.find { it.id == id }

    fun getPizzasByCategory(categoryId: String): List<Pizza> =
        MockDataProvider.pizzas.filter { it.category == categoryId }

    fun placeOrder(order: Order) {
        _orders.value = listOf(order) + _orders.value
    }

    fun updateUser(user: User) {
        _user.value = user
    }

    val deliveryFee: Double = MockDataProvider.deliveryFee
}
