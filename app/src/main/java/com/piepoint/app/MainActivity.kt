package com.piepoint.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
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

// ─── Sleek Nav Bar with Bezier Notch ─────────────────────────────────────────

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
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_index"
    )

    val density = LocalDensity.current
    val pillHeight = 76.dp
    val hMargin = 20.dp
    val bottomMargin = 24.dp
    
    val notchWidthPx = with(density) { 90.dp.toPx() }
    val notchHeightPx = with(density) { 32.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = hMargin)
            .padding(bottom = bottomMargin)
    ) {
        // ── Canvas: Curved Background Pill ──
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(pillHeight)
        ) {
            val width = size.width
            val height = size.height
            val itemWidth = width / allNavItems.size
            val notchCx = itemWidth * (animatedIndex + 0.5f)

            val path = buildSmoothNotchPath(
                width = width,
                height = height,
                notchCx = notchCx,
                notchWidth = notchWidthPx,
                notchHeight = notchHeightPx,
                cornerRadius = with(density) { 32.dp.toPx() }
            )

            // Shadow
            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas
                val paint = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        35f, 0f, 12f, 
                        android.graphics.Color.argb(45, 0, 0, 0)
                    )
                }
                nativeCanvas.drawPath(path.asAndroidPath(), paint)
            }

            // Pill Body
            drawPath(path, color = Color.White)
        }

        // ── Floating Active Circle ──
        val config = LocalConfiguration.current
        val screenWidthDp = config.screenWidthDp.dp
        val availableWidthDp = screenWidthDp - (hMargin * 2)
        val itemWidthDp = availableWidthDp / allNavItems.size
        val circleSize = 54.dp
        
        Box(
            modifier = Modifier
                .size(circleSize)
                .offset(
                    x = (itemWidthDp * animatedIndex) + (itemWidthDp / 2) - (circleSize / 2),
                    y = (-32).dp
                )
                .shadow(18.dp, CircleShape, spotColor = OrangeAccent.copy(alpha = 0.5f))
                .clip(CircleShape)
                .background(OrangeAccent),
            contentAlignment = Alignment.Center
        ) {
            val currentItem = allNavItems[selectedIndex]
            Icon(
                imageVector = navIcons[currentItem.route] ?: Icons.Rounded.Circle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        // ── Navigation Items ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(pillHeight),
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = navIcons[item.route] ?: Icons.Rounded.Circle,
                                contentDescription = null,
                                tint = TextHint.copy(alpha = 0.8f),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextHint.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        }
                    } else {
                        // Label for the active item sits below the notch
                        Text(
                            text = item.label,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangeAccent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Builds a path for a pill-shaped bar with a smooth Bezier notch at the top.
 */
private fun buildSmoothNotchPath(
    width: Float,
    height: Float,
    notchCx: Float,
    notchWidth: Float,
    notchHeight: Float,
    cornerRadius: Float
): Path {
    val pillPath = Path().apply {
        addRoundRect(
            RoundRect(
                left = 0f,
                top = 0f,
                right = width,
                bottom = height,
                cornerRadius = CornerRadius(cornerRadius)
            )
        )
    }

    val notchPath = Path().apply {
        val notchHalfWidth = notchWidth / 2f
        val start = notchCx - notchHalfWidth
        val end = notchCx + notchHalfWidth
        
        // Fluid Bell Curve shoulders
        val shoulderOffset = notchWidth * 0.35f 

        moveTo(start - 20f, -1f) // Overlap for clean subtraction
        lineTo(start, 0f)
        
        // Entry Shoulder
        cubicTo(
            x1 = start + shoulderOffset, y1 = 0f,
            x2 = notchCx - (notchWidth * 0.15f), y2 = notchHeight,
            x3 = notchCx, y3 = notchHeight
        )
        
        // Exit Shoulder
        cubicTo(
            x1 = notchCx + (notchWidth * 0.15f), y1 = notchHeight,
            x2 = end - shoulderOffset, y2 = 0f,
            x3 = end, y3 = 0f
        )
        
        lineTo(end + 20f, -1f)
        close()
    }

    return Path.combine(PathOperation.Difference, pillPath, notchPath)
}
