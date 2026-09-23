package com.example

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun GameHud(
  stats: GameStats,
  onPauseClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
  val fireScale by infiniteTransition.animateFloat(
    initialValue = 0.9f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "fire_scale"
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("game_hud"),
    shape = RoundedCornerShape(24.dp),
    color = Color(0xCC111827),
    tonalElevation = 6.dp,
    shadowElevation = 8.dp,
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
      // Top row: Score, High Score, Pause
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Current Score
        Column {
          Text(
            text = "SCORE",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF90CAF9),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
          Text(
            text = "${stats.score}",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Black
          )
        }

        // Lives indicators (Hearts)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          for (i in 1..stats.maxLives) {
            val isAlive = i <= stats.lives
            Icon(
              imageVector = if (isAlive) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = if (isAlive) "Active Life" else "Lost Life",
              tint = if (isAlive) Color(0xFFFF1744) else Color(0x44FFFFFF),
              modifier = Modifier
                .size(26.dp)
                .testTag("life_indicator_$i")
            )
          }
        }

        // Pause Button
        IconButton(
          onClick = onPauseClick,
          modifier = Modifier
            .size(44.dp)
            .background(Color(0x22FFFFFF), CircleShape)
            .testTag("pause_button")
        ) {
          Icon(
            imageVector = Icons.Default.Pause,
            contentDescription = "Pause Game",
            tint = Color.White
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Bottom row: High score pill, Combo streak, Level
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // High score badge
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .background(Color(0x22FFD54F), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = "High Score",
            tint = Color(0xFFFFD54F),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "BEST: ${stats.highScore}",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFFFE082),
            fontWeight = FontWeight.Bold
          )
        }

        // Combo streak banner
        if (stats.comboStreak >= 3) {
          val multiplier = when {
            stats.comboStreak >= 20 -> 4
            stats.comboStreak >= 10 -> 3
            stats.comboStreak >= 5 -> 2
            else -> 1
          }
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .background(
                Brush.horizontalGradient(
                  listOf(Color(0xFFFF6D00), Color(0xFFFFAB00))
                ),
                RoundedCornerShape(12.dp)
              )
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Icon(
              imageVector = Icons.Default.LocalFireDepartment,
              contentDescription = "Combo Fire",
              tint = Color.White,
              modifier = Modifier
                .size(18.dp)
                .scale(fireScale)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "COMBO x$multiplier (${stats.comboStreak})",
              style = MaterialTheme.typography.labelMedium,
              color = Color.White,
              fontWeight = FontWeight.Black
            )
          }
        }

        // Level Pill
        Text(
          text = "LVL ${stats.level}",
          style = MaterialTheme.typography.labelMedium,
          color = Color(0xFFB0BEC5),
          fontWeight = FontWeight.Bold,
          modifier = Modifier
            .background(Color(0x2290A4AE), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
        )
      }
    }
  }
}

/**
 * High-performance hardware-accelerated Canvas renderer.
 * All falling items, basket, particles, and floating text are rendered in a single
 * DrawScope pass, eliminating individual Composable node allocations and layout re-measurements.
 */
