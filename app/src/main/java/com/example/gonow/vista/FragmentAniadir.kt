package com.example.gonow.vista

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gonow.R

class FragmentAniadir : Fragment(R.layout.fragment_aniadir){
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val botonPublicar = view.findViewById<Button>(R.id.botonPublicar)
        val botonTipoUbicacion = view.findViewById<Button>(R.id.botonUbicacion)
        val botonAñadirHorario = view.findViewById<Button>(R.id.botonHorario)
        val botonAñadirImagen = view.findViewById<ImageView>(R.id.imageViewAñadirImagen)


        botonPublicar.setOnTouchListener { v, event ->
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

        botonTipoUbicacion.setOnClickListener{

            val bottomSheet = BottomSheet()

            // Mostrar el Bottom Sheet
            bottomSheet.show(parentFragmentManager, BottomSheet.TAG)
        }

        botonAñadirHorario.setOnClickListener{
            //cargamos el popup seleccionando nuestra interfaz
            val fragmento = FragmentPopUpHorario()
            val popup = popUpContenidoGeneral.newInstance(fragmento)
            popup.show(parentFragmentManager, "popUp")
        }






    }
}