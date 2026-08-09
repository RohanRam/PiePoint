package com.piepoint.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piepoint.app.data.model.PizzaSize
import com.piepoint.app.data.model.Topping
import com.piepoint.app.ui.components.*
import com.piepoint.app.ui.theme.*
import com.piepoint.app.ui.viewmodel.CartViewModel
import com.piepoint.app.ui.viewmodel.DetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PizzaDetailScreen(
    pizzaId: String,
    viewModel: DetailViewModel,
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onCartClick: () -> Unit
) {
    LaunchedEffect(pizzaId) { viewModel.loadPizza(pizzaId) }

    val uiState by viewModel.uiState.collectAsState()
    val pizza = uiState.pizza ?: return
    val cartItemCount by remember { derivedStateOf { cartViewModel.itemCount } }

    var showAddedSnackbar by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "float_detail")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_detail_anim"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation_anim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
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
                    text = pizza.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                CartBadge(itemCount = cartItemCount, onClick = onCartClick)
            }

            // Pizza image hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circles
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF3EE))
                )
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE8DC))
                )
                androidx.compose.foundation.Image(
                    painter = painterResource(id = pizza.imageRes),
                    contentDescription = pizza.name,
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .graphicsLayer {
                            translationY = floatOffset
                            rotationZ = rotation
                        }
                        .shadow(elevation = 16.dp, shape = CircleShape),
                    contentScale = ContentScale.Crop
                )
                // Rating badge
                RatingChip(
                    rating = pizza.rating,
                    reviewCount = pizza.reviewCount,
                    modifier = Modifier.align(Alignment.TopEnd).padding(end = 24.dp, top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name & price
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = pizza.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pizza.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = uiState.calculatedPrice,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                    },
                    label = "price_anim"
                ) { price ->
                    Text(
                        text = "$${String.format("%.2f", price)}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangeAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Size selector
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                SectionTitle("Choose Size")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PizzaSize.entries.forEach { size ->
                        SizeChip(
                            size = size,
                            isSelected = uiState.selectedSize == size,
                            onClick = { viewModel.selectSize(size) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Toppings
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                SectionTitle("Toppings")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select extras to customize your pizza",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                ToppingsGrid(
                    toppings = pizza.availableToppings,
                    selectedToppings = uiState.selectedToppings.map { it.id }.toSet(),
                    onToppingToggle = { viewModel.toggleTopping(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quantity
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Quantity")
                QuantitySelector(
                    quantity = uiState.quantity,
                    onIncrease = { viewModel.setQuantity(uiState.quantity + 1) },
                    onDecrease = { viewModel.setQuantity(uiState.quantity - 1) }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Add to cart button
            GradientButton(
                text = "Add to Cart — $${String.format("%.2f", uiState.calculatedPrice)}",
                onClick = {
                    cartViewModel.addToCart(pizza, uiState.selectedSize, uiState.selectedToppings)
                    showAddedSnackbar = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Snackbar overlay
        AnimatedVisibility(
            visible = showAddedSnackbar,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp, start = 24.dp, end = 24.dp)
        ) {
            LaunchedEffect(showAddedSnackbar) {
                if (showAddedSnackbar) {
                    kotlinx.coroutines.delay(2000)
                    showAddedSnackbar = false
                }
            }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TextPrimary)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Added to cart!",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SizeChip(
    size: PizzaSize,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animBg by animateColorAsState(
        targetValue = if (isSelected) OrangeAccent else CardBackground,
        animationSpec = tween(200), label = "size_bg"
    )
    val animText by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextSecondary,
        animationSpec = tween(200), label = "size_text"
    )
    val animBorder by animateColorAsState(
        targetValue = if (isSelected) OrangeAccent else Color.Transparent,
        animationSpec = tween(200), label = "size_border"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(animBg)
            .border(1.5.dp, animBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = size.label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = animText
        )
        Text(
            text = "${size.inches}\"",
            style = MaterialTheme.typography.labelSmall,
            color = animText.copy(alpha = 0.7f)
        )
        if (size.priceModifier > 0) {
            Text(
                text = "+$${String.format("%.2f", size.priceModifier)}",
                style = MaterialTheme.typography.labelSmall,
                color = animText.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ToppingsGrid(
    toppings: List<Topping>,
    selectedToppings: Set<String>,
    onToppingToggle: (Topping) -> Unit
) {
    val rows = toppings.chunked(5)
    rows.forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row.forEach { topping ->
                ToppingItem(
                    topping = topping,
                    isSelected = topping.id in selectedToppings,
                    onToggle = { onToppingToggle(topping) },
                    modifier = Modifier.weight(1f)
                )
            }
            // Fill empty slots
            repeat(5 - row.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ToppingItem(
    topping: Topping,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "topping_scale"
    )
    val animBg by animateColorAsState(
        targetValue = if (isSelected) OrangeAccent.copy(alpha = 0.15f) else CardBackground,
        animationSpec = tween(200), label = "topping_bg"
    )
    val animBorder by animateColorAsState(
        targetValue = if (isSelected) OrangeAccent else Color.Transparent,
        animationSpec = tween(200), label = "topping_border"
    )

    Column(
        modifier = modifier
            .graphicsLayer { scaleX = animScale; scaleY = animScale }
            .clip(RoundedCornerShape(12.dp))
            .background(animBg)
            .border(1.5.dp, animBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = topping.emoji, fontSize = 24.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = topping.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) OrangeAccent else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
