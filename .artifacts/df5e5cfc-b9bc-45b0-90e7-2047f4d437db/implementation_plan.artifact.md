# Implementation Plan - Pizza Size Enhancements

This plan outlines the changes to remove the XL pizza size and add a scaling animation to the pizza image based on the selected size.

## Proposed Changes

### 1. Data Model
#### [MODIFY] [Models.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/data/model/Models.kt)
- Remove `EXTRA_LARGE` from the `PizzaSize` enum.

### 2. UI Enhancements
#### [MODIFY] [PizzaDetailScreen.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/ui/screens/PizzaDetailScreen.kt)
- Implement a scale animation using `animateFloatAsState`.
- The scale factor will change dynamically based on the selected `PizzaSize`:
    - **Small**: 0.85x
    - **Medium**: 1.0x
    - **Large**: 1.15x
- Apply this scale to the main pizza image in the Hero section using `Modifier.graphicsLayer`.
- Use a `Spring` animation for a "bouncy" and tactile feel when switching sizes.

## Verification Plan

### Automated Tests
- Run `gradle_build(":app:assembleDebug")` to ensure compilation is successful after removing the enum entry.

### Manual Verification
- Open the Pizza Detail screen.
- Select different sizes (S, M, L) and observe the pizza image growing or shrinking with the animation.
- Verify that the "XL" option is no longer visible in the size selector.
