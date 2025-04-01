package com.example.gonow.vista

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.gonow.R

class generalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general)


        val botonMapa = findViewById<ImageView>(R.id.imageViewMapa)
        val botonPlus = findViewById<ImageView>(R.id.imageViewPlus)
        val botonPersona = findViewById<ImageView>(R.id.imageViewPersona)

        if (intent.getBooleanExtra("abrirMapa", false)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmentMapa())
                .addToBackStack(null)
                .commit()
        }

        // PARA EVITAR EL BUG DEL FRAGMENT QUE DESAPARECE
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }

        botonMapa.setOnClickListener {
            Toast.makeText(this, "Mapa", Toast.LENGTH_SHORT).show()
        }

        botonPlus.setOnClickListener {
            Toast.makeText(this, "Añadir", Toast.LENGTH_SHORT).show()
        }

        botonPersona.setOnClickListener {
            Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show()
        }
    }
}