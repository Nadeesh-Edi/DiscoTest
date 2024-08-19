package com.example.discotest

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Error

class RegularScreen : AppCompatActivity() {
    private var mCameraId = ""
    private var mCameraManager : CameraManager? = null
    private var colorContainer : ConstraintLayout? = null
    private var colors : Map<String, Int>? = null
    private var speed : Float? = 1f
    private var speedTextView : TextView? = null
    private var reduce : TextView? = null
    private var increase : TextView? = null
    private var colorChangeJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regular_screen)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        speedTextView = findViewById<TextView>(R.id.currentSpeed)
        colorContainer = findViewById<ConstraintLayout>(R.id.colorContainer)
        reduce = findViewById(R.id.decrease_button)
        increase = findViewById(R.id.increase_button)

//      Increase brightness to full
        increaseBrightness()

//      Check the flashLight availability
        checkFlashInstance()

//      Get the colors in res
        colors = getColorsList()

        setUiSpeed()

//      On click listener for reduce speed button
        reduce?.setOnClickListener() {
            Toast.makeText(applicationContext, "Reduce", Toast.LENGTH_SHORT).show()
            reduceSpeed()
        }

//      On click listener for increase speed button
        increase?.setOnClickListener() {
            Toast.makeText(applicationContext, "Increase", Toast.LENGTH_SHORT).show()
            increaseSpeed()
        }

        startChangeTimer()
    }

    override fun onPause() {
        super.onPause()
        colorChangeJob?.cancel()
    }

    override fun onResume() {
        super.onResume()
        startChangeTimer()
    }

    fun startChangeTimer() {
        colorChangeJob?.cancel()

        // Start a new coroutine to change color at intervals
        colorChangeJob = GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                switchTorch(true)
                changeColor()
                delay((speed?.times(1000) ?: 1000).toLong())
            }
        }
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
                    delay((speed?.times(1000)?.minus(200) ?: 200).toLong())
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
        if (randomColor != null) {
            changeStatusBarColor(randomColor)
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

    fun setUiSpeed() {
//        var setSpeed : Int? = 0
//        setSpeed = speed!!.toInt()
        var speedStr : String = "x" + speed
        speedTextView?.text = speedStr
    }

    fun reduceSpeed() {
        if (speed != 0.5f) {
            speed = speed?.minus(0.5f)
            setUiSpeed()
        }
    }

    fun increaseSpeed() {
        if (speed != 2.5f) {
            speed = speed?.plus(0.5f)
            setUiSpeed()
        }
    }

    fun changeStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = color
        }
    }

    fun increaseBrightness() {
        val window = window
        val layoutParams = window.attributes
        layoutParams.screenBrightness = 1.0f
        window.attributes = layoutParams
    }
}