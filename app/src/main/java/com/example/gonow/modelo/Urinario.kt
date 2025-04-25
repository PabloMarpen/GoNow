package com.example.gonow.modelo

import com.google.firebase.firestore.GeoPoint

data class Urinario(
    // Modelo de datos para los banios
    val nombre : String? = "",
    val creador: String? = "",
    val descripcion: String? = "",
    val etiquetas: List<String>? = emptyList(),
    val foto: String? = "",
    val horario: Map<String, String?>? = emptyMap(),
    val sinhorario: String? = "",
    val localizacion: GeoPoint? = null,
    val tipoUbi: String? = "",
    val puntuacion: Double? = 0.0,
    val mediaPuntuacion: Double? = 0.0,
    val totalCalificaciones: Int? = 0,
    val idDocumento: String? = null
)
