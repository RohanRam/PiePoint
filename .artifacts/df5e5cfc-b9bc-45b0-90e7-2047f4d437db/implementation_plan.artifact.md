# Implementation Plan - Sleek Notched Navigation Bar

This plan covers the redesign of the bottom navigation bar to match the "sleek, 3D notched" design provided in the reference image.

## Proposed Changes

### [Component: Main Activity / Navigation]

#### 1. Custom Smooth-Notched Path
- **[MODIFY] [MainActivity.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/MainActivity.kt)**:
    - Replace the current circular arc notch with a **Cubic Bezier curve** notch. This will create the smooth "bell-shape" transition seen in the design.
    - The notch will dynamically follow the selected item index.

#### 2. Center FAB Styling
- **[MODIFY] [MainActivity.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/MainActivity.kt)**:
    - Redesign the active item indicator to be a **solid dark circle** (floating inside the notch) with a white icon.
    - Apply a high elevation shadow to the center circle to make it "pop" (3D feel).

#### 3. Glassmorphic Pill Background
- **[MODIFY] [MainActivity.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/MainActivity.kt)**:
    - Refine the pill background with a cleaner shadow and a subtle white border to match the premium look of the reference image.

#### 4. Item Layout & Typography
- **[MODIFY] [MainActivity.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/MainActivity.kt)**:
    - Adjust icon sizes and label typography (font weight, spacing) to align with the minimalist aesthetic.
    - Ensure 5 items (Discover, Offers, Menu, Orders, Profile) are spaced evenly, with the Menu always inhabiting the "center" notch role when selected.

## Verification Plan

### Automated Tests
- Run `gradle_build(":app:assembleDebug")` to ensure layout changes don't break the build.

### Manual Verification
- **Visual Accuracy**: Compare the running app's navbar to the provided design image.
- **Notch Animation**: Verify that the notch slides smoothly when switching between tabs.
- **Center Button**: Ensure the black circle indicator correctly holds the icon of the selected tab and sits perfectly in the dip.
