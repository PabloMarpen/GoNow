package com.example.gonow.vista

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.example.gonow.tfg.R

class FragmentPopUpHorario : Fragment(R.layout.fragment_pop_up_horario){


    private var switchAbiertoDato : Boolean? = null
    private var switchCerradoDato : Boolean? = null
    private var switchTieneHorarioDato : Boolean? = null
    private var horaAperturaDato : String? = null
    private var horaCierreDato : String? = null


    companion object {
        // Constantes para las claves (mejora el mantenimiento)
        private const val KEY_SWITCH_ABIERTO = "switchAbiertoDato"
        private const val KEY_SWITCH_CERRADO = "switchCerradoDato"
        private const val KEY_SWITCH_TIENE_HORARIO = "switchTieneHorarioDato"
        private const val KEY_HORA_APERTURA = "horaAperturaDato"
        private const val KEY_HORA_CIERRE = "horaCierreDato"

        fun nuevaInstancia(
            switchAbierto: Boolean,
            switchCerrado: Boolean,
            switchTieneHorario: Boolean,
            horaApertura: String?,
            horaCierre: String?
        ): FragmentPopUpHorario {
            return FragmentPopUpHorario().apply {
                arguments = Bundle().apply {
                    putBoolean(KEY_SWITCH_ABIERTO, switchAbierto)
                    putBoolean(KEY_SWITCH_CERRADO, switchCerrado)
                    putBoolean(KEY_SWITCH_TIENE_HORARIO, switchTieneHorario)
                    putString(KEY_HORA_APERTURA, horaApertura)
                    putString(KEY_HORA_CIERRE, horaCierre)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            switchAbiertoDato = it.getBoolean(KEY_SWITCH_ABIERTO, false)
            switchCerradoDato = it.getBoolean(KEY_SWITCH_CERRADO, false)
            switchTieneHorarioDato = it.getBoolean(KEY_SWITCH_TIENE_HORARIO, false)
            horaAperturaDato = it.getString(KEY_HORA_APERTURA)
            horaCierreDato = it.getString(KEY_HORA_CIERRE)
        } ?: run {
            // Manejar caso donde arguments es null
            requireActivity().finish() // o mostrar error
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonGuardar = view.findViewById<Button>(R.id.buttonGuardar)
        val botonHorarioAbrir = view.findViewById<Button>(R.id.botonHorarioAbrir)
        val botonHorarioCerrar = view.findViewById<Button>(R.id.botonHorarioCerrar)
        val switchAbierto = view.findViewById<Switch>(R.id.switchAbierto)
        val switchCerrado = view.findViewById<Switch>(R.id.switchCerrado)
        val switchTieneHorario = view.findViewById<Switch>(R.id.switchTieneHorario)

        botonHorarioAbrir.text = horaAperturaDato
        botonHorarioCerrar.text = horaCierreDato
        switchAbierto.isChecked = switchAbiertoDato ?: false
        switchCerrado.isChecked = switchCerradoDato ?: false
        switchTieneHorario.isChecked = switchTieneHorarioDato ?: false


        cambiarEstadoBotones(botonHorarioAbrir, switchTieneHorario)
        cambiarEstadoBotones(botonHorarioCerrar, switchTieneHorario)


        switchAbierto.setOnClickListener{
            switchCerrado.isChecked = false
            switchTieneHorario.isChecked = false
            cambiarEstadoBotones(botonHorarioAbrir, switchTieneHorario)
            cambiarEstadoBotones(botonHorarioCerrar, switchTieneHorario)

        }
        switchCerrado.setOnClickListener{
            switchAbierto.isChecked = false
            switchTieneHorario.isChecked = false
            cambiarEstadoBotones(botonHorarioAbrir, switchTieneHorario)
            cambiarEstadoBotones(botonHorarioCerrar, switchTieneHorario)
        }
        switchTieneHorario.setOnClickListener{
            switchAbierto.isChecked = false
            switchCerrado.isChecked = false
            cambiarEstadoBotones(botonHorarioAbrir, switchTieneHorario)
            cambiarEstadoBotones(botonHorarioCerrar, switchTieneHorario)
        }


        buttonGuardar.setOnClickListener {
            if(!switchAbierto.isChecked && !switchCerrado.isChecked && !switchTieneHorario.isChecked){
                Toast.makeText(requireContext(), getString(R.string.seleccionaunaalmenos), Toast.LENGTH_SHORT).show()
            }else {
                // enviar el horario para manejar los datos en el otro fragment
                val result = Bundle().apply {
                    if(switchTieneHorario.isChecked){
                        putString("hora_apertura", botonHorarioAbrir.text.toString())
                        putString("hora_cierre", botonHorarioCerrar.text.toString())

                    }else{
                        putString("hora_apertura", null)
                        putString("hora_cierre", null)
                    }


                    putBoolean("abiertoSiempre", switchAbierto.isChecked)
                    putBoolean("cerradoSiempre", switchCerrado.isChecked)
                    putBoolean("tieneHorario", switchTieneHorario.isChecked)
                }

                requireActivity().supportFragmentManager.setFragmentResult("horario", result)


                (requireParentFragment() as? DialogFragment)?.dismiss()
            }
        }

        botonHorarioAbrir.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                botonHorarioAbrir.text = SimpleDateFormat("HH:mm").format(cal.time)


            }
            TimePickerDialog(requireContext(), timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()

        }
        botonHorarioCerrar.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)


                botonHorarioCerrar.text = SimpleDateFormat("HH:mm").format(cal.time)

            }
            TimePickerDialog(requireContext(), timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()

        }

    }

    fun cambiarEstadoBotones(boton: Button, switch: Switch){
        if(switch.isChecked){
            boton.isEnabled = true
            boton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
        }else{
            boton.isEnabled = false
            boton.text = "00:00"
            boton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.supportVariant))
        }
    }


}