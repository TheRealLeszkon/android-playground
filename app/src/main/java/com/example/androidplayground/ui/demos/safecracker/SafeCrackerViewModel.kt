package com.example.androidplayground.ui.demos.safecracker

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor

enum class GamePhase { PLAYING, WON }
enum class RotationDir { NONE, LEFT, RIGHT }

data class SafeCrackerState(
    val dialRotation: Float = 0f,     // accumulated rotation in degrees (can wrap past 360)
    val dialValue: Int = 0,            // mapped to 0–90
    val targetPins: List<Int> = emptyList(),
    val step: Int = 0,
    val phase: GamePhase = GamePhase.PLAYING,
    val currentDirection: RotationDir = RotationDir.NONE,
    val requiredDirection: RotationDir = RotationDir.RIGHT,
    val lockMessage: String = "",
    val notchIndex: Int = -1
)

class SafeCrackerViewModel : ViewModel() {

    private val _state = MutableStateFlow(SafeCrackerState())
    val state: StateFlow<SafeCrackerState> = _state.asStateFlow()

    // Continuous rotation tracking
    private var previousTouchAngle: Float = Float.NaN
    private var lastNotch: Int = -1
    private var hasMovedSinceInit = false

    init {
        resetGame()
    }

    fun resetGame() {
        val pins = List(3) { (0..90).random() }
        previousTouchAngle = Float.NaN
        lastNotch = -1
        hasMovedSinceInit = false
        _state.value = SafeCrackerState(
            targetPins = pins,
            requiredDirection = RotationDir.RIGHT
        )
    }

    // ── Touch start: called on drag start to set initial reference angle ──

    fun onDragStart(touchX: Float, touchY: Float, centerX: Float, centerY: Float) {
        val dx = touchX - centerX
        val dy = touchY - centerY
        previousTouchAngle = atan2(dy, dx)
    }

    // ── Touch drag: apply delta rotation ──

    fun onDialDrag(touchX: Float, touchY: Float, centerX: Float, centerY: Float) {
        if (_state.value.phase == GamePhase.WON) return

        val dx = touchX - centerX
        val dy = touchY - centerY
        val currentAngle = atan2(dy, dx)

        // On first call (if onDragStart was missed), just store the reference
        if (previousTouchAngle.isNaN()) {
            previousTouchAngle = currentAngle
            return
        }

        // Delta in radians, normalized to [-PI, PI] to handle wraparound
        var deltaRad = currentAngle - previousTouchAngle
        if (deltaRad > PI) deltaRad -= (2 * PI).toFloat()
        if (deltaRad < -PI) deltaRad += (2 * PI).toFloat()

        previousTouchAngle = currentAngle

        // Convert to degrees and apply sensitivity scaling
        val deltaDeg = Math.toDegrees(deltaRad.toDouble()).toFloat() * SENSITIVITY

        // Accumulate rotation
        val s = _state.value
        val newRotation = s.dialRotation + deltaDeg

        // Map accumulated rotation to dial value [0, 90]
        // Normalize rotation to [0, 360) first, then map to [0, 90)
        val normalizedAngle = ((newRotation % 360f) + 360f) % 360f
        val dialValue = ((normalizedAngle / 360f) * 90f).toInt().coerceIn(0, 89)

        // Direction detection from this frame's delta
        val direction = when {
            deltaDeg > 0.3f -> RotationDir.RIGHT
            deltaDeg < -0.3f -> RotationDir.LEFT
            else -> s.currentDirection
        }

        // Notch detection (90 notches over 360°)
        val notch = floor(normalizedAngle / 4f).toInt()

        hasMovedSinceInit = true

        _state.value = s.copy(
            dialRotation = newRotation,
            dialValue = dialValue,
            currentDirection = direction,
            notchIndex = notch,
            lockMessage = ""
        )
    }

    fun onDragEnd() {
        previousTouchAngle = Float.NaN
    }

    fun hasNotchChanged(): Boolean {
        val current = _state.value.notchIndex
        val changed = current != lastNotch
        lastNotch = current
        return changed && hasMovedSinceInit
    }

    // ── Lock attempt ──