@Composable
fun GameCanvas(
  items: List<FallingItem>,
  particles: List<Particle>,
  floatingTexts: List<FloatingText>,
  basketXPercent: Float,
  basketYPercent: Float,
  screenShake: Boolean,
  onBasketMove: (Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  val textMeasurer = rememberTextMeasurer()

  val shakeOffset = if (screenShake) {
    val randX = ((-6..6).random()).dp
    val randY = ((-6..6).random()).dp
    IntOffset(randX.value.roundToInt(), randY.value.roundToInt())
  } else {
    IntOffset.Zero
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .offset { shakeOffset }
      .testTag("game_canvas")
      .pointerInput(Unit) {
        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false)
          val normX = (down.position.x / size.width.toFloat()).coerceIn(0.12f, 0.88f)
          onBasketMove(normX)

          drag(down.id) { change ->
            change.consume()
            val dragX = (change.position.x / size.width.toFloat()).coerceIn(0.12f, 0.88f)
            onBasketMove(dragX)
          }
        }
      }
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvasWidth = size.width
      val canvasHeight = size.height

      // 1. Draw atmospheric background and twinkling space stars
      drawArcadeBackground(size)

      // 2. Draw falling items directly in DrawScope (0 layout pass overhead)
      for (item in items) {
        val cx = item.xPercent * canvasWidth
        val cy = item.yPercent * canvasHeight
        val radiusPx = (item.sizeDp * density) * 0.42f

        withTransform({
          rotate(degrees = item.rotation, pivot = Offset(cx, cy))
        }) {
          drawItemShape(item.type, cx, cy, radiusPx)
        }
      }

      // 3. Draw particles directly
      for (p in particles) {
        val px = p.xPercent * canvasWidth
        val py = p.yPercent * canvasHeight
        drawCircle(
          color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
          radius = p.sizeDp * density * 0.5f,
          center = Offset(px, py)
        )
      }

      // 4. Draw basket
      val basketWidthPx = 100.dp.toPx()
      val basketHeightPx = 44.dp.toPx()
      val basketCenterX = canvasWidth * basketXPercent
      val basketCenterY = canvasHeight * basketYPercent

      drawBasketShape(basketCenterX, basketCenterY, basketWidthPx, basketHeightPx)

      // 5. Draw floating texts directly on Canvas
      for (f in floatingTexts) {
        val fx = (canvasWidth * f.xPercent) - 50f
        val fy = canvasHeight * f.yPercent
        drawText(
          textMeasurer = textMeasurer,
          text = f.text,
          topLeft = Offset(fx, fy),
          style = TextStyle(
            color = f.color.copy(alpha = f.alpha.coerceIn(0f, 1f)),
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
          )
        )
      }

      // 6. Shake red flash vignette
      if (screenShake) {
        drawRect(Color(0x33FF0000))
      }
    }
  }
}

private fun DrawScope.drawArcadeBackground(canvasSize: Size) {
  // Vertical arcade gradient
  drawRect(
    brush = Brush.verticalGradient(
      colors = listOf(
        Color(0xFF0D1B2A),
        Color(0xFF1B263B),
        Color(0xFF2E3D52),
        Color(0xFF1B263B)
      )
    )
  )

  // Ambient stars in upper half
  val starSeeds = listOf(
    Offset(0.12f, 0.12f), Offset(0.35f, 0.06f), Offset(0.72f, 0.10f),
    Offset(0.88f, 0.18f), Offset(0.24f, 0.24f), Offset(0.55f, 0.20f),
    Offset(0.82f, 0.30f), Offset(0.18f, 0.38f), Offset(0.65f, 0.40f)
  )
  for (seed in starSeeds) {
    drawCircle(
      color = Color(0x33FFFFFF),
      radius = 3f,
      center = Offset(seed.x * canvasSize.width, seed.y * canvasSize.height)
    )
  }

  // Floor guideline
  drawLine(
    color = Color(0x22FFFFFF),
    start = Offset(0f, canvasSize.height * 0.94f),
    end = Offset(canvasSize.width, canvasSize.height * 0.94f),
    strokeWidth = 2f
  )
}

