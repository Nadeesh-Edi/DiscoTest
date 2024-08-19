package com.example.discotest

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import java.util.Objects

class MainActivity : AppCompatActivity() {
    private var shakeButton : AppCompatButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        shakeButton = findViewById(R.id.shake_button)

        shakeButton?.setOnClickListener() {
            var intent : Intent = Intent(this, ColorScreen::class.java)
            startActivity(intent)
        }
    }
}