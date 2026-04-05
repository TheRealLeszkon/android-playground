package com.example.androidplayground.ui.demos.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class OrientationPosition(val label: String) {
    // Accelerometer positions
    LEFT("LEFT"),
    RIGHT("RIGHT"),
    TOP("TOP"),
    BOTTOM("BOTTOM"),
    FLAT("FLAT"),

    // Gyroscope motions — clear, non-ambiguous labels
    TILT_TOP_UP("TILT TOP UP"),
    TILT_BOTTOM_UP("TILT BOTTOM UP"),
    TURN_LEFT("TURN LEFT"),
    TURN_RIGHT("TURN RIGHT"),
    STILL("STILL")
}

enum class SensorMode { ACCELEROMETER, GYROSCOPE }

data class OrientationState(
    val position: OrientationPosition = OrientationPosition.FLAT,
    val sensorMode: SensorMode = SensorMode.ACCELEROMETER,
    val sensorX: Float = 0f,
    val sensorY: Float = 0f,
    val sensorZ: Float = 0f,
    val dashboardExpanded: Boolean = false
)

class DeviceOrientationViewModel : ViewModel(), SensorEventListener {

    private val _state = MutableStateFlow(OrientationState())
    val state: StateFlow<OrientationState> = _state.asStateFlow()

    private var sensorManager: SensorManager? = null

    // Smoothing
    private var smoothX = 0f
    private var smoothY = 0f
    private var smoothZ = 0f
    private val alpha = 0.2f

    // Gyroscope dead-zone threshold
    private val gyroThreshold = 1.5f

    // ── Sensor lifecycle ──

    fun startSensor(context: Context) {
        if (sensorManager != null) return
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager = sm
        registerCurrentSensor(sm)
    }

    fun stopSensor() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
    }

    fun toggleSensorMode() {
        val newMode = if (_state.value.sensorMode == SensorMode.ACCELEROMETER)
            SensorMode.GYROSCOPE else SensorMode.ACCELEROMETER

        // Reset smoothed values on mode switch
        smoothX = 0f
        smoothY = 0f
        smoothZ = 0f

        _state.value = _state.value.copy(
            sensorMode = newMode,
            position = if (newMode == SensorMode.ACCELEROMETER) OrientationPosition.FLAT
            else OrientationPosition.STILL,
            sensorX = 0f,
            sensorY = 0f,
            sensorZ = 0f
        )

        sensorManager?.let { sm ->
            sm.unregisterListener(this)
            registerCurrentSensor(sm)
        }
    }

    fun toggleDashboard() {
        _state.value = _state.value.copy(dashboardExpanded = !_state.value.dashboardExpanded)
    }

    private fun registerCurrentSensor(sm: SensorManager) {
        val sensorType = when (_state.value.sensorMode) {
            SensorMode.ACCELEROMETER -> Sensor.TYPE_ACCELEROMETER
            SensorMode.GYROSCOPE -> Sensor.TYPE_GYROSCOPE
        }
        val sensor = sm.getDefaultSensor(sensorType)
        sensor?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    // ── Sensor callbacks ──

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Low-pass smoothing
        smoothX += alpha * (x - smoothX)
        smoothY += alpha * (y - smoothY)
        smoothZ += alpha * (z - smoothZ)

        val position = when (_state.value.sensorMode) {
            SensorMode.ACCELEROMETER -> detectAccelerometerPosition(smoothX, smoothY)
            SensorMode.GYROSCOPE -> detectGyroscopeMotion(smoothX, smoothY)
        }

        _state.value = _state.value.copy(
            position = position,
            sensorX = smoothX,
            sensorY = smoothY,
            sensorZ = smoothZ
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ── Detection logic ──

    private fun detectAccelerometerPosition(x: Float, y: Float): OrientationPosition {
        return when {
            x > 5f -> OrientationPosition.LEFT
            x < -5f -> OrientationPosition.RIGHT
            y > 5f -> OrientationPosition.TOP
            y < -5f -> OrientationPosition.BOTTOM
            else -> OrientationPosition.FLAT
        }
    }

    private fun detectGyroscopeMotion(rotX: Float, rotY: Float): OrientationPosition {
        val absX = kotlin.math.abs(rotX)
        val absY = kotlin.math.abs(rotY)

        return when {
            // Rotation around X axis: tilting screen toward/away from user
            absX > absY && absX > gyroThreshold -> {
                if (rotX > 0) OrientationPosition.TILT_TOP_UP else OrientationPosition.TILT_BOTTOM_UP
            }
            // Rotation around Y axis: turning left/right
            // Fixed sign: positive Y = RIGHT turn, negative Y = LEFT turn
            absY > gyroThreshold -> {
                if (rotY > 0) OrientationPosition.TURN_RIGHT else OrientationPosition.TURN_LEFT
            }
            else -> _state.value.position // Keep last detected
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSensor()
    }
}
