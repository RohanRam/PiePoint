# Implementation Plan - Cool Theme-Matched Preloader

This plan outlines the addition of a cinematic preloader (Splash Screen) that appears for 2.5 seconds when the app is launched. It features a progress bar, a moving delivery van, and smooth transitions to the main app.

## User Review Required

> [!IMPORTANT]
> - The app will now start with a **2.5-second splash screen**.
> - The "1000% loader bar" will be implemented as a high-speed styled progress bar (0-100%) with a cinematic "boost" effect.
> - A stylized PiePoint delivery van will drive across the screen above the loader.

## Proposed Changes

### Navigation

#### [MODIFY] [NavRoutes.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/ui/navigation/NavRoutes.kt)
- Add `object Splash : Screen("splash")` to the `Screen` sealed class.

#### [MODIFY] [NavGraph.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/ui/navigation/NavGraph.kt)
- Add the `SplashScreen` to the `NavHost`.
- Update `startDestination` to `Screen.Splash.route`.

### UI Screens

#### [NEW] [SplashScreen.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/ui/screens/SplashScreen.kt)
- Implement a `SplashScreen` composable.
- **Animations**:
    - **Logo & App Icon**: Fade in the app icon (`ic_launcher_foreground`) and "PiePoint" name with a slight scale-up.
    - **Progress Bar**: Animate from 0% to 100% over 2.5s.
    - **Delivery Van**: A stylized van (reused from Order Confirmation logic) that drives from left to right, synced with the progress bar.
- **Navigation Logic**: Use `LaunchedEffect` to wait 2.5s and then navigate to `Home`, clearing the splash from the backstack.

### Root Application

#### [MODIFY] [MainActivity.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/MainActivity.kt)
- Ensure the `SleekNotchedNavBar` (Bottom Nav) is hidden while on the splash screen.

## Verification Plan

### Automated Tests
- Build and compile check.

### Manual Verification
1. **Launch**: Verify the app starts with the Splash Screen.
2. **Animation**: Check if the van drives across and the progress bar fills over exactly ~2.5 seconds.
3. **Transition**: Verify the splash screen disappears completely and the `HomeScreen` is revealed.
4. **Navigation**: Verify that pressing "Back" from the Home screen exits the app instead of going back to the splash.
5. **UI**: Verify the bottom navigation bar is hidden during the splash.
