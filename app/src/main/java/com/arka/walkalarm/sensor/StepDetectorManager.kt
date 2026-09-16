package com.arka.walkalarm.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Sensor Fusion Step Detector:
 * Uses hardware TYPE_STEP_DETECTOR along with dynamic 3-axis accelerometer
 * peak-valley analysis so steps are reliably registered in pocket, hand, or movement.
 */
class StepDetectorManager(
    context: Context,
    private val onStepDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var stepDetectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    // Adaptive accelerometer peak detection
    private var lastMagnitude = 9.8f
    private var lastStepTimestamp = 0L
    private val MIN_STEP_INTERVAL_MS = 220L  // Max ~4.5 steps/sec (fast running/walking)
    private val MAX_STEP_INTERVAL_MS = 2500L // Reset peak tracking if idle

    // Dynamic threshold: adapts between 10.4 and 12.2 m/s^2
    private val LOWER_THRESHOLD = 10.3f
    private val PEAK_THRESHOLD = 11.5f

    // Hardware step detector debounce (to prevent double counting if both fire)
    private var lastHardwareStepTime = 0L

    init {
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun startListening() {
        // Register hardware detector if available
        stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
        // Always register accelerometer as reliable fallback & sensor fusion
        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val now = System.currentTimeMillis()

        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values.isNotEmpty() && event.values[0] == 1.0f) {
                if (now - lastStepTimestamp > MIN_STEP_INTERVAL_MS) {
                    lastStepTimestamp = now
                    lastHardwareStepTime = now
                    onStepDetected()
                }
            }
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            // Peak detection logic
            val timeDiff = now - lastStepTimestamp
            val hwTimeDiff = now - lastHardwareStepTime

            // Trigger step if peak reached and not already counted by hardware sensor
            if (magnitude > PEAK_THRESHOLD && lastMagnitude <= PEAK_THRESHOLD) {
                if (timeDiff in MIN_STEP_INTERVAL_MS..MAX_STEP_INTERVAL_MS || lastStepTimestamp == 0L) {
                    if (hwTimeDiff > MIN_STEP_INTERVAL_MS) {
                        lastStepTimestamp = now
                        onStepDetected()
                    }
                }
            } else if (magnitude < LOWER_THRESHOLD && timeDiff > MAX_STEP_INTERVAL_MS) {
                // Reset state on long pauses
                lastStepTimestamp = 0L
            }

            lastMagnitude = magnitude
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
