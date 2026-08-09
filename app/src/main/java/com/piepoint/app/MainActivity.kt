package com.piepoint.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.piepoint.app.ui.navigation.BottomNavItem
import com.piepoint.app.ui.navigation.PizzaNavGraph
import com.piepoint.app.ui.navigation.Screen
import com.piepoint.app.ui.theme.PiePointTheme
import com.piepoint.app.ui.theme.*
import com.piepoint.app.ui.viewmodel.CartViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PiePointTheme {
                PiePointApp()
            }
        }
    }
}

@Composable
fun PiePointApp() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Orders,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Screens that should show bottom nav
    val showBottomNav = currentDestination?.route in listOf(
        Screen.Home.route,
        Screen.OrderHistory.route,
        Screen.Profile.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundWhite,
        bottomBar = {
            if (showBottomNav) {
                PizzaBottomNavBar(
                    items = bottomNavItems,
                    currentDestination = currentDestination?.route,
                    onItemClick = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        PizzaNavGraph(
            navController = navController,
            cartViewModel = cartViewModel
        )
    }
}

@Composable
private fun PizzaBottomNavBar(
    items: List<BottomNavItem>,
    currentDestination: String?,
    onItemClick: (BottomNavItem) -> Unit
) {
    val icons: Map<String, ImageVector> = mapOf(
        BottomNavItem.Home.route to Icons.Rounded.Restaurant,
        BottomNavItem.Orders.route to Icons.Rounded.ReceiptLong,
        BottomNavItem.Profile.route to Icons.Rounded.Person
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        containerColor = SurfaceWhite,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentDestination == item.route
            val iconColor by animateColorAsState(
                targetValue = if (isSelected) OrangeAccent else TextHint,
                animationSpec = tween(200), label = "nav_icon"
            )
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item) },
                icon = {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(OrangeAccent.copy(alpha = 0.12f))
                            )
                        }
                        Icon(
                            imageVector = icons[item.route] ?: Icons.Rounded.Circle,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        color = iconColor,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OrangeAccent,
                    unselectedIconColor = TextHint,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
