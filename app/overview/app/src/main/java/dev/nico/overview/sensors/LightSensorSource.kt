package dev.nico.overview.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dev.nico.overview.theme.LightHysteresis
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class LightSensorSource(private val context: Context, private val clock: () -> Long = System::currentTimeMillis) {
    fun lowLight(): Flow<Boolean> {
        val hysteresis = LightHysteresis()
        return lux().map { hysteresis.update(it, clock()) }.distinctUntilChanged()
    }

    private fun lux(): Flow<Float> = callbackFlow {
        val manager = context.getSystemService(SensorManager::class.java)
        val sensor = manager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (sensor == null) {
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) { trySend(event.values[0]) }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }
        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { manager.unregisterListener(listener) }
    }
}
