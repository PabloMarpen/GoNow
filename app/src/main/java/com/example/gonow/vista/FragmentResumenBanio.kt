package com.example.gonow.vista

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gonow.tfg.R
import com.example.gonow.data.AuthSingleton
import com.example.gonow.data.FirestoreSingleton
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt


class FragmentResumenBanio : Fragment(R.layout.fragment_resumen_banio) {

    private lateinit var manejoCarga: ManejoDeCarga
    private lateinit var textViewPersonasPuntuado: TextView


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
        private const val ARG_ID_TIPO = "tipo"
        private const val ARG_MEDIAPUNTUACION = "mediaPuntuacion"
        private const val ARG_TOTAL_PUNTUACIONES = "totalPuntuaciones"

        fun newInstance(
            nombre: String,
            tipo: String,
            descripcion: String,
            horario: Map<String, String?>?,
            puntuacion: Double,
            sinhorario: String?,
            etiquetas: List<String>,
            cordenadasbanio: LatLng,
            ubicacionUsuario: LatLng,
            imagen: String?,
            creador: String?,
            idDocumento: String?,
            mediaPuntuacion: Double,
            totalPuntuaciones: Int
        ): FragmentResumenBanio {
            val fragment = FragmentResumenBanio()
            val args = Bundle()
            args.putString(ARG_NOMBRE, nombre)
            args.putString(ARG_DESCRIPCION, descripcion)
            val horaApertura = horario?.let {
                val Apertura = it["apertura"] ?: "Not available"
                Apertura
            } ?: ""
            val horaCierre = horario?.let {
                val Cierre = it["cierre"] ?: "Not available"
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
            args.putString(ARG_ID_TIPO, tipo)
            args.putDouble(ARG_MEDIAPUNTUACION, mediaPuntuacion)
            args.putInt(ARG_TOTAL_PUNTUACIONES, totalPuntuaciones)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textViewNombre = view.findViewById<TextView>(R.id.textViewNombre)
        val textViewDescripcion = view.findViewById<TextView>(R.id.textViewTextoDescripcion)
        val textViewHorario = view.findViewById<TextView>(R.id.textViewTextoHorario)
        val Puntuacion = view.findViewById<RatingBar>(R.id.ratingBar)
        val textViewDistancia = view.findViewById<TextView>(R.id.textViewDistanciaNum)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupEtiquetas)
        val etiquetas = arguments?.getStringArrayList(ARG_ETIQUETAS)
        val tipoDeBanio = arguments?.getString(ARG_ID_TIPO)
        val imageView = view.findViewById<ImageView>(R.id.imageViewAñadirImagenResu)
        val tarjetaEditar = view.findViewById<androidx.cardview.widget.CardView>(R.id.tarjeta)
        val botonBorrar = view.findViewById<ImageView>(R.id.imageBorrar)
        val botonEditar = view.findViewById<ImageView>(R.id.imageEditar)
        val botonLlegar = view.findViewById<Button>(R.id.botonLlegar)
        val botonPuntuar = view.findViewById<Button>(R.id.botonPuntuar)
        textViewPersonasPuntuado = view.findViewById(R.id.textViewPersonasPuntuado)

        val user = AuthSingleton.auth.currentUser
        val idBanio = arguments?.getString(ARG_ID_DOCUMENTO) ?: ""
        var totalPuntuaciones = arguments?.getInt(ARG_TOTAL_PUNTUACIONES) ?: 0


        manejoCarga = ManejoDeCarga(
            parentFragmentManager,
            timeoutMillis = 20000L // 20000L
        )

        if (totalPuntuaciones > 0) {
            textViewPersonasPuntuado.text = "($totalPuntuaciones)"
        }


        // mostrar o no el boton de editar si eres el creador
        if (arguments?.getString(ARG_CREADOR) == AuthSingleton.auth.currentUser?.uid || AuthSingleton.auth.currentUser?.email == "ospa40@gmail.com" ) {
            tarjetaEditar.visibility = View.VISIBLE
        } else {
            tarjetaEditar.visibility = View.GONE
        }

        if (arguments?.getDouble(ARG_MEDIAPUNTUACION) == 0.0) {
            Puntuacion.rating = arguments?.getDouble(ARG_PUNTUACION)?.toFloat() ?: 0f
        } else {
            Puntuacion.rating = arguments?.getDouble(ARG_MEDIAPUNTUACION)?.toFloat() ?: 0f
        }


        // mostrar la imagen si no es null o vacio
        val base64Image = arguments?.getString(ARG_IMAGEN)
        if (!base64Image.isNullOrEmpty()) {
            try {
                val decodedBitmap = decodeBase64ToBitmap(base64Image)
                imageView.setImageBitmap(decodedBitmap)
            } catch (e: IllegalArgumentException) {

                imageView.setImageResource(R.drawable.noimage)
            }
        }

        // mostrar el nombre
        textViewNombre.text =
            arguments?.getString(ARG_NOMBRE) ?: getString(R.string.nombre_no_disponible)

        // mostrar la descripcion
        val descripcion = arguments?.getString(ARG_DESCRIPCION)
        textViewDescripcion.text = if (descripcion.isNullOrEmpty()) {
            getString(R.string.sin_descripcion)
        } else {
            descripcion
        }

        // mostrar el horario
        val horaApertura = arguments?.getString(ARG_HORAPERTURA)
        val horaCierre = arguments?.getString(ARG_HORACIERRE)
        val sinHorario = arguments?.getString(ARG_SINHORARIO) ?: ""

        textViewHorario.text = if (horaApertura == "null" && horaCierre == "null") {
            sinHorario
        } else {
            val apertura = horaApertura ?: getString(R.string.no_disponible)
            val cierre = horaCierre ?: getString(R.string.no_disponible)
            "$apertura - $cierre"
        }


        // mostrar las etiquetas en chips
        if (etiquetas != null && etiquetas.isNotEmpty()) {
            chipGroup.removeAllViews()
            for (etiqueta in etiquetas) {

                val nombreBonito = when (etiqueta) {
                    "01" -> getString(R.string.etiqueta_01)
                    "10" -> getString(R.string.etiqueta_10)
                    "02" -> getString(R.string.etiqueta_02)
                    "20" -> getString(R.string.etiqueta_20)
                    "03" -> getString(R.string.etiqueta_03)
                    "30" -> getString(R.string.etiqueta_30)
                    "04" -> getString(R.string.etiqueta_04)
                    "40" -> getString(R.string.etiqueta_40)
                    "05" -> getString(R.string.etiqueta_05)
                    "50" -> getString(R.string.etiqueta_50)
                    "06" -> getString(R.string.etiqueta_06)
                    "60" -> getString(R.string.etiqueta_60)
                    else -> etiqueta
                }

                val chip = Chip(requireContext()).apply {
                    text = nombreBonito
                    isClickable = false
                    isCheckable = false

                    // Códigos "positivos" primarios (01–06)
                    if (etiqueta in listOf("01", "02", "03", "04", "05", "06")) {
                        chipBackgroundColor = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.primary
                            )
                        )
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.oscuro))
                        chipStrokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
                        chipStrokeWidth = 0f
                    } else {
                        chipBackgroundColor = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.primary
                            )
                        )
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.oscuro))
                        chipStrokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
                        chipStrokeWidth = 0f
                    }
                }

                chipGroup.addView(chip)
            }

            // Añadir el chip con el tipo de baño
            val tipo = Chip(requireContext()).apply {

                val tipoBanioBonito = when (tipoDeBanio) {
                    "01" -> getString(R.string.tipo_01)
                    "02" -> getString(R.string.tipo_02)
                    "03" -> getString(R.string.tipo_03)
                    "04" -> getString(R.string.tipo_04)
                    "05" -> getString(R.string.tipo_05)
                    "06" -> getString(R.string.tipo_06)
                    else -> tipoDeBanio
                }

                text = tipoBanioBonito
                isClickable = false
                isCheckable = false
                chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.primary
                    )
                )
                setTextColor(ContextCompat.getColor(requireContext(), R.color.oscuro))
                chipStrokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
                chipStrokeWidth = 0f
            }
            chipGroup.addView(tipo)
        } else {
            val chip = Chip(requireContext()).apply {
                text = getString(R.string.no_disponible)
                isClickable = false
                isCheckable = false

            }
            chip.chipBackgroundColor =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.secondary))
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.oscuro))
            chip.chipStrokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
            chip.chipStrokeWidth = 0f
            chipGroup.addView(chip)
        }
        // mostrar la distancia
        textViewDistancia.text = calculateAndFormatDistance(
            arguments?.getParcelable(ARG_CORDENADAS) ?: LatLng(0.0, 0.0),
            arguments?.getParcelable(ARG_UBICACION_USUARIO) ?: LatLng(0.0, 0.0)
        )

        botonPuntuar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.secondaryVariant)
                )
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.secondary)
                )
            }
            false
        }

        botonPuntuar.setOnClickListener {
            if (user != null) {
                val calificarFragmento = FragmentPopUpCalificar.newInstance(
                    arguments?.getString(ARG_ID_DOCUMENTO) ?: "",
                    arguments?.getString(ARG_CREADOR) ?: "",
                    arguments?.getDouble(ARG_PUNTUACION) ?: 0.0
                )
                PopUpContenidoGeneral.newInstance(calificarFragmento)
                    .show(parentFragmentManager, "popUp")
            } else {
                PopUpContenidoGeneral.newInstance(FragmentPopUpSpam())
                    .show(parentFragmentManager, "popUp")

            }


        }
        botonBorrar.setOnClickListener {
            val docId = arguments?.getString(ARG_ID_DOCUMENTO)
            if (docId != null) {
                val mensaje = getString(R.string.confirmar_borrar_banio)
                val popup = PopUp.newInstance(mensaje)
                popup.setOnAcceptListener { isConfirmed ->
                    if (isConfirmed) {
                        val db = FirestoreSingleton.db

                        // Primero, borrar las reseñas asociadas
                        db.collection("calificaciones")
                            .whereEqualTo("idBanio", docId)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                val batch = db.batch()
                                for (document in querySnapshot) {
                                    batch.delete(document.reference)
                                }

                                // Ejecutar el batch para eliminar todas las reseñas
                                batch.commit()
                                    .addOnSuccessListener {
                                        // Luego, borrar el baño
                                        db.collection("urinarios")
                                            .document(docId)
                                            .delete()
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    getString(R.string.banio_borrado),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                requireActivity().supportFragmentManager.beginTransaction()
                                                    .replace(R.id.frame, FragmentMapa())
                                                    .commit()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    getString(R.string.error_borrar_banio),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            requireContext(),
                                            getString(R.string.error_borrar_resenas),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error_buscar_resenas),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                popup.show(parentFragmentManager, "popUp")
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.id_no_disponible),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        botonEditar.setOnClickListener {
            val editarFragmento = FragmentAniadir.newInstance(
                nombre = arguments?.getString(ARG_NOMBRE),
                esEditar = true,
                puntuacionOriginal = arguments?.getDouble(ARG_PUNTUACION) ?: 0.0,
                descripcion = arguments?.getString(ARG_DESCRIPCION),
                tipo = arguments?.getString(ARG_ID_TIPO),
                sinHorario = arguments?.getString(ARG_SINHORARIO),
                horaApertura = arguments?.getString(ARG_HORAPERTURA),
                horaCierre = arguments?.getString(ARG_HORACIERRE),
                etiquetas = arguments?.getStringArrayList(ARG_ETIQUETAS) ?: emptyList(),
                foto = arguments?.getString(ARG_IMAGEN),
                idBanio = idBanio

            )
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame, editarFragmento)
                .addToBackStack(null)
                .commit()
        }

        botonLlegar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.secondaryVariant)
                )
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.secondary)
                )
            }
            false
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
                Toast.makeText(context, getString(R.string.googlenoinstalado), Toast.LENGTH_SHORT)
                    .show()

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
                // Menos de 100m  mostrar en metros
                val meters = (distanceKm * 1000).roundToInt()
                "$meters m"
            }

            distanceKm < 10 -> {
                // Entre 100m y 10km mostrar 1 decimal
                "%.1f km".format(distanceKm)
            }

            else -> {
                // Más de 10km  mostrar sin decimales
                "${distanceKm.roundToInt()} km"
            }
        }
    }

    // Funciones para decodificar la imagen base64
    fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        manejoCarga.ocultarCarga()
    }
}