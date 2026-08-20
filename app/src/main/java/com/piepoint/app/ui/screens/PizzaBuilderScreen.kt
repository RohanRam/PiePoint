package com.piepoint.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import com.piepoint.app.data.model.*
import com.piepoint.app.ui.components.CartBadge
import com.piepoint.app.ui.theme.*
import com.piepoint.app.ui.viewmodel.CartViewModel
import com.piepoint.app.ui.viewmodel.PizzaBuilderViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PizzaBuilderScreen(
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: PizzaBuilderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartItemCount by cartViewModel.cartItems.collectAsState()

    BackHandler(enabled = uiState.currentStep != PizzaBuilderStep.CRUST) {
        viewModel.prevStep()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Build Your Pizza",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Make it exactly how you like it",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep == PizzaBuilderStep.CRUST) onBack()
                        else viewModel.prevStep()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    CartBadge(
                        itemCount = cartItemCount.sumOf { it.quantity },
                        onClick = onCartClick,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(bottom = 110.dp)) { // Floating above custom navbar
                PizzaBuilderBottomBar(
                    uiState = uiState,
                    onNext = {
                        if (uiState.currentStep == PizzaBuilderStep.REVIEW) {
                            viewModel.addToCart(cartViewModel, onBack)
                        } else {
                            viewModel.nextStep()
                        }
                    }
                )
            }
        },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Step Progress Indicator
            StepProgressIndicator(
                currentStep = uiState.currentStep,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Pizza Preview Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                PizzaPreview(uiState = uiState)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Customization Area
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                },
                label = "step_content"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    when (step) {
                        PizzaBuilderStep.CRUST -> CrustSelection(
                            selected = uiState.selectedCrust,
                            options = viewModel.availableCrusts,
                            onSelect = viewModel::selectCrust
                        )
                        PizzaBuilderStep.SAUCE -> SauceSelection(
                            selected = uiState.selectedSauce,
                            options = viewModel.availableSauces,
                            onSelect = viewModel::selectSauce
                        )
                        PizzaBuilderStep.CHEESE -> CheeseSelection(
                            selected = uiState.selectedCheese,
                            options = viewModel.availableCheeses,
                            onSelect = viewModel::selectCheese
                        )
                        PizzaBuilderStep.TOPPINGS -> ToppingsSelection(
                            selected = uiState.selectedToppings,
                            options = viewModel.availableToppings,
                            onToggle = viewModel::toggleTopping
                        )
                        PizzaBuilderStep.SIZE -> SizeSelection(
                            selected = uiState.selectedSize,
                            onSelect = viewModel::selectSize
                        )
                        PizzaBuilderStep.REVIEW -> ReviewSelection(
                            uiState = uiState,
                            onEdit = { viewModel.setStep(PizzaBuilderStep.CRUST) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PizzaPreview(uiState: PizzaBuilderUiState) {
    val cartAnimationProgress by animateFloatAsState(
        targetValue = if (uiState.isAddingToCart) 1f else 0f,
        animationSpec = tween(1000, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)),
        label = "add_to_cart_anim"
    )

    val sizeScale by animateFloatAsState(
        targetValue = (if (uiState.isAddingToCart) 0.15f else 1f) * when (uiState.selectedSize) {
            PizzaSize.SMALL -> 0.75f
            PizzaSize.MEDIUM -> 0.85f
            PizzaSize.LARGE -> 1.0f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pizza_size"
    )

    // Cinematic 3D Rotation during fly
    val cartRotation by animateFloatAsState(
        targetValue = if (uiState.isAddingToCart) 720f else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "cart_rotation"
    )

    Box(
        modifier = Modifier
            .size(300.dp)
            .graphicsLayer {
                scaleX = sizeScale
                scaleY = sizeScale
                // Fly towards top-right (Cart Badge position)
                translationY = -cartAnimationProgress * 1500f
                translationX = cartAnimationProgress * 800f
                rotationZ = cartRotation
                rotationY = cartRotation / 2f
                alpha = 1f - (cartAnimationProgress * 0.5f).coerceIn(0f, 1f)
                cameraDistance = 15f
            },
        contentAlignment = Alignment.Center
    ) {
        // 1. Crust Layer
        val crustColor = when (uiState.selectedCrust?.id) {
            "c3" -> Color(0xFFD4AC0D)
            "c4" -> Color(0xFF8D6E63)
            else -> Color(0xFFE67E22)
        }
        
        AnimatedVisibility(
            visible = uiState.selectedCrust != null,
            enter = scaleIn(initialScale = 0.5f) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(crustColor)
                    .border(8.dp, crustColor.copy(alpha = 0.8f), CircleShape)
            )
        }

        // 2. Sauce Layer
        AnimatedVisibility(
            visible = uiState.selectedSauce != null,
            enter = scaleIn(initialScale = 0.8f) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .clip(CircleShape)
                    .background(Color(uiState.selectedSauce?.color ?: 0xFFE74C3C))
            )
        }

        // 3. Cheese Layer
        AnimatedVisibility(
            visible = uiState.selectedCheese != null,
            enter = fadeIn(tween(500)),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.8f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFF1C40F).copy(alpha = 0.85f),
                                Color(0xFFF1C40F).copy(alpha = 0.4f)
                            )
                        )
                    )
            )
        }

        // 4. Toppings Layer
        ToppingsLayer(selectedToppings = uiState.selectedToppings)
    }
}

