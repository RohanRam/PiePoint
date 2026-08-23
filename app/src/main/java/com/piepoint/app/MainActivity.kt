package com.piepoint.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.ui.geometry.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.piepoint.app.ui.navigation.BottomNavItem
import com.piepoint.app.ui.navigation.PizzaNavGraph
import com.piepoint.app.ui.navigation.Screen
import com.piepoint.app.ui.theme.*
import com.piepoint.app.ui.viewmodel.CartViewModel
import kotlin.math.*

// ─── Activity ────────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PiePointTheme {
                PiePointApp()
            }
        }
    }
}

// ─── Icon map ─────────────────────────────────────────────────────────────────

private val navIcons: Map<String, ImageVector> = mapOf(
    BottomNavItem.Create.route   to Icons.Rounded.AddCircleOutline,
    BottomNavItem.Offers.route   to Icons.Rounded.LocalOffer,
    BottomNavItem.Menu.route     to Icons.Rounded.LocalPizza,
    BottomNavItem.Orders.route   to Icons.AutoMirrored.Rounded.ReceiptLong,
    BottomNavItem.Profile.route  to Icons.Rounded.Person
)

private val allNavItems = listOf(
    BottomNavItem.Create,
    BottomNavItem.Offers,
    BottomNavItem.Menu,
    BottomNavItem.Orders,
    BottomNavItem.Profile
)

// ─── Root ────────────────────────────────────────────────────────────────────

