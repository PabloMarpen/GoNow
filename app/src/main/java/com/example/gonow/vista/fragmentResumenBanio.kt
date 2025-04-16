package com.example.gonow.vista

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gonow.R
import com.example.gonow.data.AuthSingleton
import com.example.gonow.data.FirestoreSingleton
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
        private const val ARG_HORACIERRE = "horaCierre"
        private const val ARG_HORAPERTURA = "horaApertura"
        private const val ARG_PUNTUACION = "puntuacion"
        private const val ARG_SINHORARIO = "sinhorario"
        private const val ARG_ETIQUETAS = "etiquetas"
        private const val ARG_CORDENADAS = "cordenadasbanio"
        private const val ARG_UBICACION_USUARIO = "ubicacionUsuario"
        private const val ARG_IMAGEN = "imagen"
        private const val ARG_CREADOR = "creador"
        private const val ARG_ID_DOCUMENTO = "idDocumento"

        fun newInstance(nombre: String, descripcion: String, horario: Map<String, String?>?, puntuacion: Double, sinhorario: String?, etiquetas: List<String>, cordenadasbanio: LatLng, ubicacionUsuario: LatLng, imagen: String?, creador: String?, idDocumento: String?): fragmentResumenBanio {
            val fragment = fragmentResumenBanio()
            val args = Bundle()
            args.putString(ARG_NOMBRE, nombre)
            args.putString(ARG_DESCRIPCION, descripcion)
            val horaApertura = horario?.let {
                val Apertura = it["apertura"] ?: "No disponible"
                Apertura
            } ?: ""
            val horaCierre = horario?.let {
                val Cierre = it["cierre"] ?: "No disponible"
                Cierre
            } ?: ""

            args.putString(ARG_HORAPERTURA, horaApertura)
            args.putString(ARG_HORACIERRE, horaCierre)
            args.putDouble(ARG_PUNTUACION, puntuacion)
            args.putString(ARG_SINHORARIO, sinhorario)
            args.putStringArrayList(ARG_ETIQUETAS, ArrayList(etiquetas))
            args.putParcelable(ARG_CORDENADAS, cordenadasbanio)
            args.putParcelable(ARG_UBICACION_USUARIO, ubicacionUsuario)
            args.putString(ARG_IMAGEN, imagen)
            args.putString(ARG_CREADOR, creador)
            args.putString(ARG_ID_DOCUMENTO, idDocumento)
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
        val textViewDistancia = view.findViewById<TextView>(R.id.textViewDistanciaNum)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupEtiquetas)
        val etiquetas = arguments?.getStringArrayList(ARG_ETIQUETAS)
        val imageView = view.findViewById<ImageView>(R.id.imageViewAñadirImagenResu)
        val tarjetaEditar = view.findViewById<androidx.cardview.widget.CardView>(R.id.tarjeta)
        val botonBorrar = view.findViewById<ImageView>(R.id.imageBorrar)
        val botonEditar = view.findViewById<ImageView>(R.id.imageEditar)
        val botonLlegar = view.findViewById<Button>(R.id.botonLlegar)

        if(arguments?.getString(ARG_CREADOR) == AuthSingleton.auth.currentUser?.uid){
            tarjetaEditar.visibility = View.VISIBLE
        }else{
            tarjetaEditar.visibility = View.GONE
        }

        if(arguments?.getString(ARG_IMAGEN) != null){
            val decodedBitmap = decodeBase64ToBitmap(arguments?.getString(ARG_IMAGEN)!!)  // encodedImage es tu cadena base64
            imageView.setImageBitmap(decodedBitmap)
        }

        textViewNombre.text = arguments?.getString(ARG_NOMBRE) ?: "Nombre no disponible"
        if(arguments?.getString(ARG_DESCRIPCION) == ""){
            textViewDescripcion.text = "Sin descripción"
        }else{
            textViewDescripcion.text = arguments?.getString(ARG_DESCRIPCION)
        }
        if(arguments?.getString(ARG_HORACIERRE) == "null" && arguments?.getString(ARG_HORAPERTURA) == "null"){
            textViewHorario.text = arguments?.getString(ARG_SINHORARIO) ?: ""
        }else{
            val horaApertura = arguments?.getString(ARG_HORAPERTURA) ?: "No disponible"
            val horaCierre = arguments?.getString(ARG_HORACIERRE) ?: "No disponible"
            textViewHorario.text = "$horaApertura - $horaCierre"

        }
        Puntuacion.rating = (arguments?.getDouble(ARG_PUNTUACION) ?: 0.0).toFloat()

        if (etiquetas != null && etiquetas.isNotEmpty()) {
            chipGroup.removeAllViews()
            for (etiqueta in etiquetas) {
                val chip = Chip(requireContext()).apply {
                    text = etiqueta
                    isClickable = false
                    isCheckable = false

                }
                chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.secondary))
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.general))
                chip.chipStrokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
                chip.chipStrokeWidth = 0f
                chipGroup.addView(chip)

            }
        } else {
            val chip = Chip(requireContext()).apply {
                text = "Etiquetas no disponibles"
                isClickable = false
                isCheckable = false

            }
            chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.secondary))
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.general))
            chip.chipStrokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
            chip.chipStrokeWidth = 0f
            chipGroup.addView(chip)
        }

        textViewDistancia.text = calculateAndFormatDistance(arguments?.getParcelable(ARG_CORDENADAS) ?: LatLng(0.0,0.0), arguments?.getParcelable(ARG_UBICACION_USUARIO) ?: LatLng(0.0,0.0)).toString()

        botonBorrar.setOnClickListener {
            val docId = arguments?.getString(ARG_ID_DOCUMENTO)
            if (docId != null) {
                val mensaje = "¿Seguro que quieres borrar este baño?"
                val popup = popUp.newInstance(mensaje)
                popup.setOnAcceptListener { isConfirmed ->
                    if (isConfirmed) {
                        FirestoreSingleton.db
                            .collection("urinarios")
                            .document(docId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Baño borrado", Toast.LENGTH_SHORT).show()
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.frame, FragmentMapa())
                                    .commit()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Error al borrar", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                popup.show(parentFragmentManager, "popUp")

            } else {
                Toast.makeText(requireContext(), "ID de documento no disponible", Toast.LENGTH_SHORT).show()
            }
        }
        botonEditar.setOnClickListener {

        }

        botonLlegar.setOnClickListener {
                val coordenadas = arguments?.getParcelable<LatLng>(ARG_CORDENADAS)
                val latitud = coordenadas?.latitude ?: 0.0
                val longitud = coordenadas?.longitude ?: 0.0
                    val gmmIntentUri = Uri.parse("geo:$latitud,$longitud?q=$latitud,$longitud")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")

                    if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(mapIntent)
                    } else {
                        Toast.makeText(context, "Google Maps no está instalado", Toast.LENGTH_SHORT).show()
                    }
        }


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

    fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}