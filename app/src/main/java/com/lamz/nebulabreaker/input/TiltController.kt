package com.lamz.nebulabreaker.input

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Converts device tilt into a normalized horizontal input. Rotation-vector data is preferred
 * because it is gyroscope-assisted; accelerometer data is a compatible fallback.
 */
class TiltController(
    context: Context,
    private val onTiltChanged: (Float) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)
    private var smoothedTilt = 0f

    fun start() {
        val sensor = rotationVector ?: accelerometer ?: return
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() = sensorManager.unregisterListener(this)

    override fun onSensorChanged(event: SensorEvent) {
        val rawTilt = if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientation)
            orientation[2] // roll in radians
        } else {
            // Fallback for devices without a rotation-vector sensor.
            (event.values[0] / SensorManager.GRAVITY_EARTH).coerceIn(-1f, 1f)
        }
        val normalized = (rawTilt / 0.55f).coerceIn(-1f, 1f)
        smoothedTilt += (normalized - smoothedTilt) * 0.16f
        onTiltChanged(smoothedTilt)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