@Composable
fun ToppingsLayer(selectedToppings: List<Topping>) {
    val toppingPositions = remember {
        mapOf(
            "t1" to listOf(0.2f to 0.3f, 0.5f to 0.2f, 0.8f to 0.4f, 0.4f to 0.6f, 0.7f to 0.7f),
            "t2" to listOf(0.3f to 0.2f, 0.6f to 0.3f, 0.2f to 0.5f, 0.5f to 0.5f, 0.8f to 0.6f, 0.4f to 0.8f),
            "t3" to listOf(0.4f to 0.3f, 0.7f to 0.2f, 0.3f to 0.6f, 0.6f to 0.5f, 0.5f to 0.7f),
            "t4" to listOf(0.25f to 0.45f, 0.55f to 0.25f, 0.75f to 0.55f, 0.35f to 0.75f),
            "t5" to listOf(0.15f to 0.35f, 0.45f to 0.15f, 0.85f to 0.45f, 0.55f to 0.85f),
            "t6" to listOf(0.5f to 0.4f, 0.3f to 0.3f, 0.7f to 0.7f, 0.2f to 0.6f, 0.8f to 0.2f),
            "t7" to listOf(0.4f to 0.5f, 0.6f to 0.4f, 0.5f to 0.6f),
            "t8" to listOf(0.3f to 0.4f, 0.7f to 0.6f, 0.5f to 0.3f, 0.5f to 0.7f),
            "t9" to listOf(0.2f to 0.2f, 0.8f to 0.8f, 0.2f to 0.8f, 0.8f to 0.2f),
            "t10" to listOf(0.45f to 0.45f, 0.55f to 0.55f, 0.45f to 0.55f, 0.55f to 0.45f)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        selectedToppings.forEach { topping ->
            val positions = toppingPositions[topping.id] ?: emptyList()
            positions.forEachIndexed { index, pos ->
                key("${topping.id}-$index") {
                    AnimatedTopping(topping = topping, position = pos)
                }
            }
        }
    }
}

@Composable
fun AnimatedTopping(topping: Topping, position: Pair<Float, Float>) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (topping.imageRes != null) {
                Image(
                    painter = painterResource(id = topping.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                        .offset(
                            x = (300 * (position.first - 0.5f)).dp,
                            y = (300 * (position.second - 0.5f)).dp
                        )
                )
            } else {
                Text(
                    text = topping.emoji,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(
                            x = (300 * (position.first - 0.5f)).dp,
                            y = (300 * (position.second - 0.5f)).dp
                        )
                )
            }
        }
    }
}

