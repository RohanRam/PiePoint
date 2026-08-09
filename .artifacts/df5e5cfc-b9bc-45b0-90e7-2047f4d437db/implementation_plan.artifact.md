# Implementation Plan - Realistic Toppings & Detail UI Refinement

This plan covers the integration of newly added topping images and three major UI improvements to the Pizza Detail screen: unified size selector layout, 3D ingredient feel, and infinite scrolling topping selector.

## Proposed Changes

### [Component: Data Layer]
#### [MODIFY] [MockDataProvider.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/data/repository/MockDataProvider.kt)
- Update the `toppings` list to map each `Topping` to its new realistic image resource in `res/drawable`.
- Mapping:
    - Cheese -> `R.drawable.cheese`
    - Pepperoni -> `R.drawable.pepporoni`
    - Mushroom -> `R.drawable.mushroom`
    - Olive -> `R.drawable.olive` (or `black_olive`)
    - Onion -> `R.drawable.onion`
    - Bell Pepper -> `R.drawable.green_pepper`
    - Tomato -> `R.drawable.tomato`
    - Basil -> `R.drawable.basil`
    - Bacon -> `R.drawable.beacon`
    - Jalapeño -> `R.drawable.jalapeaneo`

### [Component: Pizza Detail Screen]
#### [MODIFY] [PizzaDetailScreen.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/ui/screens/PizzaDetailScreen.kt)
- **Realistic Toppings UI**:
    - Replace emoji `Text` with `Image` using the `topping.imageRes`.
    - Apply a soft `shadow` and slight `graphicsLayer` rotation to placed toppings on the pizza to give them a natural, 3D "on-pizza" look.
- **Unified Size Selector**:
    - Update `SizeChip` to use `Modifier.weight(1f)` and a consistent `height` to ensure S, M, and L chips have identical proportions.
- **Infinite Loop Topping Selector**:
    - Implement infinite scrolling for the topping `LazyRow` by using a large virtual item count and modulo indexing.

#### 4. Interactive Drag & Drop Fixes
- **[MODIFY] [PizzaDetailScreen.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/ui/screens/PizzaDetailScreen.kt)**:
    - Fix the drop detection logic to accurately map the drag offset to the pizza image area.
    - **Cool Animations**:
        - Add a "pop" animation (scale up) when a topping is successfully dropped.
        - Add a "magnetic" effect where the topping snaps to the pizza surface.
        - Use `animateOffsetAsState` or similar for smooth placement transitions.

## Verification Plan

### Automated Tests
- Run `gradle_build(":app:assembleDebug")` to ensure all resource IDs are valid and the project compiles.

### Manual Verification
- **Toppings**: Verify that actual images are displayed instead of emojis in the selector and on the pizza.
- **Size Selector**: Check that all three size buttons are perfectly aligned and of the same size.
- **Infinite Scroll**: Scroll through the toppings and ensure the list wraps around seamlessly.
