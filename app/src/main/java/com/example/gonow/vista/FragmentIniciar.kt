package com.example.gonow.vista

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gonow.R

class FragmentIniciar : Fragment(R.layout.fragment_login){

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val botonIniciar = view.findViewById<Button>(R.id.buttonIniciarSesion)

        botonIniciar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primaryVariant))
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.primary))
            }
            false
        }

        botonIniciar.setOnClickListener {
            Toast.makeText(requireContext(), "Iniciando sesión...", Toast.LENGTH_SHORT).show()
        }
    }
}