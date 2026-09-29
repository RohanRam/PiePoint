package com.piepoint.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.piepoint.app.data.model.*
import com.piepoint.app.data.repository.MockDataProvider
import com.piepoint.app.data.repository.PizzaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PizzaBuilderViewModel(
    private val repository: PizzaRepository = PizzaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PizzaBuilderUiState())
    val uiState: StateFlow<PizzaBuilderUiState> = _uiState.asStateFlow()

    val availableCrusts = MockDataProvider.crusts
    val availableSauces = MockDataProvider.sauces
    val availableCheeses = MockDataProvider.cheeses
    val availableToppings = MockDataProvider.toppings

    companion object {
        const val MAX_TOPPING_QTY = 3
    }

    fun selectCrust(crust: Crust) {
        _uiState.update { it.copy(selectedCrust = crust) }
    }

    fun selectSauce(sauce: Sauce) {
        _uiState.update { it.copy(selectedSauce = sauce) }
    }

    fun selectCheese(cheese: Cheese) {
        _uiState.update { it.copy(selectedCheese = cheese) }
    }

    fun toggleTopping(topping: Topping) {
        _uiState.update { state ->
            val toppings = state.selectedToppings.toMutableMap()
            if (toppings.containsKey(topping)) {
                toppings.remove(topping)
            } else {
                toppings[topping] = 1
            }
            state.copy(selectedToppings = toppings)
        }
    }

    fun incrementTopping(topping: Topping) {
        _uiState.update { state ->
            val toppings = state.selectedToppings.toMutableMap()
            val current = toppings[topping] ?: 0
            if (current < MAX_TOPPING_QTY) {
                toppings[topping] = current + 1
            }
            state.copy(selectedToppings = toppings)
        }
    }

    fun decrementTopping(topping: Topping) {
        _uiState.update { state ->
            val toppings = state.selectedToppings.toMutableMap()
            val current = toppings[topping] ?: 0
            if (current <= 1) {
                toppings.remove(topping)
            } else {
                toppings[topping] = current - 1
            }
            state.copy(selectedToppings = toppings)
        }
    }

    fun clearToppings() {
        _uiState.update { it.copy(selectedToppings = emptyMap()) }
    }


    fun selectSize(size: PizzaSize) {
        _uiState.update { it.copy(selectedSize = size) }
    }

    fun nextStep() {
        _uiState.update { state ->
            val next = when (state.currentStep) {
                PizzaBuilderStep.CRUST -> PizzaBuilderStep.SAUCE
                PizzaBuilderStep.SAUCE -> PizzaBuilderStep.CHEESE
                PizzaBuilderStep.CHEESE -> PizzaBuilderStep.TOPPINGS
                PizzaBuilderStep.TOPPINGS -> PizzaBuilderStep.SIZE
                PizzaBuilderStep.SIZE -> PizzaBuilderStep.REVIEW
                PizzaBuilderStep.REVIEW -> PizzaBuilderStep.REVIEW
            }
            state.copy(currentStep = next)
        }
    }

    fun prevStep() {
        _uiState.update { state ->
            val prev = when (state.currentStep) {
                PizzaBuilderStep.CRUST -> PizzaBuilderStep.CRUST
                PizzaBuilderStep.SAUCE -> PizzaBuilderStep.CRUST
                PizzaBuilderStep.CHEESE -> PizzaBuilderStep.SAUCE
                PizzaBuilderStep.TOPPINGS -> PizzaBuilderStep.CHEESE
                PizzaBuilderStep.SIZE -> PizzaBuilderStep.TOPPINGS
                PizzaBuilderStep.REVIEW -> PizzaBuilderStep.SIZE
            }
            state.copy(currentStep = prev)
        }
    }

    fun setStep(step: PizzaBuilderStep) {
        _uiState.update { it.copy(currentStep = step) }
    }

    fun addToCart(cartViewModel: CartViewModel, onComplete: () -> Unit) {
        val state = _uiState.value
        val customPizza = Pizza(
            id = "custom-${System.currentTimeMillis()}",
            name = "Custom Pizza",
            description = "Your custom masterpiece",
            basePrice = 10.0,
            imageRes = MockDataProvider.pizzas.first().imageRes, // Use a generic one or placeholder
            category = "cat1",
            availableToppings = MockDataProvider.toppings,
            isCustom = true
        )

        // Flatten the topping map to a list for cart compatibility
        cartViewModel.addToCart(
            pizza = customPizza,
            size = state.selectedSize,
            toppings = state.selectedToppingsList,
            crust = state.selectedCrust,
            sauce = state.selectedSauce,
            cheese = state.selectedCheese
        )
        
        _uiState.update { it.copy(isAddingToCart = true) }
        
        viewModelScope.launch {
            delay(800) // Match animation duration
            onComplete()
        }
    }

    /**
     * Prefill the builder with selections from an AI-generated pizza.
     * Looks up real model objects by ID from MockDataProvider
     * and jumps directly to the REVIEW step.
     */
    fun prefillFromAi(
        crustId: String?,
        sauceId: String?,
        cheeseId: String?,
        toppingIds: List<String>,
        sizeId: String?
    ) {
        val crust = crustId?.let { id -> availableCrusts.find { it.id == id } }
        val sauce = sauceId?.let { id -> availableSauces.find { it.id == id } }
        val cheese = cheeseId?.let { id -> availableCheeses.find { it.id == id } }
        val toppings = toppingIds
            .mapNotNull { id -> availableToppings.find { it.id == id } }
            .associateWith { 1 } // 1 of each topping
        val size = sizeId?.let { id ->
            PizzaSize.entries.find { it.name.equals(id, ignoreCase = true) }
        } ?: PizzaSize.MEDIUM

        _uiState.value = PizzaBuilderUiState(
            currentStep = PizzaBuilderStep.REVIEW,
            selectedCrust = crust,
            selectedSauce = sauce,
            selectedCheese = cheese,
            selectedToppings = toppings,
            selectedSize = size
        )
    }
}
