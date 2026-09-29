package com.piepoint.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Create : Screen("create")
    object AskPie : Screen("ask_pie")
    object PizzaDetail : Screen("pizza_detail/{pizzaId}") {
        fun createRoute(pizzaId: String) = "pizza_detail/$pizzaId"
    }
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object OrderConfirmation : Screen("order_confirmation/{orderId}") {
        fun createRoute(orderId: String) = "order_confirmation/$orderId"
    }
    object OrderHistory : Screen("order_history")
    object Profile : Screen("profile")
}

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val iconName: String
) {
    object Create   : BottomNavItem("create",        "Create",   "add_circle_outline")
    object AskPie   : BottomNavItem("ask_pie",       "Ask Pie",  "auto_awesome")
    object Menu     : BottomNavItem("home",           "Menu",     "local_pizza")   // center elevated
    object Orders   : BottomNavItem("order_history",  "Orders",   "receipt_long")
    object Profile  : BottomNavItem("profile",        "Profile",  "person")
}
