package com.example.gonow.vista

// Permisos y utilidades del sistema
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.Toast

//  Componentes de AndroidX
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

//  Recursos del proyecto
import com.example.gonow.R

// API de ubicación de Google
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority

//  API de Google Maps
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions


class FragmentMapa : Fragment(R.layout.fragment_mapa), OnMapReadyCallback {

    private lateinit var filtro: ImageView
    private lateinit var mapView: MapView
    private lateinit var ubicacionActual: LatLng
    private var googleMap: GoogleMap? = null
    private var ubicacionActualMostrada = false
    private lateinit var locationCallback: LocationCallback
    private var locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
    .setMinUpdateIntervalMillis(2000L)
    .build()

    private val posicion: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    // para los permisos de ubicación
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                obtenerUbicacionActual()
            } else {
                Toast.makeText(requireContext(), "Permisos de ubicación denegados", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filtro = view.findViewById(R.id.imageViewFiltro)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        filtro.setOnClickListener {

            val fragmento = FragmentPopUpFiltro()
            val popup = popUpContenidoGeneral.newInstance(fragmento)
            popup.show(parentFragmentManager, "popUp")
        }


    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = false
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false


        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.mapstyle_night));
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.mapstyle_white));
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.mapstyle_white));
            }
        }


        obtenerUbicacionActual()
        iniciarActualizacionesUbicacion()

    }

    private fun obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        posicion.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val ubicacionActual = LatLng(it.latitude, it.longitude)
                googleMap?.addMarker(
                    MarkerOptions().position(ubicacionActual).title("Ubicación actual")
                )
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15f))
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun iniciarActualizacionesUbicacion() {
        googleMap?.isMyLocationEnabled = true

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                ubicacionActual = LatLng(location.latitude, location.longitude)

                googleMap?.clear()

                if (!ubicacionActualMostrada){
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 17f))
                    ubicacionActualMostrada = true
                }

            }

        }

        posicion.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

    }




    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        if (::locationCallback.isInitialized) {
            posicion.removeLocationUpdates(locationCallback)
        }
    }
}
