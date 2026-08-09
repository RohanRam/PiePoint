package com.pizzaorder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.pizzaorder.app.data.model.Pizza
import com.pizzaorder.app.data.model.PizzaCategory
import com.pizzaorder.app.data.repository.PizzaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val categories: List<PizzaCategory> = emptyList(),
    val featuredPizzas: List<Pizza> = emptyList(),
    val selectedCategoryId: String = "cat1",
    val featuredIndex: Int = 0,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: PizzaRepository = PizzaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val categories = repository.categories.value
        val pizzas = repository.getPizzasByCategory("cat1")
        _uiState.update {
            it.copy(
                categories = categories,
                featuredPizzas = pizzas,
                selectedCategoryId = "cat1"
            )
        }
    }

    fun selectCategory(categoryId: String) {
        val pizzas = if (categoryId == "cat1") {
            repository.getPizzasByCategory(categoryId)
        } else {
            // For non-pizza categories we return pizza data as placeholder
            repository.getPizzasByCategory("cat1")
        }
        _uiState.update {
            it.copy(
                selectedCategoryId = categoryId,
                featuredPizzas = pizzas,
                featuredIndex = 0
            )
        }
    }

    fun setFeaturedIndex(index: Int) {
        _uiState.update { it.copy(featuredIndex = index) }
    }
}
