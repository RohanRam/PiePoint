package com.piepoint.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.piepoint.app.ui.theme.PiePointTheme
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
    BottomNavItem.Discover.route to Icons.Rounded.Explore,
    BottomNavItem.Offers.route   to Icons.Rounded.LocalOffer,
    BottomNavItem.Menu.route     to Icons.Rounded.LocalPizza,
    BottomNavItem.Orders.route   to Icons.AutoMirrored.Rounded.ReceiptLong,
    BottomNavItem.Profile.route  to Icons.Rounded.Person
)

// ─── All nav items in display order ──────────────────────────────────────────

private val allNavItems = listOf(
    BottomNavItem.Discover,
    BottomNavItem.Offers,
    BottomNavItem.Menu,
    BottomNavItem.Orders,
    BottomNavItem.Profile
)

// ─── Root composable ─────────────────────────────────────────────────────────

@Composable
fun PiePointApp() {
    val navController   = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = setOf(
        Screen.Home.route,
        Screen.Discover.route,
        Screen.Offers.route,
        Screen.OrderHistory.route,
        Screen.Profile.route
    )
    val showBottomNav = currentRoute in bottomNavRoutes

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier       = Modifier.fillMaxSize(),
            containerColor = BackgroundWhite
        ) { _ ->
            PizzaNavGraph(
                navController = navController,
                cartViewModel = cartViewModel
            )
        }

        if (showBottomNav) {
            NotchedNavBar(
                currentRoute = currentRoute,
                modifier     = Modifier.align(Alignment.BottomCenter),
                onNavigate   = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                }
            )
        }
    }
}

