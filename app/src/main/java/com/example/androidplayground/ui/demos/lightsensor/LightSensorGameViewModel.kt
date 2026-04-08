package com.example.androidplayground.ui.demos.lightsensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TimeOfDay { DIM, BRIGHT, NIGHT }

data class LightSensorState(
    val lux: Float = 0f,
    val smoothedLux: Float = 0f,
    val timeOfDay: TimeOfDay = TimeOfDay.DIM,
    val sensorAvailable: Boolean = true
)

class LightSensorGameViewModel : ViewModel(), SensorEventListener {

    private val _state = MutableStateFlow(LightSensorState())
    val state: StateFlow<LightSensorState> = _state.asStateFlow()

    @Volatile private var rawLux: Float = 0f

    private var sensorManager: SensorManager? = null
    private var smoothedLux: Float = 0f
    private var currentTimeOfDay: TimeOfDay = TimeOfDay.DIM

    // Debounce: how many consecutive frames must agree before switching state
    private var pendingState: TimeOfDay? = null
    private var pendingCount: Int = 0

    fun startSensor(context: Context) {
        if (sensorManager != null) return
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager = sm
        val lightSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (lightSensor == null) {
            _state.value = _state.value.copy(sensorAvailable = false)
            return
        }

        sm.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI)
    }

    fun stopSensor() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            rawLux = event.values[0]
            processReading()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun processReading() {
        // Exponential moving average to smooth noisy readings
        smoothedLux += SMOOTHING_ALPHA * (rawLux - smoothedLux)

        // Determine time of day with hysteresis to prevent flickering
        val candidate = classifyLux(smoothedLux)

        if (candidate != currentTimeOfDay) {
            if (candidate == pendingState) {
                pendingCount++
                if (pendingCount >= DEBOUNCE_FRAMES) {
                    currentTimeOfDay = candidate
                    pendingState = null
                    pendingCount = 0
                }
            } else {
                pendingState = candidate
                pendingCount = 1
            }
        } else {
            // Already matches, reset any pending
            pendingState = null
            pendingCount = 0
        }

        _state.value = LightSensorState(
            lux = rawLux,
            smoothedLux = smoothedLux,
            timeOfDay = currentTimeOfDay,
            sensorAvailable = true
        )
    }

    /**
     * Classify lux into time of day using hysteresis buffers.
     * Going UP requires crossing the upper threshold;
     * going DOWN requires crossing the lower threshold.
     * This prevents rapid toggling at boundary values.
     */
    private fun classifyLux(lux: Float): TimeOfDay {
        return when (currentTimeOfDay) {
            TimeOfDay.NIGHT -> when {
                lux > NIGHT_TO_DIM_UP -> TimeOfDay.DIM
                else -> TimeOfDay.NIGHT
            }
            TimeOfDay.DIM -> when {
                lux < DIM_TO_NIGHT_DOWN -> TimeOfDay.NIGHT
                lux > DIM_TO_BRIGHT_UP -> TimeOfDay.BRIGHT
                else -> TimeOfDay.DIM
            }
            TimeOfDay.BRIGHT -> when {
                lux < BRIGHT_TO_DIM_DOWN -> TimeOfDay.DIM
                else -> TimeOfDay.BRIGHT
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSensor()
    }

    companion object {
        private const val SMOOTHING_ALPHA = 0.15f

        // Number of consecutive agreement frames before switching state
        private const val DEBOUNCE_FRAMES = 5

        // Hysteresis thresholds (going up vs going down differ to prevent flicker)
        private const val NIGHT_TO_DIM_UP = 65f
        private const val DIM_TO_NIGHT_DOWN = 40f
        private const val DIM_TO_BRIGHT_UP = 550f
        private const val BRIGHT_TO_DIM_DOWN = 400f
    }
}
