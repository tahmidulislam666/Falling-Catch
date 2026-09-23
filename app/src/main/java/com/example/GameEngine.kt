package com.example

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-performance game engine with fixed-timestep physics substepping (120 Hz)
 * and Continuous Collision Detection (CCD) for falling objects.
 */
class GameEngine(
  private val onPlayCatchSound: (streak: Int) -> Unit,
  private val onPlayBombSound: () -> Unit,
  private val onPlayHeartSound: () -> Unit,
  private val onPlayGameOverSound: () -> Unit,
  private val onTriggerShake: () -> Unit,
) {
  val basketY = 0.85f
  val basketCatchHalfWidth = 0.13f
  val basketCatchHalfHeight = 0.035f

  // Fixed physics timestep for deterministic collision and movement
  private val fixedDeltaSec = 1f / 120f
  private var accumulator = 0f

  private var nextId = 1L
  private var spawnTimer = 0f

  val items = mutableListOf<FallingItem>()
  val particles = mutableListOf<Particle>()
  val floatingTexts = mutableListOf<FloatingText>()

  fun reset(initialLevel: Int) {
    items.clear()
    particles.clear()
    floatingTexts.clear()
    accumulator = 0f
    spawnTimer = 0.4f
  }

  /**
   * High-performance frame tick called from Coroutine game loop (e.g. via withFrameNanos).
   * Uses an accumulator to run sub-stepped physics updates and Continuous Collision Detection (CCD).
   */
  fun update(
    rawDeltaSec: Float,
    basketX: Float,
    currentStats: GameStats,
    onStatsUpdated: (GameStats) -> Unit,
    onGameOver: () -> Unit,
  ) {
    // Clamp delta to avoid "spiral of death" if frame rate stutters
    val clampedDelta = min(rawDeltaSec, 0.08f)
    accumulator += clampedDelta

    var updatedStats = currentStats

    // Sub-step fixed physics ticks
    while (accumulator >= fixedDeltaSec) {
      updatedStats = physicsSubStep(
        dt = fixedDeltaSec,
        basketX = basketX,
        currentStats = updatedStats,
        onGameOver = onGameOver
      )
      accumulator -= fixedDeltaSec
    }

    // Update visuals (particles & floating texts) using frame delta
    updateParticles(clampedDelta)
    updateFloatingTexts(clampedDelta)

    onStatsUpdated(updatedStats)
  }

  private fun physicsSubStep(
    dt: Float,
    basketX: Float,
    currentStats: GameStats,
    onGameOver: () -> Unit,
  ): GameStats {
    var score = currentStats.score
    var lives = currentStats.lives
    var combo = currentStats.comboStreak
    var maxCombo = currentStats.maxCombo
    var itemsCaught = currentStats.itemsCaught
    var bombsAvoided = currentStats.bombsAvoided

    // Spawning logic
    spawnTimer -= dt
    val spawnInterval = max(0.55f, 1.25f - (currentStats.level * 0.06f))
    if (spawnTimer <= 0f) {
      spawnTimer = spawnInterval
      items.add(generateFallingItem(currentStats.level, currentStats.lives, currentStats.maxLives))
    }

    // Process falling items with Continuous Collision Detection
    val itemIter = items.listIterator()
    while (itemIter.hasNext()) {
      val item = itemIter.next()
      val prevY = item.yPercent
      val newY = prevY + (item.speed * dt)
      val newRotation = (item.rotation + item.rotationSpeed * dt) % 360f

      // Continuous Collision Detection (CCD):
      // Check if the item's trajectory intersects the basket's catch aperture [basketY - h, basketY + h]
      val sweptCrossesBasket = (prevY <= basketY && newY >= (basketY - basketCatchHalfHeight)) ||
          (newY in (basketY - basketCatchHalfHeight)..(basketY + basketCatchHalfHeight))

      val isWithinBasketX = abs(item.xPercent - basketX) <= basketCatchHalfWidth

      if (sweptCrossesBasket && isWithinBasketX) {
        // Direct catch detected!
        itemIter.remove()

        when {
          item.type.isHazard -> {
            // Hit a bomb
            lives = max(0, lives - 1)
            combo = 0
            onTriggerShake()
            onPlayBombSound()
            addFloatingText("BOOM! -1 Life", item.xPercent, basketY - 0.05f, Color(0xFFFF1744))
            spawnParticles(item.xPercent, basketY, item.type.secondaryColor, 16)

            if (lives <= 0) {
              onPlayGameOverSound()
              onGameOver()
            }
          }
          item.type.isHeart -> {
            if (lives < currentStats.maxLives) {
              lives += 1
              addFloatingText("+1 Life!", item.xPercent, basketY - 0.05f, Color(0xFFFF4081))
            } else {
              score += 50
              addFloatingText("+50 Bonus!", item.xPercent, basketY - 0.05f, Color(0xFFFF4081))
            }
            itemsCaught++
            onPlayHeartSound()
            spawnParticles(item.xPercent, basketY, item.type.secondaryColor, 12)
          }
          else -> {
            combo++
            if (combo > maxCombo) maxCombo = combo
            val multiplier = when {
              combo >= 20 -> 4
              combo >= 10 -> 3
              combo >= 5 -> 2
              else -> 1
            }
            val pts = item.type.basePoints * multiplier
            score += pts
            itemsCaught++

            onPlayCatchSound(combo)

            val text = if (multiplier > 1) "+$pts (x$multiplier)" else "+$pts"
            addFloatingText(text, item.xPercent, basketY - 0.05f, item.type.primaryColor)
            spawnParticles(item.xPercent, basketY, item.type.primaryColor, 10)
          }
        }
      } else if (newY >= 0.98f) {
        // Offscreen at bottom
        itemIter.remove()
        if (item.type.isHazard) {
          bombsAvoided++
        } else {
          // Missed positive item
          if (combo > 2) {
            addFloatingText("Streak Lost!", item.xPercent, 0.92f, Color(0xFFB0BEC5))
          }
          combo = 0
        }
      } else {
        // Advance object
        itemIter.set(item.copy(prevYPercent = prevY, yPercent = newY, rotation = newRotation))
      }
    }

    val calculatedLevel = 1 + (score / 140)
    val isNewHigh = score > currentStats.highScore
    val effectiveHigh = if (isNewHigh) score else currentStats.highScore

    return currentStats.copy(
      score = score,
      highScore = effectiveHigh,
      lives = lives,
      comboStreak = combo,
      maxCombo = maxCombo,
      level = calculatedLevel,
      itemsCaught = itemsCaught,
      bombsAvoided = bombsAvoided,
      isNewHighScore = isNewHigh
    )
  }

  private fun updateParticles(dt: Float) {
    val iter = particles.listIterator()
    while (iter.hasNext()) {
      val p = iter.next()
      val nx = p.xPercent + p.vx * dt
      val ny = p.yPercent + p.vy * dt
      val nAlpha = p.alpha - dt * 2.2f
      if (nAlpha <= 0f) {
        iter.remove()
      } else {
        iter.set(p.copy(xPercent = nx, yPercent = ny, alpha = nAlpha))
      }
    }
  }

  private fun updateFloatingTexts(dt: Float) {
    val iter = floatingTexts.listIterator()
    while (iter.hasNext()) {
      val t = iter.next()
      val ny = t.yPercent - dt * 0.10f
      val nAlpha = t.alpha - dt * 1.4f
      if (nAlpha <= 0f) {
        iter.remove()
      } else {
        iter.set(t.copy(yPercent = ny, alpha = nAlpha))
      }
    }
  }

  private fun generateFallingItem(level: Int, currentLives: Int, maxLives: Int): FallingItem {
    val id = nextId++
    val x = Random.nextFloat() * 0.78f + 0.11f
    val baseSpeed = 0.30f + min(level * 0.028f, 0.45f)
    val speed = baseSpeed + (Random.nextFloat() * 0.08f - 0.04f)
    val rotSpeed = (Random.nextFloat() * 120f - 60f)

    val roll = Random.nextFloat()
    val bombThreshold = min(0.18f, 0.08f + level * 0.015f)
    val heartThreshold = if (currentLives < maxLives) 0.07f else 0.03f

    val type = when {
      roll < bombThreshold -> ItemType.BOMB
      roll < bombThreshold + heartThreshold -> ItemType.HEART
      roll < bombThreshold + heartThreshold + 0.16f -> ItemType.GEM
      roll < bombThreshold + heartThreshold + 0.40f -> ItemType.STAR
      else -> ItemType.APPLE
    }

    return FallingItem(
      id = id,
      type = type,
      xPercent = x,
      yPercent = -0.05f,
      prevYPercent = -0.05f,
      speed = speed,
      rotation = Random.nextFloat() * 360f,
      rotationSpeed = rotSpeed,
      sizeDp = when (type) {
        ItemType.APPLE -> 46f
        ItemType.STAR -> 48f
        ItemType.GEM -> 44f
        ItemType.HEART -> 46f
        ItemType.BOMB -> 50f
      }
    )
  }

  private fun spawnParticles(x: Float, y: Float, color: Color, count: Int) {
    for (i in 0 until count) {
      val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
      val speed = Random.nextFloat() * 0.35f + 0.12f
      val vx = cos(angle) * speed * 0.6f
      val vy = sin(angle) * speed - 0.15f

      particles.add(
        Particle(
          id = nextId++,
          xPercent = x,
          yPercent = y,
          vx = vx,
          vy = vy,
          color = color,
          alpha = 1f,
          sizeDp = Random.nextFloat() * 6f + 4f
        )
      )
    }
  }

  private fun addFloatingText(text: String, x: Float, y: Float, color: Color) {
    floatingTexts.add(
      FloatingText(
        id = nextId++,
        text = text,
        xPercent = x,
        yPercent = y,
        color = color,
        alpha = 1f
      )
    )
  }
}
