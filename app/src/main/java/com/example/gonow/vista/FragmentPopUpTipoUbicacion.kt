package com.example.gonow.vista

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.gonow.tfg.R


class FragmentPopUpTipoUbicacion : Fragment(R.layout.fragment_pop_up_tipo_ubicacion){

    private var tipoSeleccionado : String? = null

    companion object {
        // Constantes para las claves (mejora el mantenimiento)
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val radioButtonBañoPublico = view.findViewById<RadioButton>(R.id.radioButtonBanioPublico)
        val radioButtonBar = view.findViewById<RadioButton>(R.id.radioButtonBar)
        val radioButtonTienda = view.findViewById<RadioButton>(R.id.radioButtonTienda)
        val radioButtonRestaurante = view.findViewById<RadioButton>(R.id.radioButtonRestaurante)
        val radioButtonGasolinera = view.findViewById<RadioButton>(R.id.radioButtonGasolinera)
        val radioButtonOtro = view.findViewById<RadioButton>(R.id.radioButtonOtro)
        val buttonGuardar = view.findViewById<Button>(R.id.buttonGuardar)

        if(tipoSeleccionado == "Baño publico"){
            radioButtonBañoPublico.isChecked = true
        }
        if(tipoSeleccionado == "Bar"){
            radioButtonBar.isChecked = true
        }
        if(tipoSeleccionado == "Tienda") {
            radioButtonTienda.isChecked = true
        }
        if(tipoSeleccionado == "Restaurante"){
            radioButtonRestaurante.isChecked = true
        }
        if(tipoSeleccionado == "Gasolinera") {
            radioButtonGasolinera.isChecked = true
        }
        if(tipoSeleccionado == "Otro"){
            radioButtonOtro.isChecked = true
        }

        radioButtonBañoPublico.setOnClickListener{
            tipoSeleccionado = radioButtonBañoPublico.text.toString()
        }
        radioButtonBar.setOnClickListener{
            tipoSeleccionado = radioButtonBar.text.toString()
        }
        radioButtonTienda.setOnClickListener{
            tipoSeleccionado = radioButtonTienda.text.toString()
        }
        radioButtonRestaurante.setOnClickListener{
            tipoSeleccionado = radioButtonRestaurante.text.toString()
        }
        radioButtonGasolinera.setOnClickListener{
            tipoSeleccionado = radioButtonGasolinera.text.toString()
        }
        radioButtonOtro.setOnClickListener{
            tipoSeleccionado = radioButtonOtro.text.toString()
        }
        buttonGuardar.setOnClickListener {
            if(tipoSeleccionado == null){
                Toast.makeText(requireContext(), "Selecciona al menos una opción", Toast.LENGTH_SHORT).show()
            }else {
                // enviar el horario para manejar los datos en el otro fragment
                val result = Bundle().apply {
                    putString("tipo_ubicacion", tipoSeleccionado)

                }

                requireActivity().supportFragmentManager.setFragmentResult("ubicacion", result)

                (requireParentFragment() as? DialogFragment)?.dismiss()
            }
        }


    }



}