private fun DrawScope.drawItemShape(type: ItemType, cx: Float, cy: Float, radius: Float) {
  val center = Offset(cx, cy)
  when (type) {
    ItemType.APPLE -> {
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFFFF5252), Color(0xFFD32F2F), Color(0xFF880E4F)),
          center = Offset(cx - radius * 0.2f, cy - radius * 0.2f),
          radius = radius * 1.2f
        ),
        radius = radius,
        center = center
      )
      // Leaf
      val leaf = Path().apply {
        moveTo(cx, cy - radius)
        quadraticBezierTo(cx + radius * 0.6f, cy - radius * 1.3f, cx + radius * 0.8f, cy - radius * 0.8f)
        quadraticBezierTo(cx + radius * 0.3f, cy - radius * 0.7f, cx, cy - radius)
      }
      drawPath(leaf, color = Color(0xFF4CAF50))
      // Stem
      drawLine(
        color = Color(0xFF5D4037),
        start = Offset(cx, cy - radius * 0.8f),
        end = Offset(cx, cy - radius * 1.2f),
        strokeWidth = 3f
      )
    }

    ItemType.STAR -> {
      val starPath = Path()
      val numPoints = 5
      val outer = radius * 1.05f
      val inner = radius * 0.45f
      for (i in 0 until numPoints * 2) {
        val r = if (i % 2 == 0) outer else inner
        val angle = i * Math.PI / numPoints - Math.PI / 2
        val x = cx + (r * kotlin.math.cos(angle)).toFloat()
        val y = cy + (r * kotlin.math.sin(angle)).toFloat()
        if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
      }
      starPath.close()

      drawPath(
        path = starPath,
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color(0xFFFF8F00)),
          center = center,
          radius = outer
        )
      )
      drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = inner * 0.4f,
        center = center
      )
    }

    ItemType.GEM -> {
      val gem = Path().apply {
        moveTo(cx, cy - radius * 1.05f)
        lineTo(cx + radius * 0.95f, cy - radius * 0.2f)
        lineTo(cx, cy + radius * 1.05f)
        lineTo(cx - radius * 0.95f, cy - radius * 0.2f)
        close()
      }
      drawPath(
        path = gem,
        brush = Brush.linearGradient(
          colors = listOf(Color(0xFFE0F7FA), Color(0xFF00E5FF), Color(0xFF0091EA)),
          start = Offset(cx - radius, cy - radius),
          end = Offset(cx + radius, cy + radius)
        )
      )
      drawLine(
        color = Color.White.copy(alpha = 0.7f),
        start = Offset(cx - radius * 0.95f, cy - radius * 0.2f),
        end = Offset(cx + radius * 0.95f, cy - radius * 0.2f),
        strokeWidth = 2f
      )
    }

    ItemType.HEART -> {
      val heart = Path().apply {
        moveTo(cx, cy + radius * 0.9f)
        cubicTo(
          cx - radius * 1.3f, cy - radius * 0.1f,
          cx - radius * 0.8f, cy - radius * 1.1f,
          cx, cy - radius * 0.4f
        )
        cubicTo(
          cx + radius * 0.8f, cy - radius * 1.1f,
          cx + radius * 1.3f, cy - radius * 0.1f,
          cx, cy + radius * 0.9f
        )
        close()
      }
      drawPath(
        path = heart,
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFFFF80AB), Color(0xFFFF1744), Color(0xFFC51162)),
          center = center,
          radius = radius
        )
      )
    }

    ItemType.BOMB -> {
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFF546E7A), Color(0xFF263238), Color(0xFF102027)),
          center = Offset(cx - radius * 0.3f, cy - radius * 0.3f),
          radius = radius * 1.1f
        ),
        radius = radius * 0.85f,
        center = Offset(cx, cy + radius * 0.15f)
      )
      // Fuse
      val fuse = Path().apply {
        moveTo(cx, cy - radius * 0.6f)
        quadraticBezierTo(cx + radius * 0.4f, cy - radius * 1.0f, cx + radius * 0.3f, cy - radius * 1.3f)
      }
      drawPath(fuse, color = Color(0xFFFFB300), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
      drawCircle(
        color = Color(0xFFFF1744),
        radius = radius * 0.22f,
        center = Offset(cx + radius * 0.3f, cy - radius * 1.3f)
      )
      drawCircle(
        color = Color(0xFFFFFF00),
        radius = radius * 0.1f,
        center = Offset(cx + radius * 0.3f, cy - radius * 1.3f)
      )
    }
  }
}