@Composable
fun StepProgressIndicator(currentStep: PizzaBuilderStep, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PizzaBuilderStep.entries.forEachIndexed { index, step ->
            val isCompleted = index < currentStep.ordinal
            val isCurrent = index == currentStep.ordinal
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) Color(0xFF4CAF50)
                            else if (isCurrent) OrangeAccent
                            else Color.LightGray.copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("${index + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    step.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 10.sp,
                    color = if (isCurrent) OrangeAccent else TextSecondary,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }
            
            if (index < PizzaBuilderStep.entries.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 4.dp)
                        .background(if (isCompleted) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> InfiniteHorizontalSelector(
    items: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    cardContent: @Composable (T, Boolean) -> Unit
) {
    if (items.isEmpty()) return

    val startIndex = items.indexOf(selected).coerceAtLeast(0)
    val initialPage = 1000 * items.size + startIndex
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { Int.MAX_VALUE }
    )

    LaunchedEffect(pagerState.currentPage) {
        val actualIndex = pagerState.currentPage % items.size
        val targetItem = items[actualIndex]
        if (targetItem != selected) {
            onSelect(targetItem)
        }
    }

    LaunchedEffect(selected) {
        val actualIndex = pagerState.currentPage % items.size
        if (items[actualIndex] != selected) {
            val targetIndex = items.indexOf(selected).coerceAtLeast(0)
            val currentPageGroup = pagerState.currentPage / items.size
            val targetPage = currentPageGroup * items.size + targetIndex
            pagerState.scrollToPage(targetPage)
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 115.dp),
            pageSpacing = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            verticalAlignment = Alignment.CenterVertically
        ) { page ->
            val actualIndex = page % items.size
            val item = items[actualIndex]
            val isSelected = item == selected

            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            val scale = 0.75f + 0.25f * (1f - pageOffset.coerceIn(0f, 1f))
            val alpha = 0.3f + 0.7f * (1f - pageOffset.coerceIn(0f, 1f))
            val rotation = 12f * (pagerState.currentPageOffsetFraction + (pagerState.currentPage - page))

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        rotationY = -rotation * 1.5f
                        cameraDistance = 10f
                    }
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                cardContent(item, isSelected)
            }
        }
        
        // Edge Fade Effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.horizontalGradient(
                        0f to BackgroundWhite,
                        0.15f to Color.Transparent,
                        0.85f to Color.Transparent,
                        1f to BackgroundWhite
                    )
                )
        )
    }
}

@Composable
fun CarouselOptionCard(
    emoji: String,
    title: String,
    subtitle: String,
    price: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val animBg by animateColorAsState(if (isSelected) OrangeAccent else Color.White, label = "card_bg")
    val animContent by animateColorAsState(if (isSelected) Color.White else TextPrimary, label = "card_content")
    
    Surface(
        modifier = modifier
            .width(130.dp)
            .height(150.dp),
        shape = RoundedCornerShape(28.dp),
        color = animBg,
        shadowElevation = if (isSelected) 10.dp else 2.dp,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White.copy(alpha = 0.2f) else CardBackground)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = animContent,
                textAlign = TextAlign.Center,
                maxLines = 1,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = price,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White.copy(alpha = 0.9f) else OrangeAccent
            )
        }
    }
}

private fun getCrustEmoji(crustId: String): String = when (crustId) {
    "c1" -> "🍞"
    "c2" -> "🍕"
    "c3" -> "🧀"
    "c4" -> "🌾"
    "c5" -> "🥖"
    else -> "🍞"
}

private fun getSauceEmoji(sauceId: String): String = when (sauceId) {
    "s1" -> "🍅"
    "s2" -> "🌶️"
    "s3" -> "🧄"
    "s4" -> "🍯"
    "s5" -> "🌿"
    else -> "🍅"
}

private fun getCheeseEmoji(cheeseId: String): String = when (cheeseId) {
    "ch1" -> "🧀"
    "ch2" -> "🧀"
    "ch3" -> "🧈"
    "ch4" -> "🥛"
    else -> "🧀"
}

private fun getSizeEmoji(size: PizzaSize): String = when (size) {
    PizzaSize.SMALL -> "🍕"
    PizzaSize.MEDIUM -> "🍕"
    PizzaSize.LARGE -> "🍕"
}

