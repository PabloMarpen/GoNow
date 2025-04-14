package com.example.gonow.vista

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gonow.R
import com.google.android.gms.maps.model.LatLng
import java.text.DecimalFormat
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt


class fragmentResumenBanio : Fragment(R.layout.fragment_resumen_banio) {

    companion object {
        private const val ARG_NOMBRE = "nombre"
        private const val ARG_DESCRIPCION = "descripcion"
        private const val ARG_HORARIO = "horario"
        private const val ARG_PUNTUACION = "puntuacion"
        private const val ARG_SINHORARIO = "sinhorario"
        private const val ARG_ETIQUETAS = "etiquetas"
        private const val ARG_CORDENADAS = "cordenadasbanio"
        private const val ARG_UBICACION_USUARIO = "ubicacionUsuario"

        fun newInstance(nombre: String, descripcion: String, horario: Map<String, String?>?, puntuacion: Double, sinhorario: String?, etiquetas: List<String>, cordenadasbanio: LatLng, ubicacionUsuario: LatLng): fragmentResumenBanio {
            val fragment = fragmentResumenBanio()
            val args = Bundle()
            args.putString(ARG_NOMBRE, nombre)
            args.putString(ARG_DESCRIPCION, descripcion)
            args.putString(ARG_HORARIO, horario?.toString() ?: "")
            args.putDouble(ARG_PUNTUACION, puntuacion)
            args.putString(ARG_SINHORARIO, sinhorario)
            args.putStringArrayList(ARG_ETIQUETAS, ArrayList(etiquetas))
            args.putParcelable(ARG_CORDENADAS, cordenadasbanio)
            args.putParcelable(ARG_UBICACION_USUARIO, ubicacionUsuario)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textViewNombre = view.findViewById<TextView>(R.id.textViewNombre)
        val textViewDescripcion = view.findViewById<TextView>(R.id.textViewTextoDescripcion)
        val textViewHorario = view.findViewById<TextView>(R.id.textViewTextoHorario)
        val Puntuacion = view.findViewById<RatingBar>(R.id.ratingBar)
        val textViewEtiquetas = view.findViewById<TextView>(R.id.textViewTextoEtiquetas)
        val textViewDistancia = view.findViewById<TextView>(R.id.textViewDistanciaNum)

        textViewNombre.text = arguments?.getString(ARG_NOMBRE) ?: "Nombre no disponible"
        textViewDescripcion.text = arguments?.getString(ARG_DESCRIPCION) ?: "Descripción no disponible"
        if(arguments?.getString(ARG_HORARIO) == "{cierre=null, apertura=null}"){
            textViewHorario.text = arguments?.getString(ARG_SINHORARIO) ?: ""
        }else{
            textViewHorario.text = arguments?.getString(ARG_HORARIO) ?: ""
        }
        Puntuacion.rating = (arguments?.getDouble(ARG_PUNTUACION) ?: 0.0).toFloat()
        textViewEtiquetas.text = arguments?.getStringArrayList(ARG_ETIQUETAS)?.joinToString(", ") ?: "Etiquetas no disponibles"
        textViewDistancia.text = calculateAndFormatDistance(arguments?.getParcelable(ARG_CORDENADAS) ?: LatLng(0.0,0.0), arguments?.getParcelable(ARG_UBICACION_USUARIO) ?: LatLng(0.0,0.0)).toString()



    }

    fun calculateAndFormatDistance(startPoint: LatLng, endPoint: LatLng): String {
        val earthRadius = 6371 // Radio de la Tierra en km

        val lat1 = Math.toRadians(startPoint.latitude)
        val lat2 = Math.toRadians(endPoint.latitude)
        val lon1 = Math.toRadians(startPoint.longitude)
        val lon2 = Math.toRadians(endPoint.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanceKm = earthRadius * c

        return when {
            distanceKm < 0.1 -> {
                // Menos de 100m -> mostrar en metros
                val meters = (distanceKm * 1000).roundToInt()
                "$meters m"
            }
            distanceKm < 10 -> {
                // Entre 100m y 10km -> mostrar 1 decimal
                "%.1f km".format(distanceKm)
            }
            else -> {
                // Más de 10km -> mostrar sin decimales
                "${distanceKm.roundToInt()} km"
            }
        }
    }
}