package com.example.androidplayground.ui.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class XYZReading(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f)

data class SensorDashboardUiState(
    val accelValues: XYZReading = XYZReading(),
    val accelHistory: List<XYZReading> = emptyList(),
    val accelPoint: Pair<Float, Float> = 0f to 0f,

    val gyroValues: XYZReading = XYZReading(),
    val gyroHistory: List<XYZReading> = emptyList(),

    val lightValue: Float = 0f,
    val lightHistory: List<Float> = emptyList()
)

class SensorDashboardViewModel : ViewModel(), SensorEventListener {

    private val _state = MutableStateFlow(SensorDashboardUiState())
    val state: StateFlow<SensorDashboardUiState> = _state.asStateFlow()

    private var sensorManager: SensorManager? = null

    // Raw sensor values written by sensor callbacks
    private var rawAccel = XYZReading()
    private var rawGyro = XYZReading()
    private var rawLight = 0f

    // History buffers maintained inside the frame loop
    private val accelBuffer = ArrayDeque<XYZReading>(BUFFER_SIZE)
    private val gyroBuffer = ArrayDeque<XYZReading>(BUFFER_SIZE)
    private val lightBuffer = ArrayDeque<Float>(BUFFER_SIZE)

    fun startSensor(context: Context) {
        if (sensorManager != null) return
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager = sm

        sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        sm.getDefaultSensor(Sensor.TYPE_LIGHT)?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopSensor() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                rawAccel = XYZReading(event.values[0], event.values[1], event.values[2])
            }
            Sensor.TYPE_GYROSCOPE -> {
                rawGyro = XYZReading(event.values[0], event.values[1], event.values[2])
            }
            Sensor.TYPE_LIGHT -> {
                rawLight = event.values[0]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun startFrameLoop() {
        viewModelScope.launch {
            while (true) {
                tick()
                delay(16L)
            }
        }
    }

    private fun tick() {
        // Append to history buffers
        if (accelBuffer.size >= BUFFER_SIZE) accelBuffer.removeFirst()
        accelBuffer.addLast(rawAccel)

        if (gyroBuffer.size >= BUFFER_SIZE) gyroBuffer.removeFirst()
        gyroBuffer.addLast(rawGyro)

        if (lightBuffer.size >= BUFFER_SIZE) lightBuffer.removeFirst()
        lightBuffer.addLast(rawLight)

        // Normalize accelerometer XY to [-1, 1] for the tilt dot
        val normX = (rawAccel.x / GRAVITY).coerceIn(-1f, 1f)
        val normY = (rawAccel.y / GRAVITY).coerceIn(-1f, 1f)

        _state.value = SensorDashboardUiState(
            accelValues = rawAccel,
            accelHistory = accelBuffer.toList(),
            accelPoint = normX to normY,

            gyroValues = rawGyro,
            gyroHistory = gyroBuffer.toList(),

            lightValue = rawLight,
            lightHistory = lightBuffer.toList()
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopSensor()
    }

    companion object {
        const val BUFFER_SIZE = 50
        private const val GRAVITY = 9.81f
    }
}
