package com.example.gonow.vista

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.gonow.data.AuthSingleton
import com.example.gonow.data.FirestoreSingleton
import com.example.gonow.modelo.Calificacion
import com.example.gonow.tfg.R
import com.google.firebase.firestore.FirebaseFirestore

class FragmentPopUpCalificar : Fragment(R.layout.fragment_pop_up_calificar){

    private lateinit var manejoCarga: ManejoDeCarga

    companion object {
        private const val ARG_IDBANIO = "idBanio"
        private const val ARG_CREADOR = "creador"
        private const val ARG_PUNTUACIONORI = "puntuacionoriginal"

        fun newInstance(idBanio: String, idUsuario: String, puntuacionOriginal: Double): FragmentPopUpCalificar {
            val fragment = FragmentPopUpCalificar()
            val args = Bundle()

            args.putString(ARG_IDBANIO, idBanio)
            args.putString(ARG_CREADOR, idUsuario)
            args.putDouble(ARG_PUNTUACIONORI, puntuacionOriginal)

            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        manejoCarga = ManejoDeCarga(
            parentFragmentManager,
            timeoutMillis = 20000L
        ){
            Toast.makeText(requireContext(), getString(R.string.error_muchotiempo), Toast.LENGTH_SHORT).show()
        }

        val estrellas = view.findViewById<RatingBar>(R.id.ratingBarCalificar)
        val botonGuardar = view.findViewById<Button>(R.id.buttonEnviar)
        val idBanio = arguments?.getString(ARG_IDBANIO) ?: ""
        val creador = arguments?.getString(ARG_CREADOR) ?: ""
        val idUsuario = AuthSingleton.auth.currentUser?.uid ?: ""

        // si el creador es el mismo que el usuario, no se puede calificar una nueva solo la que ya existe
        if (idUsuario == creador) {
            manejoCarga.mostrarCarga(getString(R.string.cargandocalificaciones))
            cargarCalificacionDeCreador(idBanio, idUsuario, estrellas)
        }else{
            manejoCarga.mostrarCarga(getString(R.string.cargandocalificaciones))
            cargarCalificacionExistente(idBanio, idUsuario, estrellas)
        }

        botonGuardar.setOnClickListener {

            if (estrellas.rating == 0.0f) {
                Toast.makeText(requireContext(), getString(R.string.debescalificar), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else{
                manejoCarga.mostrarCarga(getString(R.string.subiendocalificacion))
                if(idUsuario == creador){
                    guardarOActualizarCalificacionDeCreador(idBanio, estrellas, isAdded) {

                        if (isAdded) {
                            (parentFragment as? DialogFragment)?.dismiss()
                        }

                    }
                }else{
                    guardarOActualizarCalificacion(idBanio, idUsuario, estrellas, isAdded) {

                        if (isAdded) {
                            (parentFragment as? DialogFragment)?.dismiss()
                        }



                    }
                }

            }

        }


    }

    private fun actualizarMediaPuntuacion(idBanio: String) {
        val db = FirestoreSingleton.db

        db.collection("urinarios").document(idBanio).get()
            .addOnSuccessListener { documentUrinario ->
                val puntuacionCreador = documentUrinario.getDouble("puntuacion") ?: 0.0

                db.collection("calificaciones")
                    .whereEqualTo("idBanio", idBanio)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        var suma = puntuacionCreador
                        var cantidad = 1 // Ya contamos la del creador

                        for (doc in querySnapshot.documents) {
                            val puntuacion = doc.getDouble("puntuacion") ?: 0.0
                            suma += puntuacion
                            cantidad++
                        }

                        val media = if (cantidad > 0) suma / cantidad else 0.0
                        val totalCalificaciones = cantidad

                        // Actualiza ambos campos: media y total
                        db.collection("urinarios").document(idBanio)
                            .update(
                                mapOf(
                                    "mediaPuntuacion" to media,
                                    "totalCalificaciones" to totalCalificaciones
                                )
                            )
                            .addOnSuccessListener {
                                manejoCarga.ocultarCarga()
                                Log.d("MediaPuntuacion", "Media y total actualizados correctamente")
                            }
                            .addOnFailureListener { e ->
                                manejoCarga.ocultarCarga()
                                Log.e("MediaPuntuacion", "Error al actualizar urinario", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        manejoCarga.ocultarCarga()
                        Log.e("MediaPuntuacion", "Error al obtener calificaciones", e)
                    }
            }
            .addOnFailureListener { e ->
                manejoCarga.ocultarCarga()
                Log.e("MediaPuntuacion", "Error al obtener el baño", e)
            }
    }


    private fun cargarCalificacionDeCreador(idBanio: String, idUsuario: String, estrellas: RatingBar) {
        val docRef = FirestoreSingleton.db.collection("urinarios").document(idBanio)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val creador = document.getString("creador") ?: ""
                    val puntuacion = document.getDouble("puntuacion")?.toFloat() ?: 0.0f

                    if (creador == idUsuario) {
                        estrellas.rating = puntuacion
                    }
                }
                manejoCarga.ocultarCarga()
            }
            .addOnFailureListener { e ->
                context?.let {
                    manejoCarga.ocultarCarga()
                    Toast.makeText(it, it.getString(R.string.erropuntuacionbanios), Toast.LENGTH_SHORT).show()
                }
                Log.e("Firestore", "Error al cargar puntuación", e)
            }
    }



    private fun cargarCalificacionExistente(idBanio: String, idUsuario: String, estrellas: RatingBar) {
        FirestoreSingleton.db.collection("calificaciones")
            .whereEqualTo("idBanio", idBanio)
            .whereEqualTo("idUsuario", idUsuario)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val existingCalificacion = querySnapshot.documents.first()
                    val puntuacionAnterior = existingCalificacion.getDouble("puntuacion")?.toFloat() ?: 0.0f
                    estrellas.rating = puntuacionAnterior
                } else {
                    estrellas.rating = 0.0f
                }
                manejoCarga.ocultarCarga()
            }
            .addOnFailureListener { e ->
                context?.let {
                    manejoCarga.ocultarCarga()
                    Toast.makeText(it, it.getString(R.string.errorverificar), Toast.LENGTH_SHORT).show()
                }
                Log.e("Firestore", "Error al verificar calificación", e)
            }
    }


