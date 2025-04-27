package com.example.gonow.vista

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.gonow.tfg.R
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Locale

class FragmentPopUpHorario : Fragment(R.layout.fragment_pop_up_horario){


    private var switchAbiertoDato : Boolean? = null
    private var switchCerradoDato : Boolean? = null
    private var switchTieneHorarioDato : Boolean? = null
    private var horaAperturaDato : String? = null
    private var horaCierreDato : String? = null


    companion object {
        // Constantes para las claves (mejora el mantenimiento)
        private const val KEY_SWITCH_ABIERTO = "switchAbiertoDato"
        private const val KEY_SWITCH_NOSEELHORARIO = "switchCerradoDato"
        private const val KEY_SWITCH_TIENE_HORARIO = "switchTieneHorarioDato"
        private const val KEY_HORA_APERTURA = "horaAperturaDato"
        private const val KEY_HORA_CIERRE = "horaCierreDato"

        fun nuevaInstancia(
            switchAbierto: Boolean,
            noSeElHorario: Boolean,
            switchTieneHorario: Boolean,
            horaApertura: String?,
            horaCierre: String?
        ): FragmentPopUpHorario {
            return FragmentPopUpHorario().apply {
                arguments = Bundle().apply {
                    putBoolean(KEY_SWITCH_ABIERTO, switchAbierto)
                    putBoolean(KEY_SWITCH_NOSEELHORARIO, noSeElHorario)
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
            switchCerradoDato = it.getBoolean(KEY_SWITCH_NOSEELHORARIO, false)
            switchTieneHorarioDato = it.getBoolean(KEY_SWITCH_TIENE_HORARIO, false)
            horaAperturaDato = it.getString(KEY_HORA_APERTURA)
            horaCierreDato = it.getString(KEY_HORA_CIERRE)
        } ?: run {
            // Manejar caso donde arguments es null
            requireActivity().finish() // o mostrar error
        }
    }

    @SuppressLint("SimpleDateFormat", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonGuardar = view.findViewById<Button>(R.id.buttonGuardar)
        val botonHorarioAbrir = view.findViewById<Button>(R.id.botonHorarioAbrir)
        val botonHorarioCerrar = view.findViewById<Button>(R.id.botonHorarioCerrar)
        val switchAbierto = view.findViewById<Switch>(R.id.switchAbierto)
        val switchNoSeElHorario = view.findViewById<Switch>(R.id.switchCerrado)
        val switchTieneHorario = view.findViewById<Switch>(R.id.switchTieneHorario)

        botonHorarioAbrir.text = horaAperturaDato
        botonHorarioCerrar.text = horaCierreDato
        switchAbierto.isChecked = switchAbiertoDato ?: false
        switchNoSeElHorario.isChecked = switchCerradoDato ?: false
        switchTieneHorario.isChecked = switchTieneHorarioDato ?: false


        cambiarEstadoBotones(botonHorarioAbrir, switchTieneHorario)
        cambiarEstadoBotones(botonHorarioCerrar, switchTieneHorario)


        switchAbierto.setOnClickListener{
            switchNoSeElHorario.isChecked = false
            switchTieneHorario.isChecked = false
            cambiarEstadoBotones(botonHorarioAbrir, switchTieneHorario)
            cambiarEstadoBotones(botonHorarioCerrar, switchTieneHorario)

        }
        switchNoSeElHorario.setOnClickListener{
            switchAbierto.isChecked = false
            switchTieneHorario.isChecked = false
            cambiarEstadoBotones(botonHorarioAbrir, switchTieneHorario)
            cambiarEstadoBotones(botonHorarioCerrar, switchTieneHorario)
        }
        switchTieneHorario.setOnClickListener{
            switchAbierto.isChecked = false
            switchNoSeElHorario.isChecked = false
            cambiarEstadoBotones(botonHorarioAbrir, switchTieneHorario)
            cambiarEstadoBotones(botonHorarioCerrar, switchTieneHorario)
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
            if(!switchAbierto.isChecked && !switchNoSeElHorario.isChecked && !switchTieneHorario.isChecked){
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
                    putBoolean("noSeElHorario", switchNoSeElHorario.isChecked)
                    putBoolean("tieneHorario", switchTieneHorario.isChecked)
                }

                requireActivity().supportFragmentManager.setFragmentResult("horario", result)


                (requireParentFragment() as? DialogFragment)?.dismiss()
            }
        }

        botonHorarioAbrir.setOnClickListener {
            val cal = Calendar.getInstance()

            if (botonHorarioAbrir.text.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val date = sdf.parse(botonHorarioAbrir.text.toString())
                    cal.time = date!!
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(cal.get(Calendar.HOUR_OF_DAY))
                .setMinute(cal.get(Calendar.MINUTE))
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build()

            picker.show(parentFragmentManager, "timePicker")

            picker.addOnPositiveButtonClickListener {
                val hour = picker.hour
                val minute = picker.minute

                val calResult = Calendar.getInstance()
                calResult.set(Calendar.HOUR_OF_DAY, hour)
                calResult.set(Calendar.MINUTE, minute)

                botonHorarioAbrir.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calResult.time)
            }
        }


        botonHorarioCerrar.setOnClickListener {
            val cal = Calendar.getInstance()

            if (botonHorarioCerrar.text.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val date = sdf.parse(botonHorarioCerrar.text.toString())
                    cal.time = date!!
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(cal.get(Calendar.HOUR_OF_DAY))
                .setMinute(cal.get(Calendar.MINUTE))
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build()

            picker.show(parentFragmentManager, "timePicker")

            picker.addOnPositiveButtonClickListener {
                val hour = picker.hour
                val minute = picker.minute

                val calResult = Calendar.getInstance()
                calResult.set(Calendar.HOUR_OF_DAY, hour)
                calResult.set(Calendar.MINUTE, minute)

                botonHorarioCerrar.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calResult.time)
            }
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