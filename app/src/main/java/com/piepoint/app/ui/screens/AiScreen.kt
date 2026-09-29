@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.piepoint.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.piepoint.app.data.model.*
import com.piepoint.app.data.repository.MockDataProvider
import com.piepoint.app.ui.components.CartBadge
import com.piepoint.app.ui.theme.*
import com.piepoint.app.ui.viewmodel.AiViewModel
import com.piepoint.app.ui.viewmodel.CartViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Quick Chips ────────────────────────────────────────────────────────────

private val quickChips = listOf(
    "✨ Build my pizza",
    "Spicy & veg",
    "Under ₹300",
    "Cheesy for 2",
    "What's popular?"
)

// ─── Main Screen ────────────────────────────────────────────────────────────

@Composable
fun AiScreen(
    cartViewModel: CartViewModel,
    onNavigateToBuilder: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: AiViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Detect keyboard visibility
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val isKeyboardOpen = imeBottom > 0

    // Cart badge count
    val cartItemCount by remember { derivedStateOf { cartViewModel.itemCount } }

    // Toast-like feedback for add-to-cart
    var toastMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2500)
            toastMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFFF8F9FA), Color(0xFFF0F2F5)))
            )
            .statusBarsPadding()
            .imePadding()
    ) {
        // ── Header ──
        AiHeader(
            cartItemCount = cartItemCount,
            onCartClick = onCartClick
        )

        // ── Toast overlay ──
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = SuccessGreen,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = toastMessage ?: "",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // ── Content area (empty state or chat) ──
        Crossfade(
            targetState = uiState.messages.isEmpty(),
            modifier = Modifier.weight(1f),
            label = "chat_crossfade"
        ) { isEmpty ->
            if (isEmpty) {
                AiEmptyState(
                    onChipClick = { chip ->
                        viewModel.sendMessage(chip)
                    }
                )
            } else {
                ChatMessagesList(
                    messages = uiState.messages,
                    isLoading = uiState.isLoading,
                    onCustomize = { pizza ->
                        AiPizzaPrefill.pendingPizza = pizza
                        onNavigateToBuilder()
                    },
                    onAddToCart = { pizza ->
                        addAiPizzaToCart(pizza, cartViewModel)
                        toastMessage = "${pizza.name} added to cart!"
                    }
                )
            }
        }

        // ── Input bar ──
        AiInputBar(
            input = inputText,
            onInputChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                }
            },
            isLoading = uiState.isLoading,
            modifier = Modifier.padding(bottom = if (isKeyboardOpen) 8.dp else 92.dp)
        )
    }
}

// ─── Header ─────────────────────────────────────────────────────────────────

@Composable
private fun AiHeader(
    cartItemCount: Int,
    onCartClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Animated sparkle
            val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
            val sparkleRotation by infiniteTransition.animateFloat(
                initialValue = -10f, targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "sparkle_rot"
            )

            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = OrangeAccent,
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer { rotationZ = sparkleRotation }
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Ask PiePoint AI",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Your AI pizza assistant",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        CartBadge(
            itemCount = cartItemCount,
            onClick = onCartClick
        )
    }
}

// ─── Empty State ────────────────────────────────────────────────────────────

@Composable
private fun AiEmptyState(
    onChipClick: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow_alpha"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "float_y"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(0.3f))

        // Glowing sparkle icon
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                OrangeAccent.copy(alpha = glowAlpha),
                                OrangeLight.copy(alpha = glowAlpha * 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer { translationY = floatY }
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(OrangeAccent.copy(alpha = 0.12f), OrangeLight.copy(alpha = 0.06f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Ask me anything\nabout pizza!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp,
            letterSpacing = (-0.5).sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "I can build your dream pizza or\nrecommend from our menu",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(32.dp))

        // Quick chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            quickChips.forEach { chip ->
                QuickChip(text = chip, onClick = { onChipClick(chip) })
            }
        }

        Spacer(Modifier.weight(0.5f))
    }
}

@Composable
private fun QuickChip(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chip_scale"
    )

    Surface(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(50.dp),
        color = Color.White.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
        shadowElevation = 3.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

// ─── Chat Messages List ─────────────────────────────────────────────────────

@Composable
private fun ChatMessagesList(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    onCustomize: (AiPizza) -> Unit,
    onAddToCart: (AiPizza) -> Unit
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive or loading state changes
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty() || isLoading) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        reverseLayout = true,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Typing indicator (at the visual bottom when reverseLayout = true)
        if (isLoading) {
            item(key = "typing") {
                TypingIndicator()
            }
        }

        // Messages in reverse order (newest at visual bottom)
        items(
            items = messages.asReversed(),
            key = { it.id }
        ) { message ->
            ChatBubble(
                message = message,
                onCustomize = onCustomize,
                onAddToCart = onAddToCart
            )
        }
    }
}