private fun DrawScope.drawBasketShape(cx: Float, cy: Float, width: Float, height: Float) {
  val left = cx - width / 2f
  val top = cy - height / 2f

  // 1. Soft vertical catch-zone alignment glow (visual feedback for draggable catcher)
  drawRect(
    brush = Brush.verticalGradient(
      colors = listOf(Color.Transparent, Color(0x28FFD54F)),
      startY = top - 28.dp.toPx(),
      endY = top
    ),
    topLeft = Offset(left + width * 0.08f, top - 28.dp.toPx()),
    size = Size(width * 0.84f, 28.dp.toPx())
  )

  val basketRect = Path().apply {
    moveTo(left + width * 0.08f, top + height * 0.22f)
    lineTo(left + width * 0.92f, top + height * 0.22f)
    lineTo(left + width * 0.80f, top + height * 0.96f)
    lineTo(left + width * 0.20f, top + height * 0.96f)
    close()
  }

  // Shadow
  drawPath(basketRect, color = Color(0x66000000))

  // Basket Body
  drawPath(
    path = basketRect,
    brush = Brush.verticalGradient(
      colors = listOf(Color(0xFFFFB300), Color(0xFFF57C00), Color(0xFFE65100)),
      startY = top,
      endY = top + height
    )
  )

  // Weave lines
  val weaveColor = Color(0x44FFE082)
  for (i in 1..4) {
    val xOff = left + width * (0.15f + i * 0.14f)
    drawLine(
      color = weaveColor,
      start = Offset(xOff, top + height * 0.22f),
      end = Offset(xOff + (if (i % 2 == 0) -8f else 8f), top + height * 0.96f),
      strokeWidth = 2.5f
    )
  }
  drawLine(
    color = weaveColor,
    start = Offset(left + width * 0.12f, top + height * 0.55f),
    end = Offset(left + width * 0.88f, top + height * 0.55f),
    strokeWidth = 2.5f
  )

  // Rim pill
  drawRoundRect(
    brush = Brush.horizontalGradient(
      colors = listOf(Color(0xFFFFCA28), Color(0xFFFFD54F), Color(0xFFFFB300)),
      startX = left,
      endX = left + width
    ),
    topLeft = Offset(left, top),
    size = Size(width, height * 0.30f),
    cornerRadius = CornerRadius(14f, 14f)
  )

  // Rim interior
  drawRoundRect(
    color = Color(0xFF5D4037),
    topLeft = Offset(left + width * 0.10f, top + height * 0.06f),
    size = Size(width * 0.80f, height * 0.18f),
    cornerRadius = CornerRadius(8f, 8f)
  )

  // Tactile grip handle indicator (center dots indicating draggable affordance)
  val gripY = top + height * 0.15f
  val gripDotRadius = 2.5.dp.toPx()
  for (offset in listOf(-8.dp.toPx(), 0f, 8.dp.toPx())) {
    drawCircle(
      color = Color(0xAA3E2723),
      radius = gripDotRadius,
      center = Offset(cx + offset, gripY)
    )
  }

  // Directional guide arrows on rim ends ◄ ►
  val arrowColor = Color(0xCCFFFFFF)
  val leftArrow = Path().apply {
    moveTo(left + 6.dp.toPx(), top + height * 0.15f)
    lineTo(left + 12.dp.toPx(), top + height * 0.08f)
    lineTo(left + 12.dp.toPx(), top + height * 0.22f)
    close()
  }
  drawPath(leftArrow, color = arrowColor)

  val rightArrow = Path().apply {
    moveTo(left + width - 6.dp.toPx(), top + height * 0.15f)
    lineTo(left + width - 12.dp.toPx(), top + height * 0.08f)
    lineTo(left + width - 12.dp.toPx(), top + height * 0.22f)
    close()
  }
  drawPath(rightArrow, color = arrowColor)
}

