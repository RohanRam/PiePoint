package com.piepoint.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piepoint.app.data.model.Pizza
import com.piepoint.app.ui.components.*
import com.piepoint.app.ui.theme.*
import com.piepoint.app.ui.viewmodel.CartViewModel
import com.piepoint.app.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    cartViewModel: CartViewModel,
    onPizzaClick: (String) -> Unit,
    onCartClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartItemCount by remember { derivedStateOf { cartViewModel.itemCount } }
    val scope = rememberCoroutineScope()

    val pizzas = uiState.featuredPizzas
    val initialPage = if (pizzas.isNotEmpty()) 1000 * pizzas.size + uiState.featuredIndex else 0
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { if (pizzas.isNotEmpty()) Int.MAX_VALUE else 0 }
    )

    // Auto-slide logic
    LaunchedEffect(key1 = pagerState.currentPage, key2 = pizzas.size) {
        if (pizzas.isNotEmpty()) {
            delay(5000)
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        }
    }

    // Sync pager state back to ViewModel for other components (indicators, thumbnails)
    LaunchedEffect(pagerState.currentPage) {
        if (pizzas.isNotEmpty()) {
            val actualIndex = pagerState.currentPage % pizzas.size
            viewModel.setFeaturedIndex(actualIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8F9FA), Color(0xFFF0F2F5))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HomeTopBar(
                cartItemCount = cartItemCount,
                onCartClick = onCartClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Greeting Section
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Freshly Baked",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "Artisan pizzas delivered to your door",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Featured Pizza Section (3D Hero with Pager)
            if (pizzas.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val actualIndex = page % pizzas.size
                    FeaturedPizzaCard(
                        pizza = pizzas[actualIndex],
                        currentIndex = actualIndex,
                        totalCount = pizzas.size,
                        onPizzaClick = onPizzaClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Thumbnails for selection
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(pizzas) { index, pizza ->
                    val isSelected = index == uiState.featuredIndex
                    val borderAlpha by animateFloatAsState(if (isSelected) 1f else 0f, label = "thumb_border")
                    
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) OrangeAccent.copy(alpha = 0.05f) else Color.White)
                            .border(
                                width = 2.dp,
                                color = OrangeAccent.copy(alpha = borderAlpha),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { 
                                val currentPage = pagerState.currentPage
                                val currentActualIndex = currentPage % pizzas.size
                                val diff = index - currentActualIndex
                                scope.launch {
                                    pagerState.animateScrollToPage(currentPage + diff)
                                }
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            imageRes = pizza.imageRes,
                            contentDescription = pizza.name,
                            modifier = Modifier.size(50.dp).clip(CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Popular section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                SectionTitle(title = "Popular Right Now")
                Text(
                    text = "See All",
                    color = OrangeAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                itemsIndexed(pizzas) { _, pizza ->
                    PizzaCard(
                        pizza = pizza,
                        onClick = { onPizzaClick(pizza.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
private fun HomeTopBar(
    cartItemCount: Int,
    onCartClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome to",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "PiePoint",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = OrangeAccent,
                letterSpacing = (-1).sp
            )
        }
        CartBadge(
            itemCount = cartItemCount,
            onClick = onCartClick
        )
    }
}

@Composable
private fun FeaturedPizzaCard(
    pizza: Pizza,
    currentIndex: Int,
    totalCount: Int,
    onPizzaClick: (String) -> Unit
) {
    // Idle animation for 3D float
    val infiniteTransition = rememberInfiniteTransition(label = "hero_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y_offset"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "z_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(220.dp)
    ) {
        // Main Card (Sleek Gradient)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.1f))
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFFE8DC), Color(0xFFFFF3EE), Color.White)
                    )
                )
                .clickable { onPizzaClick(pizza.id) }
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.55f)) {
                Surface(
                    color = OrangeAccent.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "CHEF'S CHOICE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = OrangeAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = pizza.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$${pizza.basePrice}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = OrangeAccent
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Indicators
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(totalCount) { index ->
                        val width by animateDpAsState(if (index == currentIndex) 20.dp else 6.dp, label = "dot")
                        Box(
                            modifier = Modifier
                                .size(width, 6.dp)
                                .clip(CircleShape)
                                .background(if (index == currentIndex) OrangeAccent else Color(0xFFDCDCDC))
                        )
                    }
                }
            }
        }

        // Pizza "Popping Out" Image
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 20.dp, y = floatOffset.dp)
                .graphicsLayer {
                    rotationZ = rotation
                    cameraDistance = 12f
                },
            contentAlignment = Alignment.Center
        ) {
            // Shadow under pizza
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer { 
                        scaleX = 0.9f 
                        scaleY = 0.4f
                        translationY = 80f
                    }
                    .shadow(40.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.8f))
            )
            
            Image(
                imageRes = pizza.imageRes,
                contentDescription = pizza.name,
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .shadow(20.dp, CircleShape)
            )
        }
    }
}
