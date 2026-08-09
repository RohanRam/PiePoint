package com.piepoint.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.piepoint.app.data.model.Order
import com.piepoint.app.data.repository.PizzaRepository
import kotlinx.coroutines.flow.StateFlow

class OrderViewModel(
    private val repository: PizzaRepository = PizzaRepository()
) : ViewModel() {
    val orders: StateFlow<List<Order>> = repository.orders
}
