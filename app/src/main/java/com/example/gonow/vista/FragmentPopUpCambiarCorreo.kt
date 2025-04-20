package com.example.gonow.vista

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.gonow.data.AuthSingleton
import com.example.gonow.tfg.R
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException

class FragmentPopUpCambiarCorreo : Fragment(R.layout.fragment_pop_up_cambiar_correo) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val botonCancelar = view.findViewById<Button>(R.id.ButtonCancelar)
        val botonAceptar = view.findViewById<Button>(R.id.ButtonAceptar)
        val textoCorreo = view.findViewById<EditText>(R.id.CambiarCorreo)

        val auth = AuthSingleton.auth
        val user = auth.currentUser

        botonAceptar.setOnClickListener {

        }

        botonCancelar.setOnClickListener {
            (requireParentFragment() as? DialogFragment)?.dismiss()
        }
    }
}
