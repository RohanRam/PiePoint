package com.piepoint.app.data.model

enum class PizzaBuilderStep {
    CRUST,
    SAUCE,
    CHEESE,
    TOPPINGS,
    SIZE,
    REVIEW
}

data class PizzaBuilderUiState(
    val currentStep: PizzaBuilderStep = PizzaBuilderStep.CRUST,
    val selectedCrust: Crust? = null,
    val selectedSauce: Sauce? = null,
    val selectedCheese: Cheese? = null,
    val selectedToppings: Map<Topping, Int> = emptyMap(),
    val selectedSize: PizzaSize = PizzaSize.MEDIUM,
    val isAddingToCart: Boolean = false
) {
    val totalPrice: Double
        get() {
            val base = 10.0 // Base price for custom pizza
            val componentsPrice = (selectedCrust?.price ?: 0.0) +
                    (selectedSauce?.price ?: 0.0) +
                    (selectedCheese?.price ?: 0.0) +
                    selectedToppings.entries.sumOf { (topping, qty) -> topping.price * qty }
            return base + selectedSize.priceModifier + componentsPrice
        }

    /** Flatten the map to a list (repeating toppings by quantity) for cart compatibility */
    val selectedToppingsList: List<Topping>
        get() = selectedToppings.flatMap { (topping, qty) ->
            List(qty) { topping }
        }
}
