package com.example.gonow.vista

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Switch
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gonow.tfg.R
import com.example.gonow.modelo.Urinario
import java.util.Locale
import android.location.Geocoder
import android.media.ExifInterface
import com.google.android.gms.location.LocationRequest
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.gonow.data.AuthSingleton
import com.example.gonow.data.FirestoreSingleton
import com.example.gonow.viewModel.UbicacionViewModel
import android.graphics.Matrix

import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import kotlinx.coroutines.CoroutineScope
import java.io.FileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentAniadir : Fragment(R.layout.fragment_aniadir){

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequest: LocationSettingsRequest
    private val settingsClient by lazy { LocationServices.getSettingsClient(requireContext()) }
    private lateinit var locationResolutionLauncher: ActivityResultLauncher<IntentSenderRequest>

    private lateinit var botonPublicar: Button
    private lateinit var botonTipoUbicacion: Button
    private lateinit var botonAñadirHorario: Button

    // Campos rellenables
    private lateinit var botonAñadirImagen: ImageView
    private lateinit var textoDescripcion: EditText
    private lateinit var textoNombre: EditText
    private lateinit var ratingBar: RatingBar

    // Switches
    private lateinit var switchAcesibilidad: Switch
    private lateinit var switchUnisex: Switch
    private lateinit var switchJabon: Switch
    private lateinit var switchPapel: Switch
    private lateinit var switchGratis: Switch
    private lateinit var switchCambiar: Switch
    private lateinit var imagenAccesibilidad: ImageView
    private lateinit var imagenJabon: ImageView
    private lateinit var imagenPapel: ImageView
    private lateinit var imagenCambiar: ImageView

    //imagen
    private lateinit var photoUri: Uri
    private var photoFile: File? = null
    lateinit var ubicacionActual : LatLng
    private lateinit var ubicacionViewModel: UbicacionViewModel
    var horaApertura : String? = null
    var horaCierre : String? = null
    var abiertoSiempre : Boolean = true
    var noSeElHorario : Boolean = false
    var tieneHorario : Boolean = false
    var tipoUbiSeleccionado : String? = null
    private lateinit var manejoCarga: ManejoDeCarga


    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            // El usuario tomó la foto
            foto = photoFile?.name
            botonAñadirImagen.setImageURI(photoUri)
            Log.d("FOTO", "Foto tomada: $foto")
        } else {
            // El usuario canceló
            photoFile?.delete()
            photoFile = null
            foto = null
            botonAñadirImagen.setImageResource(R.drawable.aniadirimagen)
            Log.d("FOTO", "El usuario canceló la cámara")
        }
    }


    // para editar un baño
    private var idBanio: String? = null
    private var puntuacionOriginal: Double? = null
    private var nombreDato : String? = null
    private var descripcion : String? = null
    private var etiquetas : List<String>? = null
    private var foto : String? = null
    private var tipo: String? = null
    private var horario : Map<String, String?>? = null
    private var sinhorario: String? = null
    private var esEditar: Boolean = false
    private var horaapeturadatoeditar : String? = null
    private var horacierredatoeditar : String? = null

    companion object {
        // Constantes para las claves (mejora el mantenimiento)
        private const val KEY_ID_BANIO = "idBanio"
        private const val KEY_PUNTUACION_ORIGINAL = "puntuacionOriginal"
        private const val KEY_NOMBRE = "nombre"
        private const val KEY_DESCRIPCION = "descripcion"
        private const val KEY_ETIQUETAS = "etiquetas"
        private const val KEY_FOTO = "foto"
        private const val KEY_TIPO = "tipo"
        private const val KEY_HORAPERTURA = "horapertura"
        private const val KEY_HORACIERRE = "horacierre"
        private const val KEY_SINHORARIO = "sinhorario"
        private const val KEY_ES_EDITAR = "esEditar"

        fun newInstance(
            nombre: String?,
            esEditar: Boolean,
            puntuacionOriginal: Double? = null,
            descripcion: String? = null,
            tipo: String? = null,
            sinHorario: String? = null,
            horaApertura: String? = null,
            horaCierre: String? = null,
            etiquetas: List<String>? = null,
            foto: String? = null,
            idBanio: String? = null
        ): FragmentAniadir {
            return FragmentAniadir().apply {
                arguments = Bundle().apply {
                    putString(KEY_NOMBRE, nombre)
                    putBoolean(KEY_ES_EDITAR, esEditar)
                    putDouble(KEY_PUNTUACION_ORIGINAL, puntuacionOriginal ?: 0.0)
                    putString(KEY_DESCRIPCION, descripcion)
                    putString(KEY_TIPO, tipo)
                    putString(KEY_SINHORARIO, sinHorario)
                    putString(KEY_HORAPERTURA, horaApertura)
                    putString(KEY_HORACIERRE, horaCierre)
                    putStringArrayList(KEY_ETIQUETAS, etiquetas?.let { ArrayList(it) })
                    putString(KEY_FOTO, foto)
                    putString(KEY_ID_BANIO, idBanio)
                }
            }
        }
    }


    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            lanzarCamara()
        } else {
            Toast.makeText(requireContext(), getString(R.string.permisocamaradenegado), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // para evitar que de el error del bundel al pasarle muchos datos de golpe (la foto) lo he pasado a null en vez de "savedInstanceState"
        // desactivando una función útil de Android (guardar y restaurar el estado al rotar o al cerrar/reabrir).
        super.onCreate(null)

        locationResolutionLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // El usuario aceptó, ubicación de alta precisión activada
                Toast.makeText(requireContext(), getString(R.string.location_dialog_title), Toast.LENGTH_SHORT).show()
                // si se activa te devulve a la pantalla de mapa
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame, FragmentAniadir())
                    .addToBackStack(null)
                    .commit()
            } else {
                // El usuario rechazó, volver a llamar al método
                comprobarUbicacionAltaPrecision()
            }
        }


        arguments?.let {
            nombreDato = it.getString(KEY_NOMBRE)
            esEditar = it.getBoolean(KEY_ES_EDITAR)
            puntuacionOriginal = it.getDouble(KEY_PUNTUACION_ORIGINAL)
            descripcion = it.getString(KEY_DESCRIPCION)
            tipo = it.getString(KEY_TIPO)
            sinhorario = it.getString(KEY_SINHORARIO)
            horaapeturadatoeditar = it.getString(KEY_HORAPERTURA)
            horacierredatoeditar = it.getString(KEY_HORACIERRE)
            etiquetas = it.getStringArrayList(KEY_ETIQUETAS)
            foto = it.getString(KEY_FOTO)
            idBanio = it.getString(KEY_ID_BANIO)
        } ?: run {
            requireActivity().finish()
        }

        requireActivity().supportFragmentManager.setFragmentResultListener("horario", this) { _, bundle ->
            // Recibimos los datos del fragmento y los asignamos a las variables
            val horaApertura = bundle.getString("hora_apertura")
            val horaCierre = bundle.getString("hora_cierre")
            val abiertoSiempre = bundle.getBoolean("abiertoSiempre")
            val noSeElHorario = bundle.getBoolean("noSeElHorario")
            val tieneHorario = bundle.getBoolean("tieneHorario")

            this.horaApertura = horaApertura.toString()
            this.horaCierre = horaCierre.toString()
            this.abiertoSiempre = abiertoSiempre
            this.noSeElHorario = noSeElHorario
            this.tieneHorario = tieneHorario
        }
        requireActivity().supportFragmentManager.setFragmentResultListener("ubicacion", this) { _, bundle ->
            val tipoUbiSeleccionado = bundle.getString("tipo_ubicacion")

            this.tipoUbiSeleccionado = tipoUbiSeleccionado.toString()
        }

    }

    @SuppressLint("ClickableViewAccessibility", "UseSwitchCompatOrMaterialCode")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        botonPublicar = view.findViewById(R.id.botonPublicar)
        botonTipoUbicacion = view.findViewById(R.id.botonUbicacion)
        botonAñadirHorario = view.findViewById(R.id.botonHorario)

        botonAñadirImagen = view.findViewById(R.id.imageViewAñadirImagen)
        textoDescripcion = view.findViewById(R.id.EditTextDescripcion)
        textoNombre = view.findViewById(R.id.EditTextNombre)
        ratingBar = view.findViewById(R.id.ratingBar)
        val scrollView = view.findViewById<ScrollView>(R.id.scrollView)
        val textoNombreBanio = view.findViewById<TextView>(R.id.textViewTitulo)

        switchAcesibilidad = view.findViewById(R.id.switchAcesibilidad)
        switchUnisex = view.findViewById(R.id.switchUnisex)
        switchJabon = view.findViewById(R.id.switchJabon)
        switchPapel = view.findViewById(R.id.switchPapel)
        switchGratis = view.findViewById(R.id.switchGratis)
        switchCambiar = view.findViewById(R.id.switchCambiar)

        imagenAccesibilidad = view.findViewById(R.id.imageViewAcesibilidad)
        imagenJabon = view.findViewById(R.id.imageViewUnisex)
        imagenPapel = view.findViewById(R.id.imageViewPapel)
        imagenCambiar = view.findViewById(R.id.imageViewCambiar)

        imagenAccesibilidad.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.toast_accesibilidad), Toast.LENGTH_SHORT).show()
        }

        imagenJabon.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.toast_jabon), Toast.LENGTH_SHORT).show()
        }

        imagenPapel.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.toast_papel), Toast.LENGTH_SHORT).show()
        }

        imagenCambiar.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.toast_bebe), Toast.LENGTH_SHORT).show()
        }



        ubicacionViewModel = ViewModelProvider(this)[UbicacionViewModel::class.java]

        comprobarUbicacionAltaPrecision()

        manejoCarga = ManejoDeCarga(parentFragmentManager, 10000L) {
            textoNombre.setText(getString(R.string.banio))
            Toast.makeText(requireContext(), getString(R.string.ubicacion_no_obtenida), Toast.LENGTH_SHORT).show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            mostrarConfirmacionDeSalida()
        }


        // para que se marque el campo cuando se toca y que se mueva la pantalla
        textoDescripcion.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                scrollView.post {
                    scrollView.smoothScrollTo(0, textoDescripcion.top)
                }
            }
        }
        // para que se marque el campo cuando se toca y que se mueva la pantalla
        textoNombre.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                scrollView.post {
                    scrollView.smoothScrollTo(0, textoDescripcion.top)
                }
            }
        }

        // si le pasamos editar al fragment se cargan los datos pasados si no, se carga la ubicación actual
        if(esEditar){
            botonPublicar.text = getString(R.string.actualizarbanio)
            textoNombreBanio.text = getString(R.string.actualizar)
            textoNombre.setText(nombreDato)
            ratingBar.rating = puntuacionOriginal!!.toFloat()
            textoDescripcion.setText(descripcion)
            tipoUbiSeleccionado = tipo
            if(horaApertura == "null" || horaCierre == "null" || sinhorario != "null"){
                if(sinhorario == getString(R.string.abiertosiempre)){
                   abiertoSiempre = true
                    noSeElHorario = false
                    tieneHorario = false
                    horaApertura = "null"
                    horaCierre = "null"
                }
                if(sinhorario == getString(R.string.noSeElHorario)){
                    noSeElHorario = true
                    abiertoSiempre = false
                    tieneHorario = false
                    horaApertura = "null"
                    horaCierre = "null"
                }
            }else{
                tieneHorario = true
                abiertoSiempre = false
                noSeElHorario = false
                horaApertura = horaapeturadatoeditar
                horaCierre = horacierredatoeditar
            }
            if (etiquetas != null && etiquetas!!.isNotEmpty()) {
                for (etiqueta in etiquetas!!) {

                    when (etiqueta) {
                        "01" -> switchAcesibilidad.isChecked = true
                        "02" -> switchUnisex.isChecked = true
                        "03" -> switchJabon.isChecked = true
                        "04" -> switchPapel.isChecked = true
                        "05" -> switchGratis.isChecked = true
                        "06" -> switchCambiar.isChecked = true
                    }

                }
            }

            // mostrar la imagen si no es null o vacio

            Toast.makeText(requireContext(), foto, Toast.LENGTH_SHORT).show()
            if (!foto.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load("https://pablommp.myvnc.com/gonowfotos/${foto}")
                    .into(botonAñadirImagen)
            }

        }else{
            ratingBar.rating = 5f
            //Obtener la ubicación actual con Geolocalización por nombre de la calle
            actualizarNombreDesdeUbicacion(manejoCarga)
        }


        botonAñadirImagen.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                lanzarCamara()
            }
        }

        // cambiar color al pulsar
        botonTipoUbicacion.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.secondaryVariant)
                )
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.secondary)
                )
            }
            false
        }

        botonAñadirHorario.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.secondaryVariant)
                )
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.secondary)
                )
            }
            false
        }

        botonPublicar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.secondaryVariant
                    )
                )

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.secondary)
                )
            }
            false
        }
        // fin de cambiar color al pulsar

        botonTipoUbicacion.setOnClickListener{
            // cargamos el popup seleccionando nuestra interfaz y pasamos los datos
            val tipoUbicacionFragmento = FragmentPopUpTipoUbicacion.nuevaInstancia(
                tipoSeleccionado = tipoUbiSeleccionado
            )
            PopUpContenidoGeneral.newInstance(tipoUbicacionFragmento).show(parentFragmentManager, "popUp")
        }

        botonAñadirHorario.setOnClickListener {
            // le pasamos los datos al fragment del horario si ya se habian guardado previamente
            val horarioFragment = FragmentPopUpHorario.nuevaInstancia(
                switchAbierto = abiertoSiempre,
                noSeElHorario = noSeElHorario,
                switchTieneHorario = tieneHorario,
                horaApertura = horaApertura,
                horaCierre = horaCierre
            )

            PopUpContenidoGeneral.newInstance(horarioFragment).show(parentFragmentManager, "popUp")

        }

        botonPublicar.setOnClickListener {
                if (validarCampos()) {
                    manejoCarga.mostrarCarga(getString(R.string.publicando))
                    Toast.makeText(requireContext(), getString(R.string.publicandomensaje), Toast.LENGTH_SHORT).show()
                    // Crear el objeto horario
                    val horario = mapOf(
                        "apertura" to horaApertura,
                        "cierre" to horaCierre
                    )
                    // Crear la lista de etiquetas
                    val etiquetas = listOf(
                        if (switchAcesibilidad.isChecked) "01" else "10", // 01 = Accesible, 10 = No accesible
                        if (switchUnisex.isChecked) "02" else "20",       // 02 = Unisex, 20 = No unisex
                        if (switchJabon.isChecked) "03" else "30",        // 03 = Con jabón, 30 = Sin jabón
                        if (switchPapel.isChecked) "04" else "40",        // 04 = Con papel, 40 = Sin papel
                        if (switchGratis.isChecked) "05" else "50",       // 05 = Gratis, 50 = De pago
                        if (switchCambiar.isChecked) "06" else "60"       // 06 = Con zona bebé, 60 = Sin zona bebé
                    )

                    val sinhorario = if (horaApertura != null && horaCierre != null) {
                        when {
                            abiertoSiempre -> getString(R.string.abiertosiempre)
                            noSeElHorario -> getString(R.string.noSeElHorario)
                            else -> "null"
                        }
                    } else {
                        "null"  // Si horaApertura o horaCierre son nulos
                    }

                    val compressedFile = if (photoFile != null) {
                        compressAndResizeImageToFile(photoFile!!, 1024 * 500) // 500 KB
                    } else {
                        null
                    }

                    if (compressedFile != null) {

                        val rutaRemota = "/var/www/html/gonowfotos/${photoFile!!.name}"
                        CoroutineScope(Dispatchers.Main).launch {
                            val exito = subirFotoASftp(
                                servidor = "pablommp.myvnc.com",
                                usuario = "pablo",
                                contrasena = "YWzWDneybJmxN5Waz4heP7",
                                rutaRemota = rutaRemota,
                                archivoLocal = compressedFile
                            )

                            if (!exito) {
                                Log.e("SFTP", "Error al subir la imagen al SFTP")
                            } else {
                                Log.d("SFTP", "Imagen subida exitosamente a $rutaRemota")
                            }
                        }
                    }



                    // si estamos editando hacemos una actualizacion a la base de datos de lo contrario creamos uno nuevo
                    if(esEditar){
                        var nombreFoto = photoFile?.name
                        if (nombreFoto == null && foto != null) {
                            nombreFoto = foto
                        }
                        // Crear el objeto con los datos editados
                        val banioActualizado = mapOf(
                            "nombre" to textoNombre.text.toString(),
                            "sinhorario" to sinhorario,
                            "descripcion" to textoDescripcion.text.toString(),
                            "etiquetas" to etiquetas,
                            "foto" to nombreFoto,
                            "horario" to horario,
                            "puntuacion" to ratingBar.rating.toDouble(),
                            "tipoUbi" to tipoUbiSeleccionado
                        )

                        // Actualizar el baño en Firestore
                        idBanio?.let { it1 ->
                            FirestoreSingleton.db.collection("urinarios")
                                .document(it1)  // Usamos el ID del baño existente para actualizarlo
                                .update(banioActualizado)  // Solo actualizamos los datos modificados
                                .addOnSuccessListener {
                                    val rutaImagenRemota = "/var/www/html/gonowfotos/${arguments?.getString(KEY_FOTO)}"
                                    if (rutaImagenRemota.isNotEmpty()) {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            borrarArchivoSftp(
                                                servidor = "pablommp.myvnc.com",
                                                usuario = "pablo",
                                                contrasena = "YWzWDneybJmxN5Waz4heP7",
                                                rutaRemota = rutaImagenRemota
                                            )

                                        }
                                    }
                                    manejoCarga.ocultarCarga()
                                    Toast.makeText(requireContext(), getString(R.string.actualizadobien), Toast.LENGTH_SHORT).show()
                                    requireActivity().supportFragmentManager.beginTransaction()
                                        .replace(R.id.frame, FragmentMapa())
                                        .addToBackStack(null)
                                        .commit()
                                }
                                .addOnFailureListener { e ->
                                    manejoCarga.ocultarCarga()
                                    Toast.makeText(requireContext(), getString(R.string.error_actualizarbanio), Toast.LENGTH_SHORT).show()
                                    Log.e("Firestore", "Error al actualizar", e)
                                }
                        }
                    }else{
                        val nombreFoto = photoFile?.name
                        ubicacionViewModel.ubicacionActual.value?.let { latLng ->
                            val banio = Urinario(
                                nombre = textoNombre.text.toString(),
                                sinhorario = sinhorario,
                                creador = AuthSingleton.auth.currentUser?.uid ?: "",
                                descripcion = textoDescripcion.text.toString(),
                                etiquetas = etiquetas,
                                foto = nombreFoto,
                                horario = horario,
                                localizacion = GeoPoint(latLng.latitude, latLng.longitude),
                                tipoUbi = tipoUbiSeleccionado,
                                puntuacion = ratingBar.rating.toDouble(),
                            )

                            // Guardar en Firestore con el UID del usuario como ID
                            FirestoreSingleton.db.collection("urinarios")
                                .add(banio)
                                .addOnSuccessListener { documentReference ->
                                    manejoCarga.ocultarCarga()
                                    Toast.makeText(requireContext(), getString(R.string.publicacion_exitosa), Toast.LENGTH_SHORT).show()
                                    requireActivity().supportFragmentManager.beginTransaction()
                                        .replace(R.id.frame, FragmentMapa())
                                        .addToBackStack(null)
                                        .commit()
                                }
                                .addOnFailureListener { e ->
                                    manejoCarga.ocultarCarga()
                                    Toast.makeText(requireContext(), getString(R.string.error_al_publicar), Toast.LENGTH_SHORT).show()
                                    Log.e("Firestore", "Error al publicar", e)
                                }
                        } ?: run {
                            Toast.makeText(requireContext(), getString(R.string.ubicacion_no_obtenida), Toast.LENGTH_SHORT).show()
                        }



                    }


                }

            }



    }

    private fun getFullAddress(context: Context, lat: Double, lng: Double): Map<String, String> {
        // Geocodificación inversa para obtener la dirección
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            addresses?.firstOrNull()?.let { address ->
                mapOf(
                    "calle" to (address.thoroughfare ?: ""),
                    "numero" to (address.subThoroughfare ?: ""),
                    "ciudad" to (address.locality ?: ""),
                )
            } ?: emptyMap()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    // validacion de campos para publicar
    private fun validarCampos(): Boolean {
        return when {
            textoNombre.text.toString().isEmpty() -> {
                Toast.makeText(context, getString(R.string.error_nombre_vacio), Toast.LENGTH_SHORT).show()
                false
            }
            ratingBar.rating < 0.5 -> {
                Toast.makeText(context, getString(R.string.error_puntuacion_minima), Toast.LENGTH_SHORT).show()
                false
            }
            horaApertura == null && horaCierre == null && sinhorario == null -> {
                Toast.makeText(context, getString(R.string.error_falta_horario), Toast.LENGTH_SHORT).show()
                false
            }
            tipoUbiSeleccionado == null -> {
                Toast.makeText(context, getString(R.string.error_falta_tipo_ubicacion), Toast.LENGTH_SHORT).show()
                false
            }

            else -> true
        }
    }

// creamos la imagen temporalmente en memoria
    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_$timestamp", ".jpg", storageDir)
    }

    private fun lanzarCamara() {
        photoFile = createImageFile()
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "com.example.gonow.fileprovider",
            photoFile!!
        )
        cameraLauncher.launch(photoUri)
    }



    override fun onDestroyView() {
        // para evitar que al cambiar de tema oscuro crashe
        if (::manejoCarga.isInitialized) {
            manejoCarga.ocultarCarga()
        }
        super.onDestroyView()
    }


    private fun compressAndResizeImageToFile(originalFile: File, maxSize: Int): File? {
        val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath) ?: return null
        // Rotar la imagen si es necesario
        val rotatedBitmap = rotateImageIfRequired(originalFile, bitmap)

        val width = rotatedBitmap.width
        val height = rotatedBitmap.height
        val ratio = width.toFloat() / height.toFloat()

        var newWidth = 600
        var newHeight = (newWidth / ratio).toInt()

        if (width > height) {
            newHeight = (newWidth / ratio).toInt()
        } else {
            newWidth = (newHeight * ratio).toInt()
        }

        val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, newWidth, newHeight, true)

        val byteArrayOutputStream = ByteArrayOutputStream()
        var quality = 70

        resizedBitmap.compress(Bitmap.CompressFormat.WEBP, quality, byteArrayOutputStream)

        while (byteArrayOutputStream.size() > maxSize && quality > 10) {
            byteArrayOutputStream.reset()
            quality -= 5
            resizedBitmap.compress(Bitmap.CompressFormat.WEBP, quality, byteArrayOutputStream)
        }

        // Crear un nuevo archivo temporal para guardar la imagen comprimida
        val compressedFile = File(originalFile.parent, "compressed_${originalFile.name}")
        compressedFile.outputStream().use {
            it.write(byteArrayOutputStream.toByteArray())
        }

        return compressedFile
    }


    // para rotar la imagen si es necesario
    private fun rotateImageIfRequired(file: File, bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }



    // popup para evitar que se borren los datos de añadir cuando cambias de fragment
    private fun mostrarConfirmacionDeSalida() {
        val mensaje = getString(R.string.mensajeConfirmacion1) + "\n\n" + getString(R.string.mensajeConfirmacion2)
        val popup = PopUp.newInstance(mensaje)
        popup.setOnAcceptListener { isConfirmed ->
            if (isConfirmed) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame, FragmentMapa())
                    .addToBackStack(null)
                    .commit()
            }
        }
        popup.show(parentFragmentManager, "popUp")
    }

    private fun actualizarNombreDesdeUbicacion(manejoCarga: ManejoDeCarga) {
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

        manejoCarga.mostrarCarga(getString(R.string.cargandoNombreCalle))

        ubicacionViewModel.obtenerUbicacionActual(requireContext())

        ubicacionViewModel.ubicacionActual.observe(viewLifecycleOwner) { latLng ->
            if (latLng != null) {
                val direccion = getFullAddress(requireContext(), latLng.latitude, latLng.longitude)
                val calle = direccion["calle"] ?: ""
                val numero = direccion["numero"] ?: ""
                val ciudad = direccion["ciudad"] ?: ""

                val nombre = getString(R.string.banio) + " " + calle + " " + numero + " " + ciudad
                textoNombre.setText(nombre.trim())
            } else {
                textoNombre.setText(getString(R.string.banio))
                Toast.makeText(requireContext(), getString(R.string.ubicacion_no_obtenida), Toast.LENGTH_SHORT).show()
            }
            manejoCarga.ocultarCarga()
        }
    }

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
            .addOnSuccessListener {

            }
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

    private fun borrarArchivoSftp(
        servidor: String,
        puerto: Int = 8022,
        usuario: String,
        contrasena: String,
        rutaRemota: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                try {
                    Log.d("SFTP", "Iniciando conexión con el servidor SFTP: $servidor en el puerto $puerto")
                    val jsch = JSch()
                    val session = jsch.getSession(usuario, servidor, puerto)
                    session.setPassword(contrasena)
                    session.setConfig("StrictHostKeyChecking", "no")
                    session.connect()

                    Log.d("SFTP", "Conexión establecida con éxito")

                    val channel = session.openChannel("sftp") as ChannelSftp
                    channel.connect()

                    Log.d("SFTP", "Canal SFTP abierto con éxito")

                    // Intentamos borrar el archivo
                    channel.rm(rutaRemota)  // ⬅️ Aquí se borra el archivo
                    Log.d("SFTP", "Archivo borrado con éxito: $rutaRemota")

                    channel.disconnect()
                    session.disconnect()
                    Log.d("SFTP", "Conexión y canal desconectados")
                } catch (e: Exception) {
                    Log.e("SFTP", "Error al borrar el archivo: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    suspend fun subirFotoASftp(
        servidor: String,
        puerto: Int = 8022,
        usuario: String,
        contrasena: String,
        rutaRemota: String,
        archivoLocal: File?
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jsch = JSch()
                val session = jsch.getSession(usuario, servidor, puerto)
                session.setPassword(contrasena)

                session.setConfig("StrictHostKeyChecking", "no")
                session.connect()

                val channel = session.openChannel("sftp") as ChannelSftp
                channel.connect()

                val inputStream = FileInputStream(archivoLocal)
                channel.put(inputStream, rutaRemota)
                inputStream.close()

                channel.disconnect()
                session.disconnect()

                return@withContext true
            } catch (e: Exception) {
                Log.e("SFTP", "Error al subir archivo: ${e.message}", e)
                return@withContext false
            }
        }
    }


}