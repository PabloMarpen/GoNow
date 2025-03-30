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

class FragmentRegistro : Fragment(R.layout.fragment_registro){

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val botonRegistrar = view.findViewById<Button>(R.id.buttonRegistrarme)

        botonRegistrar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primaryVariant))
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.primary))
            }
            false
        }

        botonRegistrar.setOnClickListener {
            Toast.makeText(requireContext(), "Registrandose...", Toast.LENGTH_SHORT).show()
        }
    }
}