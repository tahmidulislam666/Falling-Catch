package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.min

class GameViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs = application.getSharedPreferences("falling_catch_prefs", Context.MODE_PRIVATE)
  private val audioHelper = AudioAndHapticHelper(application)

  private val _gameState = MutableStateFlow(GameState.START)
  val gameState: StateFlow<GameState> = _gameState.asStateFlow()

  private val _stats = MutableStateFlow(
    GameStats(highScore = prefs.getInt("high_score", 0))
  )
  val stats: StateFlow<GameStats> = _stats.asStateFlow()

  private val _basketXPercent = MutableStateFlow(0.5f)
  val basketXPercent: StateFlow<Float> = _basketXPercent.asStateFlow()

  private val _items = MutableStateFlow<List<FallingItem>>(emptyList())
  val items: StateFlow<List<FallingItem>> = _items.asStateFlow()

  private val _particles = MutableStateFlow<List<Particle>>(emptyList())
  val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

  private val _floatingTexts = MutableStateFlow<List<FloatingText>>(emptyList())
  val floatingTexts: StateFlow<List<FloatingText>> = _floatingTexts.asStateFlow()

  private val _screenShake = MutableStateFlow(false)
  val screenShake: StateFlow<Boolean> = _screenShake.asStateFlow()

  private var shakeCountdown = 0f

  val engine = GameEngine(
    onPlayCatchSound = { streak -> audioHelper.playCatchSound(streak) },
    onPlayBombSound = { audioHelper.playBombSound() },
    onPlayHeartSound = { audioHelper.playHeartSound() },
    onPlayGameOverSound = { audioHelper.playGameOverSound() },
    onTriggerShake = {
      shakeCountdown = 0.25f
      _screenShake.value = true
    }
  )

  val basketY get() = engine.basketY

  fun startGame() {
    val currentHigh = prefs.getInt("high_score", 0)
    _stats.value = GameStats(highScore = currentHigh, lives = 3)
    engine.reset(initialLevel = 1)
    _items.value = emptyList()
    _particles.value = emptyList()
    _floatingTexts.value = emptyList()
    _basketXPercent.value = 0.5f
    _gameState.value = GameState.PLAYING
  }

  fun pauseGame() {
    if (_gameState.value == GameState.PLAYING) {
      _gameState.value = GameState.PAUSED
    }
  }

  fun resumeGame() {
    if (_gameState.value == GameState.PAUSED) {
      _gameState.value = GameState.PLAYING
    }
  }

  fun returnToStart() {
    _gameState.value = GameState.START
    engine.reset(initialLevel = 1)
    _items.value = emptyList()
    _particles.value = emptyList()
    _floatingTexts.value = emptyList()
  }

  fun moveBasketTo(xPercent: Float) {
    _basketXPercent.value = xPercent.coerceIn(0.12f, 0.88f)
  }

  fun stepBasketLeft(delta: Float = 0.08f) {
    _basketXPercent.value = (_basketXPercent.value - delta).coerceIn(0.12f, 0.88f)
  }

  fun stepBasketRight(delta: Float = 0.08f) {
    _basketXPercent.value = (_basketXPercent.value + delta).coerceIn(0.12f, 0.88f)
  }

  /**
   * High-performance tick executed per frame in the coroutine loop.
   */
  fun updateGame(rawDeltaSec: Float) {
    if (_gameState.value != GameState.PLAYING) return

    val dt = min(rawDeltaSec, 0.08f)

    if (shakeCountdown > 0f) {
      shakeCountdown -= dt
      if (shakeCountdown <= 0f) {
        _screenShake.value = false
      }
    }

    engine.update(
      rawDeltaSec = dt,
      basketX = _basketXPercent.value,
      currentStats = _stats.value,
      onStatsUpdated = { newStats ->
        if (newStats.isNewHighScore && newStats.score > 0) {
          prefs.edit().putInt("high_score", newStats.score).apply()
        }
        _stats.value = newStats
      },
      onGameOver = {
        _gameState.value = GameState.GAME_OVER
      }
    )

    // Expose snapshot lists for Compose Canvas rendering
    _items.value = engine.items.toList()
    _particles.value = engine.particles.toList()
    _floatingTexts.value = engine.floatingTexts.toList()
  }

  override fun onCleared() {
    super.onCleared()
    audioHelper.release()
  }
}
