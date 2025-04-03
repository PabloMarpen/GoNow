package com.example.gonow.vista

import android.annotation.SuppressLint
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


    }
}