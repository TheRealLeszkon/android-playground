package com.example.androidplayground.ui.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
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

    // Raw sensor values written by sensor callbacks.
    // @Volatile ensures writes from the sensor thread are visible to the tick() coroutine.
    @Volatile private var rawAccel = XYZReading()
    @Volatile private var rawGyro = XYZReading()
    @Volatile private var rawLight = 0f

    // Smoothed values (low-pass filtered)
    private var smoothAccel = XYZReading()
    private var smoothGyro = XYZReading()
    private var smoothLight = 0f

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

        val lightSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (lightSensor != null) {
            sm.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Light sensor registered: ${lightSensor.name}")
        } else {
            Log.w(TAG, "No light sensor available on this device")
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
                val lux = event.values[0]
                rawLight = lux
                Log.d(TAG, "Light sensor raw: $lux lux")
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
        // Low-pass filter: blend raw into smoothed
        smoothAccel = XYZReading(
            x = smoothAccel.x + SMOOTHING * (rawAccel.x - smoothAccel.x),
            y = smoothAccel.y + SMOOTHING * (rawAccel.y - smoothAccel.y),
            z = smoothAccel.z + SMOOTHING * (rawAccel.z - smoothAccel.z)
        )
        smoothGyro = XYZReading(
            x = smoothGyro.x + SMOOTHING * (rawGyro.x - smoothGyro.x),
            y = smoothGyro.y + SMOOTHING * (rawGyro.y - smoothGyro.y),
            z = smoothGyro.z + SMOOTHING * (rawGyro.z - smoothGyro.z)
        )
        smoothLight += LIGHT_SMOOTHING * (rawLight - smoothLight)

        // Append smoothed values to history buffers
        if (accelBuffer.size >= BUFFER_SIZE) accelBuffer.removeFirst()
        accelBuffer.addLast(smoothAccel)

        if (gyroBuffer.size >= BUFFER_SIZE) gyroBuffer.removeFirst()
        gyroBuffer.addLast(smoothGyro)

        if (lightBuffer.size >= BUFFER_SIZE) lightBuffer.removeFirst()
        lightBuffer.addLast(smoothLight)

        // Normalize accelerometer XY to [-1, 1] for the tilt dot
        val normX = (smoothAccel.x / GRAVITY).coerceIn(-1f, 1f)
        val normY = (smoothAccel.y / GRAVITY).coerceIn(-1f, 1f)

        _state.value = SensorDashboardUiState(
            accelValues = smoothAccel,
            accelHistory = accelBuffer.toList(),
            accelPoint = normX to normY,

            gyroValues = smoothGyro,
            gyroHistory = gyroBuffer.toList(),

            lightValue = smoothLight,
            lightHistory = lightBuffer.toList()
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopSensor()
    }

    companion object {
        private const val TAG = "SensorDashboard"
        const val BUFFER_SIZE = 50
        private const val GRAVITY = 9.81f
        private const val SMOOTHING = 0.15f
        // Light sensor updates less frequently so use a faster alpha to converge
        private const val LIGHT_SMOOTHING = 0.35f
    }
}
