package com.example.gonow.vista

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.gonow.tfg.R

class FragmentPopUpFiltro : Fragment(R.layout.fragment_pop_up_filtros){



    private var switchUnisexDato : Boolean? = null
    private var switchDiscapacitadosDato : Boolean? = null
    private var switchGratisDato : Boolean? = null
    private var tipoUbiSeleccionado : String? = null
    private var estrellasDato : Float? = null


    companion object {
        // Constantes para las claves (mejora el mantenimiento)
        private const val KEY_SWITCH_UNISEX = "switchUnisexDato"
        private const val KEY_SWITCH_DISCAPACITADOS = "switchDiscapacitadosDato"
        private const val KEY_SWITCH_GRATIS = "switchGratisDato"
        private const val KEY_TIPOUBI = "tipoUbiSeleccionado"
        private const val KEY_PUTUACION = "estrellasDato"

        fun newInstance(
            switchUnisexDato: Boolean,
            switchDiscapacitadosDato: Boolean,
            switchGratisDato: Boolean,
            tipoUbiSeleccionado: String,
            estrellasDato: Float
        ): FragmentPopUpFiltro {
            return FragmentPopUpFiltro().apply {
                arguments = Bundle().apply {
                    putBoolean(KEY_SWITCH_UNISEX, switchUnisexDato)
                    putBoolean(KEY_SWITCH_DISCAPACITADOS, switchDiscapacitadosDato)
                    putBoolean(KEY_SWITCH_GRATIS, switchGratisDato)
                    putString(KEY_TIPOUBI, tipoUbiSeleccionado)
                    putFloat(KEY_PUTUACION, estrellasDato)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().supportFragmentManager.setFragmentResultListener("ubicacion", this) { _, bundle ->
            val tipoUbiSeleccionado = bundle.getString("tipo_ubicacion")

            this.tipoUbiSeleccionado = tipoUbiSeleccionado
        }

        arguments?.let {
            switchUnisexDato = it.getBoolean(KEY_SWITCH_UNISEX, false)
            switchDiscapacitadosDato = it.getBoolean(KEY_SWITCH_DISCAPACITADOS, false)
            switchGratisDato = it.getBoolean(KEY_SWITCH_GRATIS, false)
            tipoUbiSeleccionado = it.getString(KEY_TIPOUBI)
            estrellasDato = if (it.containsKey(KEY_PUTUACION)) it.getFloat(KEY_PUTUACION) else null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val botonUbicacion = view.findViewById<Button>(R.id.botonUbicacion)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val switchDiscapacitados = view.findViewById<Switch>(R.id.switchDiscapacitados)
        val switchUnisex = view.findViewById<Switch>(R.id.switchUnisex)
        val switchGratis = view.findViewById<Switch>(R.id.switchGratis)
        val buttonGuardarFiltros = view.findViewById<Button>(R.id.buttonGuardarFiltros)
        val buttonBorrarFiltros = view.findViewById<Button>(R.id.buttonBorrarFiltros)


        botonUbicacion.setOnClickListener {
            // cargamos el popup seleccionando nuestra interfaz y pasamos los datos
            val tipoUbicacionFragmento = FragmentPopUpTipoUbicacion.nuevaInstancia(
                tipoSeleccionado = tipoUbiSeleccionado
            )
            PopUpContenidoGeneral.newInstance(tipoUbicacionFragmento).show(parentFragmentManager, "popUp")
        }

        buttonBorrarFiltros.setOnClickListener {
            ratingBar.rating = 0f
            switchDiscapacitados.isChecked = false
            switchUnisex.isChecked = false
            switchGratis.isChecked = false
            tipoUbiSeleccionado = null

        }


        buttonGuardarFiltros.setOnClickListener {
            val estrellas = if (ratingBar.rating == 0f) null else ratingBar.rating

            val discapacitados = if (switchDiscapacitados.isChecked) true else null
            val unisex = if (switchUnisex.isChecked) true else null
            val gratis = if (switchGratis.isChecked) true else null



            // enviar el horario para manejar los datos en el otro fragment
            val result = Bundle().apply {
                estrellas?.let { putFloat("estrellas", it) }
                discapacitados?.let { putBoolean("discapacitados", it) }
                unisex?.let { putBoolean("unisex", it) }
                gratis?.let { putBoolean("gratis", it) }
                tipoUbiSeleccionado?.let { putString("tipo_ubicacion", it) }
            }

            requireActivity().supportFragmentManager.setFragmentResult("filtros", result)

            (requireParentFragment() as? DialogFragment)?.dismiss()
        }

        ratingBar.rating = estrellasDato ?: 0f
        switchDiscapacitados.isChecked = switchDiscapacitadosDato ?: false
        switchUnisex.isChecked = switchUnisexDato ?: false
        switchGratis.isChecked = switchGratisDato ?: false

    }
}
