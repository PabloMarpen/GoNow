package com.example.gonow.vista

// Permisos y utilidades del sistema
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint


class FragmentMapa : Fragment(R.layout.fragment_mapa), OnMapReadyCallback {

    // para el mensaje de la ubicacion
    private lateinit var locationSettingsRequest: LocationSettingsRequest
    private val settingsClient by lazy { LocationServices.getSettingsClient(requireContext()) }
    private lateinit var locationResolutionLauncher: ActivityResultLauncher<IntentSenderRequest>


    private val db = FirestoreSingleton.db
    private lateinit var filtro: ImageView
    private lateinit var botonUbiEstado: ImageView
    private lateinit var mapView: MapView
    private lateinit var rastrear: ImageView
    private lateinit var localizar: ImageView
    private lateinit var ubicacionActual: LatLng
    private lateinit var ubicacionViewModel: UbicacionViewModel
    private lateinit var manejoCarga: ManejoDeCarga
    private lateinit var buscador: EditText
    private var googleMap: GoogleMap? = null
    private var ubicacionActualMostrada = false
    private lateinit var locationCallback: LocationCallback
    private var esCamaraEnMovimiento = false
    // baños cargados
    private var baniosCargados = false
    // filtros
    private var tipoUbiSeleccionado : String? = null
    private var estrellas : Float? = null
    private var discapacitados : Boolean? = null
    private var unisex : Boolean? = null
    private var gratis : Boolean? = null
    //buscador
    private var busqueda = ""
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

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
                ubicacionViewModel.obtenerUbicacionActual(requireContext())
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
                Toast.makeText(requireContext(), getString(R.string.location_dialog_title), Toast.LENGTH_SHORT).show()
            } else {
                // El usuario rechazó, volver a llamar al método
                comprobarUbicacionAltaPrecision()
            }
        }

        requireActivity().supportFragmentManager.setFragmentResultListener("filtros", this) { _, bundle ->
            val tipoUbiSeleccionado = bundle.getString("tipo_ubicacion")
            val estrellas = bundle.getFloat("estrellas")
            val discapacitados = bundle.getBoolean("discapacitados")
            val unisex = bundle.getBoolean("unisex")
            val gratis = bundle.getBoolean("gratis")

            this.tipoUbiSeleccionado = tipoUbiSeleccionado.toString()
            this.estrellas = estrellas
            this.discapacitados = discapacitados
            this.unisex = unisex
            this.gratis = gratis

            // se guardan los filtros
            val prefs = requireContext().getSharedPreferences("filtros", Context.MODE_PRIVATE)
            with(prefs.edit()) {
                putString("tipo_ubicacion", tipoUbiSeleccionado)
                putFloat("estrellas", estrellas)
                putBoolean("discapacitados", discapacitados)
                putBoolean("unisex", unisex)
                putBoolean("gratis", gratis)
                apply()
            }

            googleMap?.clear()
            obtenerYMostrarBanios()
        }
        // se recogen los filtros
        val prefs = requireContext().getSharedPreferences("filtros", Context.MODE_PRIVATE)
        tipoUbiSeleccionado = prefs.getString("tipo_ubicacion", "") ?: ""
        estrellas = prefs.getFloat("estrellas", 0f)
        discapacitados = prefs.getBoolean("discapacitados", false)
        unisex = prefs.getBoolean("unisex", false)
        gratis = prefs.getBoolean("gratis", false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filtro = view.findViewById(R.id.imageViewFiltro)
        mapView = view.findViewById(R.id.mapView)
        botonUbiEstado = view.findViewById(R.id.imageViewBotonUbi)
        rastrear = view.findViewById(R.id.imageViewUbiOn)
        localizar = view.findViewById(R.id.imageViewUbi)
        buscador = view.findViewById(R.id.editTextBuscador)

        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        // Solicitar permisos si no están concedidos
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {

            // Si no se tienen los permisos de ubicación, se piden
            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else {
            // Si los permisos ya están concedidos, obtener la ubicación
            ubicacionViewModel.obtenerUbicacionActual(requireContext())
            iniciarActualizacionesUbicacion()
        }

        // Observas los cambios de ubicación
        ubicacionViewModel.ubicacionActual.observe(viewLifecycleOwner) { latLng ->
            ubicacionActual = latLng
            if (esCamaraEnMovimiento || !ubicacionActualMostrada) {
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                ubicacionActualMostrada = true
            }
        }

        // Pedir la ubicación inicial
        ubicacionViewModel.obtenerUbicacionActual(requireContext())

        //manejo de la carga
        manejoCarga = ManejoDeCarga(
            parentFragmentManager,
            timeoutMillis = 20000L
        ){
            Toast.makeText(requireContext(), getString(R.string.error_muchotiempo), Toast.LENGTH_SHORT).show()
        }

        filtro.setOnClickListener {
            if (tipoUbiSeleccionado != null &&
                estrellas != null &&
                discapacitados != null &&
                unisex != null &&
                gratis != null
            ) {
                val filtroFragment = FragmentPopUpFiltro.newInstance(
                    switchUnisexDato = unisex!!,
                    switchDiscapacitadosDato = discapacitados!!,
                    switchGratisDato = gratis!!,
                    tipoUbiSeleccionado = tipoUbiSeleccionado!!,
                    estrellasDato = estrellas!!
                )

                PopUpContenidoGeneral.newInstance(filtroFragment)
                    .show(parentFragmentManager, "popUp")
            } else {
                // Si no hay datos guardados, abrimos el popup vacío
                val filtroFragment = FragmentPopUpFiltro()
                PopUpContenidoGeneral.newInstance(filtroFragment)
                    .show(parentFragmentManager, "popUp")
            }
        }

        buscador.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                runnable?.let { handler.removeCallbacks(it) }

                runnable = Runnable {
                    val texto = s.toString()
                    busqueda = texto
                    googleMap?.clear()
                    obtenerYMostrarBanios(texto)
                }

                handler.postDelayed(runnable!!, 500) // Espera 300ms después de que el usuario deje de escribir
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //boton de localizacion
        rastrear.visibility = View.GONE
        localizar.visibility = View.VISIBLE
        botonUbiEstado.setOnClickListener {
            if(esCamaraEnMovimiento){
                rastrear.visibility = View.GONE
                localizar.visibility = View.VISIBLE
                esCamaraEnMovimiento = false
                Toast.makeText(requireContext(), getString(R.string.modo_libre), Toast.LENGTH_SHORT).show()
                ubicacionViewModel.obtenerUbicacionActual(requireContext())
            }else{
                rastrear.visibility = View.VISIBLE
                localizar.visibility = View.GONE
                esCamaraEnMovimiento = true
                Toast.makeText(requireContext(), getString(R.string.siguiendo_usuario), Toast.LENGTH_SHORT).show()
                ubicacionViewModel.obtenerUbicacionActual(requireContext())
            }
        }

    }

    override fun onMapReady(map: GoogleMap) {
        comprobarUbicacionAltaPrecision()
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

        manejoCarga.mostrarCarga(getString(R.string.cargandobanios))
        ubicacionViewModel.obtenerUbicacionActual(requireContext()) // para la ubicación actual
        obtenerYMostrarBanios() // para los baños
        iniciarActualizacionesUbicacion()// para la ubicación actual
    }

    private fun mostrarDatosBanio(marker: Marker) {
        manejoCarga.mostrarCarga(getString(R.string.cargandobanio))
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
                            nombre = it.nombre ?: getString(R.string.nombre_no_disponible),
                            descripcion = it.descripcion ?: getString(R.string.sin_descripcion),
                            horario = it.horario,
                            puntuacion = it.puntuacion ?: 0.0,
                            sinhorario = it.sinhorario,
                            etiquetas = it.etiquetas ?: emptyList(),
                            cordenadasbanio = LatLng(it.localizacion?.latitude ?: 0.0, it.localizacion?.longitude ?: 0.0),
                            ubicacionUsuario = ubicacionActual,
                            imagen = it.foto,
                            creador = it.creador,
                            idDocumento = documents.documents[0].id,
                            tipo = it.tipoUbi ?: "Sin tipo",
                            mediaPuntuacion = it.mediaPuntuacion ?: 0.0,
                            totalPuntuaciones = it.totalCalificaciones ?: 0
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
                Toast.makeText(context, getString(R.string.error_obtener_datos), Toast.LENGTH_SHORT).show()
            }

    }

    private fun obtenerYMostrarBanios(textoBuscar : String? = null) {
        var seHanEncontradoBanios = false
        val textoBuscarLimpio = textoBuscar?.trimEnd()
        db.collection("urinarios")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val banio = document.toObject(Urinario::class.java)

                    // Comprobar si el baño cumple los filtros
                    val cumpleFiltros = cumpleFiltros(banio)

                    // Verificar si el nombre contiene el texto de búsqueda (si existe)
                    val cumpleBusquedaNombre = textoBuscarLimpio?.let { banio.nombre?.contains(it, ignoreCase = true) } ?: true
                    val cumpleBusquedaDescripcion = textoBuscarLimpio?.let { banio.descripcion?.contains(it, ignoreCase = true) } ?: true

                    // Si cumple ambos criterios, agregar el marcador
                    if (cumpleFiltros && (cumpleBusquedaNombre || cumpleBusquedaDescripcion)) {
                        agregarMarcadorAlMapa(banio)
                        seHanEncontradoBanios = true
                    }
                }

                if (!seHanEncontradoBanios) {
                    Toast.makeText(context, getString(R.string.no_hay_banios), Toast.LENGTH_SHORT).show()
                }
                baniosCargados = true
                manejoCarga.ocultarCarga()
            }
            .addOnFailureListener {
                manejoCarga.ocultarCarga()
                Toast.makeText(context, getString(R.string.error_cargar_banios), Toast.LENGTH_SHORT).show()
            }
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

        // Habilitar la ubicación en el mapa
        googleMap?.isMyLocationEnabled = true

        // Crear un LocationCallback para recibir actualizaciones de ubicación
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                ubicacionActual = LatLng(location.latitude, location.longitude)

                // Mueve la cámara solo si es necesario (si la cámara está en movimiento o si la ubicación aún no ha sido mostrada)
                if (esCamaraEnMovimiento || !ubicacionActualMostrada) {
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15f))
                    ubicacionActualMostrada = true
                }

                // Si hay una animación de carga activa, ocultarla
                if (manejoCarga.estaCargando()) {
                    manejoCarga.ocultarCarga()
                }
            }
        }

        // Solicitar actualizaciones de ubicación utilizando el ViewModel
        ubicacionViewModel.obtenerUbicacionActual(requireContext()) // Obtener la ubicación actual
        ubicacionViewModel.ubicacionActual.observe(viewLifecycleOwner) { nuevaUbicacion ->
            nuevaUbicacion?.let {
                // Actualizar la ubicación mostrada en el mapa
                ubicacionActual = it
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
            }
        }

        // Iniciar la solicitud de actualizaciones de ubicación
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

    // para evitar que se quede pillada la carga
    override fun onStart() {
        super.onStart()
        if(baniosCargados){
            manejoCarga.ocultarCarga()

        }
    }

    private fun cumpleFiltros(banio: Urinario): Boolean {
        val etiquetas = banio.etiquetas ?: emptyList()

        if (tipoUbiSeleccionado != null && tipoUbiSeleccionado != "" && tipoUbiSeleccionado != "null" && tipoUbiSeleccionado != banio.tipoUbi) return false
        if(banio.mediaPuntuacion == 0.0){
            if (estrellas != null && banio.puntuacion!! < estrellas!!) return false
        }else{
            if (estrellas != null && banio.mediaPuntuacion!! < estrellas!!) return false
        }
        if (discapacitados == true && !etiquetas.any { it.equals("01", ignoreCase = true) }) return false
        if (unisex == true && !etiquetas.any { it.equals("02", ignoreCase = true) }) return false
        if (gratis == true && !etiquetas.any { it.equals("05", ignoreCase = true) }) return false

        return true
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
                    Toast.makeText(requireContext(), getString(R.string.location_dialog_message), Toast.LENGTH_SHORT).show()
                }
            }
    }

}
