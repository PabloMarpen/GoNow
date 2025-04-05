package com.example.gonow.vista

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.gonow.R

class FragmentPopUpHorario : Fragment(R.layout.fragment_pop_up_horario){

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonGuardar = view.findViewById<Button>(R.id.buttonGuardar)
        val botonHorarioAbrir = view.findViewById<Button>(R.id.botonHorarioAbrir)
        val botonHorarioCerrar = view.findViewById<Button>(R.id.botonHorarioCerrar)
        buttonGuardar.setOnClickListener {
            // Cerrar el fragmento actual
            (requireParentFragment() as? DialogFragment)?.dismiss()
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

}