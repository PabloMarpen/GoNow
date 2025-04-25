package com.example.gonow.vista

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.gonow.tfg.R

class FragmetnPopUpCreaBanios : Fragment(R.layout.fragment_pop_up_crea_banios_spam){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val botonVolver = view.findViewById<Button>(R.id.buttonVolver)

        botonVolver.setOnClickListener {
            (requireParentFragment() as? DialogFragment)?.dismiss()
        }

    }
}