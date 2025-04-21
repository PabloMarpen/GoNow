package com.example.gonow.vista

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import android.location.Location
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.gonow.data.AuthSingleton
import com.example.gonow.data.FirestoreSingleton
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class FragmentAniadir : Fragment(R.layout.fragment_aniadir){

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

    //imagen
    private lateinit var photoUri: Uri
    private var photoFile: File? = null
    private lateinit var encodedImage : String
    lateinit var ubicacionActual : LatLng
    var horaApertura : String? = null
    var horaCierre : String? = null
    var abiertoSiempre : Boolean = true
    var cerradoSiempre : Boolean = false
    var tieneHorario : Boolean = false
    var tipoUbiSeleccionado : String? = null
    private lateinit var manejoCarga: ManejoDeCarga
    private val posicion: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            // Mostrar imagen en el ImageView
            botonAñadirImagen.setImageURI(photoUri)
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
            val cerradoSiempre = bundle.getBoolean("cerradoSiempre")
            val tieneHorario = bundle.getBoolean("tieneHorario")

            this.horaApertura = horaApertura.toString()
            this.horaCierre = horaCierre.toString()
            this.abiertoSiempre = abiertoSiempre
            this.cerradoSiempre = cerradoSiempre
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
        ubicacionActual = LatLng(0.0, 0.0)

        val manejoCarga = ManejoDeCarga(parentFragmentManager, 10000L) {
            textoNombre.setText(getString(R.string.banio))
            Toast.makeText(requireContext(), getString(R.string.ubicacion_no_obtenida), Toast.LENGTH_SHORT).show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            mostrarConfirmacionDeSalida()
        }

        // Obtener la ubicación actual
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
                    cerradoSiempre = false
                    tieneHorario = false
                    horaApertura = "null"
                    horaCierre = "null"
                }
                if(sinhorario == getString(R.string.cerradosiempre)){
                    cerradoSiempre = true
                    abiertoSiempre = false
                    tieneHorario = false
                    horaApertura = "null"
                    horaCierre = "null"
                }
            }else{
                tieneHorario = true
                abiertoSiempre = false
                cerradoSiempre = false
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
            val base64Image = arguments?.getString(KEY_FOTO)
            if (!base64Image.isNullOrEmpty()) {
                try {
                    val decodedBitmap = decodeBase64ToBitmap(base64Image)
                    botonAñadirImagen.setImageBitmap(decodedBitmap)
                } catch (e: IllegalArgumentException) {

                    botonAñadirImagen.setImageResource(R.drawable.noimage)
                }
            }

        }else{
            ratingBar.rating = 5f
            manejoCarga.mostrarCarga(getString(R.string.cargandoNombreCalle))
            //Obtener la ubicación actual con Geolocalización por nombre de la calle
            posicion.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    ubicacionActual = LatLng(it.latitude, it.longitude)

                    val direccion = getFullAddress(requireContext(), ubicacionActual.latitude, ubicacionActual.longitude)
                    val calle = direccion["calle"] ?: ""
                    val numero = direccion["numero"] ?: ""
                    val ciudad = direccion["ciudad"] ?: ""

                    val nombre = getString(R.string.banio) + " " + calle + " " + numero + " " + ciudad
                    textoNombre.setText(nombre.trim())

                    manejoCarga.ocultarCarga()
                } ?: run {
                    manejoCarga.ocultarCarga()
                    textoNombre.setText(getString(R.string.banio))
                    Toast.makeText(requireContext(), getString(R.string.ubicacion_no_obtenida), Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                manejoCarga.ocultarCarga()
                textoNombre.setText(getString(R.string.banio))
                Toast.makeText(requireContext(), getString(R.string.error_obtener_ubicacion), Toast.LENGTH_SHORT).show()
            }
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



        botonPublicar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.primaryVariant
                    )
                )

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.primary)
                )
            }
            false
        }

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
                switchCerrado = cerradoSiempre,
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
                            cerradoSiempre -> getString(R.string.cerradosiempre)
                            else -> "null"
                        }
                    } else {
                        "null"  // Si horaApertura o horaCierre son nulos
                    }

                    // Crear el objeto banio

                    val encodedImage = if (photoFile != null) {
                        compressAndEncodeImageToBase64(photoFile!!, 1048487)
                    } else {
                        if(esEditar){
                            arguments?.getString(KEY_FOTO)
                        }else{
                            ""
                        }
                    }



                    if(esEditar){


                        // Crear el objeto con los datos editados
                        val banioActualizado = mapOf(
                            "nombre" to textoNombre.text.toString(),
                            "sinhorario" to sinhorario,
                            "descripcion" to textoDescripcion.text.toString(),
                            "etiquetas" to etiquetas,
                            "foto" to encodedImage,
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
                        val banio = Urinario(
                            nombre = textoNombre.text.toString(),
                            sinhorario = sinhorario,
                            creador = AuthSingleton.auth.currentUser?.uid ?: "",
                            descripcion = textoDescripcion.text.toString(),
                            etiquetas = etiquetas,
                            foto = encodedImage,
                            horario = horario,
                            localizacion = GeoPoint(ubicacionActual.latitude, ubicacionActual.longitude),
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
                    }


                }

            }



    }

    fun getFullAddress(context: Context, lat: Double, lng: Double): Map<String, String> {
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


    fun compressAndEncodeImageToBase64(file: File, maxSize: Int): String {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return ""

        // Redimensionar la imagen si es demasiado grande (a un tamaño más pequeño)
        val width = bitmap.width
        val height = bitmap.height
        val ratio = width.toFloat() / height.toFloat()

        var newWidth = 600 // Ancho máximo más pequeño
        var newHeight = (newWidth / ratio).toInt()

        if (width > height) {
            newHeight = (newWidth / ratio).toInt()
        } else {
            newWidth = (newHeight * ratio).toInt()
        }

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

        // Comprimir la imagen con calidad baja
        val byteArrayOutputStream = ByteArrayOutputStream()
        var quality = 70 // Comenzamos con calidad baja

        // Usamos el formato WEBP para una mejor compresión
        resizedBitmap.compress(Bitmap.CompressFormat.WEBP, quality, byteArrayOutputStream)

        // Si el tamaño sigue siendo grande, seguir reduciendo la calidad
        while (byteArrayOutputStream.size() > maxSize) {
            byteArrayOutputStream.reset() // Limpiar el flujo de bytes
            quality -= 5 // Reducir la calidad en pasos más pequeños
            resizedBitmap.compress(Bitmap.CompressFormat.WEBP, quality, byteArrayOutputStream)

            // Si ya no se puede reducir más, salimos del bucle
            if (quality <= 10) {
                break
            }
        }

        // Convertir a Base64
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

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


}