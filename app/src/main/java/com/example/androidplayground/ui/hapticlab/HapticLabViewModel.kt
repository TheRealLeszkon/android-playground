package com.example.androidplayground.ui.hapticlab

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import androidx.lifecycle.ViewModel

class HapticLabViewModel : ViewModel() {

    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun playFallbackOneShot(context: Context) {
        try {
            val vibrator = getVibrator(context)
            vibrator.vibrate(VibrationEffect.createOneShot(50L, 255))
        } catch (_: Exception) { }
    }

    // ── System Constants ──

    fun playSystemConstant(view: View, constant: Int) {
        try {
            view.performHapticFeedback(constant)
        } catch (_: Exception) { }
    }

    // ── Predefined Effects (SDK 29+) ──

    fun playPredefinedEffect(context: Context, effectId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val vibrator = getVibrator(context)
                val effect = VibrationEffect.createPredefined(effectId)
                vibrator.vibrate(effect)
            } catch (_: Exception) {
                playFallbackOneShot(context)
            }
        } else {
            playFallbackOneShot(context)
        }
    }
}