// ─── Notched nav-bar ─────────────────────────────────────────────────────────
//
//  The white pill is drawn on a Canvas with a circular arc cut from the top
//  edge at the selected item position.  The active icon circle sits inside
//  that notch.  The notch x-position is animated as a Float fraction of the
//  bar width, so it smoothly slides left/right when the tab changes.
//
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NotchedNavBar(
    currentRoute: String?,
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    val selectedIndex = allNavItems.indexOfFirst { it.route == currentRoute }
        .coerceAtLeast(0)

    // Animate notch position as a normalised fraction 0..1 across the bar
    // Using Float for simple inter-item interpolation.
    val animatedIndexF by animateFloatAsState(
        targetValue   = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "notch_index"
    )

    val itemCount = allNavItems.size

    // Design tokens (dp)
    val pillHeightDp   = 72.dp
    val circleRadiusDp = 28.dp   // active-circle radius
    val cornerRadiusDp = 36.dp
    val hMarginDp      = 20.dp

    val density = LocalDensity.current

    val bottomInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val bottomMargin = maxOf(bottomInset, 10.dp)

    // Convert design tokens to px once (outside Canvas so available to layout)
    val pillHeightPx   = with(density) { pillHeightDp.toPx() }
    val circleRadiusPx = with(density) { circleRadiusDp.toPx() }
    val cornerRadiusPx = with(density) { cornerRadiusDp.toPx() }

    // How far the active circle protrudes above the pill top
    val protrudePx     = circleRadiusPx * 0.60f
    // Total canvas height = pill + protrude
    val canvasHeightPx = pillHeightPx + protrudePx
    val canvasHeightDp = with(density) { canvasHeightPx.toDp() }

    // Top y of the pill inside the canvas
    val pillTopY = protrudePx
    // Centre-y of the active circle (sits on the pill's top edge)
    val circleCy = protrudePx

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = hMarginDp)
            .padding(bottom = bottomMargin)
    ) {
        // ── Canvas: white pill + notch + active circle ─────────────────
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(canvasHeightDp)
        ) {
            val widthPx    = size.width
            val itemWidthPx = widthPx / itemCount

            // Active circle x-centre travels with animated index
            val circleCx = itemWidthPx * (animatedIndexF + 0.5f)

            // Build notched-pill path
            val pillPath = buildNotchedPillPath(
                widthPx      = widthPx,
                pillTopY     = pillTopY,
                pillBottomY  = canvasHeightPx,
                cornerRadius = cornerRadiusPx,
                notchCx      = circleCx,
                notchCy      = circleCy,
                // Notch arc radius: explicitly sizing the cutout
                notchRadius  = circleRadiusPx + 2f
            )

            // ── Shadow ───────────────────────────────────────────────
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(28f, 0f, 5f,
                            android.graphics.Color.argb(40, 0, 0, 0))
                    }
                }
                canvas.drawPath(pillPath, paint)
            }

            // ── White pill fill ──────────────────────────────────────
            drawPath(pillPath, color = SurfaceWhite)

            // No circle drawn here, just the notch in the pill.
        }

        // ── Tappable item slots ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(pillHeightDp)      // same as pill
                .align(Alignment.BottomCenter),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            allNavItems.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                // Raise active item icon so it sits inside the cutout
                val raiseBy = if (isSelected) 24.dp else 0.dp

                NavItemSlot(
                    item       = item,
                    isSelected = isSelected,
                    raiseByDp  = raiseBy,
                    onClick    = { onNavigate(item.route) },
                    modifier   = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─── Builds white pill path with a circular notch cut from the top edge ───────
//
//  The notch is an arc that curves downward. We find where the notch circle
//  intersects the pill top edge, then substitute that section with the arc.
//
private fun buildNotchedPillPath(
    widthPx: Float,
    pillTopY: Float,
    pillBottomY: Float,
    cornerRadius: Float,
    notchCx: Float,
    notchCy: Float,
    notchRadius: Float
): Path {
    val cr = cornerRadius.coerceAtMost(minOf(widthPx / 2f, (pillBottomY - pillTopY) / 2f))

    // The notch arc circle has its centre at (notchCx, notchCy).
    // Find the two points where this circle intersects y = pillTopY.
    val dy = pillTopY - notchCy  // typically negative (circle centre is above pillTopY)
    val discriminant = (notchRadius * notchRadius - dy * dy).coerceAtLeast(0f)
    val halfWidth = sqrt(discriminant)
    val arcLeft   = (notchCx - halfWidth).coerceIn(0f, widthPx)
    val arcRight  = (notchCx + halfWidth).coerceIn(0f, widthPx)

    // Ensure the top corners don't clip the notch
    val crTopLeft  = cr.coerceAtMost(arcLeft)
    val crTopRight = cr.coerceAtMost(widthPx - arcRight)

    // Angles from arc-circle-centre to intersection points
    val startAngle = atan2(dy, arcLeft  - notchCx)   // angle to left intersection
    val endAngle   = atan2(dy, arcRight - notchCx)   // angle to right intersection
    // Sweep clockwise (arc goes downward, i.e. positive y direction)
    var sweepRad = endAngle - startAngle
    if (sweepRad <= 0f) sweepRad += (2f * PI.toFloat())

    val startDeg = Math.toDegrees(startAngle.toDouble()).toFloat()
    val sweepDeg = Math.toDegrees(sweepRad.toDouble()).toFloat()

    val arcRect = Rect(
        left   = notchCx - notchRadius,
        top    = notchCy - notchRadius,
        right  = notchCx + notchRadius,
        bottom = notchCy + notchRadius
    )

    return Path().apply {
        // ── Top-left corner ──────────────────────────────────────────
        moveTo(0f, pillTopY + crTopLeft)
        arcTo(Rect(0f, pillTopY, crTopLeft * 2f, pillTopY + crTopLeft * 2f),
            startAngleDegrees = 180f, sweepAngleDegrees = 90f, forceMoveTo = false)

        // ── Top edge left segment → notch ──────────────────────────
        lineTo(arcLeft, pillTopY)

        // ── Notch arc (curves down around the active circle) ────────
        arcTo(arcRect, startAngleDegrees = startDeg, sweepAngleDegrees = sweepDeg, forceMoveTo = false)

        // ── Top edge right segment → corner ────────────────────────
        lineTo(widthPx - crTopRight, pillTopY)

        // ── Top-right corner ────────────────────────────────────────
        arcTo(Rect(widthPx - crTopRight * 2f, pillTopY, widthPx, pillTopY + crTopRight * 2f),
            startAngleDegrees = 270f, sweepAngleDegrees = 90f, forceMoveTo = false)

        // ── Right edge ──────────────────────────────────────────────
        lineTo(widthPx, pillBottomY - cr)

        // ── Bottom-right corner ─────────────────────────────────────
        arcTo(Rect(widthPx - cr * 2f, pillBottomY - cr * 2f, widthPx, pillBottomY),
            startAngleDegrees = 0f, sweepAngleDegrees = 90f, forceMoveTo = false)

        // ── Bottom edge ─────────────────────────────────────────────
        lineTo(cr, pillBottomY)

        // ── Bottom-left corner ──────────────────────────────────────
        arcTo(Rect(0f, pillBottomY - cr * 2f, cr * 2f, pillBottomY),
            startAngleDegrees = 90f, sweepAngleDegrees = 90f, forceMoveTo = false)

        // ── Left edge back to start ─────────────────────────────────
        lineTo(0f, pillTopY + crTopLeft)
        close()
    }
}

// ─── Single tappable nav item slot ───────────────────────────────────────────

@Composable
private fun NavItemSlot(
    item: BottomNavItem,
    isSelected: Boolean,
    raiseByDp: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint by animateColorAsState(
        targetValue   = if (isSelected) OrangeAccent else TextHint,
        animationSpec = tween(220),
        label         = "icon_tint_${item.label}"
    )
    val labelColor by animateColorAsState(
        targetValue   = if (isSelected) OrangeAccent else TextHint,
        animationSpec = tween(220),
        label         = "label_color_${item.label}"
    )

    val animatedOffset by animateDpAsState(
        targetValue   = raiseByDp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label         = "item_offset_${item.label}"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .semantics(mergeDescendants = true) {
                contentDescription = "${item.label}, navigation tab"
                role               = Role.Tab
                selected           = isSelected
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            // Shift icon+label upward for the active item so they sit
            // inside the cutout.
            modifier            = Modifier.offset(y = -animatedOffset),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = navIcons[item.route] ?: Icons.Rounded.Circle,
                contentDescription = null,
                tint               = iconTint,
                modifier           = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text          = item.label,
                fontSize      = 10.sp,
                fontWeight    = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color         = labelColor,
                letterSpacing = 0.3.sp
            )
        }
    }
}
