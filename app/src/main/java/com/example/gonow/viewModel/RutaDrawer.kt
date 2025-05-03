package com.example.gonow.viewModel

import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

// Object que maneja la lógica para dibujar rutas en el mapa utilizando la Directions API de Google Maps
object RutaDrawer {

    // Variable para almacenar la referencia de la polilínea actual
    private var polyline: Polyline? = null
    private var origenAnterior: LatLng? = null
    private var destinoAnterior: LatLng? = null

    // Función principal para dibujar la ruta en el mapa entre dos puntos
    fun dibujarRuta(
        map: GoogleMap,
        origen: LatLng,
        destino: LatLng,
        apiKey: String,
        modo: String
    ) {
        // Si el origen y destino no han cambiado, no redibujamos
        if (origen == origenAnterior && destino == destinoAnterior) return

        // Actualizamos las variables
        origenAnterior = origen
        destinoAnterior = destino

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = buildUrl(origen, destino, apiKey, modo)
                val result = URL(url).readText()
                val puntosRuta = decodePolylineFromJson(result)

                withContext(Dispatchers.Main) {
                    if (puntosRuta.isNotEmpty()) {
                        polyline?.remove()
                        val polylineOptions = PolylineOptions()
                            .addAll(puntosRuta)
                            .color(Color.parseColor("#10BDC0"))
                            .width(17f)
                        polyline = map.addPolyline(polylineOptions)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    // Construye la URL para hacer la solicitud a la API Directions de Google Maps
    private fun buildUrl(origen: LatLng, destino: LatLng, apiKey: String, modo: String): String {
        return "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origen.latitude},${origen.longitude}" +
                "&destination=${destino.latitude},${destino.longitude}" +
                "&mode=$modo" + // Agregar el parámetro mode
                "&key=$apiKey"
    }

    // Decodifica la respuesta JSON para obtener los puntos de la ruta
    private fun decodePolylineFromJson(json: String): List<LatLng> {
        val puntos = mutableListOf<LatLng>()
        val jsonObject = JSONObject(json)
        val routes = jsonObject.getJSONArray("routes")
        // Si no hay rutas disponibles, retorna una lista vacía
        if (routes.length() == 0) return puntos

        // Obtiene el objeto que contiene la polilínea (ruta) codificada
        val overviewPolyline = routes.getJSONObject(0)
            .getJSONObject("overview_polyline")
            .getString("points")

        // Decodifica la polilínea y la agrega a la lista de puntos
        puntos.addAll(decodePolyline(overviewPolyline))
        return puntos
    }

    // Decodifica una polilínea codificada en formato de cadena
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        // Itera a través de los caracteres codificados de la polilínea
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            // Calcula la latitud y longitud en base a la cadena codificada
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            // Convierte los valores decodificados en coordenadas LatLng y las agrega a la lista
            val p = LatLng(lat / 1E5, lng / 1E5)
            poly.add(p)
        }

        return poly
    }
}


