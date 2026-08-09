package com.pizzaorder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.pizzaorder.app.data.model.Order
import com.pizzaorder.app.data.repository.PizzaRepository
import kotlinx.coroutines.flow.StateFlow

class OrderViewModel(
    private val repository: PizzaRepository = PizzaRepository()
) : ViewModel() {
    val orders: StateFlow<List<Order>> = repository.orders
}
