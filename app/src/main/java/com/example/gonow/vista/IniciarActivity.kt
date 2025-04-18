package com.example.gonow.vista


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gonow.tfg.R
import androidx.activity.addCallback

class IniciarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciar)


        if (intent.getBooleanExtra("abrirRegistro", false)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmentRegistro())
                .addToBackStack(null)
                .commit()
        }

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
