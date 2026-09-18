# Walkthrough - Cool Theme-Matched Preloader

I have implemented a cinematic preloader (Splash Screen) that enhances the app's startup experience with high-quality animations and theme-matched visuals.

## Changes Made

### 1. Navigation Setup
- **[NavRoutes.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/ui/navigation/NavRoutes.kt)**: Added the `Splash` route.
- **[NavGraph.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/ui/navigation/NavGraph.kt)**: Integrated `SplashScreen` and set it as the `startDestination`. It automatically clears itself from the backstack after navigation to ensure the user doesn't return to it when pressing back from the home screen.

### 2. Cinematic SplashScreen
- **[SplashScreen.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/ui/screens/SplashScreen.kt)**: Created a new screen featuring:
    - **App Icon & Brand**: The `ic_launcher_foreground` icon and "PiePoint" name fade in with a scale-up animation.
    - **Delivery Van Animation**: A stylized PiePoint van drives across the screen from left to right.
    - **1000% Progress Bar**: A sleek progress bar that fills over exactly 2.5 seconds, matching the "1000%" speed intent with a smooth, high-speed feel.
    - **Live Percentage**: Displays the loading progress in real-time.

### 3. Root Integration
- **[MainActivity.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/MainActivity.kt)**: The bottom navigation bar is automatically hidden while on the splash screen because it's not included in the `bottomNavRoutes` set.

## Verification Results

### Automated Tests
- ✅ **Build Success**: The project compiles successfully with `app:assembleDebug`.

### Manual Verification
- Verified the van animation timing (2.5s).
- Verified the app icon visibility and scale animation.
- Verified the smooth transition to `HomeScreen`.
- Verified the bottom nav remains hidden during the splash.

> [!TIP]
> The van animation is calculated using `BoxWithConstraints` to ensure it starts and ends completely off-screen regardless of the device width.
