package com.example.gonow.vista

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gonow.tfg.R

class FragmentAjustesAnonimo : Fragment(R.layout.fragment_ajustes_anonimo){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val botonRegistro = view.findViewById<Button>(R.id.buttonRegistroAnonimo)
        val botonIniciarSesion = view.findViewById<Button>(R.id.buttonIniciarSesionAnonimo)
        val botonAyuda = view.findViewById<Button>(R.id.buttonAyudaAnonimo)

        botonRegistro.setOnClickListener {
            val intent = Intent(requireContext(), IniciarActivity::class.java)
            intent.putExtra("abrirRegistro", true) // Pasar una señal para abrir el fragmento
            startActivity(intent)


        }

        botonIniciarSesion.setOnClickListener {
            val intent = Intent(requireContext(), IniciarActivity::class.java)
            intent.putExtra("abrirCuenta", true) // Pasar una señal para abrir el fragmento
            startActivity(intent)
        }

        botonAyuda.setOnClickListener {
            Toast.makeText(requireContext(), "Esta función no está disponible", Toast.LENGTH_SHORT).show()
        }

    }
}