package com.example.gonow.vista

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.gonow.tfg.R

class FragmentPopUpSpam : Fragment(R.layout.fragment_pop_up_spam){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val botonRegistro = view.findViewById<Button>(R.id.buttonRegistrarme)

        botonRegistro.setOnClickListener {
            val intent = Intent(requireContext(), IniciarActivity::class.java)
            intent.putExtra("abrirRegistro", true) // Pasar una señal para abrir el fragmento
            startActivity(intent)


        }

    }
}