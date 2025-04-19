package com.example.gonow.vista

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gonow.data.AuthSingleton
import com.example.gonow.tfg.R
import com.google.firebase.auth.FirebaseAuth

class GeneralActivity : AppCompatActivity() {

    private lateinit var selectorPlaneta: ImageView
    private lateinit var selectorCrear: ImageView
    private lateinit var selectorAjustes: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general)


        val botonMapa = findViewById<ImageView>(R.id.imageViewMapa)
        val botonPlus = findViewById<ImageView>(R.id.imageViewPlus)
        val botonPersona = findViewById<ImageView>(R.id.imageViewPersona)
        selectorPlaneta = findViewById(R.id.imageViewCirculo1)
        selectorCrear = findViewById(R.id.imageViewCirculo2)
        selectorAjustes = findViewById(R.id.imageViewCirculo3)

        val currentUser = AuthSingleton.auth.currentUser

        selectorPlaneta.visibility = ImageView.VISIBLE
        selectorCrear.visibility = ImageView.INVISIBLE
        selectorAjustes.visibility = ImageView.INVISIBLE

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
            gestionarSelectorVisual(FragmentMapa())
            val fragmentActual = supportFragmentManager.findFragmentById(R.id.frame)
            if (fragmentActual !is FragmentMapa) {
                cambiarFragmentoConConfirmacion(FragmentMapa())
            }

        }

        botonPlus.setOnClickListener {
            gestionarSelectorVisual(FragmentAniadir())
            val fragmentActual = supportFragmentManager.findFragmentById(R.id.frame)

            if (fragmentActual !is FragmentAniadir) {
                if (currentUser != null) {
                    cambiarFragmentoConConfirmacion(FragmentAniadir())
                } else {
                    PopUpContenidoGeneral.newInstance(FragmentPopUpSpam()).show(supportFragmentManager, "popUp")
                }
            }
        }


        botonPersona.setOnClickListener {
            gestionarSelectorVisual(FragmentAjustes())
            val destino = if (currentUser != null) FragmentAjustes() else FragmentAjustesAnonimo()
            cambiarFragmentoConConfirmacion(destino)

        }

    }

    fun gestionarSelectorVisual(fragment: Fragment) {
        // Obtener el fragmento actual
        val fragmentActual = supportFragmentManager.findFragmentById(R.id.frame)

        // Establecer la visibilidad de los selectores según el fragmento actual
        when (fragment) {
            is FragmentMapa -> {
                // Si el fragmento actual es FragmentMapa, mostrar el selectorPlaneta
                selectorPlaneta.visibility = ImageView.VISIBLE
                selectorCrear.visibility = ImageView.INVISIBLE
                selectorAjustes.visibility = ImageView.INVISIBLE
            }
            is FragmentAniadir -> {
                // Si el fragmento actual es FragmentAniadir, mostrar el selectorCrear
                selectorPlaneta.visibility = ImageView.INVISIBLE
                selectorCrear.visibility = ImageView.VISIBLE
                selectorAjustes.visibility = ImageView.INVISIBLE
            }
            is FragmentAjustes -> {
                // Si el fragmento actual es FragmentAjustes, mostrar el selectorAjustes
                selectorPlaneta.visibility = ImageView.INVISIBLE
                selectorCrear.visibility = ImageView.INVISIBLE
                selectorAjustes.visibility = ImageView.VISIBLE
            }
            else -> {
                // Si el fragmento no es ninguno de los anteriores, ocultar todos los selectores
                selectorPlaneta.visibility = ImageView.INVISIBLE
                selectorCrear.visibility = ImageView.INVISIBLE
                selectorAjustes.visibility = ImageView.INVISIBLE
            }
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