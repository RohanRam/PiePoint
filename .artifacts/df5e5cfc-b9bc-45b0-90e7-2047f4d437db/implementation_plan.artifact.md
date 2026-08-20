# Implementation Plan - Advanced Transition Animations

This plan introduces two high-end cinematic animations: a "Fly-to-Cart" effect in the Pizza Builder and a "Packing & Delivery" sequence upon order completion.

## Proposed Changes

### [Component: Pizza Builder Screen]
#### [MODIFY] [PizzaBuilderScreen.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/ui/screens/PizzaBuilderScreen.kt)
- **Enhanced Add-to-Cart Animation**:
    - Update `PizzaPreview` logic to include a 3D rotation (`rotationY`) and a spiral-like movement towards the cart icon.
    - Calculate the target coordinates for the Cart icon dynamically using `LocalDensity`.
    - Add a "magnetic pull" feel where the pizza shrinks and spins faster as it approaches the cart.
    - Add a haptic-like visual pulse to the `CartBadge` when the pizza "lands".

### [Component: Order Confirmation Screen]
#### [MODIFY] [OrderConfirmationScreen.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/ui/screens/OrderConfirmationScreen.kt)
- **Cinematic Delivery Sequence**:
    - Implement a 3-stage introductory animation before the success message appears.
    - **Stage 1 (Packing)**: A pizza image is shown; two halves of a 3D pizza box (custom drawn or vector) animate to close over it.
    - **Stage 2 (Loading)**: the closed box shrinks and moves into the back of a stylized delivery van graphic.
    - **Stage 3 (Departure)**: The van's wheels spin (simulated with rotation), the van vibrates (engine start), and then it accelerates smoothly off the right side of the screen.
- Use `Animatable` and `updateTransition` to coordinate these multi-step movements.

## Verification Plan

### Automated Tests
- Run `gradle_build(":app:assembleDebug")` to ensure complex animations don't introduce build errors.

### Manual Verification
- **Builder Flow**: Create a pizza and tap "Add to Cart". Verify it spins and targets the cart icon correctly.
- **Checkout Flow**: Complete a checkout. Verify the box closes, enters the van, and the van drives away before the success UI is revealed.
- **Timing**: Ensure animations are snappy enough (approx 1.5s - 2.5s total) so they don't hinder the user experience.
