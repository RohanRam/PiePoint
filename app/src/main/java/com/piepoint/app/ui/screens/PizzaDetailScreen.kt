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
import androidx.compose.ui.draw.scale
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

// Drag and Drop State
internal class DragAndDropState {
    var isDragging by mutableStateOf(false)
    var dragOffset by mutableStateOf(Offset.Zero)
    var dragTopping by mutableStateOf<Topping?>(null)
}

internal val LocalDragAndDropState = compositionLocalOf { DragAndDropState() }

data class PlacedTopping(
    val topping: Topping,
    val position: Offset,
    val rotation: Float,
    val scale: Float = 1f
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
    
    val dndState = remember { DragAndDropState() }
    var placedToppings by remember { mutableStateOf(listOf<PlacedTopping>()) }
    
    LaunchedEffect(uiState.selectedToppings) {
        placedToppings = placedToppings.filter { pt -> 
            uiState.selectedToppings.any { it.id == pt.topping.id } 
        }
    }

    // Modern Motion Constants
    val infiniteTransition = rememberInfiniteTransition(label = "modern_float")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pizza_y"
    )
    val tiltRotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pizza_rotation"
    )

    val pizzaScale by animateFloatAsState(
        targetValue = when (uiState.selectedSize) {
            PizzaSize.SMALL -> 0.82f
            PizzaSize.MEDIUM -> 1.0f
            PizzaSize.LARGE -> 1.18f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pizza_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceGradient)
    ) {
        CompositionLocalProvider(LocalDragAndDropState provides dndState) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Bar (Sleek)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .shadow(2.dp, RoundedCornerShape(14.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                    
                    Text(
                        text = "Customize",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )

                    CartBadge(itemCount = cartItemCount, onClick = onCartClick)
                }

                // 3D HERO SECTION
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Shadow floor (Dynamic scale with pizza)
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .graphicsLayer {
                                scaleX = 1.2f * pizzaScale
                                scaleY = 0.4f * pizzaScale
                                translationY = 140f
                                alpha = 0.2f
                            }
                            .shadow(60.dp, CircleShape, spotColor = Color.Black)
                    )

                    // Background Glow
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(OrangeAccent.copy(alpha = 0.15f), Color.Transparent)
                                )
                            )
                    )
                    
                    // Main Pizza Image
                    Image(
                        imageRes = pizza.imageRes,
                        contentDescription = pizza.name,
                        modifier = Modifier
                            .size(260.dp)
                            .clip(CircleShape)
                            .graphicsLayer {
                                translationY = floatY
                                rotationZ = tiltRotation
                                scaleX = pizzaScale
                                scaleY = pizzaScale
                                cameraDistance = 15f
                            }
                            .shadow(24.dp, CircleShape)
                    )

                    // Render Ingredients on Pizza
                    placedToppings.forEach { placed ->
                        Image(
                            imageRes = placed.topping.imageRes ?: 0,
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .offset { 
                                    IntOffset(
                                        (placed.position.x * pizzaScale).roundToInt(), 
                                        (placed.position.y * pizzaScale).roundToInt()
                                    ) 
                                }
                                .graphicsLayer {
                                    translationY = floatY
                                    rotationZ = tiltRotation + placed.rotation
                                    scaleX = pizzaScale
                                    scaleY = pizzaScale
                                }
                                .shadow(6.dp, CircleShape)
                        )
                    }

                    // Floating Rating
                    RatingChip(
                        rating = pizza.rating,
                        reviewCount = pizza.reviewCount,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 24.dp, top = 20.dp)
                    )
                }

                // Text Content
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = pizza.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pizza.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    AnimatedContent(
                        targetState = uiState.calculatedPrice,
                        transitionSpec = {
                            (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                        },
                        label = "price_modern"
                    ) { price ->
                        Text(
                            text = "$${String.format("%.2f", price)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = OrangeAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Modern Size Selector
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    SectionTitle("Select Size")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
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

                Spacer(modifier = Modifier.height(32.dp))

                // Infinite Topping Slider
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    SectionTitle("Add Ingredients")
                    Text(
                        text = "Hold and drag onto the pizza",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    val toppingCount = pizza.availableToppings.size
                    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = 1000 * toppingCount)

                    LazyRow(
                        state = lazyListState,
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
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

                Spacer(modifier = Modifier.height(32.dp))

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

                Spacer(modifier = Modifier.height(40.dp))

                GradientButton(
                    text = "Add to Cart",
                    onClick = {
                        cartViewModel.addToCart(pizza, uiState.selectedSize, uiState.selectedToppings)
                        showAddedSnackbar = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        // DRAG OVERLAY
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
                                if (dndState.dragOffset.y < 1200) { // Pizza Region
                                    dndState.dragTopping?.let { topping ->
                                        val randomX = (-75..75).random().toFloat()
                                        val randomY = (-75..75).random().toFloat()
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
                // Large Floating Topping Shadow
                dndState.dragTopping?.let { topping ->
                    Image(
                        imageRes = topping.imageRes ?: 0,
                        contentDescription = null,
                        modifier = Modifier
                            .size(90.dp)
                            .offset { 
                                IntOffset(
                                    dndState.dragOffset.x.roundToInt() - 120, 
                                    dndState.dragOffset.y.roundToInt() - 120
                                ) 
                            }
                            .rotate(15f)
                            .shadow(20.dp, CircleShape)
                            .scale(1.2f)
                    )
                }
            }
        }

        // Modern Snackbar
        AnimatedVisibility(
            visible = showAddedSnackbar,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
        ) {
            LaunchedEffect(showAddedSnackbar) {
                if (showAddedSnackbar) {
                    kotlinx.coroutines.delay(2000)
                    showAddedSnackbar = false
                }
            }
            Surface(
                color = TextPrimary,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 12.dp,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Text("Added to your cart!", color = Color.White, fontWeight = FontWeight.Bold)
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
            .size(100.dp)
            .shadow(if (isSelected) 8.dp else 2.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) OrangeAccent.copy(alpha = 0.1f) else Color.White)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) OrangeAccent else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            )
            .pointerInput(topping) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        state.dragTopping = topping
                        state.isDragging = true
                        state.dragOffset = Offset(400f, 1600f) // Heuristic start
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        state.dragOffset += dragAmount
                    },
                    onDragEnd = { state.isDragging = false },
                    onDragCancel = { state.isDragging = false }
                )
            }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            imageRes = topping.imageRes ?: 0,
            contentDescription = null,
            modifier = Modifier.size(50.dp).shadow(4.dp, CircleShape)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = topping.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) OrangeAccent else TextSecondary,
            fontWeight = FontWeight.Bold,
            maxLines = 1
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
    val animBg by animateColorAsState(if (isSelected) OrangeAccent else Color.White, label = "bg")
    val animContent by animateColorAsState(if (isSelected) Color.White else TextPrimary, label = "content")

    Column(
        modifier = modifier
            .height(90.dp)
            .shadow(if (isSelected) 12.dp else 2.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(animBg)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = size.label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = animContent
        )
        Text(
            text = "${size.inches}\"",
            style = MaterialTheme.typography.labelMedium,
            color = animContent.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold
        )
        if (size.priceModifier > 0) {
            Text(
                text = "+$${String.format("%.0f", size.priceModifier)}",
                style = MaterialTheme.typography.labelSmall,
                color = animContent.copy(alpha = 0.7f),
                fontWeight = FontWeight.Black
            )
        }
    }
}
