package com.trustathanas.letsplay.repositories

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.trustathanas.letsplay.models.AccelerometerModel
import java.util.concurrent.ExecutorService

class TiltRepository(
    private val context: Context,
    private val executorService: ExecutorService
) : SensorEventListener {


    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor

    private var accelerometerValues: MutableLiveData<AccelerometerModel>

    init {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        accelerometerValues = MutableLiveData()
    }

    fun getAccelerometerValues(): LiveData<AccelerometerModel> {
        return accelerometerValues
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            executorService.execute {
                accelerometerValues.postValue(AccelerometerModel(event.values[0], event.values[1]))
            }
        }

    }
}