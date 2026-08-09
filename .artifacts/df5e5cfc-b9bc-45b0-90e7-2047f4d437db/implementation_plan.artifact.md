# Implementation Plan - Total Rebrand to PiePoint

This plan covers a full rebrand of the application from "PizzaOrder" to "PiePoint", including package renaming, class/theme renaming, and UI adjustments to focus exclusively on pizzas.

## Proposed Changes

### 1. Brand & Identity
#### [MODIFY] [strings.xml](file:///D:/00/0 Working/PiePoint/app/src/main/res/values/strings.xml)
- Change `app_name` to "PiePoint".

#### [MODIFY] [settings.gradle.kts](file:///D:/00/0 Working/PiePoint/settings.gradle.kts)
- Change `rootProject.name` to "PiePoint".

### 2. Package & Code Refactoring
#### [MOVE & MODIFY] Package Renaming
- Rename package `com.pizzaorder.app` to `com.piepoint.app`.
- This involves moving all files from `app/src/main/java/com/pizzaorder/app/` to `app/src/main/java/com/piepoint/app/`.
- Update all `package` declarations and `import` statements.

#### [MODIFY] [build.gradle.kts](file:///D:/00/0 Working/PiePoint/app/build.gradle.kts)
- Update `namespace` and `applicationId` to `com.piepoint.app`.

#### [MODIFY] [AndroidManifest.xml](file:///D:/00/0 Working/PiePoint/app/src/main/AndroidManifest.xml)
- Update theme references and any other package-related strings.

#### [MODIFY] Class & Theme Renaming
- `PizzaOrderApp` -> `PiePointApp`
- `PizzaOrderTheme` -> `PiePointTheme`
- Update `Theme.PizzaOrder` in XML to `Theme.PiePoint`.

### 3. Data & UI Simplification
#### [MODIFY] [MockDataProvider.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/data/repository/MockDataProvider.kt)
- Remove all categories except "Pizza".

#### [MODIFY] [HomeScreen.kt](file:///D:/00/0 Working/PiePoint/app/src/main/java/com/piepoint/app/ui/screens/HomeScreen.kt)
- Update brand text in `HomeTopBar` to "PiePoint".
- **Hide the Category Selector** as requested.

## Verification Plan

### Automated Tests
- Run `gradle_sync` to ensure IDE and build system are aligned with package changes.
- Run `gradle_build(":app:assembleDebug")` to verify successful compilation.

### Manual Verification
- Deploy to device/emulator.
- Verify app name, package name (via adb if needed), and UI changes.
- Ensure the app launches and functions correctly with the new package structure.
