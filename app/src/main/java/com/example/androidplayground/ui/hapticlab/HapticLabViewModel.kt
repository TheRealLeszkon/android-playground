package com.example.androidplayground.ui.hapticlab

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HapticLabViewModel : ViewModel() {

    // ── Saved patterns ──
    private val _savedPatterns = MutableStateFlow<List<SavedVibration>>(emptyList())
    val savedPatterns: StateFlow<List<SavedVibration>> = _savedPatterns.asStateFlow()

    // ── Composition slider scale ──
    private val _primitiveScale = MutableStateFlow(0.5f)
    val primitiveScale: StateFlow<Float> = _primitiveScale.asStateFlow()

    fun updatePrimitiveScale(scale: Float) {
        _primitiveScale.value = scale.coerceIn(0f, 1f)
    }

    // ── Helpers ──

    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    // ── Category A: System Constants ──

    fun playSystemConstant(view: View, constant: Int) {
        view.performHapticFeedback(constant)
    }

    // ── Category B: Predefined Effects (SDK 29+) ──

    fun playPredefinedEffect(context: Context, effectId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val vibrator = getVibrator(context)
                val effect = VibrationEffect.createPredefined(effectId)
                vibrator.vibrate(effect)
            } catch (_: Exception) {
                // Effect not supported on this device
            }
        }
    }

    // ── Category C: Composition (SDK 30+) ──

    fun playComposition(context: Context, primitiveId: Int, scale: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val vibrator = getVibrator(context)
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(primitiveId, scale)
                    .compose()
                vibrator.vibrate(effect)
            } catch (_: Exception) {
                // Primitive not supported on this device
            }
        }
    }

    fun playSequence(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val vibrator = getVibrator(context)
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN, 1f)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1f)
                    .compose()
                vibrator.vibrate(effect)
            } catch (_: Exception) {
                // Primitives not supported on this device
            }
        }
    }

    // ── Category D: Waveform ──

    fun playWaveform(context: Context, timings: LongArray, amplitudes: IntArray) {
        try {
            val vibrator = getVibrator(context)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } catch (_: Exception) {
            // Waveform not supported on this device
        }
    }

    fun savePattern(name: String, timings: LongArray, amplitudes: IntArray) {
        if (name.isBlank()) return
        val pattern = SavedVibration(name, timings, amplitudes)
        _savedPatterns.value = _savedPatterns.value + pattern
    }

    fun deletePattern(index: Int) {
        val current = _savedPatterns.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _savedPatterns.value = current
        }
    }
}
