package com.piepoint.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piepoint.app.data.model.PizzaSize
import com.piepoint.app.data.model.Topping
import com.piepoint.app.ui.components.*
import com.piepoint.app.ui.theme.*
import com.piepoint.app.ui.viewmodel.CartViewModel
import com.piepoint.app.ui.viewmodel.DetailViewModel
import kotlin.math.roundToInt

// Drag and Drop Data
internal class DragAndDropState {
    var isDragging by mutableStateOf(false)
    var dragOffset by mutableStateOf(Offset.Zero)
    var dragTopping by mutableStateOf<Topping?>(null)
}

internal val LocalDragAndDropState = compositionLocalOf { DragAndDropState() }

data class PlacedTopping(
    val topping: Topping,
    val position: Offset,
    val rotation: Float
)

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
    
    // Drag and Drop State
    val dndState = remember { DragAndDropState() }
    var placedToppings by remember { mutableStateOf(listOf<PlacedTopping>()) }
    
    // Sync with viewModel toppings (if user manually deselects, remove from visual)
    LaunchedEffect(uiState.selectedToppings) {
        placedToppings = placedToppings.filter { pt: PlacedTopping -> 
            uiState.selectedToppings.any { it.id == pt.topping.id } 
        }
    }

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
    val idleRotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation_anim"
    )

    val pizzaScale by animateFloatAsState(
        targetValue = when (uiState.selectedSize) {
            PizzaSize.SMALL -> 0.85f
            PizzaSize.MEDIUM -> 1.0f
            PizzaSize.LARGE -> 1.15f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pizza_size_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        CompositionLocalProvider(LocalDragAndDropState provides dndState) {
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
                        .height(320.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Drop Zone / Pizza Container
                    Box(
                        modifier = Modifier
                            .size(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background circles
                        Box(
                            modifier = Modifier
                                .size(300.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF3EE))
                        )
                        Box(
                            modifier = Modifier
                                .size(260.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFE8DC))
                        )
                        
                        // The Pizza
                        Image(
                            painter = painterResource(id = pizza.imageRes),
                            contentDescription = pizza.name,
                            modifier = Modifier
                                .size(240.dp)
                                .clip(CircleShape)
                                .graphicsLayer {
                                    translationY = floatOffset
                                    rotationZ = idleRotation
                                    scaleX = pizzaScale
                                    scaleY = pizzaScale
                                }
                                .shadow(elevation = 16.dp, shape = CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        // Dropped Toppings
                        placedToppings.forEach { placed ->
                            val toppingScale by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = spring(Spring.DampingRatioMediumBouncy),
                                label = "drop_pop"
                            )
                            
                            Image(
                                painter = painterResource(id = placed.topping.imageRes ?: 0),
                                contentDescription = placed.topping.name,
                                modifier = Modifier
                                    .size(48.dp)
                                    .offset { 
                                        IntOffset(
                                            placed.position.x.roundToInt(), 
                                            placed.position.y.roundToInt()
                                        ) 
                                    }
                                    .graphicsLayer {
                                        translationY = floatOffset
                                        rotationZ = idleRotation + placed.rotation
                                        scaleX = pizzaScale * toppingScale
                                        scaleY = pizzaScale * toppingScale
                                    }
                                    .shadow(elevation = 4.dp, shape = CircleShape)
                            )
                        }
                    }

                    // Rating badge
                    RatingChip(
                        rating = pizza.rating,
                        reviewCount = pizza.reviewCount,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 24.dp, top = 8.dp)
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

                // Toppings Slide Selector (Infinite Loop)
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    SectionTitle("Add Toppings")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Drag and drop ingredients onto your pizza",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val toppingCount = pizza.availableToppings.size
                    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = 1000 * toppingCount)

                    LazyRow(
                        state = lazyListState,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(Int.MAX_VALUE) { index ->
                            val topping = pizza.availableToppings[index % toppingCount]
                            ToppingDragItem(
                                topping = topping,
                                isSelected = uiState.selectedToppings.any { it.id == topping.id }
                            )
                        }
                    }
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
        }

        // Drag and Drop Overlay
        if (dndState.isDragging) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dndState.dragOffset += dragAmount
                            },
                            onDragEnd = {
                                // Drop logic: check if dropped in the pizza area (roughly Y < 800)
                                if (dndState.dragOffset.y < 1000) { 
                                    dndState.dragTopping?.let { topping ->
                                        // Snap to pizza center with random offset for realistic placement
                                        val randomX = (-70..70).random().toFloat()
                                        val randomY = (-70..70).random().toFloat()
                                        placedToppings = placedToppings + PlacedTopping(
                                            topping = topping, 
                                            position = Offset(randomX, randomY),
                                            rotation = (0..360).random().toFloat()
                                        )
                                        viewModel.toggleTopping(topping)
                                    }
                                }
                                dndState.isDragging = false
                            },
                            onDragCancel = { dndState.isDragging = false }
                        )
                    }
            ) {
                // Drag Shadow (Realistic Image)
                dndState.dragTopping?.let { topping ->
                    Image(
                        painter = painterResource(id = topping.imageRes ?: 0),
                        contentDescription = topping.name,
                        modifier = Modifier
                            .size(70.dp)
                            .offset { 
                                IntOffset(
                                    dndState.dragOffset.x.roundToInt() - 100, 
                                    dndState.dragOffset.y.roundToInt() - 100
                                ) 
                            }
                            .rotate(15f)
                            .shadow(8.dp, CircleShape)
                    )
                }
            }
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
private fun ToppingDragItem(
    topping: Topping,
    isSelected: Boolean
) {
    val state = LocalDragAndDropState.current
    
    Column(
        modifier = Modifier
            .size(90.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) OrangeAccent.copy(alpha = 0.12f) else CardBackground)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) OrangeAccent else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .pointerInput(topping) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        state.dragTopping = topping
                        state.isDragging = true
                        state.dragOffset = Offset(300f, 1500f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        state.dragOffset += dragAmount
                    },
                    onDragEnd = { state.isDragging = false },
                    onDragCancel = { state.isDragging = false }
                )
            }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = topping.imageRes ?: 0),
            contentDescription = topping.name,
            modifier = Modifier
                .size(44.dp)
                .shadow(4.dp, CircleShape)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = topping.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) OrangeAccent else TextSecondary,
            maxLines = 1,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center
        )
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
            .height(80.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(animBg)
            .border(1.5.dp, animBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = size.label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = animText
        )
        Text(
            text = "${size.inches}\"",
            style = MaterialTheme.typography.labelMedium,
            color = animText.copy(alpha = 0.8f)
        )
        if (size.priceModifier > 0) {
            Text(
                text = "+$${String.format("%.2f", size.priceModifier)}",
                style = MaterialTheme.typography.labelSmall,
                color = animText.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
