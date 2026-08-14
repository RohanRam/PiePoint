package com.piepoint.app.data.repository

import com.piepoint.app.R
import com.piepoint.app.data.model.*

object MockDataProvider {

    val toppings = listOf(
        Topping("t1", "Cheese", "\uD83E\uDDC0", 1.0, R.drawable.cheese),
        Topping("t2", "Pepperoni", "\uD83C\uDF56", 1.5, R.drawable.pepporoni),
        Topping("t3", "Mushroom", "\uD83C\uDF44", 1.0, R.drawable.mushroom),
        Topping("t4", "Olive", "\uD83E\uDEB4", 0.75, R.drawable.olive2),
        Topping("t5", "Onion", "\uD83E\uDDC5", 0.5, R.drawable.onion),
        Topping("t6", "Bell Pepper", "\uD83E\uDED1", 0.75, R.drawable.green_pepper),
        Topping("t7", "Tomato", "\uD83C\uDF45", 0.5, R.drawable.tomato),
        Topping("t8", "Basil", "\uD83C\uDF3F", 0.5, R.drawable.basil),
        Topping("t9", "Bacon", "\uD83E\uDD53", 1.5, R.drawable.beacon),
        Topping("t10", "Jalapeño", "\uD83C\uDF36", 0.75, R.drawable.jalapeaneo)
    )

    val crusts = listOf(
        Crust("c1", "Classic", "Hand-tossed classic crust", 0.0),
        Crust("c2", "Thin & Crispy", "Thin and crunchy base", 0.0),
        Crust("c3", "Cheese Burst", "Filled with melting cheese", 3.0),
        Crust("c4", "Whole Wheat", "Healthy whole grain choice", 1.5),
        Crust("c5", "Stuffed Crust", "Mozzarella-filled edges", 2.5)
    )

    val sauces = listOf(
        Sauce("s1", "Classic Tomato", 0.0, 0xFFE74C3C),
        Sauce("s2", "Spicy Arrabbiata", 0.5, 0xFFC0392B),
        Sauce("s3", "Garlic Cream", 1.0, 0xFFFDFEFE),
        Sauce("s4", "BBQ Sauce", 1.0, 0xFF7E5109),
        Sauce("s5", "Pesto", 1.5, 0xFF27AE60)
    )

    val cheeses = listOf(
        Cheese("ch1", "Mozzarella", 0.0),
        Cheese("ch2", "Cheddar", 1.0),
        Cheese("ch3", "Parmesan", 1.5),
        Cheese("ch4", "Four Cheese", 2.5)
    )

    val categories = listOf(
        PizzaCategory("cat1", "Pizza", "\uD83C\uDF55")
    )

    val pizzas = listOf(
        Pizza(
            id = "p1",
            name = "Margherita Classic",
            description = "The timeless classic with fresh tomato, mozzarella and basil. A pizza that never goes out of style.",
            basePrice = 12.99,
            imageRes = R.drawable.pizza_margherita,
            category = "cat1",
            availableToppings = toppings,
            rating = 4.8f,
            reviewCount = 245
        ),
        Pizza(
            id = "p2",
            name = "Pepperoni Supreme",
            description = "Loaded with premium pepperoni slices on a rich tomato base with melted mozzarella.",
            basePrice = 14.99,
            imageRes = R.drawable.pizza_pepperoni,
            category = "cat1",
            availableToppings = toppings,
            rating = 4.9f,
            reviewCount = 387
        ),
        Pizza(
            id = "p3",
            name = "BBQ Chicken",
            description = "Smoky BBQ sauce, tender chicken, caramelized onions and a blend of cheeses.",
            basePrice = 15.99,
            imageRes = R.drawable.pizza_bbq_chicken,
            category = "cat1",
            availableToppings = toppings,
            rating = 4.7f,
            reviewCount = 198
        ),
        Pizza(
            id = "p4",
            name = "Veggie Delight",
            description = "A colorful medley of fresh garden vegetables on our signature tomato sauce.",
            basePrice = 13.49,
            imageRes = R.drawable.pizza_veggie,
            category = "cat1",
            availableToppings = toppings,
            rating = 4.6f,
            reviewCount = 156
        ),
        Pizza(
            id = "p5",
            name = "Four Cheese",
            description = "An indulgent combination of mozzarella, cheddar, parmesan and gorgonzola.",
            basePrice = 16.49,
            imageRes = R.drawable.pizza_four_cheese,
            category = "cat1",
            availableToppings = toppings,
            rating = 4.9f,
            reviewCount = 312
        ),
        Pizza(
            id = "p6",
            name = "Hawaiian Dream",
            description = "Sweet pineapple chunks and savory ham on a creamy base – a sweet and salty paradise.",
            basePrice = 13.99,
            imageRes = R.drawable.pizza_hawaiian,
            category = "cat1",
            availableToppings = toppings,
            rating = 4.4f,
            reviewCount = 178
        )
    )

    val mockUser = User(
        id = "u1",
        name = "Alex Johnson",
        email = "alex.johnson@email.com",
        phone = "+1 (555) 234-5678",
        address = "123 Main Street, New York, NY 10001"
    )

    val deliveryFee = 2.99
}
