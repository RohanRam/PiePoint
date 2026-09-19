# 🍕 PiePoint

**PiePoint** is a premium, high-performance artisan pizza ordering application built with **Jetpack Compose**. It focuses on a cinematic user experience, featuring interactive customization, smooth transitions, and a sleek, modern design system.

---

## ✨ Key Features

### 🛠️ Interactive Pizza Builder
Experience a layer-by-layer customization flow. Build your perfect pizza with real-time visual feedback:
- **Cinematic Preview**: 3D fly-to-cart animations using `graphicsLayer`.
- **Dynamic Layering**: Animated crust, sauce, cheese, and topping layers.
- **Spring-Loaded UI**: Tactile interactions with bouncy spring animations.

### 🎬 Cinematic UI/UX
- **Custom Preloader**: High-speed (1000%) loader with a delivery van racing across the screen.
- **Delivery Cinematic**: A multi-stage animation sequence (Packing → Loading → Departing) for order confirmation.
- **Glassmorphism**: Modern UI elements with subtle blurs and translucent surfaces.

### 🧭 Sleek Navigation
A custom-engineered **Notched Floating Navigation Bar** that uses:
- **Canvas Drawing**: Hand-drawn concave notches using cubic Bezier curves.
- **Animated Indicator**: A floating action circle that glides between tabs with high-damping spring physics.

---

## 🛠️ Technical Stack

- **Language**: 100% Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF)
- **Navigation**: Type-safe Compose Navigation
- **State Management**: Kotlin Coroutines & `StateFlow`
- **Animations**: `animate*AsState`, `InfiniteTransition`, and low-level `GraphicsLayer` transformations.

---

## 🚀 "The Cool Bits" (Code Highlights)

### 🎨 Custom Canvas Notch
The navigation bar isn't just a simple box; it's a hand-drawn path that creates a smooth concave dip for the active tab icon.

```kotlin
// Snippet from SleekNotchedNavBar
val path = Path().apply {
    moveTo(cornerRadiusPx, 0f)
    lineTo(leftShoulderStart, 0f)
    // Left shoulder curve
    cubicTo(x1, y1, x2, y2, x3, y3)
    // Concave arc under the floating circle
    cubicTo(notchLeft, depth, notchRight, depth, notchRight, transitionY)
    // ...
}
drawPath(path, color = Color.White)
```

### 🏎️ Stage-Based Animations
We use a state-machine approach for complex delivery animations in `OrderConfirmationScreen.kt`.

```kotlin
enum class DeliveryStage {
    PACKING,    // Box closing over pizza
    LOADING,    // Box entering van
    DEPARTING,  // Van driving away
    SUCCESS     // Final UI revealed
}
```

### 🍕 3D Fly-to-Cart
When adding a custom pizza to the cart, the entire preview rotates and flies towards the cart icon in 3D space.

```kotlin
modifier = Modifier.graphicsLayer {
    scaleX = sizeScale
    scaleY = sizeScale
    translationY = -cartAnimationProgress * 1500f
    translationX = cartAnimationProgress * 800f
    rotationZ = cartRotation
    rotationY = cartRotation / 2f
}
```

---

## 🏗️ Project Structure

```text
com.piepoint.app
├── data
│   ├── model        # Domain models (Pizza, CartItem, etc.)
│   └── repository   # Mock data sources & state holders
├── ui
│   ├── components   # Reusable UI widgets (Cards, Buttons, Badges)
│   ├── navigation   # NavGraph & Route definitions
│   ├── screens      # Feature-specific screens (Builder, Home, Checkout)
│   ├── theme        # M3 Color palette, Typography, and Shapes
│   └── viewmodel    # Business logic & UI state management
└── MainActivity.kt  # Root activity & Navigation setup
```

---

## 📦 Getting Started

1.  **Clone the repo**: `git clone https://github.com/your-repo/piepoint.git`
2.  **Open in Android Studio**: Ladybug (2024.2.1) or higher recommended.
3.  **Sync Gradle**: Ensure all dependencies are downloaded.
4.  **Run**: Deploy to an emulator or physical device (API 26+).

---

*Designed with ❤️ for Pizza Lovers and Devs alike.*
