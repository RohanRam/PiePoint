package com.pizzaorder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.pizzaorder.app.data.model.User
import com.pizzaorder.app.data.repository.PizzaRepository
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel(
    private val repository: PizzaRepository = PizzaRepository()
) : ViewModel() {
    val user: StateFlow<User> = repository.user

    fun updateUser(user: User) {
        repository.updateUser(user)
    }
}
