package com.example.gonow.vista

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.gonow.R

class fragmentPopUpCambiarContraseña : Fragment(R.layout.fragment_pop_up_cambiar_contrasenia){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val botonCancelar = view.findViewById<Button>(R.id.ButtonCancelar)
        val botonAceptar = view.findViewById<Button>(R.id.ButtonAceptar)

        botonAceptar.setOnClickListener {
            // Cerrar el fragmento actual
            (requireParentFragment() as? DialogFragment)?.dismiss()
        }

        botonCancelar.setOnClickListener {
            // Cerrar el fragmento actual
            (requireParentFragment() as? DialogFragment)?.dismiss()
        }

    }
}