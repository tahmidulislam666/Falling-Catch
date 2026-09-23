package com.example

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AudioAndHapticHelper(context: Context) {
  private val vibrator: Vibrator? = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  } catch (_: Exception) {
    null
  }

  private val toneGenerator: ToneGenerator? = try {
    ToneGenerator(AudioManager.STREAM_MUSIC, 50)
  } catch (_: Exception) {
    null
  }

  fun playCatchSound(streak: Int) {
    try {
      val tone = when {
        streak >= 15 -> ToneGenerator.TONE_PROP_PROMPT
        streak >= 5 -> ToneGenerator.TONE_PROP_ACK
        else -> ToneGenerator.TONE_PROP_BEEP
      }
      toneGenerator?.startTone(tone, 60)
    } catch (_: Exception) {}

    vibrateCatch()
  }

  fun playBombSound() {
    try {
      toneGenerator?.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 160)
    } catch (_: Exception) {}

    vibrateBomb()
  }

  fun playHeartSound() {
    try {
      toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
    } catch (_: Exception) {}

    vibrateCatch()
  }

  fun playGameOverSound() {
    try {
      toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 250)
    } catch (_: Exception) {}
    vibrateBomb()
  }

  private fun vibrateCatch() {
    try {
      if (vibrator?.hasVibrator() == true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          @Suppress("DEPRECATION")
          vibrator.vibrate(25)
        }
      }
    } catch (_: Exception) {}
  }

  private fun vibrateBomb() {
    try {
      if (vibrator?.hasVibrator() == true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator.vibrate(
            VibrationEffect.createWaveform(
              longArrayOf(0, 70, 40, 90),
              intArrayOf(0, 255, 0, 255),
              -1
            )
          )
        } else {
          @Suppress("DEPRECATION")
          vibrator.vibrate(140)
        }
      }
    } catch (_: Exception) {}
  }

  fun release() {
    try {
      toneGenerator?.release()
    } catch (_: Exception) {}
  }
}
