package com.pizzaorder.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzaorder.app.data.model.CartItem
import com.pizzaorder.app.ui.components.*
import com.pizzaorder.app.ui.theme.*
import com.pizzaorder.app.ui.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    onContinueShopping: () -> Unit
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val subtotal by remember { derivedStateOf { cartViewModel.subtotal } }
    val deliveryFee by remember { derivedStateOf { cartViewModel.deliveryFee } }
    val total by remember { derivedStateOf { cartViewModel.total } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "My Cart",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                if (cartItems.isNotEmpty()) {
                    TextButton(onClick = { cartViewModel.clearCart() }) {
                        Text("Clear", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            if (cartItems.isEmpty()) {
                EmptyCartState(onContinueShopping = onContinueShopping)
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems, key = { it.id }) { item ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            CartItemCard(
                                item = item,
                                onIncrease = { cartViewModel.increaseQuantity(item.id) },
                                onDecrease = { cartViewModel.decreaseQuantity(item.id) },
                                onRemove = { cartViewModel.removeItem(item.id) }
                            )
                        }
                    }
                }

                // Order Summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Order Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PriceLine("Subtotal", "$${String.format("%.2f", subtotal)}")
                        Spacer(modifier = Modifier.height(8.dp))
                        PriceLine("Delivery Fee", "$${String.format("%.2f", deliveryFee)}")
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFFEEEEEE))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "$${String.format("%.2f", total)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = OrangeAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        GradientButton(
                            text = "Proceed to Checkout",
                            onClick = onCheckout,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = item.pizza.imageRes),
                    contentDescription = item.pizza.name,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.pizza.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Size: ${item.selectedSize.label} (${item.selectedSize.inches}\")",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (item.selectedToppings.isNotEmpty()) {
                    Text(
                        text = item.selectedToppings.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$${String.format("%.2f", item.totalPrice)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = OrangeAccent
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(ErrorRed.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Remove",
                        tint = ErrorRed,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                QuantitySelector(
                    quantity = item.quantity,
                    onIncrease = onIncrease,
                    onDecrease = onDecrease
                )
            }
        }
    }
}

@Composable
private fun PriceLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EmptyCartState(onContinueShopping: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "\uD83D\uDED2", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your cart is empty",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add some delicious pizzas to get started!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(32.dp))
        GradientButton(
            text = "Browse Menu",
            onClick = onContinueShopping,
            modifier = Modifier.fillMaxWidth(0.7f)
        )
    }
}