    fun tryLock(): LockResult {
        val s = _state.value
        if (s.phase == GamePhase.WON) return LockResult.ALREADY_WON
        if (s.step >= s.targetPins.size) return LockResult.ALREADY_WON

        val target = s.targetPins[s.step]
        val diff = abs(s.dialValue - target)

        // Direction check
        if (s.currentDirection != s.requiredDirection && s.currentDirection != RotationDir.NONE) {
            _state.value = s.copy(
                lockMessage = "Wrong direction! Turn ${s.requiredDirection.name}"
            )
            return LockResult.WRONG_DIRECTION
        }

        // Tolerance: within ±2
        if (diff > 2) {
            _state.value = s.copy(lockMessage = "Not close enough — keep turning")
            return LockResult.TOO_FAR
        }

        // Correct pin!
        val nextStep = s.step + 1
        if (nextStep >= 3) {
            _state.value = s.copy(
                step = nextStep,
                phase = GamePhase.WON,
                lockMessage = "Cracked!"
            )
            return LockResult.WIN
        }

        val nextDirection = if (s.requiredDirection == RotationDir.RIGHT)
            RotationDir.LEFT else RotationDir.RIGHT

        _state.value = s.copy(
            step = nextStep,
            requiredDirection = nextDirection,
            lockMessage = "Pin ${s.step + 1} unlocked! Now turn ${nextDirection.name}",
            currentDirection = RotationDir.NONE
        )
        return LockResult.CORRECT
    }

    // ── Proximity check ──

    fun getProximity(): Proximity {
        val s = _state.value
        if (s.phase == GamePhase.WON || s.step >= s.targetPins.size) return Proximity.FAR

        val diff = abs(s.dialValue - s.targetPins[s.step])
        return when {
            diff <= 2 -> Proximity.EXACT
            diff <= 5 -> Proximity.NEAR
            else -> Proximity.FAR
        }
    }

    // ── Haptic feedback (strong, distinct patterns) ──

    /** Light tick on each notch — the core tactile texture of the dial */
    fun playTickHaptic(context: Context) {
        vibrate(context, 12, 100)
    }

    /** Warmer, heavier pulse when getting close */
    fun playNearHaptic(context: Context) {
        try {
            val vibrator = getVibrator(context)
            val timings = longArrayOf(0, 35, 30, 35)
            val amplitudes = intArrayOf(0, 150, 0, 180)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } catch (_: Exception) {
            vibrate(context, 35, 150)
        }
    }

    /** Strong, unmistakable buzz when ON the target — this is the "sweet spot" feel */
    fun playExactHaptic(context: Context) {
        try {
            val vibrator = getVibrator(context)
            val timings = longArrayOf(0, 50, 20, 60)
            val amplitudes = intArrayOf(0, 220, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } catch (_: Exception) {
            vibrate(context, 60, 255)
        }
    }

    /** Double-pulse confirmation when a pin locks correctly */
    fun playLockCorrectHaptic(context: Context) {
        try {
            val vibrator = getVibrator(context)
            val timings = longArrayOf(0, 70, 80, 70)
            val amplitudes = intArrayOf(0, 220, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } catch (_: Exception) {
            vibrate(context, 70, 220)
        }
    }

    /** Celebration pattern: escalating triple pulse */
    fun playWinHaptic(context: Context) {
        try {
            val vibrator = getVibrator(context)
            val timings = longArrayOf(0, 80, 60, 100, 60, 160)
            val amplitudes = intArrayOf(0, 150, 0, 200, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } catch (_: Exception) {
            vibrate(context, 160, 255)
        }
    }

    private fun vibrate(context: Context, durationMs: Long, amplitude: Int) {
        try {
            val vibrator = getVibrator(context)
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
        } catch (_: Exception) {}
    }

    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

    companion object {
        /** Controls how fast the dial moves relative to finger drag.
         *  1.0 = 1:1 mapping, lower = slower, more precise dial. */
        private const val SENSITIVITY = 0.85f
    }
}

enum class LockResult {
    CORRECT, WIN, TOO_FAR, WRONG_DIRECTION, ALREADY_WON
}

enum class Proximity {
    FAR, NEAR, EXACT
}
