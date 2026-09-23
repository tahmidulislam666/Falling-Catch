package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        FallingCatchApp()
      }
    }
  }
}

@Composable
fun FallingCatchApp(
  viewModel: GameViewModel = viewModel()
) {
  val gameState by viewModel.gameState.collectAsState()
  val stats by viewModel.stats.collectAsState()
  val basketXPercent by viewModel.basketXPercent.collectAsState()
  val items by viewModel.items.collectAsState()
  val particles by viewModel.particles.collectAsState()
  val floatingTexts by viewModel.floatingTexts.collectAsState()
  val screenShake by viewModel.screenShake.collectAsState()

  // High-performance game loop synced with display frame refresh rate
  LaunchedEffect(gameState) {
    if (gameState == GameState.PLAYING) {
      var lastFrameTime = withFrameNanos { it }
      while (isActive && gameState == GameState.PLAYING) {
        withFrameNanos { frameTimeNanos ->
          val dt = (frameTimeNanos - lastFrameTime) / 1_000_000_000f
          lastFrameTime = frameTimeNanos
          viewModel.updateGame(dt)
        }
      }
    }
  }

  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
    ) {
      // Main interactive game canvas
      GameCanvas(
        items = items,
        particles = particles,
        floatingTexts = floatingTexts,
        basketXPercent = basketXPercent,
        basketYPercent = viewModel.basketY,
        screenShake = screenShake,
        onBasketMove = { xPercent -> viewModel.moveBasketTo(xPercent) }
      )

      // Top HUD when playing or paused
      if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
        GameHud(
          stats = stats,
          onPauseClick = { viewModel.pauseGame() },
          modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
        )
      }

      // Bottom Catcher Steering Control Bar when playing
      if (gameState == GameState.PLAYING) {
        CatcherControlBar(
          basketXPercent = basketXPercent,
          onBasketMove = { xPercent -> viewModel.moveBasketTo(xPercent) },
          onMoveLeft = { viewModel.stepBasketLeft() },
          onMoveRight = { viewModel.stepBasketRight() },
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
        )
      }

      // Start Screen Overlay
      if (gameState == GameState.START) {
        StartScreen(
          highScore = stats.highScore,
          onStartClick = { viewModel.startGame() }
        )
      }

      // Pause Dialog Overlay
      if (gameState == GameState.PAUSED) {
        PauseDialog(
          stats = stats,
          onResume = { viewModel.resumeGame() },
          onRestart = { viewModel.startGame() },
          onQuit = { viewModel.returnToStart() }
        )
      }

      // Game Over Overlay
      if (gameState == GameState.GAME_OVER) {
        GameOverDialog(
          stats = stats,
          onPlayAgain = { viewModel.startGame() },
          onMainMenu = { viewModel.returnToStart() }
        )
      }
    }
  }
}
