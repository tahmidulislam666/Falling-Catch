# Falling Catch

An arcade game for Android built with Kotlin and Jetpack Compose where players catch falling collectibles in a basket while dodging hazards to earn high scores.

---

## 🎮 Gameplay Overview

- **Core Goal**: Collect falling items into your basket at the bottom of the screen while avoiding bombs. Survive as long as you can and beat your high score.
- **Controls**:
  - **Full-Screen Drag & Instant Tap**: Touch or drag anywhere across the playfield; the catcher basket immediately snaps to tap positions and tracks dragging with zero input lag via `awaitEachGesture`.
  - **Bottom Steering Control Bar**: Features dedicated Left (`◄`) and Right (`►`) nudge buttons for incremental adjustments, plus an interactive touch track with a draggable thumb indicator matching the catcher's exact horizontal location.
  - **Catch-Zone Visual Feedback**: The catcher basket displays an illuminated vertical catch guide, directional arrows (`◄ ►`), and a tactile grip handle indicator.

---

## 🍎 Falling Objects & Scoring

| Item | Points / Effect | Details |
| :--- | :--- | :--- |
| **Juicy Apple** | **+10 pts** | Common fruit collectible to start and maintain combos. |
| **Golden Star** | **+25 pts** | Radiant star that accelerates your score. |
| **Diamond Gem** | **+50 pts** | Rare high-value reward for precision catches. |
| **Heart** | **+1 Life** (or +50 pts) | Restores a lost heart (max 3 lives) or grants bonus points at full health. |
| **Bomb** | **-1 Life** | Explodes on contact, deducts 1 life, shakes the screen, and resets streak. Let it fall past harmlessly to dodge! |

---

## 🔥 Combo Multiplier System

Catching consecutive positive items without dropping them builds an active combo streak:

- **5 Catches**: **2x Multiplier**
- **10 Catches**: **3x Multiplier**
- **20+ Catches**: **4x Multiplier**

An animated fire badge appears in the top HUD when a multiplier is active, multiplying the points of every item caught.

---

## ⚡ Difficulty Progression

- **Level Scaling**: Every 140 points increases your game level.
- **Speed & Frequency**: Falling velocity and spawn rates gradually increase with each level.
- **Lives & Game Over**: Players begin with 3 lives. Catching bombs depletes hearts. When all 3 lives are lost, the game ends and presents a performance breakdown.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: Modern Jetpack Compose with Material Design 3.
- **Architecture**: MVVM (Model-View-ViewModel) with `StateFlow` and unidirectional data flow.
- **Coroutine Game Loop (`withFrameNanos`)**: VSync-aligned frame loop running inside a Compose Coroutine scope (`LaunchedEffect`), providing jitter-free display synchronization at 60Hz, 90Hz, or 120Hz.
- **Deterministic Fixed-Timestep Physics**: Accumulator-based engine substepping at a fixed 120 Hz (`fixedDeltaSec = 1/120s`), decoupling game physics from variable screen refresh rates and preventing tunneling or delta spikes.
- **Continuous Collision Detection (CCD)**: Swept trajectory intersection tests that evaluate the segment between an object's previous and current coordinates against the basket aperture, ensuring rapid or lag-affected drops never skip collision checks.
- **Direct Canvas Batch Rendering**: Entire gameplay field (falling items, basket, particles, floating texts) renders directly in Skia `DrawScope` on a single Compose `Canvas`, avoiding individual Composable layout/measure passes and garbage-collection churn.
- **Local Persistence**: `SharedPreferences` saves all-time high scores across game sessions.
- **Audio & Haptics**: Built-in Android `ToneGenerator` and `Vibrator` / `VibrationEffect` for tactile feedback on catches, explosions, and game-over states.
- **Launcher Art**: Custom adaptive launcher icon with vector graphics.

---

## 🚀 Building & Running

To build the debug APK:
```bash
gradle :app:assembleDebug
```

To run unit tests:
```bash
gradle :app:testDebugUnitTest
```
