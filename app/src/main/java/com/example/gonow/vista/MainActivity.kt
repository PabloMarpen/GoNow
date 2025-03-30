package com.example.gonow.vista

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gonow.R

class MainActivity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val botonIniciar = findViewById<Button>(R.id.buttonIniciar)
        val botonRegistrarme = findViewById<Button>(R.id.buttonRegistrarme)
        val botonIniciarSinCuenta = findViewById<Button>(R.id.buttonIniciarSinCuenta)

        botonIniciar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(ContextCompat.getColor(this, R.color.primaryVariant))
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
            }
            false
        }

        botonRegistrarme.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(ContextCompat.getColor(this, R.color.secondaryVariant))
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            }
            false
        }

        botonIniciarSinCuenta.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(ContextCompat.getColor(this, R.color.supportVariantDark))
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(ContextCompat.getColor(this, R.color.supportVariant))
            }
            false
        }

    }
}