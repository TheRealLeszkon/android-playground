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

/**
 * Game state for Tilt Pong.
 *
 * All positions are normalised to 0..1 so the composable can scale to any canvas size.
 */
data class PongState(
    // Ball
    val ballX: Float = 0.5f,
    val ballY: Float = 0.1f,
    val ballVx: Float = 0.008f,
    val ballVy: Float = 0.006f,
    val ballRadius: Float = 0.025f,

    // Paddle (centred horizontally)
    val paddleX: Float = 0.5f,
    val paddleWidth: Float = 0.25f,
    val paddleHeight: Float = 0.02f,
    val paddleY: Float = 0.92f,   // distance from top

    // Score & state
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val isRunning: Boolean = true,

    // Collision events (consumed each frame by the UI for haptics)
    val wallHit: Boolean = false,
    val paddleHit: Boolean = false,
)

class TiltPongViewModel : ViewModel(), SensorEventListener {

    private val _state = MutableStateFlow(PongState())
    val state: StateFlow<PongState> = _state.asStateFlow()

    // Exposed tilt for stats panel
    private val _tiltX = MutableStateFlow(0f)
    val tiltX: StateFlow<Float> = _tiltX.asStateFlow()

    // Internal smoothed tilt
    private var rawTiltX: Float = 0f
    private val smoothingFactor = 0.15f

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
            // Smooth tilt: negative X tilts right on most devices
            val raw = -event.values[0] / 10f          // normalise roughly to -1..1
            rawTiltX = rawTiltX + smoothingFactor * (raw - rawTiltX)
            _tiltX.value = rawTiltX
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ── Game loop tick ──

    fun tick() {
        val s = _state.value
        if (s.isGameOver || !s.isRunning) return

        // Move paddle from tilt
        val speed = 0.025f
        var newPaddleX = (s.paddleX + rawTiltX * speed).coerceIn(s.paddleWidth / 2, 1f - s.paddleWidth / 2)
        var wallHit = false
        var paddleHit = false

        // Move ball
        var bx = s.ballX + s.ballVx
        var by = s.ballY + s.ballVy
        var vx = s.ballVx
        var vy = s.ballVy
        var score = s.score

        // Wall collisions (left / right)
        if (bx - s.ballRadius <= 0f) {
            bx = s.ballRadius
            vx = -vx
            wallHit = true
        } else if (bx + s.ballRadius >= 1f) {
            bx = 1f - s.ballRadius
            vx = -vx
            wallHit = true
        }

        // Ceiling collision
        if (by - s.ballRadius <= 0f) {
            by = s.ballRadius
            vy = -vy
            wallHit = true
        }

        // Paddle collision
        val paddleLeft = newPaddleX - s.paddleWidth / 2
        val paddleRight = newPaddleX + s.paddleWidth / 2
        val paddleTop = s.paddleY

        if (vy > 0 &&
            by + s.ballRadius >= paddleTop &&
            by - s.ballRadius <= paddleTop + s.paddleHeight &&
            bx >= paddleLeft && bx <= paddleRight
        ) {
            by = paddleTop - s.ballRadius
            vy = -vy

            // Add slight angle variation based on where ball hits paddle
            val hitOffset = (bx - newPaddleX) / (s.paddleWidth / 2)  // -1..1
            vx = vx + hitOffset * 0.003f
            vx = vx.coerceIn(-0.015f, 0.015f)

            score++
            paddleHit = true
        }

        // Game over — ball passed paddle
        val gameOver = by - s.ballRadius > 1f

        _state.value = s.copy(
            ballX = bx,
            ballY = by,
            ballVx = vx,
            ballVy = vy,
            paddleX = newPaddleX,
            score = score,
            isGameOver = gameOver,
            wallHit = wallHit,
            paddleHit = paddleHit,
        )
    }

    fun restart() {
        _state.value = PongState()
    }

    override fun onCleared() {
        super.onCleared()
        stopSensor()
    }
}
