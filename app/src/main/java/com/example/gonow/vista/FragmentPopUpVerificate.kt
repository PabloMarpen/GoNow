package com.example.gonow.vista

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.gonow.tfg.R

class FragmentPopUpVerificate : Fragment(R.layout.fragment_pop_up_correoverificacion){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val botonIniciarSesion = view.findViewById<Button>(R.id.buttonIniciarSesionVerificacion)

        botonIniciarSesion.setOnClickListener {
            val intent = Intent(requireContext(), IniciarActivity::class.java)
            intent.putExtra("abrirCuenta", true) // Pasar una señal para abrir el fragmento
            startActivity(intent)


        }

    }
}