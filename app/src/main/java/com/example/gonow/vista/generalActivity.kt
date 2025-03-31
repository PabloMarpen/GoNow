package com.example.gonow.vista

import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.gonow.R

class generalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general)


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
    }
}