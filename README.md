# 🍕 PiePoint — Artisan Pizza Ordering Experience

<div align="center">

<img src="PiePointLogo.png" alt="PiePoint Logo" width="160" />

### *Where Culinary Passion Meets Cinematic Native Android Craftsmanship*

[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Groq AI](https://img.shields.io/badge/AI%20Inference-Groq%20LLM-F55036?style=for-the-badge)](https://groq.com/)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20UDF-3DDC84?style=for-the-badge&logo=android)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

</div>

---

## 📽️ App Promo Video Showcase

Watch the high-velocity animations, interactive layer-by-layer 3D pizza builder, and AI culinary assistant in action!

<div align="center">

### 🎬 PiePoint in Motion

<!-- Video Thumbnail Space -->
<a href="./Promo-PiePoint.MOV">
  <img src="PiePointLogo.png" alt="PiePoint Promo Video Preview" width="560" style="border-radius: 20px; box-shadow: 0 12px 36px rgba(255, 107, 53, 0.25); border: 2px solid #FFE0D3;" />
</a>

<br/>
<p><em>Click the thumbnail above or select an option below:</em></p>

<!-- Video Action Buttons -->
<a href="./Promo-PiePoint.MOV" download>
  <img src="https://img.shields.io/badge/%E2%AC%87%EF%B8%8F_Download-Promo%20Video%20(.MOV)-FF6B35?style=for-the-badge&logo=apple&logoColor=white" alt="Download Promo Video" />
</a>
&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://youtu.be/YOUR_VIDEO_LINK_HERE" target="_blank" rel="noopener noreferrer">
  <img src="https://img.shields.io/badge/%E2%96%B6%EF%B8%8F_Watch-Video%20(YouTube%20%2F%20Web)-FF0000?style=for-the-badge&logo=youtube&logoColor=white" alt="Watch Promo Video" />
</a>

<br/><br/>

| Action | Direct Link | Destination |
| :--- | :--- | :--- |
| 📥 **Download** | [Download Promo-PiePoint.MOV](./Promo-PiePoint.MOV) | Direct repository video file |
| 🌐 **Watch** | [Watch Online / YouTube](https://youtu.be/YOUR_VIDEO_LINK_HERE) | High-definition web stream |

</div>

---

## 📖 About PiePoint

**PiePoint** transforms the traditional, utilitarian food-ordering workflow into a sensory, tactile, and cinematic journey. Crafted entirely in **Jetpack Compose** with **Material 3**, PiePoint reimagines how food lovers interact with artisan pizza: from composing gourmet crusts and toppings in a fluid layer-by-layer builder, to consulting a smart AI sommelier that crafts customized recipes on the fly.

Every single screen, gesture, and transition has been engineered with purpose:
- **Zero Static Friction**: No dull lists of checkboxes or static images; ingredients drop, bounce, and animate in real time.
- **Physicality & Tactility**: UI components compress, elevate, and react with spring physics that mirror real-world materials.
- **State-Driven Storytelling**: From the moment your pizza is assembled to the second it is packed into an animated delivery van, PiePoint tells an engaging visual story.

---

## 🎨 Aesthetics & UI / UX Philosophy

PiePoint is designed around the idea that modern mobile interfaces should feel alive, appetizing, and joyful.

### 🌟 Visual Identity & Color Harmony
The color palette reflects the warmth of wood-fired brick ovens, garden-fresh herbs, and artisanal dough:
- **Vibrant Electric Orange (`#FF6B35` / `#FF8C5E`)**: Primary brand accent evoking tomato zest, warmth, and appetite.
- **Golden Saffron (`#FFC107`)**: Highlights ratings, crust crisps, and gourmet badges.
- **Magical AI Violet (`#7C3AED` / `#EDE9FE`)**: Symbolizes the intelligent conversational culinary engine (*Ask PiePoint AI*).
- **Deep Artisan Slate (`#1A1A2E`)**: High-contrast, bold typography ensuring effortless readability.
- **Pristine Soft Canvas (`#FAFAFA` & `#FFFFFF`)**: Clean, breathable background surfaces that allow culinary visuals to shine.

### ⚡ Spring Physics & Micro-Interactions
PiePoint avoids rigid linear animations in favor of natural physics:
- **Tactile Touch Feedback**: Interactive cards and buttons compress dynamically to `0.96x` with an elevation drop from `8dp` to `2dp` on touch, providing physical, button-like feedback.
- **Spring-Loaded Bounces**: Dialogs, bottom sheets, and badges settle using `Spring.DampingRatioMediumBouncy` for an organic feel.
- **Reactive Keyboard Awareness**: The chat interface utilizes reactive window insets (`WindowInsets.isImeVisible`) with animated spring padding transitions so input bars glide over the keyboard without jarring layout cuts.

### 💎 Glassmorphism & Modern Geometry
- **Soft Curved Architecture**: Cards feature friendly `24dp` rounded corners, action buttons sport `20dp` contours, and filter pills employ a comfortable `50dp` stadium shape.
- **Translucent Overlays**: Subtle glass-like blurs and alpha tints (`0.3` to `0.6`) create depth and visual separation between background layers and active interactive components.
- **8dp Spatial Grid**: Strict geometric alignment creates rhythm and visual balance across varying screen sizes.

### 🖌️ Bespoke Canvas Navigation
Rather than relying on default bottom navigation bars, PiePoint features a **custom canvas-rendered notched navigation bar**:
- **Cubic Bézier Curvature**: The navigation bar's upper boundary curves downward smoothly around the active item using calculated Bézier shoulder curves.
- **Gliding Floating Action Indicator**: A floating circular selector glides between tabs using high-damping spring physics, settling perfectly into the custom notch.

---

## 🤖 Ask PiePoint AI: Autonomous Conversational Culinary Intelligence

> *"A personal pizza chef right in your pocket."*

The centerpiece of PiePoint's intelligent features is **Ask PiePoint AI**, an autonomous culinary assistant and pizza builder driven by cutting-edge Large Language Models (LLMs).

```
   ┌──────────────────────┐         ┌─────────────────────────┐         ┌───────────────────────────┐
   │   Ask PiePoint AI    │ ──────> │  PiePoint AI Express    │ ──────> │     Groq Cloud LLM        │
   │  (Android Compose)   │ <────── │  Proxy (Rate-Limited)   │ <────── │ (Qwen 3.8B / Llama 3.3)   │
   └──────────────────────┘         └─────────────────────────┘         └───────────────────────────┘
              │                                  │
              │                                  ▼
              │                     Authoritative Server Pricing
              │                         (Strict Menu Grounding)
              ▼
   ┌───────────────────────────────────────────────────────────┐
   │               Interactive In-Chat Pizza Card              │
   ├─────────────────────────────┬─────────────────────────────┤
   │    [🛒 1-Tap Add to Cart]   │  [🛠️ Customize in Builder]  │
   └─────────────────────────────┴─────────────────────────────┘
                                                 │
                                                 ▼
                                     AiPizzaPrefill Singleton
                                                 │
                                                 ▼
                                3D Layer-by-Layer Pizza Builder
```

### 🧠 Key AI Capabilities

#### 1. Natural Language Pizza Generation
Users can describe their exact cravings, moods, budgets, or dietary restrictions in natural conversation:
- *"Spicy vegetarian pizza with mushrooms under ₹350"*
- *"Extra cheesy large feast for a game night with 3 friends"*
- *"Low-carb, thin and crispy pizza loaded with veggies and olives"*
- *"Sweet and savory combination with pineapple and jalapeño"*

The AI understands the culinary intent, selects appropriate crusts, sauces, cheeses, and topping combinations, and formats the output into a structured, validated `AiPizza` specification.

#### 2. Conversational Chef & Flavor Sommelier
- Ask about crust differences (*"What's the difference between Whole Wheat and Classic?"*).
- Request flavor pairings (*"What cheese goes best with Garlic Cream sauce?"*).
- Discover best-sellers and chef recommendations based on the current time or mood.

#### 3. Zero-Hallucination Server-Side Price Grounding
Unlike unconstrained chatbots that invent imaginary prices or fake ingredients:
- All generated pizzas are grounded against an authoritative `menu.json` catalog.
- **The AI never calculates prices**: The backend server computes the authoritative price dynamically using the deterministic pricing formula:
  $$\text{Price} = \text{Base Price} + \text{Size Modifier} + \text{Crust} + \text{Sauce} + \text{Cheese} + \sum (\text{Topping Price} \times \text{Quantity})$$
- Ingredient IDs and names are fuzzy-matched to official kitchen inventory, ensuring 100% order fulfillability.

#### 4. Seamless In-Chat Actionability
Every AI recommendation renders as a rich, interactive Compose card directly in the conversation stream:
- **Visual Ingredient Breakdown**: View crust style, sauce, cheese, and emoji-decorated toppings (`🍄 Mushroom`, `🌶️ Jalapeño`, `🫒 Olive`).
- **Instant 1-Tap Add-to-Cart**: Add the AI's recipe directly to your shopping cart without leaving the chat, complete with animated toast feedback.
- **Deep Builder Bridge (`AiPizzaPrefill`)**: Tap **"Customize in Builder"** to launch the interactive 3D Pizza Builder preloaded with every single component recommended by the AI. You can fine-tune toppings, change sizes, or swap sauces with full visual feedback!

#### 5. Graceful Resilience & Dual-Mode Engine
- **High-Speed Cloud Inference**: Accelerated by Groq Cloud inference (Qwen / Llama) for sub-second responses.
- **Offline Fallback Engine (`FakeAiRepository`)**: If the AI backend server is offline or unreachable, the client automatically switches to an internal intelligent fallback repository with local intent parsing, ensuring the app never fails or freezes.
- **Privacy & Security**: Zero client-side API keys; all traffic is routed through a rate-limited Express proxy with a 500-character prompt safety guardrail.

---

## 🛠️ Interactive Pizza Builder & 3D Fly-to-Cart

The manual builder is a masterpiece of declarative animation:
- **Dynamic Layer Stack**: Watch your pizza assemble in real-time as crust textures, sauce spread animations, melted cheese, and scattered toppings drop into place.
- **Portion & Size Controls**: Toggle between Small (8"), Medium (10"), and Large (12") with smooth canvas re-scaling.
- **3D Fly-to-Cart Animation**: When you click *"Add to Cart"*, the assembled pizza lifts into 3D space, rotates along its Z and Y axes, shrinks down, and flies straight into the top-right cart badge.

```kotlin
// 3D Fly-to-Cart Graphics Layer
modifier = Modifier.graphicsLayer {
    scaleX = sizeScale
    scaleY = sizeScale
    translationY = -cartAnimationProgress * 1500f
    translationX = cartAnimationProgress * 800f
    rotationZ = cartRotation
    rotationY = cartRotation / 2f
}
```

---

## 🚚 Cinematic Order Confirmation & Delivery Engine

PiePoint doesn't end with a static "Order Placed" text. It features an automated state-machine delivery animation sequence:
1. **`PACKING`**: The artisan pizza box gracefully closes over your hot pizza.
2. **`LOADING`**: The packed box is transported and loaded into the PiePoint delivery van.
3. **`DEPARTING`**: The delivery van accelerates across the screen with speed streaks.
4. **`SUCCESS`**: The final tracking screen reveals order numbers, live countdown, and driver details.

---

## 💻 Tech Stack & Architecture

- **Language**: 100% Modern Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with Unidirectional Data Flow (UDF)
- **Navigation**: Compose Navigation with Type-Safe Routes
- **Asynchronous & Reactive**: Kotlin Coroutines & `StateFlow`
- **Network & Serialization**: Retrofit 2, OkHttp, Moshi / JSON
- **AI Backend**: Node.js / Express proxy integrating Groq Cloud API
- **Graphics & Motion**: Compose `graphicsLayer`, `Animatable`, `InfiniteTransition`, custom `Canvas` drawing

---

## 📂 Project Architecture

```text
com.piepoint.app
├── data
│   ├── model            # Domain models (Pizza, CartItem, Order, AiPizza)
│   ├── remote           # Retrofit services & Groq AI client
│   └── repository       # AiRepository, MockDataProvider, & Cart state
├── ui
│   ├── components       # Notched nav bar, Cart badges, Custom cards
│   ├── navigation       # NavGraph, NavRoutes, & screen transitions
│   ├── screens
│   │   ├── AiScreen.kt               # Conversational AI assistant
│   │   ├── PizzaBuilderScreen.kt     # 3D interactive pizza builder
│   │   ├── HomeScreen.kt             # Curated menu & promotions
│   │   ├── PizzaDetailScreen.kt      # Detailed pizza showcase
│   │   ├── CartScreen.kt             # Cart & discount vouchers
│   │   ├── CheckoutScreen.kt         # Address & payment selection
│   │   ├── OrderConfirmationScreen.kt# Multi-stage delivery cinematic
│   │   └── OrderHistoryScreen.kt     # Order tracking & re-ordering
│   └── theme            # Color, Typography, Shapes, & Elevation
└── MainActivity.kt      # Root Activity & Notched Navigation host
```

---

## 🚀 Getting Started

### 1. Prerequisites
- Android Studio Ladybug (2024.2.1+) or newer
- Android SDK 34+ (minSdk 26)
- Node.js 18+ (for running the local AI backend proxy)

### 2. Running the AI Backend (Optional but Recommended)
```bash
cd backend
npm install
cp .env.example .env
# Add your GROQ_API_KEY from https://console.groq.com/
npm run dev
```
*Note: If the backend is not started, the app automatically switches to the built-in offline intelligent fallback engine!*

### 3. Running the Android App
1. Open the `PiePoint` directory in Android Studio.
2. Allow Gradle to sync dependencies.
3. Select an emulator or physical device (API 26+).
4. Click **Run (`Shift + F10`)**.

---

<div align="center">

*Crafted with ❤️ for Pizza Connoisseurs & Android Developers alike.*

</div>
