# Implementation Plan - Auto-sliding Infinite Pizza Carousel

This plan details the steps to implement an automatic sliding mechanism for the featured pizza card on the Home screen, including infinite looping.

## Proposed Changes

### [Component: Home Screen]
#### [MODIFY] [HomeScreen.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/ui/screens/HomeScreen.kt)
- **Auto-Slide Logic**:
    - Add a `LaunchedEffect` in `HomeScreen` that runs a timer.
    - Every 5 seconds, it will increment the `featuredIndex` in the `HomeViewModel`.
    - To prevent conflicts with manual user interaction, the timer will reset if the user manually selects a pizza or swipes the carousel.
- **Infinite Carousel Implementation**:
    - Refactor `FeaturedPizzaSection` to use `HorizontalPager` from `androidx.compose.foundation.pager`.
    - Set `pageCount` to a very large value (e.g., `Int.MAX_VALUE`) to simulate an infinite loop.
    - Map the pager's `currentPage` to the `pizzas` list index using modulo arithmetic (`currentPage % pizzas.size`).
    - Synchronize the `pagerState.currentPage` with the `HomeViewModel.featuredIndex` (and vice versa) to keep indicators and thumbnails in sync.
- **Visual Polish**:
    - Ensure the 3D floating animations continue to work seamlessly within each pager slide.
    - Use `animateScrollToPage` for the auto-sliding transition to give it a smooth horizontal motion.

## Verification Plan

### Automated Tests
- Run `gradle_build(":app:assembleDebug")` to verify successful compilation with the new Pager components.

### Manual Verification
- **Auto-Slide**: Open the app and wait 5 seconds. The featured card should automatically slide to the next pizza.
- **Infinite Loop**: Swipe manually multiple times in one direction. It should never reach a hard end and continue to loop through the pizzas.
- **Sync**: Verify that auto-sliding the card also updates the dot indicators and the selected thumbnail below the carousel.
- **User Interruption**: Verify that if the user manually taps a thumbnail, the carousel jumps to that pizza and the 5-second timer restarts.
