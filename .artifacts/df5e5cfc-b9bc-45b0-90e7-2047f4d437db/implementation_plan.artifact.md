# Implementation Plan - Pizza Builder & Layout Refinement

This plan addresses several issues in the Pizza Builder screen and the main app layout to improve aesthetics, visibility, and 3D depth.

## Proposed Changes

### [Component: Pizza Builder Screen]
#### [MODIFY] [PizzaBuilderScreen.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/ui/screens/PizzaBuilderScreen.kt)
- **Interactive Pizza Preview**:
    - Update `PizzaPreview` to visually respond to state changes.
    - Change the crust color and border based on the selected `Crust` type.
    - Dynamically update the sauce layer color using `uiState.selectedSauce?.color`.
    - Enhance the cheese layer's visibility when selected.
- **Refined Horizontal Selector**:
    - Increase `contentPadding` to `115.dp` in `InfiniteHorizontalSelector` to shrink the focused item and pull the side items further into view for a better "peek" UX.
    - Reduce the overall height of the selector and cards for a more compact, modern look.
    - Add a horizontal fade effect at the edges of the carousel to guide the user's eye.
    - Improve the 3D tilt and scale animations for smoother focus transitions.
- **Layout Fixes**:
    - Increase the bottom spacer height to `120.dp` to ensure the "Next" button and customization options are not obscured by the custom navigation bar.

### [Component: App Root / Navbar]
#### [MODIFY] [MainActivity.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/MainActivity.kt)
- **Navbar Depth Fix**:
    - Ensure the `SleekNotchedNavBar` is at the top of the `Box` and its shadow is correctly rendered against the background, restoring the 3D "floating" feel.
    - Remove any hardcoded background colors that might be causing a "box" look around the navbar.

## Verification Plan

### Automated Tests
- Run `gradle_build(":app:assembleDebug")` to verify build integrity.

### Manual Verification
- **Pizza Preview**: Select different crusts and sauces; verify the pizza image updates in real-time.
- **Carousel Peek**: Verify that the previous and next options are clearly visible in the horizontal slider.
- **Button Visibility**: Scroll to the bottom of the builder and ensure the "Next" button is fully visible above the navigation bar.
- **Navbar Aesthetics**: Confirm the navbar looks 3D and its notch/shadows are clear.
