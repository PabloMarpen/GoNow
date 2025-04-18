package com.example.gonow.vista

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.gonow.tfg.R

class PopUpContenidoGeneral(private val fragmentoInterno: Fragment) : DialogFragment() {

    companion object {
        fun newInstance(fragmento: Fragment): PopUpContenidoGeneral {
            return PopUpContenidoGeneral(fragmento)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_pop_up_contenido_general, container, false)

        childFragmentManager.beginTransaction()
            .replace(R.id.contenedor_fragmento, fragmentoInterno)
            .commit()

        val botonCerrar = view.findViewById<ImageButton>(R.id.botonCerrar)
        botonCerrar.setOnClickListener {
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }
}