/**
 * Dedicated bottom steering control bar providing both continuous dragging
 * via a slider touch track and discrete tap-to-nudge buttons.
 */
@Composable
fun CatcherControlBar(
  basketXPercent: Float,
  onBasketMove: (Float) -> Unit,
  onMoveLeft: () -> Unit,
  onMoveRight: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("catcher_control_bar"),
    shape = RoundedCornerShape(24.dp),
    color = Color(0xD90F172A),
    tonalElevation = 6.dp,
    shadowElevation = 8.dp,
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Step Left Nudge Button
      FilledTonalButton(
        onClick = onMoveLeft,
        modifier = Modifier
          .size(46.dp)
          .testTag("btn_steer_left"),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
          containerColor = Color(0x33FFB300),
          contentColor = Color(0xFFFFD54F)
        )
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Move Catcher Left",
          modifier = Modifier.size(24.dp)
        )
      }

      // Interactive Draggable / Tap Touch Track
      BoxWithConstraints(
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("catcher_slider_track")
          .pointerInput(Unit) {
            awaitEachGesture {
              val down = awaitFirstDown(requireUnconsumed = false)
              val normX = (down.position.x / size.width.toFloat()).coerceIn(0.12f, 0.88f)
              onBasketMove(normX)

              drag(down.id) { change ->
                change.consume()
                val dragX = (change.position.x / size.width.toFloat()).coerceIn(0.12f, 0.88f)
                onBasketMove(dragX)
              }
            }
          },
        contentAlignment = Alignment.CenterStart
      ) {
        val trackWidth = maxWidth

        // Background Track Slot
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .background(Color(0x26FFFFFF), RoundedCornerShape(7.dp))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(7.dp))
        )

        // Center Hint Label
        Text(
          text = "◄ DRAG OR TAP TO STEER ►",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0x99FFFFFF),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp,
          modifier = Modifier.align(Alignment.Center)
        )

        // Draggable Thumb Indicator matching catcher basket position
        val thumbOffset = (trackWidth - 36.dp) * ((basketXPercent - 0.12f) / 0.76f).coerceIn(0f, 1f)
        Surface(
          modifier = Modifier
            .offset(x = thumbOffset)
            .size(width = 36.dp, height = 24.dp)
            .testTag("catcher_thumb"),
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFFB300),
          shadowElevation = 4.dp,
          border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.DragHandle,
              contentDescription = "Draggable Catcher Thumb",
              tint = Color(0xFF1E293B),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Step Right Nudge Button
      FilledTonalButton(
        onClick = onMoveRight,
        modifier = Modifier
          .size(46.dp)
          .testTag("btn_steer_right"),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
          containerColor = Color(0x33FFB300),
          contentColor = Color(0xFFFFD54F)
        )
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = "Move Catcher Right",
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}

@Composable
fun StartScreen(
  highScore: Int,
  onStartClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
        )
      )
      .padding(24.dp)
      .testTag("start_screen"),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(16.dp, RoundedCornerShape(28.dp)),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xF01E293B)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // App Title Badge
        Box(
          modifier = Modifier
            .size(76.dp)
            .background(
              Brush.radialGradient(
                listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
              ),
              CircleShape
            )
            .border(2.dp, Color.White, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Falling Catch Icon",
            tint = Color.White,
            modifier = Modifier.size(44.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Falling Catch",
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Black,
          color = Color.White,
          letterSpacing = 0.5.sp
        )

        Text(
          text = "Catch good items, dodge bombs!",
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFF94A3B8),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // High Score Card
        if (highScore > 0) {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0x22FFD54F),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFD54F))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Trophy",
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "BEST SCORE: $highScore",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFE082)
              )
            }
          }
          Spacer(modifier = Modifier.height(18.dp))
        }

        // Rules Guide
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = Color(0x1AFFFFFF)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "HOW TO PLAY",
              style = MaterialTheme.typography.labelMedium,
              color = Color(0xFF90CAF9),
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            RuleRow(name = "Apples (+10) & Stars (+25)", desc = "Build combo streaks")
            RuleRow(name = "Gems (+50)", desc = "High point bonus")
            RuleRow(name = "Hearts (+1 Life)", desc = "Recover lost health")
            RuleRow(name = "Bombs (Avoid!)", desc = "-1 Life and breaks streak", isHazard = true)
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start Game Button
        Button(
          onClick = onStartClick,
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("start_game_button"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFB300),
            contentColor = Color(0xFF1E293B)
          )
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "PLAY NOW",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black
          )
        }
      }
    }
  }
}

