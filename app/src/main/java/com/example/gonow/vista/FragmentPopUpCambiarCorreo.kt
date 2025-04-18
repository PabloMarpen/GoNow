package com.example.gonow.vista

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.gonow.tfg.R

class FragmentPopUpCambiarCorreo : Fragment(R.layout.fragment_pop_up_cambiar_correo){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val botonCancelar = view.findViewById<Button>(R.id.buttonCancelar)
        val botonAceptar = view.findViewById<Button>(R.id.buttonAceptar)

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