package com.example.gonow.vista


// Permisos y utilidades del sistema
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

//  Componentes de AndroidX
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

//  Recursos del proyecto
import com.example.gonow.tfg.R
import com.example.gonow.data.FirestoreSingleton
import com.example.gonow.modelo.Urinario
import com.example.gonow.viewModel.RutaDrawer
import com.example.gonow.viewModel.UbicacionViewModel
import com.google.android.gms.common.api.ResolvableApiException

// API de ubicación de Google
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationRequest.Builder

//  API de Google Maps
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt


class FragmentComoLlegar : Fragment(R.layout.fragment_mapa_llegar), OnMapReadyCallback {

    // para el mensaje de la ubicacion
    private lateinit var locationSettingsRequest: LocationSettingsRequest
    private val settingsClient by lazy { LocationServices.getSettingsClient(requireContext()) }
    private lateinit var locationResolutionLauncher: ActivityResultLauncher<IntentSenderRequest>


    private val db = FirestoreSingleton.db
    private lateinit var filtro: ImageView
    private lateinit var textViewTiempo: TextView
    private lateinit var botonUbiEstado: ImageView
    private lateinit var mapView: MapView
    private lateinit var rastrear: ImageView
    private lateinit var localizar: ImageView
    private lateinit var ubicacionActual: LatLng
    private lateinit var ubicacionViewModel: UbicacionViewModel
    private lateinit var buscador: EditText
    private var googleMap: GoogleMap? = null
    private var ubicacionActualMostrada = false
    private lateinit var locationCallback: LocationCallback
    private var esCamaraEnMovimiento = false
    private val apiKey = "AIzaSyD_-gB0Qn9fkv5kTLA8dKkvq4zvJSe4B90"
    private lateinit var cerrar : ImageView


    companion object {
        // Constantes para las claves (mejora el mantenimiento)
        private const val ARG_CORDENADAS = "cordenadasbanio"
        private const val ARG_ID_TIPO = "tipo"

        fun newInstance(
            tipo: String,
            cordenadasbanio: LatLng
        ): FragmentComoLlegar {
            val fragment = FragmentComoLlegar()
            val args = Bundle()

            args.putParcelable(ARG_CORDENADAS, cordenadasbanio)
            args.putString(ARG_ID_TIPO, tipo)
            fragment.arguments = args
            return fragment
        }

    }


