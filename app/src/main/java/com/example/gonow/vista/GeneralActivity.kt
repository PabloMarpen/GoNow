package com.example.gonow.vista

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gonow.data.AuthSingleton
import com.example.gonow.tfg.R
import com.google.firebase.auth.FirebaseAuth

class GeneralActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general)


        val botonMapa = findViewById<ImageView>(R.id.imageViewMapa)
        val botonPlus = findViewById<ImageView>(R.id.imageViewPlus)
        val botonPersona = findViewById<ImageView>(R.id.imageViewPersona)
        val currentUser = AuthSingleton.auth.currentUser



        if (intent.getBooleanExtra("abrirMapa", false)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmentMapa())
                .addToBackStack(null)
                .commit()
        }

        // PARA EVITAR EL BUG DEL FRAGMENT QUE DESAPARECE
        onBackPressedDispatcher.addCallback(this) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmentMapa())
                .addToBackStack(null)
                .commit()
        }

        botonMapa.setOnClickListener {
            cambiarFragmentoConConfirmacion(FragmentMapa())
        }

        botonPlus.setOnClickListener {
            if (currentUser != null) {
                cambiarFragmentoConConfirmacion(FragmentAniadir())
            } else {
                PopUpContenidoGeneral.newInstance(FragmentPopUpSpam()).show(supportFragmentManager, "popUp")
            }

                

        }

        botonPersona.setOnClickListener {
            val destino = if (currentUser != null) FragmentAjustes() else FragmentAjustesAnonimo()
            cambiarFragmentoConConfirmacion(destino)

        }

    }

    fun cambiarFragmentoConConfirmacion(fragment: Fragment) {
        val fragmentActual = supportFragmentManager.findFragmentById(R.id.frame)

        if (fragmentActual is FragmentAniadir) {
            val mensaje =  "¿Estás seguro de que quieres salir?\n\nSe perderá la información no guardada."
            val popup = PopUp.newInstance(mensaje)

            popup.setOnAcceptListener { isConfirmed ->
                if (isConfirmed) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
            popup.show(supportFragmentManager, "popUp")
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame, fragment)
                .addToBackStack(null)
                .commit()
        }
    }



}