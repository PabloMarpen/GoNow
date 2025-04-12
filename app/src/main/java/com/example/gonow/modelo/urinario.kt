package com.example.gonow.modelo

import com.google.firebase.firestore.GeoPoint

data class Urinario(
    val creador: String? = "",
    val descripcion: String? = "",
    val etiquetas: List<String>? = emptyList(),
    val foto: String? = "",
    val horario: Map<String, String?>? = emptyMap(),
    val localizacion: GeoPoint? = null,
    val tipoUbi: String? = "",
    val puntuacion: Double? = 0.0
)