    // para la actualización de ubicación
    private var locationRequest = Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        2000L // Cada 2 segundos
    )
        .setMinUpdateIntervalMillis(1000L) // Cada 1 segundo si hay nueva ubicación
        .setMaxUpdateDelayMillis(4000L) // No más de 4 segundos entre updates
        .build()

    // Inicializa de forma diferida (lazy) el cliente de ubicación de Google (FusedLocationProviderClient),
    // que se usa para obtener la ubicación actual del dispositivo.
    // Se asocia al contexto de la actividad que contiene el fragmento.

    private val posicion: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    // para los permisos de ubicación
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false

            if (fineLocationGranted) {
                context?.let { context ->
                    ubicacionViewModel.obtenerUbicacionActual(context)
                }
                iniciarActualizacionesUbicacion()
            } else {
                PopUpContenidoGeneral.newInstance(FragmentPopUpPermisos()).show(parentFragmentManager, "popUp")
            }
        }

    // para los filtros
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ubicacionViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[UbicacionViewModel::class.java]

        // maneja la respuesta del usuario para ser un pesado
        locationResolutionLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // El usuario aceptó, ubicación de alta precisión activada

                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.location_dialog_title),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // El usuario rechazó, volver a llamar al método
                comprobarUbicacionAltaPrecision()
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filtro = view.findViewById(R.id.imageViewFiltro)
        mapView = view.findViewById(R.id.mapView)
        botonUbiEstado = view.findViewById(R.id.imageViewBotonUbi)
        rastrear = view.findViewById(R.id.imageViewUbiOn)
        localizar = view.findViewById(R.id.imageViewUbi)
        buscador = view.findViewById(R.id.editTextBuscador)
        cerrar = view.findViewById(R.id.imageViewSalir)
        textViewTiempo = view.findViewById(R.id.textViewTiempo)

        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)


        // Solicitar permisos si no están concedidos
        if (isAdded) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                // Si no se tienen los permisos de ubicación, se piden
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                // Si los permisos ya están concedidos, obtener la ubicación
                context?.let { context ->
                    ubicacionViewModel.obtenerUbicacionActual(context)
                }
                iniciarActualizacionesUbicacion()
            }
        }

        // Observas los cambios de ubicación
        ubicacionViewModel.ubicacionActual.observe(viewLifecycleOwner) { latLng ->
            ubicacionActual = latLng
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

        }

        // Pedir la ubicación inicial
        context?.let { context ->
            ubicacionViewModel.obtenerUbicacionActual(context)
        }


        cerrar.setOnClickListener{
            parentFragmentManager.popBackStack()
        }


    }

    override fun onMapReady(map: GoogleMap) {
        comprobarUbicacionAltaPrecision()
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = false
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false


        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                if (isAdded) {
                    map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            requireContext(),
                            R.raw.mapstyle_night
                        )
                    );
                }
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                if (isAdded) {
                    map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            requireContext(),
                            R.raw.mapstyle_white
                        )
                    );
                }
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                if (isAdded) {
                    map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            requireContext(),
                            R.raw.mapstyle_white
                        )
                    );
                }
            }
        }

        val latLng = arguments?.getParcelable(ARG_CORDENADAS) ?: LatLng(0.0, 0.0)
        val geoPoint = GeoPoint(latLng.latitude, latLng.longitude)
        val tipoUbi = arguments?.getString(ARG_ID_TIPO) ?: ""


        val urinario = Urinario(
            localizacion = geoPoint,
            tipoUbi = tipoUbi
        )


        context?.let { context ->
            ubicacionViewModel.obtenerUbicacionActual(context)
        } // para la ubicación actual
        agregarMarcadorAlMapa(urinario)// para los baños
        iniciarActualizacionesUbicacion()// para la ubicación actual

    }




    // Esta función agrega un marcador al mapa para representar la ubicación de un baño.
    // 1. Obtiene las coordenadas (latitud y longitud) del baño desde su objeto `localizacion`.
    // 2. Si las coordenadas son válidas (no nulas), convierte estas coordenadas en un objeto `LatLng`.
    // 3. Carga diferentes imágenes de marcador según el tipo de baño:
    //    - Se usa un bitmap original (`ubicacionpoint`), un bitmap con una imagen de bar (`ubicafe`), o un bitmap con una imagen predeterminada (`idkubi`).
    //    - Redimensiona las imágenes a un tamaño específico (190x190 píxeles).
    // 4. Según el tipo de baño (`tipoUbi`), selecciona el icono adecuado para el marcador:
    //    - "02" usa el icono de `ubicafe`.
    //    - "06" usa el icono de `idkubi`.
    //    - En caso contrario, usa el icono original (`ubicacionpoint`).
    // 5. Agrega el marcador al mapa con la posición de la ubicación y la descripción del baño.
    // 6. Si la ubicación no es válida (nula), no hace nada.

    private fun agregarMarcadorAlMapa(banio: Urinario) {
        val location = banio.localizacion
        location?.let { geoPoint ->
            val latLng = LatLng(geoPoint.latitude, geoPoint.longitude)
            val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.ubicacionpoint)
            val barBitmap = BitmapFactory.decodeResource(resources, R.drawable.ubicafe)
            val idkBitmap = BitmapFactory.decodeResource(resources, R.drawable.idkubi)
            val scaledoriginalBitmap = Bitmap.createScaledBitmap(originalBitmap, 190, 190, false) // ancho, alto en píxeles
            val scaledbarBitmap = Bitmap.createScaledBitmap(barBitmap, 190, 190, false)
            val scaledidkBitmap = Bitmap.createScaledBitmap(idkBitmap, 190, 190, false)

            val markerIcon = when (banio.tipoUbi) {
                "02" -> BitmapDescriptorFactory.fromBitmap(scaledbarBitmap)
                "06" -> BitmapDescriptorFactory.fromBitmap(scaledidkBitmap)
                else -> BitmapDescriptorFactory.fromBitmap(scaledoriginalBitmap)
            }

            googleMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(banio.descripcion)
                    .icon(markerIcon)
            )

        } ?: run {

        }
    }

    // Esta función inicia las actualizaciones en tiempo real de la ubicación del usuario:
    // 1. Comprueba si se tienen los permisos de ubicación necesarios. Si no, termina sin hacer nada.
    // 2. Activa el botón de "mi ubicación" en el mapa (`isMyLocationEnabled = true`).
    // 3. Crea un `LocationCallback` que se ejecuta cada vez que se recibe una nueva ubicación.
    //    - Actualiza `ubicacionActual` con las nuevas coordenadas.
    //    - Mueve la cámara del mapa a la nueva ubicación si la cámara está en movimiento o aún no se ha mostrado la ubicación.
    //    - Si hay una animación de carga activa, la oculta.
    // 4. Solicita actualizaciones de ubicación usando `requestLocationUpdates` con el `locationRequest` y el `locationCallback` definido.

    private fun iniciarActualizacionesUbicacion() {
        // Verificar si los permisos de ubicación están concedidos
        if (isAdded) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return // Si no se tienen permisos, no continuar
            }
        }

        // Habilitar la ubicación en el mapa
        googleMap?.isMyLocationEnabled = true

        // Crear un LocationCallback para recibir actualizaciones de ubicación
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                ubicacionActual = LatLng(location.latitude, location.longitude)

                // Mueve la cámara solo si es necesario (si la cámara está en movimiento o si la ubicación aún no ha sido mostrada)

                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 17f))
                ubicacionActualMostrada = true
                RutaDrawer.dibujarRuta(
                    map = googleMap!!,
                    origen = ubicacionActual,
                    destino = arguments?.getParcelable(ARG_CORDENADAS) ?: LatLng(0.0, 0.0),
                    apiKey = apiKey,
                    modo = "walking"
                )
                textViewTiempo.text = calcularTiempoEntreDosPuntos(ubicacionActual, arguments?.getParcelable(ARG_CORDENADAS) ?: LatLng(0.0, 0.0)).toString()



            }
        }

        // Solicitar actualizaciones de ubicación utilizando el ViewModel
        context?.let { context ->
            ubicacionViewModel.obtenerUbicacionActual(context)
        }// Obtener la ubicación actual
        ubicacionViewModel.ubicacionActual.observe(viewLifecycleOwner) { nuevaUbicacion ->
            nuevaUbicacion?.let {
                // Actualizar la ubicación mostrada en el mapa
                ubicacionActual = it
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 17f))
                // para que se dibuje nada mas entrar
                RutaDrawer.dibujarRuta(
                    map = googleMap!!,
                    origen = ubicacionActual,
                    destino = arguments?.getParcelable(ARG_CORDENADAS) ?: LatLng(0.0, 0.0),
                    apiKey = apiKey,
                    modo = "walking"
                )
                textViewTiempo.text = calcularTiempoEntreDosPuntos(ubicacionActual, arguments?.getParcelable(ARG_CORDENADAS) ?: LatLng(0.0, 0.0)).toString()

            }
        }



        // Iniciar la solicitud de actualizaciones de ubicación
        posicion.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        if (::locationCallback.isInitialized) {
            posicion.removeLocationUpdates(locationCallback)
        }
    }

    // para evitar que se quede pillada la carga
    override fun onStart() {
        super.onStart()

    }


    // Esta función asegura que la configuración de ubicación del dispositivo esté configurada para obtener una alta precisión:
    // 1. Crea una solicitud de ubicación con la alta precisión como prioridad (Priority.PRIORITY_HIGH_ACCURACY) y un intervalo de actualización de 10 segundos.
    // 2. Configura una solicitud de ajustes de ubicación que garantiza que el usuario tenga la configuración de ubicación necesaria para alta precisión.
    // 3. Realiza una verificación de los ajustes de ubicación mediante `settingsClient.checkLocationSettings`:
    //    - Si los ajustes de ubicación no están configurados correctamente, se muestra un cuadro de diálogo para que el usuario pueda corregirlos.
    //    - Si la excepción que se produce es una `ResolvableApiException`, se intenta resolver la configuración de ubicación mostrando un cuadro de diálogo.
    //    - Si la excepción es otro tipo de error, se muestra un mensaje de advertencia con un `Toast` indicando que la ubicación no está correctamente configurada.

    private fun comprobarUbicacionAltaPrecision() {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000
        ).build()

        locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                        locationResolutionLauncher.launch(intentSenderRequest)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        sendEx.printStackTrace()
                    }
                } else {
                    if (isAdded) {
                    Toast.makeText(requireContext(), getString(R.string.location_dialog_message), Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun calcularTiempoEntreDosPuntos(ubicacionActual: LatLng, ubicacionDestino: LatLng): String {
        val R = 6371.0 // Radio de la Tierra en km
        val lat1 = Math.toRadians(ubicacionActual.latitude)
        val lon1 = Math.toRadians(ubicacionActual.longitude)
        val lat2 = Math.toRadians(ubicacionDestino.latitude)
        val lon2 = Math.toRadians(ubicacionDestino.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distancia = R * c
        val tiempo = distancia / 5 // Velocidad media de 5 km/h
        val tiempoMinutos = (tiempo * 60).roundToInt()
        if(tiempoMinutos > 60){
            val horas = tiempoMinutos / 60
            val minutos = tiempoMinutos % 60
            return "$horas h $minutos min"
        }else if ( tiempoMinutos == 0){
            return getString(com.example.gonow.tfg.R.string.ha_llegado)
        }else{
            return "$tiempoMinutos min"
        }


    }

}