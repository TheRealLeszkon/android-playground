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
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.roundToInt

enum class GamePhase { PLAYING, WON }
enum class RotationDir { NONE, LEFT, RIGHT }

data class SafeCrackerState(
    val angle: Float = 0f,
    val dialValue: Int = 0,
    val targetPins: List<Int> = emptyList(),
    val step: Int = 0,               // 0, 1, 2 → which pin we're on
    val phase: GamePhase = GamePhase.PLAYING,
    val currentDirection: RotationDir = RotationDir.NONE,
    val requiredDirection: RotationDir = RotationDir.RIGHT,  // first turn: RIGHT
    val lockMessage: String = "",
    val notchIndex: Int = -1
)

class SafeCrackerViewModel : ViewModel() {

    private val _state = MutableStateFlow(SafeCrackerState())
    val state: StateFlow<SafeCrackerState> = _state.asStateFlow()

    private var lastAngle: Float = 0f
    private var lastNotch: Int = -1
    private var hasMovedSinceInit = false

    init {
        resetGame()
    }

    fun resetGame() {
        val pins = List(3) { (0..90).random() }
        lastAngle = 0f
        lastNotch = -1
        hasMovedSinceInit = false
        _state.value = SafeCrackerState(
            targetPins = pins,
            requiredDirection = RotationDir.RIGHT
        )
    }

    // ── Touch interaction: compute angle from touch point relative to center ──

    fun onDialTouch(touchX: Float, touchY: Float, centerX: Float, centerY: Float) {
        if (_state.value.phase == GamePhase.WON) return

        val dx = touchX - centerX
        val dy = touchY - centerY

        // angle in degrees [0, 360)
        var angle = atan2(dy.toDouble(), dx.toDouble()) * 180.0 / Math.PI
        angle = (angle + 360.0) % 360.0
        val anglef = angle.toFloat()

        // Angle → dial value (0–90)
        val dialValue = (anglef / 360f) * 90f
        val dialInt = dialValue.roundToInt().coerceIn(0, 90)

        // Direction detection
        var delta = anglef - lastAngle
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f

        val direction = when {
            delta > 0.5f -> RotationDir.RIGHT
            delta < -0.5f -> RotationDir.LEFT
            else -> _state.value.currentDirection
        }

        // Notch detection (90 notches over 360°, so every 4°)
        val notch = floor(anglef / 4f).toInt()

        lastAngle = anglef
        hasMovedSinceInit = true

        _state.value = _state.value.copy(
            angle = anglef,
            dialValue = dialInt,
            currentDirection = direction,
            notchIndex = notch,
            lockMessage = ""
        )
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

        // Direction check: must be turning in the required direction
        if (s.currentDirection != s.requiredDirection && s.currentDirection != RotationDir.NONE) {
            _state.value = s.copy(
                lockMessage = "Wrong direction! Turn ${s.requiredDirection.name}"
            )
            return LockResult.WRONG_DIRECTION
        }

        // Tolerance check: within ±2
        if (diff > 2) {
            _state.value = s.copy(lockMessage = "Not close enough — keep turning")
            return LockResult.TOO_FAR
        }

        // Correct!
        val nextStep = s.step + 1
        if (nextStep >= 3) {
            _state.value = s.copy(
                step = nextStep,
                phase = GamePhase.WON,
                lockMessage = "Cracked!"
            )
            return LockResult.WIN
        }

        // Advance to next pin, flip direction
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

    // ── Proximity check (for haptic feedback) ──

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

    // ── Haptic feedback ──

    fun playTickHaptic(context: Context) {
        vibrate(context, 8, 80)
    }

    fun playNearHaptic(context: Context) {
        vibrate(context, 25, 120)
    }

    fun playExactHaptic(context: Context) {
        vibrate(context, 40, 200)
    }

    fun playLockCorrectHaptic(context: Context) {
        // Double-pulse confirmation
        try {
            val vibrator = getVibrator(context)
            val timings = longArrayOf(0, 60, 80, 60)
            val amplitudes = intArrayOf(0, 200, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } catch (_: Exception) {
            vibrate(context, 60, 200)
        }
    }

    fun playWinHaptic(context: Context) {
        // Celebration pattern: escalating triple pulse
        try {
            val vibrator = getVibrator(context)
            val timings = longArrayOf(0, 80, 60, 100, 60, 150)
            val amplitudes = intArrayOf(0, 150, 0, 200, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } catch (_: Exception) {
            vibrate(context, 150, 255)
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
}

enum class LockResult {
    CORRECT, WIN, TOO_FAR, WRONG_DIRECTION, ALREADY_WON
}

enum class Proximity {
    FAR, NEAR, EXACT
}
