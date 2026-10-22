package com.piepoint.app.data.repository

import com.piepoint.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PizzaRepository {

    companion object {
        private val _sharedOrders = MutableStateFlow<List<Order>>(MockDataProvider.mockOrders)
    }

    private val _pizzas = MutableStateFlow(MockDataProvider.pizzas)
    val pizzas: StateFlow<List<Pizza>> = _pizzas.asStateFlow()

    private val _categories = MutableStateFlow(MockDataProvider.categories)
    val categories: StateFlow<List<PizzaCategory>> = _categories.asStateFlow()

    val orders: StateFlow<List<Order>> = _sharedOrders.asStateFlow()

    private val _user = MutableStateFlow(MockDataProvider.mockUser)
    val user: StateFlow<User> = _user.asStateFlow()

    fun getPizzaById(id: String): Pizza? = MockDataProvider.pizzas.find { it.id == id }

    fun getPizzasByCategory(categoryId: String): List<Pizza> =
        MockDataProvider.pizzas.filter { it.category == categoryId }

    fun placeOrder(order: Order) {
        _sharedOrders.value = listOf(order) + _sharedOrders.value
    }

    fun updateUser(user: User) {
        _user.value = user
    }

    val deliveryFee: Double = MockDataProvider.deliveryFee
}
