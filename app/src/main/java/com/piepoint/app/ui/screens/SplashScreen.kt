package com.piepoint.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piepoint.app.R
import com.piepoint.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val progress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(2500, easing = LinearEasing),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2600) // Slightly more than 2.5s for a smooth transition
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // ── App Icon & Brand ──
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        val scale = if (startAnimation) 1f else 0.8f
                        scaleX = scale
                        scaleY = scale
                        alpha = if (startAnimation) 1f else 0f
                    }
                    .animateContentSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = OrangeAccent.copy(alpha = 0.1f)
                ) {}
                
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp).scale(1.5f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PiePoint",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = OrangeAccent,
                letterSpacing = (-1).sp,
                modifier = Modifier.graphicsLayer {
                    alpha = if (startAnimation) 1f else 0f
                }
            )

            Spacer(modifier = Modifier.height(64.dp))

            // ── Delivery Van Animation ──
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val density = LocalDensity.current
                val maxWidthPx = with(density) { this@BoxWithConstraints.maxWidth.toPx() }
                val vanWidthPx = with(density) { 100.dp.toPx() }
                
                // Van position: starts off-screen left (-vanWidth), ends off-screen right (maxWidth)
                val vanX = (progress * (maxWidthPx + vanWidthPx)) - vanWidthPx
                
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = vanX.roundToInt(),
                                y = 0
                            )
                        }
                        .size(100.dp, 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Reused Stylized Van Logic
                    Box(
                        modifier = Modifier
                            .size(80.dp, 50.dp)
                            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 30.dp, bottomStart = 5.dp, bottomEnd = 5.dp))
                            .background(OrangeAccent)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(25.dp, 20.dp)
                                .align(Alignment.TopEnd)
                                .padding(top = 5.dp, end = 5.dp)
                                .clip(RoundedCornerShape(topEnd = 15.dp, bottomEnd = 2.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                        )
                    }
                    // Wheels
                    Row(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 5.dp).fillMaxWidth(0.6f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SplashWheel(isSpinning = true)
                        SplashWheel(isSpinning = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── 1000% Progress Bar ──
            Box(
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(OrangeAccent.copy(alpha = 0.7f), OrangeAccent)
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "${(progress * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun SplashWheel(isSpinning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_wheel")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    Box(
        modifier = Modifier
            .size(16.dp)
            .graphicsLayer { rotationZ = rotation }
            .clip(CircleShape)
            .background(Color(0xFF333333))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(6.dp, 1.dp).background(Color.Gray))
        Box(modifier = Modifier.size(1.dp, 6.dp).background(Color.Gray))
    }
}