@Composable
fun PiePointApp() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = setOf(
        Screen.Home.route,
        Screen.Create.route,
        Screen.Offers.route,
        Screen.OrderHistory.route,
        Screen.Profile.route
    )
    val showBottomNav = currentRoute in bottomNavRoutes

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BackgroundWhite) // Set background here
            ) {
                PizzaNavGraph(
                    navController = navController,
                    cartViewModel = cartViewModel
                )
            }
        }

        if (showBottomNav) {
            SleekNotchedNavBar(
                currentRoute = currentRoute,
                modifier = Modifier.align(Alignment.BottomCenter),
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

// ─── Animated Floating Tab Bar ───────────────────────────────────────────────

@Composable
fun SleekNotchedNavBar(
    currentRoute: String?,
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    val selectedIndex = allNavItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tab_index"
    )

    // Dynamic accent color per active tab (derived from PiePoint theme palette)
    val activeAccentColor = when (selectedIndex) {
        0 -> OrangeAccent       // Create
        1 -> Color(0xFFFF6D00)  // Offers (Vibrant Deep Orange)
        2 -> Color(0xFFD84315)  // Menu (Rich Warm Rust)
        3 -> Color(0xFFE65100)  // Orders (Amber Gold)
        else -> OrangeAccent     // Profile
    }

    val animatedAccentColor by animateColorAsState(
        targetValue = activeAccentColor,
        animationSpec = tween(durationMillis = 400),
        label = "accent_color"
    )

    val density = LocalDensity.current
    val barHeight = 64.dp
    val circleRadius = 28.dp
    val circleOffset = (-22).dp  // how far above bar top the circle floats
    val hMargin = 16.dp
    val bottomMargin = 20.dp
    val cornerRadiusDp = 24.dp

    // Pixel values that don't depend on measured width
    val circleRadiusPx      = with(density) { circleRadius.toPx() }
    val cutoutGapPx         = with(density) { 6.dp.toPx() }
    val outerCutoutRadiusPx = circleRadiusPx + cutoutGapPx
    val earWidthPx          = with(density) { 18.dp.toPx() }
    val cornerRadiusPx      = with(density) { cornerRadiusDp.toPx() }
    val circleDiameter      = circleRadius * 2

    // Capture actual measured width to derive tab positions
    var measuredWidthPx by remember { mutableIntStateOf(0) }
    val tabWidthDp = with(density) { (measuredWidthPx / allNavItems.size).toDp() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = hMargin)
            .padding(bottom = bottomMargin)
            .onSizeChanged { measuredWidthPx = it.width },
        contentAlignment = Alignment.BottomStart
    ) {

        // ── Canvas: Bar body with smooth concave notch ──
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
        ) {
            val W = size.width
            val H = size.height
            val tabW = W / allNavItems.size
            val cx = tabW * (animatedIndex + 0.5f)   // animated notch center

            val notchLeft  = cx - outerCutoutRadiusPx
            val notchRight = cx + outerCutoutRadiusPx
            val notchDepth = outerCutoutRadiusPx      // depth of concave dip

            val path = Path().apply {
                // Start: top-left corner
                moveTo(cornerRadiusPx, 0f)

                // Top edge → approach left shoulder
                val leftShoulderStart = (notchLeft - earWidthPx).coerceAtLeast(cornerRadiusPx)
                lineTo(leftShoulderStart, 0f)

                // Left shoulder: convex curve easing into the notch
                cubicTo(
                    x1 = notchLeft - earWidthPx * 0.3f, y1 = 0f,
                    x2 = notchLeft,                     y2 = 0f,
                    x3 = notchLeft,                     y3 = notchDepth * 0.25f
                )

                // Concave arc under the floating circle
                // Use a smooth cubic that bottoms out exactly at notchDepth
                cubicTo(
                    x1 = notchLeft,  y1 = notchDepth,
                    x2 = notchRight, y2 = notchDepth,
                    x3 = notchRight, y3 = notchDepth * 0.25f
                )

                // Right shoulder: ease back to flat
                val rightShoulderEnd = (notchRight + earWidthPx).coerceAtMost(W - cornerRadiusPx)
                cubicTo(
                    x1 = notchRight,                     y1 = 0f,
                    x2 = notchRight + earWidthPx * 0.3f, y2 = 0f,
                    x3 = rightShoulderEnd,               y3 = 0f
                )

                // Top edge → top-right corner
                lineTo(W - cornerRadiusPx, 0f)

                // Top-right rounded corner
                cubicTo(
                    x1 = W, y1 = 0f,
                    x2 = W, y2 = 0f,
                    x3 = W, y3 = cornerRadiusPx
                )

                // Right edge
                lineTo(W, H - cornerRadiusPx)

                // Bottom-right rounded corner
                cubicTo(
                    x1 = W,               y1 = H,
                    x2 = W,               y2 = H,
                    x3 = W - cornerRadiusPx, y3 = H
                )

                // Bottom edge
                lineTo(cornerRadiusPx, H)

                // Bottom-left rounded corner
                cubicTo(
                    x1 = 0f, y1 = H,
                    x2 = 0f, y2 = H,
                    x3 = 0f, y3 = H - cornerRadiusPx
                )

                // Left edge
                lineTo(0f, cornerRadiusPx)

                // Top-left rounded corner
                cubicTo(
                    x1 = 0f,             y1 = 0f,
                    x2 = 0f,             y2 = 0f,
                    x3 = cornerRadiusPx, y3 = 0f
                )
                close()
            }

            // Drop shadow
            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas
                val paint = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(24f, 0f, 8f, android.graphics.Color.argb(40, 0, 0, 0))
                }
                nativeCanvas.drawPath(path.asAndroidPath(), paint)
            }

            // White bar fill
            drawPath(path, color = Color.White)
        }

        // ── Floating Circle — positioned using the same tabWidthDp ──
        Box(
            modifier = Modifier
                .size(circleDiameter)
                .offset(
                    x = (tabWidthDp * animatedIndex) + (tabWidthDp / 2) - circleRadius,
                    y = circleOffset
                )
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = animatedAccentColor.copy(alpha = 0.55f),
                    ambientColor = animatedAccentColor.copy(alpha = 0.25f)
                )
                .clip(CircleShape)
                .background(animatedAccentColor),
            contentAlignment = Alignment.Center
        ) {
            val currentItem = allNavItems[selectedIndex]
            Icon(
                imageVector = navIcons[currentItem.route] ?: Icons.Rounded.Circle,
                contentDescription = currentItem.label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        // ── Nav Item Row — spans exactly the same width as the Canvas ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            allNavItems.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onNavigate(item.route) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isSelected) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = navIcons[item.route] ?: Icons.Rounded.Circle,
                                contentDescription = item.label,
                                tint = TextHint.copy(alpha = 0.65f),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextHint.copy(alpha = 0.65f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 9.5.sp
                            )
                        }
                    } else {
                        // Active tab: label sits at the very bottom of the bar
                        Text(
                            text = item.label,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = animatedAccentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

