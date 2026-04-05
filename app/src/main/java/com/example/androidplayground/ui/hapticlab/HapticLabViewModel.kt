package com.example.androidplayground.ui.hapticlab

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WaveformStep(
    val isRest: Boolean = false,
    val durationMs: Long = 100L,
    val amplitude: Int = 128
)

data class PrimitiveNode(
    val primitiveId: Int = 1, // PRIMITIVE_CLICK
    val scale: Float = 1.0f,
    val delayMs: Int = 0
)

class HapticLabViewModel : ViewModel() {

    // --- One-Shot State ---

    private val _oneShotDuration = MutableStateFlow(100f)
    val oneShotDuration: StateFlow<Float> = _oneShotDuration.asStateFlow()

    private val _oneShotAmplitude = MutableStateFlow(128f)
    val oneShotAmplitude: StateFlow<Float> = _oneShotAmplitude.asStateFlow()

    fun updateOneShotDuration(value: Float) { _oneShotDuration.value = value }
    fun updateOneShotAmplitude(value: Float) { _oneShotAmplitude.value = value }

    // --- Waveform State ---

    private val _waveformSteps = MutableStateFlow(listOf(WaveformStep()))
    val waveformSteps: StateFlow<List<WaveformStep>> = _waveformSteps.asStateFlow()

    private val _waveformLoop = MutableStateFlow(false)
    val waveformLoop: StateFlow<Boolean> = _waveformLoop.asStateFlow()

    fun addWaveformStep() {
        _waveformSteps.value = _waveformSteps.value + WaveformStep()
    }

    fun removeWaveformStep(index: Int) {
        _waveformSteps.value = _waveformSteps.value.toMutableList().apply {
            if (index in indices) removeAt(index)
        }
    }

    fun updateWaveformStep(index: Int, step: WaveformStep) {
        _waveformSteps.value = _waveformSteps.value.toMutableList().apply {
            if (index in indices) set(index, step)
        }
    }

    fun updateWaveformLoop(loop: Boolean) { _waveformLoop.value = loop }

    // --- Composition State ---

    private val _compositionNodes = MutableStateFlow(listOf(PrimitiveNode()))
    val compositionNodes: StateFlow<List<PrimitiveNode>> = _compositionNodes.asStateFlow()

    private val _compositionError = MutableStateFlow<String?>(null)
    val compositionError: StateFlow<String?> = _compositionError.asStateFlow()

    fun addPrimitiveNode() {
        _compositionNodes.value = _compositionNodes.value + PrimitiveNode()
    }

    fun removePrimitiveNode(index: Int) {
        _compositionNodes.value = _compositionNodes.value.toMutableList().apply {
            if (index in indices) removeAt(index)
        }
    }

    fun updatePrimitiveNode(index: Int, node: PrimitiveNode) {
        _compositionNodes.value = _compositionNodes.value.toMutableList().apply {
            if (index in indices) set(index, node)
        }
    }

    // --- Hardware Support ---

    fun areAllPrimitivesSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        return try {
            val vibrator = getVibrator(context)
            val ids = _compositionNodes.value.map { it.primitiveId }.distinct().toIntArray()
            vibrator.areAllPrimitivesSupported(*ids)
        } catch (_: Exception) {
            false
        }
    }

    fun refreshCompositionSupport(context: Context) {
        _compositionError.value = if (!areAllPrimitivesSupported(context)) {
            "One or more selected primitives are not supported by this device."
        } else {
            null
        }
    }

    // --- Vibrator Accessor ---

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

    // --- Existing Triggers ---

    fun playSystemConstant(view: View, constant: Int) {
        try {
            view.performHapticFeedback(constant)
        } catch (_: Exception) { }
    }

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

    // --- New Triggers ---

    fun playOneShot(context: Context) {
        try {
            val vibrator = getVibrator(context)
            val duration = _oneShotDuration.value.toLong()
            val amplitude = _oneShotAmplitude.value.toInt()
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } catch (_: Exception) {
            playFallbackOneShot(context)
        }
    }

    fun playWaveform(context: Context) {
        try {
            val vibrator = getVibrator(context)
            val steps = _waveformSteps.value
            if (steps.isEmpty()) return

            val timings = LongArray(steps.size) { steps[it].durationMs }
            val amplitudes = IntArray(steps.size) { if (steps[it].isRest) 0 else steps[it].amplitude }
            val repeat = if (_waveformLoop.value) 0 else -1

            val effect = VibrationEffect.createWaveform(timings, amplitudes, repeat)
            vibrator.vibrate(effect)
        } catch (_: Exception) {
            playFallbackOneShot(context)
        }
    }

    fun playComposition(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        try {
            val vibrator = getVibrator(context)
            val nodes = _compositionNodes.value
            if (nodes.isEmpty()) return

            val composition = VibrationEffect.startComposition()
            nodes.forEach { node ->
                composition.addPrimitive(node.primitiveId, node.scale, node.delayMs)
            }
            vibrator.vibrate(composition.compose())
        } catch (_: Exception) {
            playFallbackOneShot(context)
        }
    }

    fun stopVibration(context: Context) {
        try {
            getVibrator(context).cancel()
        } catch (_: Exception) { }
    }
}
