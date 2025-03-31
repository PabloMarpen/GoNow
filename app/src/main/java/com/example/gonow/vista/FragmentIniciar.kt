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

class FragmentIniciar : Fragment(R.layout.fragment_login) {

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
        val botonIniciar = view.findViewById<Button>(R.id.buttonIniciarSesion)
        val textoOlvidoContrasena = view.findViewById<TextView>(R.id.TextOlvidoContrasena)
        val textoRegistrarme = view.findViewById<TextView>(R.id.textViewRegistrarme)
        val correo = view.findViewById<EditText>(R.id.Correo)
        val contraseña = view.findViewById<EditText>(R.id.Contraseña)

        botonIniciar.setOnTouchListener { v, event ->
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

        botonIniciar.setOnClickListener {
            if (!correo.text.toString().isValidEmail()) {
                Toast.makeText(requireContext(), "Correo no válido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Iniciando sesión...", Toast.LENGTH_SHORT).show()
            }
        }

        textoOlvidoContrasena.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmentRecuperacion())
                .addToBackStack(null)
                .commit()
        }
        textoRegistrarme.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmentRegistro())
                .addToBackStack(null)
                .commit()
        }
    }
}