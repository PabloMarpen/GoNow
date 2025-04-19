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

        fun newInstance(nombre: String,tipo : String, descripcion: String, horario: Map<String, String?>?, puntuacion: Double, sinhorario: String?, etiquetas: List<String>, cordenadasbanio: LatLng, ubicacionUsuario: LatLng, imagen: String?, creador: String?, idDocumento: String?): FragmentResumenBanio {
            val fragment = FragmentResumenBanio()
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
            args.putString(ARG_ID_TIPO, tipo)
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
        val tipoDeBanio = arguments?.getString(ARG_ID_TIPO)
        val imageView = view.findViewById<ImageView>(R.id.imageViewAñadirImagenResu)
        val tarjetaEditar = view.findViewById<androidx.cardview.widget.CardView>(R.id.tarjeta)
        val botonBorrar = view.findViewById<ImageView>(R.id.imageBorrar)
        val botonEditar = view.findViewById<ImageView>(R.id.imageEditar)
        val botonLlegar = view.findViewById<Button>(R.id.botonLlegar)
        val botonPuntuar = view.findViewById<Button>(R.id.botonPuntuar)
        val user = AuthSingleton.auth.currentUser
        val idBanio = arguments?.getString(ARG_ID_DOCUMENTO) ?: ""

        manejoCarga = ManejoDeCarga(
            parentFragmentManager,
            timeoutMillis = 20000L
        ) {
            Toast.makeText(requireContext(), "Tiempo de carga agotado", Toast.LENGTH_SHORT).show()
        }

        calcularMediaPuntuacion(idBanio, Puntuacion)

        // mostrar o no el boton de editar si eres el creador
        if(arguments?.getString(ARG_CREADOR) == AuthSingleton.auth.currentUser?.uid){
            tarjetaEditar.visibility = View.VISIBLE
        }else{
            tarjetaEditar.visibility = View.GONE
        }

        // mostrar la imagen si no es null o vacio
        val base64Image = arguments?.getString(ARG_IMAGEN)
        if (!base64Image.isNullOrEmpty()) {
            try {
                val decodedBitmap = decodeBase64ToBitmap(base64Image)
                imageView.setImageBitmap(decodedBitmap)
            } catch (e: IllegalArgumentException) {
                Log.e("Base64Decode", "Error al decodificar la imagen: ${e.message}")
                // Puedes poner una imagen por defecto o simplemente ocultar el ImageView si prefieres
                imageView.setImageResource(R.drawable.noimage) // o lo que tengas
            }
        }

        // mostrar el nombre
        textViewNombre.text = arguments?.getString(ARG_NOMBRE) ?: "Nombre no disponible"
        // mostrar la descripcion
        if(arguments?.getString(ARG_DESCRIPCION) == ""){
            textViewDescripcion.text = "Sin descripción"
        }else{
            textViewDescripcion.text = arguments?.getString(ARG_DESCRIPCION)
        }
        // mostrar el horario
        if(arguments?.getString(ARG_HORACIERRE) == "null" && arguments?.getString(ARG_HORAPERTURA) == "null"){
            textViewHorario.text = arguments?.getString(ARG_SINHORARIO) ?: ""
        }else{
            val horaApertura = arguments?.getString(ARG_HORAPERTURA) ?: "No disponible"
            val horaCierre = arguments?.getString(ARG_HORACIERRE) ?: "No disponible"
            textViewHorario.text = "$horaApertura - $horaCierre"

        }


        // mostrar las etiquetas en chips
        if (etiquetas != null && etiquetas.isNotEmpty()) {
            chipGroup.removeAllViews()
            for (etiqueta in etiquetas) {
                val chip = Chip(requireContext()).apply {
                    text = etiqueta
                    isClickable = false
                    isCheckable = false
                    if(etiqueta == "Accesible" || etiqueta == "Con jabón" || etiqueta == "Con papel" || etiqueta == "Gratis" || etiqueta == "Con zona bebé" || etiqueta == "No unisex"){
                        chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary))
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.oscuro))
                        chipStrokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
                        chipStrokeWidth = 0f
                    }else{
                        chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.secondary))
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.oscuro))
                        chipStrokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
                        chipStrokeWidth = 0f
                    }

                }
                chipGroup.addView(chip)

            }
            val tipo = Chip(requireContext()).apply {
                text = tipoDeBanio
                isClickable = false
                isCheckable = false
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.secondary))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.oscuro))
                chipStrokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
                chipStrokeWidth = 0f
            }
            chipGroup.addView(tipo)
        } else {
            val chip = Chip(requireContext()).apply {
                text = "Etiquetas no disponibles"
                isClickable = false
                isCheckable = false

            }
            chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.secondary))
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.oscuro))
            chip.chipStrokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
            chip.chipStrokeWidth = 0f
            chipGroup.addView(chip)
        }
        // mostrar la distancia
        textViewDistancia.text = calculateAndFormatDistance(arguments?.getParcelable(ARG_CORDENADAS) ?: LatLng(0.0,0.0), arguments?.getParcelable(ARG_UBICACION_USUARIO) ?: LatLng(0.0,0.0)).toString()

        botonPuntuar.setOnClickListener {
            if (user != null){
                val calificarFragmento = FragmentPopUpCalificar.newInstance(
                    arguments?.getString(ARG_ID_DOCUMENTO) ?: "",
                    arguments?.getString(ARG_CREADOR) ?: "",
                    arguments?.getDouble(ARG_PUNTUACION) ?: 0.0
                )
                PopUpContenidoGeneral.newInstance(calificarFragmento).show(parentFragmentManager, "popUp")
            }else{
                PopUpContenidoGeneral.newInstance(FragmentPopUpSpam()).show(parentFragmentManager, "popUp")

            }


        }
        botonBorrar.setOnClickListener {
            val docId = arguments?.getString(ARG_ID_DOCUMENTO)
            if (docId != null) {
                val mensaje = "¿Seguro que quieres borrar este baño?"
                val popup = PopUp.newInstance(mensaje)
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

    private fun calcularMediaPuntuacion(idBanio: String, Puntuacion: RatingBar) {
        manejoCarga.mostrarCarga()
        FirestoreSingleton.db.collection("urinarios")
            .document(idBanio)
            .get()
            .addOnSuccessListener { documentUrinario ->
                val puntuacionUrinario = documentUrinario.getDouble("puntuacion") ?: 0.0

                FirestoreSingleton.db.collection("calificaciones")
                    .whereEqualTo("idBanio", idBanio)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        var sumaPuntuaciones = puntuacionUrinario
                        var cantidadCalificaciones = 1 // contamos con la del creador

                        for (document in querySnapshot.documents) {
                            val puntuacion = document.getDouble("puntuacion") ?: 0.0
                            sumaPuntuaciones += puntuacion
                            cantidadCalificaciones++
                        }

                        val mediaPuntuacion = if (cantidadCalificaciones > 0) {
                            sumaPuntuaciones / cantidadCalificaciones
                        } else {
                            0.0
                        }

                        Puntuacion.rating = mediaPuntuacion.toFloat()
                        manejoCarga.ocultarCarga()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(Puntuacion.context, "Error al obtener las calificaciones.", Toast.LENGTH_SHORT).show()
                        Log.e("Firestore", "Error en calificaciones", e)
                        manejoCarga.ocultarCarga()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(Puntuacion.context, "Error al obtener el baño.", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error en urinario", e)
                manejoCarga.ocultarCarga()
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

    // Funciones para decodificar la imagen base64
    fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    override fun onStart() {
        super.onStart()
        manejoCarga.reiniciarCargaSiEsNecesario()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        manejoCarga.ocultarCarga()
    }
}