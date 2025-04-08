package com.example.gonow.vista

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gonow.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FragmentAjustes : Fragment(R.layout.fragment_ajustes) {
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
        val botonBorrarCuenta = view.findViewById<Button>(R.id.buttonBorrarCuenta)
        val botonCerrarSesion = view.findViewById<Button>(R.id.buttonCerrarSesion)
        val botonCambiarCorreo = view.findViewById<Button>(R.id.buttonCambiarCorreo)
        val botonCambiarContrasena = view.findViewById<Button>(R.id.buttonCambiarContraseña)
        val botonAyuda = view.findViewById<Button>(R.id.buttonAyuda)


        val touchListener = View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.primaryVariant)
                )
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.primary)
                )
            }
            false
        }

        listOf(
            botonBorrarCuenta,
            botonCerrarSesion,
            botonCambiarCorreo,
            botonCambiarContrasena,
            botonAyuda
        ).forEach { it.setOnTouchListener(touchListener) }

        botonBorrarCuenta.setOnClickListener {
            val mensaje = "¿Seguro que quieres BORRAR LA CUENTA?"
            val popup = popUp.newInstance(mensaje)
            popup.show(parentFragmentManager, "popUp")
        }

        botonCerrarSesion.setOnClickListener {
            val mensaje = "¿Seguro que quieres cerrar sesión?"
            val popup = popUp.newInstance(mensaje)
            popup.setOnAcceptListener { isConfirmed ->
                if (isConfirmed) {

                    FirebaseAuth.getInstance().signOut()

                    FirebaseDatabase.getInstance().goOffline()

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)


                    // Cerrar todas las actividades y evitar que el usuario regrese a la actividad anterior
                    requireActivity().finishAffinity()
                }
            }
            popup.show(parentFragmentManager, "popUp")


        }

        botonCambiarCorreo.setOnClickListener {
            //cargamos el popup seleccionando nuestra interfaz
            val fragmento = fragmentPopUpCambiarCorreo()
            val popup = popUpContenidoGeneral.newInstance(fragmento)
            popup.show(parentFragmentManager, "popUp")
        }

        botonCambiarContrasena.setOnClickListener {
            //cargamos el popup seleccionando nuestra interfaz
            val fragmento = fragmentPopUpCambiarContraseña()
            val popup = popUpContenidoGeneral.newInstance(fragmento)
            popup.show(parentFragmentManager, "popUp")
        }




    }
}