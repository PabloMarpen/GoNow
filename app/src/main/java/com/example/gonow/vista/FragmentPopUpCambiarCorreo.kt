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

        val botonCancelar = view.findViewById<Button>(R.id.buttonCancelar)
        val botonAceptar = view.findViewById<Button>(R.id.buttonAceptar)
        val textoCorreo = view.findViewById<EditText>(R.id.CambiarCorreo)

        val auth = AuthSingleton.auth
        val user = auth.currentUser

        botonAceptar.setOnClickListener {
            val nuevoCorreo = textoCorreo.text.toString().trim()

            if (nuevoCorreo.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(nuevoCorreo).matches()) {
                context?.let {
                    Toast.makeText(it, "Introduce un correo válido.", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            user?.updateEmail(nuevoCorreo)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    context?.let {
                        Toast.makeText(it, "Correo actualizado correctamente.", Toast.LENGTH_SHORT).show()
                    }
                    (requireParentFragment() as? DialogFragment)?.dismiss()
                } else {
                    context?.let {
                        Toast.makeText(it, "Error al cambiar el correo.", Toast.LENGTH_SHORT).show()
                    }

                    // Si el error es de reautenticación, puedes manejarlo así:
                    val exception = task.exception
                    if (exception is FirebaseAuthRecentLoginRequiredException) {
                        // Aquí podrías pedir al usuario que inicie sesión de nuevo.
                        context?.let {
                            Toast.makeText(it, "Por seguridad, vuelve a iniciar sesión.", Toast.LENGTH_LONG).show()
                        }
                    } else {

                    }
                }
            }
        }

        botonCancelar.setOnClickListener {
            (requireParentFragment() as? DialogFragment)?.dismiss()
        }
    }
}