@Composable
fun CrustSelection(selected: Crust?, options: List<Crust>, onSelect: (Crust) -> Unit) {
    Column {
        Text("Choose Your Crust", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        InfiniteHorizontalSelector(
            items = options,
            selected = selected,
            onSelect = onSelect
        ) { crust, isSelected ->
            CarouselOptionCard(
                emoji = getCrustEmoji(crust.id),
                title = crust.name,
                subtitle = crust.description,
                price = if (crust.price > 0) "+$${crust.price}" else "Free",
                isSelected = isSelected
            )
        }
    }
}

@Composable
fun SauceSelection(selected: Sauce?, options: List<Sauce>, onSelect: (Sauce) -> Unit) {
    Column {
        Text("Choose Your Sauce", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        InfiniteHorizontalSelector(
            items = options,
            selected = selected,
            onSelect = onSelect
        ) { sauce, isSelected ->
            CarouselOptionCard(
                emoji = getSauceEmoji(sauce.id),
                title = sauce.name,
                subtitle = "Premium sauce base",
                price = if (sauce.price > 0) "+$${sauce.price}" else "Free",
                isSelected = isSelected
            )
        }
    }
}

@Composable
fun CheeseSelection(selected: Cheese?, options: List<Cheese>, onSelect: (Cheese) -> Unit) {
    Column {
        Text("Pick Your Cheese", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        InfiniteHorizontalSelector(
            items = options,
            selected = selected,
            onSelect = onSelect
        ) { cheese, isSelected ->
            CarouselOptionCard(
                emoji = getCheeseEmoji(cheese.id),
                title = cheese.name,
                subtitle = "Freshly grated",
                price = if (cheese.price > 0) "+$${cheese.price}" else "Free",
                isSelected = isSelected
            )
        }
    }
}

@Composable
fun ToppingsSelection(selected: List<Topping>, options: List<Topping>, onToggle: (Topping) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Load It Up", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Choose your favorite toppings", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Surface(
                color = OrangeAccent.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "${selected.size} selected",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = OrangeAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 8.dp
        ) {
            options.forEach { topping ->
                val isSelected = selected.any { it.id == topping.id }
                ToppingChip(
                    topping = topping,
                    isSelected = isSelected,
                    onClick = { onToggle(topping) }
                )
            }
        }
    }
}

@Composable
fun SizeSelection(selected: PizzaSize, onSelect: (PizzaSize) -> Unit) {
    Column {
        Text("Choose Your Size", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        InfiniteHorizontalSelector(
            items = PizzaSize.entries,
            selected = selected,
            onSelect = onSelect
        ) { size, isSelected ->
            CarouselOptionCard(
                emoji = getSizeEmoji(size),
                title = size.label + " Size",
                subtitle = "${size.inches}\" Pizza base",
                price = if (size.priceModifier > 0) "+$${size.priceModifier}" else "Free",
                isSelected = isSelected
            )
        }
    }
}

@Composable
fun ReviewSelection(uiState: PizzaBuilderUiState, onEdit: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Your Perfect Pizza", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = onEdit) {
                Text("Edit", color = OrangeAccent, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        ReviewItem("Crust", uiState.selectedCrust?.name ?: "None")
        ReviewItem("Sauce", uiState.selectedSauce?.name ?: "None")
        ReviewItem("Cheese", uiState.selectedCheese?.name ?: "None")
        ReviewItem("Toppings", if (uiState.selectedToppings.isEmpty()) "None" else uiState.selectedToppings.joinToString { it.name })
        ReviewItem("Size", uiState.selectedSize.label + " - " + uiState.selectedSize.inches + "\"")
    }
}

@Composable
fun ReviewItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextHint, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ToppingChip(topping: Topping, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) OrangeAccent else Color.White,
        border = BorderStroke(1.dp, if (isSelected) OrangeAccent else Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (topping.imageRes != null) {
                Image(
                    painter = painterResource(id = topping.imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(topping.emoji)
            }
            Text(
                topping.name,
                color = if (isSelected) Color.White else TextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun PizzaBuilderBottomBar(uiState: PizzaBuilderUiState, onNext: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        color = Color.White.copy(alpha = 0.95f),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Current Price", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                AnimatedContent(
                    targetState = uiState.totalPrice,
                    transitionSpec = {
                        slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
                    },
                    label = "price_anim"
                ) { price ->
                    Text(
                        "$${String.format(Locale.US, "%.2f", price)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = OrangeAccent
                    )
                }
            }
            
            Button(
                onClick = onNext,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                modifier = Modifier.height(52.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    when (uiState.currentStep) {
                        PizzaBuilderStep.CRUST -> "Choose Sauce"
                        PizzaBuilderStep.SAUCE -> "Pick Cheese"
                        PizzaBuilderStep.CHEESE -> "Add Toppings"
                        PizzaBuilderStep.TOPPINGS -> "Choose Size"
                        PizzaBuilderStep.SIZE -> "Review Pizza"
                        PizzaBuilderStep.REVIEW -> "Add to Cart"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: Dp = 0.dp,
    crossAxisSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing),
        verticalArrangement = Arrangement.spacedBy(crossAxisSpacing),
        content = { content() }
    )
}
