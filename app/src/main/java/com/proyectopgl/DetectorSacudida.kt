package com.proyectopgl

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class DetectorSacudida(contexto: Context, private val oyente: EnSacudirOyente) : SensorEventListener {

    // Interfaz que se debe implementar en las clases que quieran recibir notificaciones de agitación
    interface EnSacudirOyente {
        fun enSacudir()
    }

    // El sensor de acelerómetro del móvil
    private val sensorManager: SensorManager = contexto.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Se obtiene el sensor de acelerómetro
    private val acelerometro: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Umbral de aceleración para considerar que se ha producido una sacudida
    private var umbralSacudida = 5.0f

    // Tiempo en milisegundos desde la última sacudida
    private var ultimaSacudida: Long = 0

    // El evento comienza a escuchar el acelerómetro
    fun iniciar() {
        acelerometro?.also { acc ->
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun detener() {
        sensorManager.unregisterListener(this)
    }

    // Cuando el acelerómetro del móvil detecta un cambio
    override fun onSensorChanged(evento: SensorEvent?) {
        evento?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]

            // La aceleración es la raíz cuadrada de la suma de los cuadrados de las componentes
            val aceleracion = sqrt((x * x + y * y + z * z).toDouble()).toFloat() - SensorManager.GRAVITY_EARTH

            // Si la aceleración es mayor que el umbral, se considera que se ha producido una sacudida
            if (aceleracion > umbralSacudida) {
                val tiempoActual = System.currentTimeMillis()
                if (tiempoActual - ultimaSacudida > 1000) {
                    ultimaSacudida = tiempoActual
                    oyente.enSacudir()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, precision: Int) {
        // No es necesario manejar esto
    }
}
