package com.example.gonow.viewModel

// Permisos y utilidades del sistema
import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import androidx.lifecycle.AndroidViewModel


class UbicacionViewModel(application: Application) : AndroidViewModel(application) {

    // Cliente de ubicación para obtener la última ubicación conocida
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // Configuración de la solicitud de ubicación con alta precisión
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,  // Prioridad de alta precisión
        2000L  // Intervalo de actualización en milisegundos
    )
        .setMinUpdateIntervalMillis(1000L)  // Intervalo mínimo entre actualizaciones
        .setMaxUpdateDelayMillis(4000L)    // Retraso máximo permitido entre actualizaciones
        .build()

    // LiveData que contiene la ubicación actual del usuario
    private val _ubicacionActual = MutableLiveData<LatLng>()
    val ubicacionActual: LiveData<LatLng> = _ubicacionActual  // Exponemos la ubicación como LiveData

    /**
     * Esta función intenta obtener la ubicación actual del usuario:
     * 1. Comprueba si se tienen los permisos necesarios (ubicación fina y aproximada).
     *    Si no los tiene, termina la función.
     * 2. Si los permisos están concedidos, solicita la última ubicación conocida del dispositivo.
     * 3. Si se obtiene una ubicación válida, guarda sus coordenadas en `ubicacionActual`.
     * 4. Si ocurre un error al obtener la ubicación, muestra un mensaje de error al usuario.
     */
    fun obtenerUbicacionActual(context: Context) {
        // Verificamos si los permisos necesarios han sido concedidos
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return  // Si no se tienen permisos, no hacemos nada y salimos de la función
        }

        // Si los permisos están concedidos, obtenemos la última ubicación conocida
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                // Si obtenemos una ubicación válida, guardamos las coordenadas en LiveData
                _ubicacionActual.value = LatLng(it.latitude, it.longitude)
            }
        }
    }
}

