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

class FragmentRegistro : Fragment(R.layout.fragment_registro){

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
        val botonRegistrar = view.findViewById<Button>(R.id.buttonRegistrarme)
        val textoIniciarSesion = view.findViewById<TextView>(R.id.textViewIniciarSesion)
        val correo = view.findViewById<EditText>(R.id.Correo)
        val contraseña = view.findViewById<EditText>(R.id.Contraseña)
        val contraseña2 = view.findViewById<EditText>(R.id.contraseña)

        botonRegistrar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primaryVariant))
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.primary))
            }
            false
        }

        botonRegistrar.setOnClickListener {
            if(!correo.text.toString().isValidEmail()){
                Toast.makeText(requireContext(), "Correo no válido", Toast.LENGTH_SHORT).show()
            }else{
                if(contraseña.text.toString() != contraseña2.text.toString()){
                    Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(requireContext(), "Registrandose...", Toast.LENGTH_SHORT).show()
                }
            }
        }
        textoIniciarSesion.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmentIniciar())
                .addToBackStack(null)
                .commit()
    }
}
    }