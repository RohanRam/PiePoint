package com.piepoint.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.piepoint.app.ui.theme.*

@Composable
fun PizzaCard(
    pizza: Pizza,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "card_scale"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 8.dp,
        label = "card_shadow"
    )

    Card(
        modifier = modifier
            .width(180.dp)
            .graphicsLayer { 
                scaleX = scale
                scaleY = scale
            }
            .shadow(shadowElevation, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFF9F9F9), Color(0xFFF0F0F0)))),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    imageRes = pizza.imageRes,
                    contentDescription = pizza.name,
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(12.dp, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = pizza.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = YellowAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = pizza.rating.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${String.format("%.2f", pizza.basePrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = OrangeAccent
                )
                SmallAddButton(onClick = onClick)
            }
        }
    }
}

@Composable
fun SmallAddButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PremiumGradient)
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Add",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFFF0F0F0))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onDecrease,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceWhite)
                .shadow(2.dp, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = "Decrease",
                tint = OrangeAccent,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        IconButton(
            onClick = onIncrease,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(OrangeAccent)
                .shadow(4.dp, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Increase",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun CartBadge(
    itemCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.6f))
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.ShoppingCart,
                contentDescription = "Cart",
                tint = TextPrimary,
                modifier = Modifier.size(26.dp)
            )
        }
        if (itemCount > 0) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .clip(CircleShape)
                    .background(PremiumGradient)
                    .border(2.dp, SurfaceWhite, CircleShape)
                    .shadow(4.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (itemCount > 9) "9+" else itemCount.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "btn_scale")

    Box(
        modifier = modifier
            .height(60.dp)
            .graphicsLayer { 
                scaleX = buttonScale
                scaleY = buttonScale
            }
            .shadow(if (enabled) 12.dp else 0.dp, RoundedCornerShape(20.dp), spotColor = OrangeAccent.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (enabled) PremiumGradient
                else Brush.horizontalGradient(listOf(Color(0xFFD1D1D1), Color(0xFFBCBCBC)))
            )
            .clickable(enabled = enabled, interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Inner Glow Effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
        )
        
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = TextPrimary,
        letterSpacing = (-0.5).sp,
        modifier = modifier
    )
}

@Composable
fun Image(
    imageRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    androidx.compose.foundation.Image(
        painter = painterResource(id = imageRes),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

@Composable
fun RatingChip(rating: Float, reviewCount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White.copy(alpha = 0.8f))
            .border(1.dp, Color.White, RoundedCornerShape(50.dp))
            .shadow(2.dp, RoundedCornerShape(50.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = null,
            tint = YellowAccent,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$rating",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Text(
            text = " ($reviewCount)",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
