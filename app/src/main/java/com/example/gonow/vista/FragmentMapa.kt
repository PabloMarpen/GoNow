package com.example.gonow.vista

// Permisos y utilidades del sistema
import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.example.gonow.tfg.R
import com.example.gonow.data.FirestoreSingleton
import com.example.gonow.modelo.Urinario

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint


class FragmentMapa : Fragment(R.layout.fragment_mapa), OnMapReadyCallback {

    private val db = FirestoreSingleton.db
    private lateinit var filtro: ImageView
    private lateinit var botonUbiEstado: ImageView
    private lateinit var mapView: MapView
    private lateinit var rastrear: ImageView
    private lateinit var localizar: ImageView
    private lateinit var ubicacionActual: LatLng
    private lateinit var manejoCarga: ManejoDeCarga
    private var googleMap: GoogleMap? = null
    private var ubicacionActualMostrada = false
    private lateinit var locationCallback: LocationCallback
    private var esCamaraEnMovimiento = false

    private var locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        2000L // Cada 2 segundos
    )
        .setMinUpdateIntervalMillis(1000L) // Cada 1 segundo si hay nueva ubicación
        .setMaxUpdateDelayMillis(4000L) // No más de 4 segundos entre updates
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
                iniciarActualizacionesUbicacion()
            } else {
                Toast.makeText(requireContext(), "Permisos de ubicación denegados", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filtro = view.findViewById(R.id.imageViewFiltro)
        mapView = view.findViewById(R.id.mapView)
        botonUbiEstado = view.findViewById(R.id.imageViewBotonUbi)
        rastrear = view.findViewById(R.id.imageViewUbiOn)
        localizar = view.findViewById(R.id.imageViewUbi)

        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        //manejo de la carga
        manejoCarga = ManejoDeCarga(
            parentFragmentManager,
            timeoutMillis = 20000L
        ) {
            Toast.makeText(requireContext(), "Tiempo de carga agotado", Toast.LENGTH_SHORT).show()
        }

        filtro.setOnClickListener {

            val fragmento = FragmentPopUpFiltro()
            val popup = PopUpContenidoGeneral.newInstance(fragmento)
            popup.show(parentFragmentManager, "popUp")
        }

        //boton de localizacion
        rastrear.visibility = View.GONE
        localizar.visibility = View.VISIBLE
        botonUbiEstado.setOnClickListener {
            if(esCamaraEnMovimiento){
                rastrear.visibility = View.GONE
                localizar.visibility = View.VISIBLE
                esCamaraEnMovimiento = false
                Toast.makeText(requireContext(), "Modo libre", Toast.LENGTH_SHORT).show()
                obtenerUbicacionActual()
            }else{
                rastrear.visibility = View.VISIBLE
                localizar.visibility = View.GONE
                esCamaraEnMovimiento = true
                Toast.makeText(requireContext(), "Siguiendo al usuario", Toast.LENGTH_SHORT).show()
                obtenerUbicacionActual()
            }
        }

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = false
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false

        googleMap?.setOnMarkerClickListener { marker ->
            mostrarDatosBanio(marker)
            true
        }

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

        ubicacionActual = LatLng(0.0, 0.0)
        manejoCarga.ocultarCarga() // para evitar que al cambiar al modo oscuro se quede cargando
        obtenerUbicacionActual() // para la ubicación actual
        obtenerYMostrarBanios() // para los baños
        iniciarActualizacionesUbicacion()// para la ubicación actual
    }

    private fun mostrarDatosBanio(marker: Marker) {
        manejoCarga.mostrarCarga()
        // Versión con consulta a Firestore
        db.collection("urinarios")
            .whereEqualTo("descripcion", marker.title)
            .whereEqualTo("localizacion", GeoPoint(marker.position.latitude, marker.position.longitude))
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val banio = documents.documents[0].toObject(Urinario::class.java)
                    banio?.let {
                        val fragment = FragmentResumenBanio.newInstance(
                            nombre = it.nombre ?: "Sin nombre",
                            descripcion = it.descripcion ?: "Sin descripción",
                            horario = it.horario,
                            puntuacion = it.puntuacion ?: 0.0,
                            sinhorario = it.sinhorario,
                            etiquetas = it.etiquetas ?: emptyList(),
                            cordenadasbanio = LatLng(it.localizacion?.latitude ?: 0.0, it.localizacion?.longitude ?: 0.0),
                            ubicacionUsuario = ubicacionActual,
                            imagen = it.foto,
                            creador = it.creador,
                            idDocumento = documents.documents[0].id,
                            tipo = it.tipoUbi ?: "Sin tipo"
                        )

                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.frame, fragment)
                            .addToBackStack("resumen_banio")
                            .commit()
                        manejoCarga.ocultarCarga()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al obtener datos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun obtenerYMostrarBanios() {
        db.collection("urinarios")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val banio = document.toObject(Urinario::class.java)
                    agregarMarcadorAlMapa(banio)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al cargar baños", Toast.LENGTH_SHORT).show()

            }

    }

    private fun agregarMarcadorAlMapa(banio: Urinario) {
        val location = banio.localizacion
        location?.let { geoPoint ->
            val latLng = LatLng(geoPoint.latitude, geoPoint.longitude)
            val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.ubicacionpoint)
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 140, 170, false) // ancho, alto en píxeles

            googleMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(banio.descripcion)
                    .snippet("Tipo: ${banio.tipoUbi}")
                    .icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
            )
        } ?: run {

        }
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
                ubicacionActual = LatLng(it.latitude, it.longitude)  // Inicialización correcta de la propiedad
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15f))
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Error obteniendo ubicación", Toast.LENGTH_SHORT).show()
        }
    }


    private fun iniciarActualizacionesUbicacion() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        googleMap?.isMyLocationEnabled = true

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                ubicacionActual = LatLng(location.latitude, location.longitude)

                // Mueve la cámara solo si es necesario
                if (esCamaraEnMovimiento || !ubicacionActualMostrada) {
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15f))
                    ubicacionActualMostrada = true
                }

                if (manejoCarga.estaCargando()) {
                    manejoCarga.ocultarCarga()
                }
            }
        }

        posicion.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }



    override fun onDestroyView() {
        super.onDestroyView()
        manejoCarga.ocultarCarga()
        mapView.onDestroy()
        if (::locationCallback.isInitialized) {
            posicion.removeLocationUpdates(locationCallback)
        }
    }

    override fun onStart() {
        super.onStart()
        manejoCarga.ocultarCarga()
    }




}
