package com.piepoint.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piepoint.app.R
import com.piepoint.app.ui.components.GradientButton
import com.piepoint.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

enum class DeliveryStage {
    PACKING,    // Box closing over pizza
    LOADING,    // Box entering van
    DEPARTING,  // Van driving away
    SUCCESS     // Final UI revealed
}

@Composable
fun OrderConfirmationScreen(
    orderId: String,
    onBackToHome: () -> Unit,
    onViewOrders: () -> Unit
) {
    var stage by remember { mutableStateOf(DeliveryStage.PACKING) }
    
    // Sequence Controller
    LaunchedEffect(Unit) {
        delay(1200) // Watch packing
        stage = DeliveryStage.LOADING
        delay(1000) // Watch loading
        stage = DeliveryStage.DEPARTING
        delay(1500) // Watch van speed off
        stage = DeliveryStage.SUCCESS
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite),
        contentAlignment = Alignment.Center
    ) {
        if (stage != DeliveryStage.SUCCESS) {
            DeliveryCinematic(stage = stage)
        }

        AnimatedVisibility(
            visible = stage == DeliveryStage.SUCCESS,
            enter = fadeIn(tween(600)) + slideInVertically { it / 3 }
        ) {
            SuccessContent(
                orderId = orderId,
                onBackToHome = onBackToHome,
                onViewOrders = onViewOrders
            )
        }
    }
}

@Composable
fun DeliveryCinematic(stage: DeliveryStage) {
    val infiniteTransition = rememberInfiniteTransition(label = "engine_vibration")
    val vibrate by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vibrate"
    )

    // Van Animation
    val vanOffset by animateFloatAsState(
        targetValue = if (stage == DeliveryStage.DEPARTING) 1500f else 0f,
        animationSpec = tween(1200, easing = FastOutLinearInEasing),
        label = "van_move"
    )

    // Box Animation
    val boxScale by animateFloatAsState(
        targetValue = when (stage) {
            DeliveryStage.PACKING -> 1f
            DeliveryStage.LOADING -> 0.4f
            else -> 0f
        },
        animationSpec = tween(800),
        label = "box_scale"
    )

    val boxOffsetY by animateFloatAsState(
        targetValue = if (stage == DeliveryStage.LOADING) 50f else 0f,
        animationSpec = tween(800),
        label = "box_y"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // ── Delivery Van ──
        Box(
            modifier = Modifier
                .offset { IntOffset(vanOffset.roundToInt(), (vibrate.takeIf { stage == DeliveryStage.DEPARTING } ?: 0f).roundToInt()) }
                .graphicsLayer { alpha = if (stage == DeliveryStage.PACKING) 0f else 1f }
                .size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Stylized Van using Icons
            Box(
                modifier = Modifier
                    .size(180.dp, 120.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 60.dp, bottomStart = 10.dp, bottomEnd = 10.dp))
                    .background(OrangeAccent)
            ) {
                // Window
                Box(
                    modifier = Modifier
                        .size(50.dp, 40.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 10.dp)
                        .clip(RoundedCornerShape(topEnd = 30.dp, bottomEnd = 5.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                )
                // Brand Text on Van
                Text(
                    "PiePoint",
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
            // Wheels
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 45.dp).fillMaxWidth(0.6f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Wheel(isSpinning = stage == DeliveryStage.DEPARTING)
                Wheel(isSpinning = stage == DeliveryStage.DEPARTING)
            }
        }

        // ── Pizza Box & Pizza ──
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = boxScale
                    scaleY = boxScale
                    translationY = boxOffsetY
                    translationX = if (stage == DeliveryStage.LOADING) vanOffset else 0f
                },
            contentAlignment = Alignment.Center
        ) {
            // Pizza Image (Visible during packing)
            Image(
                painter = painterResource(id = R.drawable.pizza_margherita),
                contentDescription = null,
                modifier = Modifier.size(120.dp).clip(CircleShape)
            )
            
            // Pizza Box (Two halves closing)
            val lidRotation by animateFloatAsState(
                targetValue = if (stage == DeliveryStage.PACKING) -90f else 0f,
                animationSpec = tween(1000),
                label = "lid"
            )

            // Bottom of box
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .border(2.dp, Color(0xFFD7CCC8), RoundedCornerShape(8.dp))
                    .background(Color(0xFFEFEBE9))
            )
            
            // Top of box (Lid)
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        rotationX = lidRotation
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    }
                    .border(2.dp, Color(0xFFD7CCC8), RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).scale(1.5f)
                )
            }
        }
    }
}

@Composable
fun Wheel(isSpinning: Boolean) {
    val rotation by animateFloatAsState(
        targetValue = if (isSpinning) 1080f else 0f,
        animationSpec = tween(1500, easing = LinearEasing),
        label = "wheel"
    )
    Box(
        modifier = Modifier
            .size(36.dp)
            .graphicsLayer { rotationZ = rotation }
            .clip(CircleShape)
            .background(Color(0xFF333333))
            .border(4.dp, Color.Gray, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(12.dp, 2.dp).background(Color.Gray))
        Box(modifier = Modifier.size(2.dp, 12.dp).background(Color.Gray))
    }
}

@Composable
fun SuccessContent(
    orderId: String,
    onBackToHome: () -> Unit,
    onViewOrders: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(OrangeAccent, OrangeLight))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(70.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Order Placed! \uD83C\uDF89",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Your pizza is on its way!\nSit back and enjoy the wait.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Order ID badge
        Surface(
            color = CardBackground,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Order ID: $orderId",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        GradientButton(
            text = "Back to Menu",
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onViewOrders,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, OrangeAccent)
        ) {
            Text("Track My Order", color = OrangeAccent, fontWeight = FontWeight.Bold)
        }
    }
}