    private fun guardarOActualizarCalificacionDeCreador(
        idBanio: String,
        estrellas: RatingBar,
        isAdded: Boolean,
        cerrarDialog: () -> Unit
    ) {
        FirestoreSingleton.db.collection("urinarios")
            .document(idBanio)
            .update("puntuacion", estrellas.rating.toDouble())
            .addOnSuccessListener {
                context?.let {
                    manejoCarga.ocultarCarga()
                    Toast.makeText(it, it.getString(R.string.puntuacionactualizadabien), Toast.LENGTH_SHORT).show()
                }
                actualizarMediaPuntuacion(idBanio)
                cerrarDialog()
            }
            .addOnFailureListener { e ->
                context?.let {
                    manejoCarga.ocultarCarga()
                    Toast.makeText(it, it.getString(R.string.erropuntuacionbanios), Toast.LENGTH_SHORT).show()
                }
                Log.e("Firestore", "Error al actualizar puntuación del urinario", e)
            }
    }



    private fun guardarOActualizarCalificacion(
        idBanio: String,
        idUsuario: String,
        estrellas: RatingBar,
        isAdded: Boolean,
        cerrarDialog: () -> Unit
    ) {
        val objetoCalificacion = Calificacion(
            puntuacion = estrellas.rating,
            idBanio = idBanio,
            idUsuario = idUsuario
        )

        FirestoreSingleton.db.collection("calificaciones")
            .whereEqualTo("idBanio", idBanio)
            .whereEqualTo("idUsuario", idUsuario)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val existingDocument = querySnapshot.documents.first()

                    FirestoreSingleton.db.collection("calificaciones")
                        .document(existingDocument.id)
                        .set(objetoCalificacion)
                        .addOnSuccessListener {
                            context?.let {
                                Toast.makeText(it, it.getString(R.string.calificacionenviada), Toast.LENGTH_SHORT).show()
                            }
                            actualizarMediaPuntuacion(idBanio)
                            cerrarDialog()
                        }
                        .addOnFailureListener { e ->
                            context?.let {
                                manejoCarga.ocultarCarga()
                                Toast.makeText(it, it.getString(R.string.erropuntuacionbanios), Toast.LENGTH_SHORT).show()
                            }
                            Log.e("Firestore", "Error al actualizar", e)
                        }
                } else {
                    FirestoreSingleton.db.collection("calificaciones")
                        .add(objetoCalificacion)
                        .addOnSuccessListener {
                            context?.let {
                                Toast.makeText(it, it.getString(R.string.calificacionenviada), Toast.LENGTH_SHORT).show()
                            }
                            actualizarMediaPuntuacion(idBanio)
                            cerrarDialog()
                        }
                        .addOnFailureListener { e ->
                            context?.let {
                                manejoCarga.ocultarCarga()
                                Toast.makeText(it, it.getString(R.string.error_al_publicar), Toast.LENGTH_SHORT).show()
                            }
                            Log.e("Firestore", "Error al publicar", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                context?.let {
                    manejoCarga.ocultarCarga()
                    Toast.makeText(it, it.getString(R.string.errorverificar), Toast.LENGTH_SHORT).show()
                }
                Log.e("Firestore", "Error al verificar calificación", e)
            }
    }



}