@Composable
private fun RuleRow(name: String, desc: String, isHazard: Boolean = false) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = name,
      style = MaterialTheme.typography.bodySmall,
      color = if (isHazard) Color(0xFFFF5252) else Color(0xFFECEFF1),
      fontWeight = FontWeight.SemiBold
    )
    Text(
      text = desc,
      style = MaterialTheme.typography.labelSmall,
      color = Color(0xFF90A4AE)
    )
  }
}

@Composable
fun PauseDialog(
  stats: GameStats,
  onResume: () -> Unit,
  onRestart: () -> Unit,
  onQuit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0x99000000))
      .padding(24.dp)
      .testTag("pause_dialog"),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(0.92f),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "GAME PAUSED",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Black,
          color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Current Score: ${stats.score}",
          style = MaterialTheme.typography.titleMedium,
          color = Color(0xFFFFD54F),
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = onResume,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("resume_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300), contentColor = Color(0xFF1E293B))
        ) {
          Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "RESUME", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        FilledTonalButton(
          onClick = onRestart,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("restart_button"),
          shape = RoundedCornerShape(14.dp)
        ) {
          Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "RESTART")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
          onClick = onQuit,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("quit_button"),
          shape = RoundedCornerShape(14.dp)
        ) {
          Text(text = "MAIN MENU", color = Color(0xFFB0BEC5))
        }
      }
    }
  }
}

@Composable
fun GameOverDialog(
  stats: GameStats,
  onPlayAgain: () -> Unit,
  onMainMenu: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xBB000000))
      .padding(24.dp)
      .testTag("game_over_dialog"),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(20.dp, RoundedCornerShape(28.dp)),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        if (stats.isNewHighScore) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFD54F)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFF1E293B),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "NEW HIGH SCORE!",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E293B)
              )
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
          text = "GAME OVER",
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Black,
          color = Color(0xFFFF5252)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Final score presentation
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          color = Color(0x1AFFFFFF),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "FINAL SCORE",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF90CAF9),
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
            Text(
              text = "${stats.score}",
              style = MaterialTheme.typography.displayMedium,
              color = Color.White,
              fontWeight = FontWeight.Black
            )
            Text(
              text = "All-Time Best: ${stats.highScore}",
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFFFFE082),
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Performance Statistics
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          StatPill(
            title = "CAUGHT",
            value = "${stats.itemsCaught}",
            modifier = Modifier.weight(1f)
          )
          StatPill(
            title = "MAX COMBO",
            value = "${stats.maxCombo}",
            modifier = Modifier.weight(1f)
          )
          StatPill(
            title = "DODGED",
            value = "${stats.bombsAvoided}",
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = onPlayAgain,
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("play_again_button"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFB300),
            contentColor = Color(0xFF1E293B)
          )
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "PLAY AGAIN",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
          onClick = onMainMenu,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("game_over_menu_button"),
          shape = RoundedCornerShape(16.dp)
        ) {
          Text(text = "MAIN MENU", color = Color(0xFFB0BEC5), fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun StatPill(title: String, value: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = Color(0x14FFFFFF)
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFF90A4AE),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
        fontWeight = FontWeight.Black
      )
    }
  }
}
