package com.pizzaorder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.pizzaorder.app.data.model.Pizza
import com.pizzaorder.app.data.model.PizzaSize
import com.pizzaorder.app.data.model.Topping
import com.pizzaorder.app.data.repository.PizzaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DetailUiState(
    val pizza: Pizza? = null,
    val selectedSize: PizzaSize = PizzaSize.MEDIUM,
    val selectedToppings: List<Topping> = emptyList(),
    val quantity: Int = 1
) {
    val calculatedPrice: Double
        get() = ((pizza?.basePrice ?: 0.0) + selectedSize.priceModifier + selectedToppings.sumOf { it.price }) * quantity
}

class DetailViewModel(
    private val repository: PizzaRepository = PizzaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadPizza(pizzaId: String) {
        val pizza = repository.getPizzaById(pizzaId)
        _uiState.update {
            it.copy(
                pizza = pizza,
                selectedSize = PizzaSize.MEDIUM,
                selectedToppings = emptyList(),
                quantity = 1
            )
        }
    }

    fun selectSize(size: PizzaSize) {
        _uiState.update { it.copy(selectedSize = size) }
    }

    fun toggleTopping(topping: Topping) {
        _uiState.update { state ->
            val current = state.selectedToppings.toMutableList()
            if (current.any { it.id == topping.id }) {
                current.removeAll { it.id == topping.id }
            } else {
                current.add(topping)
            }
            state.copy(selectedToppings = current)
        }
    }

    fun setQuantity(qty: Int) {
        if (qty >= 1) _uiState.update { it.copy(quantity = qty) }
    }

    fun isToppingSelected(toppingId: String): Boolean =
        _uiState.value.selectedToppings.any { it.id == toppingId }
}
