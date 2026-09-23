package com.example

import androidx.compose.ui.graphics.Color

enum class ItemType(
  val displayName: String,
  val basePoints: Int,
  val primaryColor: Color,
  val secondaryColor: Color,
  val isHazard: Boolean = false,
  val isHeart: Boolean = false,
) {
  APPLE("Apple", 10, Color(0xFFE53935), Color(0xFFFF8A80)),
  STAR("Star", 25, Color(0xFFFFB300), Color(0xFFFFF176)),
  GEM("Gem", 50, Color(0xFF00E5FF), Color(0xFF80D8FF)),
  HEART("Heart", 15, Color(0xFFFF4081), Color(0xFFFF80AB), isHeart = true),
  BOMB("Bomb", -30, Color(0xFF263238), Color(0xFFFF1744), isHazard = true),
}

data class FallingItem(
  val id: Long,
  val type: ItemType,
  val xPercent: Float,          // 0.08f to 0.92f
  val yPercent: Float,          // Current vertical position (0.0 to 1.0)
  val prevYPercent: Float,      // Previous vertical position for continuous collision detection (CCD)
  val speed: Float,             // Vertical velocity (screen fractions / second)
  val rotation: Float = 0f,
  val rotationSpeed: Float = 0f,
  val sizeDp: Float = 46f,
)

data class Particle(
  val id: Long,
  val xPercent: Float,
  val yPercent: Float,
  val vx: Float,
  val vy: Float,
  val color: Color,
  val alpha: Float = 1f,
  val sizeDp: Float = 6f,
)

data class FloatingText(
  val id: Long,
  val text: String,
  val xPercent: Float,
  val yPercent: Float,
  val color: Color,
  val alpha: Float = 1f,
)

enum class GameState {
  START,
  PLAYING,
  PAUSED,
  GAME_OVER,
}

data class GameStats(
  val score: Int = 0,
  val highScore: Int = 0,
  val lives: Int = 3,
  val maxLives: Int = 3,
  val comboStreak: Int = 0,
  val maxCombo: Int = 0,
  val level: Int = 1,
  val itemsCaught: Int = 0,
  val bombsAvoided: Int = 0,
  val isNewHighScore: Boolean = false,
)
