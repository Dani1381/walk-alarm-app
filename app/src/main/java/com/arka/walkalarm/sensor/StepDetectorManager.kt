package com.arka.walkalarm.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class StepDetectorManager(
    context: Context,
    private val onStepDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var stepDetectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    // Fallback accelerometer peak detection variables
    private var lastMagnitude = 0f
    private var lastStepTime = 0L
    private val STEP_THRESHOLD = 11.8f
    private val MIN_STEP_DELAY_MS = 280L

    init {
        // Try hardware step detector first
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        // Fallback to accelerometer
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun startListening() {
        if (stepDetectorSensor != null) {
            sensorManager.registerListener(
                this,
                stepDetectorSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        } else if (accelerometerSensor != null) {
            sensorManager.registerListener(
                this,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0f) {
                onStepDetected()
            }
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val currentMagnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val currentTime = System.currentTimeMillis()

            if (currentMagnitude > STEP_THRESHOLD && lastMagnitude <= STEP_THRESHOLD) {
                if (currentTime - lastStepTime > MIN_STEP_DELAY_MS) {
                    lastStepTime = currentTime
                    onStepDetected()
                }
            }
            lastMagnitude = currentMagnitude
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
