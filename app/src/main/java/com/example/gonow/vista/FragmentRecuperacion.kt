package com.example.gonow.vista

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.contentValuesOf
import androidx.fragment.app.Fragment
import com.example.gonow.R

class FragmentRecuperacion : Fragment(R.layout.fragmet_recuperarcorreo){

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
        val botonEnviar = view.findViewById<Button>(R.id.buttonEnviar)
        val botonVolver = view.findViewById<Button>(R.id.buttonVolver)
        val correoRecu = view.findViewById<EditText>(R.id.Correo)

        botonEnviar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primaryVariant))
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.primary))
            }
            false
        }

        botonVolver.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondaryVariant))
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            }
            false
        }


        botonEnviar.isEnabled = false
        botonEnviar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.supportVariant))

        correoRecu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    botonEnviar.isEnabled = false
                    botonEnviar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.supportVariant))
                } else {
                    botonEnviar.isEnabled = true
                    botonEnviar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })




        botonEnviar.setOnClickListener {
            if(!correoRecu.text.toString().isValidEmail()){
                Toast.makeText(requireContext(), "Correo no válido", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(), "Enviar", Toast.LENGTH_SHORT).show()

                val mensaje = "Se enviará un correo para restablecer la contraseña a ${correoRecu.text}"
                val popup = PopupDialog.newInstance(mensaje)
                popup.show(parentFragmentManager, "PopupDialog")
            }
        }

        botonVolver.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmentIniciar())
                .addToBackStack(null)
                .commit()

        }

    }
}