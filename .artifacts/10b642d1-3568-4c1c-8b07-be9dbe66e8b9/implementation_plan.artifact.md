# Implementation Plan - Interactive Custom Pizza Builder

This plan outlines the steps to replace the "Discover" tab with a highly interactive "Create" tab, allowing users to build a custom pizza layer-by-layer with animations and real-time price updates.

## User Review Required

> [!IMPORTANT]
> The "Discover" section will be completely removed and replaced by the "Create" (Pizza Builder) section.
> The `CartItem` model will be updated to include `crust`, `sauce`, and `cheese` to support custom pizza details in the cart.

## Proposed Changes

### Data Layer

#### [MODIFY] [Models.kt](file:///D:/Projects/Android Pizza/app/src/main/java/com/piepoint/app/data/model/Models.kt)
- Add `Crust`, `Sauce`, and `Cheese` data classes/enums.
- Update `CartItem` to include optional `crust`, `sauce`, and `cheese` fields.
- Update `CartItem.totalPrice` calculation to include prices of these new components.

#### [NEW] [PizzaBuilderModels.kt](file:///D:/Projects/Android Pizza/app/src/main/java/com/piepoint/app/data/model/PizzaBuilderModels.kt)
- Define `PizzaBuilderStep` enum (Crust, Sauce, Cheese, Toppings, Size, Review).
- Define `PizzaBuilderUiState` to hold current selections and current step.

### ViewModels

#### [NEW] [PizzaBuilderViewModel.kt](file:///D:/Projects/Android Pizza/app/src/main/java/com/piepoint/app/ui/viewmodel/PizzaBuilderViewModel.kt)
- Manage the multi-step flow.
- Handle selection of crust, sauce, cheese, toppings, and size.
- Calculate live price.
- Handle "Add to Cart" by interacting with `CartViewModel`.

### UI - Components & Screens

#### [NEW] [PizzaBuilderScreen.kt](file:///D:/Projects/Android Pizza/app/src/main/java/com/piepoint/app/ui/screens/PizzaBuilderScreen.kt)
- Implement the main screen for building the pizza.
- Include a layered `PizzaPreview` component with animations (scale, fade, spring).
- Implement step-specific selection areas (Crust options, Sauce options, etc.).
- Implement a persistent bottom area with live price and the "Next/Add to Cart" button.
- Implement the "Add to Cart" lift-and-move animation.

### Navigation

#### [MODIFY] [NavRoutes.kt](file:///D:/Projects/Android Pizza/app/src/main/java/com/piepoint/app/ui/navigation/NavRoutes.kt)
- Rename `Screen.Discover` to `Screen.Create`.
- Rename `BottomNavItem.Discover` to `BottomNavItem.Create`.

#### [MODIFY] [NavGraph.kt](file:///D:/Projects/Android Pizza/app/src/main/java/com/piepoint/app/ui/navigation/NavGraph.kt)
- Replace `DiscoverScreen` route with `PizzaBuilderScreen`.

#### [MODIFY] [MainActivity.kt](file:///D:/Projects/Android Pizza/app/src/main/java/com/piepoint/app/MainActivity.kt)
- Update `navIcons` map and `bottomNavRoutes` to reflect the change from Discover to Create.

## Verification Plan

### Automated Tests
- Build and compile check.
- (Manual verification is primary for UI/Animations)

### Manual Verification
1. **Navigation**: Verify "Create" tab exists and opens the builder. Verify navbar notch still works perfectly.
2. **Crust Step**: Select crusts and verify the pizza base animates in.
3. **Sauce Step**: Select sauce and verify the sauce layer appears.
4. **Cheese Step**: Select cheese and verify the cheese layer appears.
5. **Toppings Step**: Add/Remove toppings and verify they "drop" onto or "fly" off the pizza at specific positions. Verify topping count and price update.
6. **Size Step**: Change size and verify the preview smoothly resizes.
7. **Live Price**: Verify price updates immediately at every selection.
8. **Review Step**: Verify all selections are correctly summarized.
9. **Add to Cart**: Verify the "lift-and-move" animation.
10. **Cart Verification**: Open cart and verify the custom pizza shows all details (Crust, Sauce, Cheese, Toppings) and correct price.
11. **Regression**: Verify standard pizzas from the Menu tab can still be added and managed in the cart.
