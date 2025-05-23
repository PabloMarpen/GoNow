package com.example.gonow.vista

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.gonow.tfg.R


class FragmentPopUpTipoUbicacion : Fragment(R.layout.fragment_pop_up_tipo_ubicacion){

    private var tipoSeleccionado : String? = null

    companion object {
        private const val KEY_TIPO_SELECCIONADO = "tipoSeleccionado"

        fun nuevaInstancia(
            tipoSeleccionado: String?
        ): FragmentPopUpTipoUbicacion {
            return FragmentPopUpTipoUbicacion().apply {
                arguments = Bundle().apply {
                    putString(KEY_TIPO_SELECCIONADO, tipoSeleccionado)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tipoSeleccionado = arguments?.getString(KEY_TIPO_SELECCIONADO)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val radioButtonBañoPublico = view.findViewById<RadioButton>(R.id.radioButtonBanioPublico)
        val radioButtonBar = view.findViewById<RadioButton>(R.id.radioButtonBar)
        val radioButtonTienda = view.findViewById<RadioButton>(R.id.radioButtonTienda)
        val radioButtonRestaurante = view.findViewById<RadioButton>(R.id.radioButtonRestaurante)
        val radioButtonGasolinera = view.findViewById<RadioButton>(R.id.radioButtonGasolinera)
        val radioButtonOtro = view.findViewById<RadioButton>(R.id.radioButtonOtro)
        val buttonGuardar = view.findViewById<Button>(R.id.buttonGuardar)

        if(tipoSeleccionado == "01"){
            radioButtonBañoPublico.isChecked = true
        }
        if(tipoSeleccionado == "02"){
            radioButtonBar.isChecked = true
        }
        if(tipoSeleccionado == "03") {
            radioButtonTienda.isChecked = true
        }
        if(tipoSeleccionado == "04"){
            radioButtonRestaurante.isChecked = true
        }
        if(tipoSeleccionado == "05") {
            radioButtonGasolinera.isChecked = true
        }
        if(tipoSeleccionado == "06"){
            radioButtonOtro.isChecked = true
        }

        radioButtonBañoPublico.setOnClickListener{
            tipoSeleccionado = "01" //baño publico
        }
        radioButtonBar.setOnClickListener{
            tipoSeleccionado = "02" //Bar
        }
        radioButtonTienda.setOnClickListener{
            tipoSeleccionado = "03" //Tienda
        }
        radioButtonRestaurante.setOnClickListener{
            tipoSeleccionado = "04" //Restaurante
        }
        radioButtonGasolinera.setOnClickListener{
            tipoSeleccionado = "05" // Gasolinera
        }
        radioButtonOtro.setOnClickListener{
            tipoSeleccionado = "06" // Otro
        }

        buttonGuardar.setOnTouchListener { v, event ->
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

        buttonGuardar.setOnClickListener {
                // enviar el horario para manejar los datos en el otro fragment
                val result = Bundle().apply {
                    putString("tipo_ubicacion", tipoSeleccionado)

                }

                requireActivity().supportFragmentManager.setFragmentResult("ubicacion", result)

                (requireParentFragment() as? DialogFragment)?.dismiss()

        }


    }



}