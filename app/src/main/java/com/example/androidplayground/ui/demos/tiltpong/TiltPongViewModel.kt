package com.example.androidplayground.ui.demos.tiltpong

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PlayerSide { TOP, BOTTOM }

data class PongState(
    // Ball
    val ballX: Float = 0.5f,
    val ballY: Float = 0.5f,
    val ballVx: Float = 0.002904f,
    val ballVy: Float = 0.004356f,
    val ballRadius: Float = 0.018f,

    // Paddles — horizontal bars at fixed Y positions, moving along X
    val topPaddleX: Float = 0.5f,
    val bottomPaddleX: Float = 0.5f,
    val paddleWidth: Float = 0.25f,     // width of the paddle in normalized X
    val paddleThickness: Float = 0.015f, // height of the paddle in normalized Y
    val topPaddleY: Float = 0.05f,      // fixed Y center of the top paddle
    val bottomPaddleY: Float = 0.95f,   // fixed Y center of the bottom paddle

    // Active control
    val playerSide: PlayerSide = PlayerSide.BOTTOM,

    // Score & state
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val isRunning: Boolean = true,

    // Collision events (consumed each frame by the UI for haptics)
    val wallHit: Boolean = false,
    val paddleHit: Boolean = false,
    val controlSwitched: Boolean = false,
)

class TiltPongViewModel : ViewModel(), SensorEventListener {

    private val _state = MutableStateFlow(PongState())
    val state: StateFlow<PongState> = _state.asStateFlow()

    // Exposed tilt for stats panel
    private val _tiltX = MutableStateFlow(0f)
    val tiltX: StateFlow<Float> = _tiltX.asStateFlow()

    // Internal raw sensor input (written on sensor thread)
    @Volatile private var rawSensorX: Float = 0f

    // Smoothed tilt input (processed in tick)
    private var smoothedTilt: Float = 0f

    // Velocity-based paddle movement
    private var paddleVelocity: Float = 0f

    private var sensorManager: SensorManager? = null

    // ── Sensor lifecycle ──

    fun startSensor(context: Context) {
        if (sensorManager != null) return
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager = sm
        val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accel?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopSensor() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Negative X = tilt right on most devices.
            // We invert so tilting right moves paddle right.
            rawSensorX = -event.values[0]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ── Game loop tick ──

    fun tick() {
        val s = _state.value
        if (s.isGameOver || !s.isRunning) return

        // --- Tilt input processing ---
        val rawNormalized = rawSensorX / TILT_DIVISOR
        val delta = kotlin.math.abs(rawNormalized - smoothedTilt)
        val alpha = if (delta > 0.15f) SMOOTHING_FAST else SMOOTHING_SLOW
        smoothedTilt += alpha * (rawNormalized - smoothedTilt)
        _tiltX.value = smoothedTilt

        // --- Velocity-based paddle movement along X ---
        paddleVelocity += smoothedTilt * PADDLE_ACCELERATION
        paddleVelocity *= PADDLE_DAMPING
        paddleVelocity = paddleVelocity.coerceIn(-MAX_PADDLE_SPEED, MAX_PADDLE_SPEED)

        val halfPaddle = s.paddleWidth / 2
        var topPadX = s.topPaddleX
        var bottomPadX = s.bottomPaddleX

        when (s.playerSide) {
            PlayerSide.BOTTOM -> {
                bottomPadX = (bottomPadX + paddleVelocity).coerceIn(halfPaddle, 1f - halfPaddle)
            }
            PlayerSide.TOP -> {
                topPadX = (topPadX + paddleVelocity).coerceIn(halfPaddle, 1f - halfPaddle)
            }
        }

        // --- Ball movement ---
        var bx = s.ballX + s.ballVx
        var by = s.ballY + s.ballVy
        var vx = s.ballVx
        var vy = s.ballVy
        var score = s.score
        var wallHit = false
        var paddleHit = false
        var controlSwitched = false
        var playerSide = s.playerSide

        // Left / right wall bounces
        if (bx - s.ballRadius <= 0f) {
            bx = s.ballRadius
            vx = -vx
            wallHit = true
        } else if (bx + s.ballRadius >= 1f) {
            bx = 1f - s.ballRadius
            vx = -vx
            wallHit = true
        }

        // Bottom paddle collision (ball moving downward)
        val bottomEdge = s.bottomPaddleY - s.paddleThickness / 2
        val bottomPadLeft = bottomPadX - s.paddleWidth / 2
        val bottomPadRight = bottomPadX + s.paddleWidth / 2

        if (vy > 0 &&
            by + s.ballRadius >= bottomEdge &&
            by - s.ballRadius <= s.bottomPaddleY + s.paddleThickness / 2 &&
            bx >= bottomPadLeft && bx <= bottomPadRight
        ) {
            by = bottomEdge - s.ballRadius
            vy = -vy

            // Angle variation based on where ball hits the paddle
            val hitOffset = (bx - bottomPadX) / (s.paddleWidth / 2)
            vx = vx + hitOffset * 0.003f
            vx = vx.coerceIn(-0.015f, 0.015f)

            score++
            paddleHit = true

            if (playerSide == PlayerSide.BOTTOM) {
                playerSide = PlayerSide.TOP
                paddleVelocity = 0f
                controlSwitched = true
            }
        }

        // Top paddle collision (ball moving upward)
        val topEdge = s.topPaddleY + s.paddleThickness / 2
        val topPadLeft = topPadX - s.paddleWidth / 2
        val topPadRight = topPadX + s.paddleWidth / 2

        if (vy < 0 &&
            by - s.ballRadius <= topEdge &&
            by + s.ballRadius >= s.topPaddleY - s.paddleThickness / 2 &&
            bx >= topPadLeft && bx <= topPadRight
        ) {
            by = topEdge + s.ballRadius
            vy = -vy

            val hitOffset = (bx - topPadX) / (s.paddleWidth / 2)
            vx = vx + hitOffset * 0.003f
            vx = vx.coerceIn(-0.015f, 0.015f)

            score++
            paddleHit = true

            if (playerSide == PlayerSide.TOP) {
                playerSide = PlayerSide.BOTTOM
                paddleVelocity = 0f
                controlSwitched = true
            }
        }

        // Game over — ball escaped past top or bottom without hitting a paddle
        val gameOver = by - s.ballRadius < 0f || by + s.ballRadius > 1f

        _state.value = s.copy(
            ballX = bx,
            ballY = by,
            ballVx = vx,
            ballVy = vy,
            topPaddleX = topPadX,
            bottomPaddleX = bottomPadX,
            playerSide = playerSide,
            score = score,
            isGameOver = gameOver,
            wallHit = wallHit,
            paddleHit = paddleHit,
            controlSwitched = controlSwitched,
        )
    }

    fun restart() {
        smoothedTilt = 0f
        paddleVelocity = 0f
        _state.value = PongState()
    }

    override fun onCleared() {
        super.onCleared()
        stopSensor()
    }

    companion object {
        private const val TILT_DIVISOR = 10f

        private const val SMOOTHING_FAST = 0.25f
        private const val SMOOTHING_SLOW = 0.15f

        private const val PADDLE_ACCELERATION = 0.004f
        private const val PADDLE_DAMPING = 0.95f
        private const val MAX_PADDLE_SPEED = 0.018f
    }
}
