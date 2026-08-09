package com.pizzaorder.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.pizzaorder.app.data.model.Pizza
import com.pizzaorder.app.data.model.PizzaCategory
import com.pizzaorder.app.ui.components.*
import com.pizzaorder.app.ui.theme.*
import com.pizzaorder.app.ui.viewmodel.CartViewModel
import com.pizzaorder.app.ui.viewmodel.HomeViewModel
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
            HomeTopBar(
                cartItemCount = cartItemCount,
                onCartClick = onCartClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Greeting
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Order Manually",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Choose your perfect pizza",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category selector
            CategorySelector(
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Featured pizza
            if (uiState.featuredPizzas.isNotEmpty()) {
                FeaturedPizzaSection(
                    pizzas = uiState.featuredPizzas,
                    featuredIndex = uiState.featuredIndex,
                    onPizzaClick = onPizzaClick,
                    onIndexChanged = { viewModel.setFeaturedIndex(it) }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Popular section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle(title = "Popular Picks")
                TextButton(onClick = {}) {
                    Text("See All", color = OrangeAccent, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(uiState.featuredPizzas) { _, pizza ->
                    PizzaCard(
                        pizza = pizza,
                        onClick = { onPizzaClick(pizza.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
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
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good Evening! 👋",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = "PizzaOrder",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = OrangeAccent
            )
        }
        CartBadge(
            itemCount = cartItemCount,
            onClick = onCartClick
        )
    }
}

@Composable
private fun CategorySelector(
    categories: List<PizzaCategory>,
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(categories) { _, category ->
            val isSelected = category.id == selectedCategoryId
            val animatedBg by animateColorAsState(
                targetValue = if (isSelected) OrangeAccent else CardBackground,
                animationSpec = tween(200), label = "cat_bg"
            )
            val animatedText by animateColorAsState(
                targetValue = if (isSelected) Color.White else TextSecondary,
                animationSpec = tween(200), label = "cat_text"
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(animatedBg)
                    .clickable { onCategorySelected(category.id) }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.emoji,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = animatedText,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedPizzaSection(
    pizzas: List<Pizza>,
    featuredIndex: Int,
    onPizzaClick: (String) -> Unit,
    onIndexChanged: (Int) -> Unit
) {
    val featured = pizzas[featuredIndex]
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_anim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFFF3EE), Color(0xFFFFE8DC))
                )
            )
            .clickable { onPizzaClick(featured.id) }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Featured",
                    style = MaterialTheme.typography.labelMedium,
                    color = OrangeAccent,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = featured.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = featured.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "$${String.format("%.2f", featured.basePrice)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangeAccent
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(OrangeAccent)
                            .clickable { onPizzaClick(featured.id) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Order Now",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Pizza image floating
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer { translationY = floatOffset }
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = featured.imageRes),
                    contentDescription = featured.name,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Dot indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            pizzas.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(if (index == featuredIndex) 24.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (index == featuredIndex) OrangeAccent else Color(0xFFCCCCCC))
                        .clickable { onIndexChanged(index) }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Mini pizza thumbnails row
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(pizzas) { index, pizza ->
            val isSelected = index == featuredIndex
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) OrangeAccent.copy(alpha = 0.15f) else CardBackground)
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) OrangeAccent else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onIndexChanged(index) }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = pizza.imageRes),
                    contentDescription = pizza.name,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
