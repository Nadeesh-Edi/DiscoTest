package com.example.discotest

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import java.util.Objects

class MainActivity : AppCompatActivity() {
    private var shakeButton : AppCompatButton? = null
    private var regularButton : AppCompatButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.red)
        }

        shakeButton = findViewById(R.id.shake_button)
        regularButton = findViewById(R.id.regular_button)

        shakeButton?.setOnClickListener() {
            var intent : Intent = Intent(this, ColorScreen::class.java)
            startActivity(intent)
        }

        regularButton?.setOnClickListener() {
            var intent : Intent = Intent(this, RegularScreen::class.java)
            startActivity(intent)
        }
    }
}