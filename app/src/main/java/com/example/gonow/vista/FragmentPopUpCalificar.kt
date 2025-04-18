package com.example.gonow.vista

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.gonow.data.AuthSingleton
import com.example.gonow.data.FirestoreSingleton
import com.example.gonow.modelo.Calificacion
import com.example.gonow.tfg.R
import com.google.firebase.firestore.FirebaseFirestore

class FragmentPopUpCalificar : Fragment(R.layout.fragment_pop_up_calificar){

    companion object {
        private const val ARG_IDBANIO = "idBanio"


        fun newInstance(idBanio: String): FragmentPopUpCalificar {
            val fragment = FragmentPopUpCalificar()
            val args = Bundle()

            args.putString(ARG_IDBANIO, idBanio)

            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val estrellas = view.findViewById<RatingBar>(R.id.ratingBarCalificar)
        val botonGuardar = view.findViewById<Button>(R.id.buttonEnviar)
        val idBanio = arguments?.getString(ARG_IDBANIO) ?: ""
        val idUsuario = AuthSingleton.auth.currentUser?.uid ?: ""

        FirestoreSingleton.db.collection("calificaciones")
            .whereEqualTo("idBanio", idBanio)
            .whereEqualTo("idUsuario", idUsuario)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Ya existe una calificación para este baño por este usuario
                    val existingCalificacion = querySnapshot.documents.first()
                    val puntuacionAnterior = existingCalificacion.getDouble("puntuacion")?.toFloat() ?: 0.0f

                    // Mostrar la calificación anterior en el RatingBar
                    estrellas.rating = puntuacionAnterior

                    Toast.makeText(requireContext(), "Ya has calificado este baño. Puedes actualizar tu calificación", Toast.LENGTH_SHORT).show()
                } else {
                    // El usuario no ha calificado este baño, proceder con la calificación
                    estrellas.rating = 0.0f // Asegúrate de que el RatingBar esté vacío al principio
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al verificar la calificación: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error al verificar calificación", e)
            }

        botonGuardar.setOnClickListener {
            if (estrellas.rating == 0.0f) {
                Toast.makeText(requireContext(), "Debes calificar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
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
                            // Si ya existe, actualizamos la calificación
                            val existingDocument = querySnapshot.documents.first()
                            FirestoreSingleton.db.collection("calificaciones")
                                .document(existingDocument.id)
                                .set(objetoCalificacion)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Calificación actualizada", Toast.LENGTH_SHORT).show()

                                    if (isAdded) {
                                        (requireParentFragment() as? DialogFragment)?.dismiss()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                                    Log.e("Firestore", "Error al actualizar", e)
                                }
                        } else {
                            // Si no existe, agregamos una nueva calificación
                            FirestoreSingleton.db.collection("calificaciones")
                                .add(objetoCalificacion)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Calificación enviada", Toast.LENGTH_SHORT).show()

                                    if (isAdded) {
                                        (requireParentFragment() as? DialogFragment)?.dismiss()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Error al publicar: ${e.message}", Toast.LENGTH_SHORT).show()
                                    Log.e("Firestore", "Error al publicar", e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error al verificar la calificación: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("Firestore", "Error al verificar calificación", e)
                    }
            }
        }

    }
}