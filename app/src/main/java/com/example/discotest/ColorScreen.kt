package com.example.discotest

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Error
import java.util.Objects
import kotlin.math.sqrt

class ColorScreen : AppCompatActivity() {
    private var sensorManager : SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var mCameraId = ""
    private var mCameraManager : CameraManager? = null
    private var colorContainer : ConstraintLayout? = null
    private var colors : Map<String, Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_screen)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        colorContainer = findViewById<ConstraintLayout>(R.id.colorContainer)

//      Check the flashLight availability
        checkFlashInstance()

//      Get the colors in res
        colors = getColorsList()

        // Getting the Sensor Manager instance
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH
    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration

            // Getting current accelerations
            // with the help of fetched x,y,z values
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.2f + delta

            print(acceleration)

            // Display a Toast message if
            // acceleration value is over 12
            if (acceleration > 8) {
//                Toast.makeText(applicationContext, "Shake event detected", Toast.LENGTH_SHORT).show()
                switchTorch(true);
                changeColor()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onResume() {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }

    fun checkFlashInstance() {
        val isFlashAvailable: Boolean = applicationContext.packageManager
            .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        if (!isFlashAvailable) {
            return Toast.makeText(applicationContext, "Flash not available", Toast.LENGTH_SHORT).show()
        }

        mCameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            mCameraId = mCameraManager!!.cameraIdList[0]
        } catch (e : Error) {
            return Toast.makeText(applicationContext, "Flash not available", Toast.LENGTH_SHORT).show()
        }
    }

    fun switchTorch(isOn : Boolean) {
        try {
            mCameraManager?.setTorchMode(mCameraId, isOn)
            if (isOn) {
                // Launch a coroutine to wait for 500 milliseconds before switching off
                GlobalScope.launch(Dispatchers.Main) {
                    delay(200) // Delay for 500 milliseconds
                    mCameraManager?.setTorchMode(mCameraId, false)
                }
            }
        } catch (e : Error) {
            return Toast.makeText(applicationContext, "Flash not available", Toast.LENGTH_SHORT).show()
        }
    }

    fun changeColor() {
        var currentColor = (colorContainer?.background as? ColorDrawable)?.color ?: Color.RED
        val randomColor = getRandomColor(currentColor)

        randomColor?.let {
            colorContainer?.setBackgroundColor(randomColor)
        }
    }

    fun getColorsList(): Map<String, Int> {
        val colorsList = mutableMapOf<String, Int>()
        val fields = R.color::class.java.fields

        for (field in fields) {
            try {
                val colorName = field.name
                val colorResId = field.getInt(null)
                val colorValue = ContextCompat.getColor(this, colorResId)
                colorsList[colorName] = colorValue
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return colorsList
    }

    fun getRandomColor(excludedColor: Int): Int? {
        // Ensure colors map is not null or empty
        val colorsList = colors?.values?.toMutableList() ?: mutableListOf()

        // Remove the excluded color from the list
        colorsList.removeAll { it == excludedColor }

        // Return a random color from the remaining list
        return if (colorsList.isNotEmpty()) {
            colorsList.random()
        } else {
            null
        }
    }
}