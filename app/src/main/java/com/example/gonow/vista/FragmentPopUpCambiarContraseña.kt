package com.example.gonow.vista

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.gonow.data.AuthSingleton
import com.example.gonow.tfg.R

class FragmentPopUpCambiarContraseña : Fragment(R.layout.fragment_pop_up_cambiar_contrasenia){

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val botonCancelar = view.findViewById<Button>(R.id.ButtonCancelar)
        val botonAceptar = view.findViewById<Button>(R.id.ButtonAceptar)
        val textoContraAntigua = view.findViewById<EditText>(R.id.ContraseñaAntigua)
        val textoContraNueva = view.findViewById<EditText>(R.id.ContraseñaNueva)
        val auth = AuthSingleton.auth
        val user = auth.currentUser

        botonAceptar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.primaryVariant
                    )
                )

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.primary)
                )
            }
            false
        }

        botonAceptar.setOnClickListener {

            val nuevaContrasena = textoContraNueva.text.toString()
            if (nuevaContrasena.length >= 6) {
                user?.updatePassword(nuevaContrasena)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), getString(R.string.contraseñaactualizada), Toast.LENGTH_SHORT).show()
                        // Cerrar el fragmento actual
                        if (isAdded) {
                            (requireParentFragment() as? DialogFragment)?.dismiss()
                        }
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.error_revisar_contrasena), Toast.LENGTH_SHORT).show()

                    }
                }
            } else {
                    Toast.makeText(requireContext(), getString(R.string.error_contrasena_corta), Toast.LENGTH_SHORT).show()
            }



        }

        botonCancelar.setOnClickListener {
            // Cerrar el fragmento actual
            if (isAdded) {
                (requireParentFragment() as? DialogFragment)?.dismiss()
            }
        }

    }
}