// ─── Chat Bubble ────────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onCustomize: (AiPizza) -> Unit,
    onAddToCart: (AiPizza) -> Unit
) {
    val isUser = message.role == MessageRole.USER

    // Enter animation
    val enterAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enterAnim.animateTo(1f, tween(350, easing = FastOutSlowInEasing)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = enterAnim.value
                translationY = (1f - enterAnim.value) * 30f
            },
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // ── Text bubble ──
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .then(
                    if (isUser) {
                        Modifier
                            .shadow(4.dp, RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp))
                            .clip(RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp))
                            .background(PremiumGradient)
                    } else {
                        Modifier
                            .shadow(4.dp, RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                            .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                            .background(Color.White)
                    }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) Color.White else TextPrimary,
                lineHeight = 22.sp
            )
        }

        // ── Pizza card (assistant only) ──
        if (!isUser && message.pizza != null) {
            Spacer(Modifier.height(10.dp))
            AiPizzaCard(
                pizza = message.pizza,
                onCustomize = { onCustomize(message.pizza) },
                onAddToCart = { onAddToCart(message.pizza) }
            )
        }
    }
}

// ─── Pizza Card ─────────────────────────────────────────────────────────────

@Composable
private fun AiPizzaCard(
    pizza: AiPizza,
    onCustomize: () -> Unit,
    onAddToCart: () -> Unit
) {
    // Scale-in animation
    val enterScale = remember { Animatable(0.85f) }
    LaunchedEffect(Unit) {
        enterScale.animateTo(
            1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    Card(
        modifier = Modifier
            .widthIn(max = 300.dp)
            .graphicsLayer { scaleX = enterScale.value; scaleY = enterScale.value }
            .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = OrangeAccent.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Pizza name
            Text(
                text = pizza.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = (-0.3).sp
            )

            Spacer(Modifier.height(8.dp))

            // Components row
            Text(
                text = "${pizza.crust.name} · ${pizza.sauce.name} · ${pizza.cheese.name}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(10.dp))

            // Topping pills
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                pizza.toppings.forEach { topping ->
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = CardBackground,
                        border = BorderStroke(0.5.dp, Color(0xFFE8E8E8))
                    ) {
                        Text(
                            text = "${topping.emoji} ${topping.name}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Size & price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = OrangeAccent.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "${pizza.size.id} · ${pizza.size.inches}\"",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent
                        )
                    }
                }
                Text(
                    text = "₹${pizza.price}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = OrangeAccent,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(Modifier.height(14.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Customize button
                OutlinedButton(
                    onClick = onCustomize,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, OrangeAccent),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(
                        Icons.Rounded.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = OrangeAccent
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Customize",
                        color = OrangeAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Add to cart button
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(
                        Icons.Rounded.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Add to Cart",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ─── Typing Indicator ───────────────────────────────────────────────────────

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    Row(
        modifier = Modifier
            .shadow(3.dp, RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
            .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, delayMillis = index * 120, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer { translationY = offsetY }
                    .clip(CircleShape)
                    .background(OrangeAccent.copy(alpha = 0.45f))
            )
        }
    }
}

// ─── Input Bar ──────────────────────────────────────────────────────────────

@Composable
private fun AiInputBar(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val sendEnabled = input.isNotBlank() && !isLoading

    // Send button spring animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val sendScale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "send_scale"
    )
    val sendBgColor by animateColorAsState(
        targetValue = if (sendEnabled) OrangeAccent else Color(0xFFD4D4D4),
        animationSpec = tween(250),
        label = "send_color"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    "Ask about pizza…",
                    color = TextHint,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                cursorColor = OrangeAccent
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
            singleLine = true,
            enabled = !isLoading
        )

        // Send button
        Box(
            modifier = Modifier
                .size(44.dp)
                .graphicsLayer { scaleX = sendScale; scaleY = sendScale }
                .clip(CircleShape)
                .background(sendBgColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = sendEnabled,
                    onClick = onSend
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowUpward,
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────────

/**
 * Map an AI pizza response to existing model types and add to cart.
 * Uses MockDataProvider to look up components by ID.
 */
private fun addAiPizzaToCart(
    aiPizza: AiPizza,
    cartViewModel: CartViewModel
) {
    val pizza = Pizza(
        id = "ai-${System.currentTimeMillis()}",
        name = aiPizza.name,
        description = "Created by PiePoint AI",
        basePrice = 10.0, // base for custom pizzas in existing pricing
        imageRes = MockDataProvider.pizzas.first().imageRes,
        category = "cat1",
        availableToppings = MockDataProvider.toppings,
        isCustom = true
    )

    val crust = MockDataProvider.crusts.find { it.id == aiPizza.crust.id }
    val sauce = MockDataProvider.sauces.find { it.id == aiPizza.sauce.id }
    val cheese = MockDataProvider.cheeses.find { it.id == aiPizza.cheese.id }
    val toppings = aiPizza.toppings.mapNotNull { t ->
        MockDataProvider.toppings.find { it.id == t.id }
    }
    val size = PizzaSize.entries.find { it.name == aiPizza.size.id } ?: PizzaSize.MEDIUM

    cartViewModel.addToCart(
        pizza = pizza,
        size = size,
        toppings = toppings,
        crust = crust,
        sauce = sauce,
        cheese = cheese
    )
}
