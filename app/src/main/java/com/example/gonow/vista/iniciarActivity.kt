package com.example.gonow.vista

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BuildCompat
import com.example.gonow.R
import androidx.activity.addCallback

class iniciarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciar)

        // Verifica si se envió el extra para abrir el fragmento de registro
        if (intent.getBooleanExtra("abrirRegistro", false)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmentRegistro())
                .addToBackStack(null)
                .commit()
        }

        // Verifica si se envió el extra para abrir el fragmento de registro
        if (intent.getBooleanExtra("abrirCuenta", false)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmentIniciar())
                .addToBackStack(null)
                .commit()
        }

        // PARA EVITAR EL BUG DEL FRAGMENT QUE DESAPARECE
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }


    }
}
