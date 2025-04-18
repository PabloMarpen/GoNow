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
import android.util.Log
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.example.gonow.data.AuthSingleton
import com.example.gonow.data.FirestoreSingleton
import com.example.gonow.modelo.Calificacion
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



    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            lanzarCamara()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        ratingBar.rating = 5f

        switchAcesibilidad = view.findViewById(R.id.switchAcesibilidad)
        switchUnisex = view.findViewById(R.id.switchUnisex)
        switchJabon = view.findViewById(R.id.switchJabon)
        switchPapel = view.findViewById(R.id.switchPapel)
        switchGratis = view.findViewById(R.id.switchGratis)
        switchCambiar = view.findViewById(R.id.switchCambiar)
        ubicacionActual = LatLng(0.0, 0.0)

        manejoCarga = ManejoDeCarga(
            parentFragmentManager,
            timeoutMillis = 20000L
        ) {
            Toast.makeText(requireContext(), "Tiempo de carga agotado", Toast.LENGTH_SHORT).show()
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


        manejoCarga.mostrarCarga()
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
                Toast.makeText(requireContext(), "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            manejoCarga.ocultarCarga()
            textoNombre.setText(getString(R.string.banio))
            Toast.makeText(requireContext(), "Error al obtener ubicación", Toast.LENGTH_SHORT).show()
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
                manejoCarga.mostrarCarga()
                Toast.makeText(requireContext(), "Publicando...", Toast.LENGTH_SHORT).show()
                // Crear el objeto horario
                val horario = mapOf(
                    "apertura" to horaApertura,
                    "cierre" to horaCierre
                )
                // Crear la lista de etiquetas
                val etiquetas = mutableListOf<String>(
                    if (switchAcesibilidad.isChecked) "Accesible" else "No accesible",
                    if (switchUnisex.isChecked) "Unisex" else "No unisex",
                    if (switchJabon.isChecked) "Con jabón" else "Sin jabón",
                    if (switchPapel.isChecked) "Con papel" else "Sin papel",
                    if (switchGratis.isChecked) "Gratis" else "De pago",
                    if (switchCambiar.isChecked) "Con zona bebé" else "Sin zona bebé"
                )

                // Crear el objeto banio


                val encodedImage = if (photoFile != null) {
                    compressAndEncodeImageToBase64(photoFile!!, 1048487)
                } else {
                    ""
                }

                val banio = Urinario(
                    nombre = textoNombre.text.toString(),
                    sinhorario = if(horaApertura != null && horaCierre != null){
                        if(abiertoSiempre){
                            "abierto 24/7"
                        }else{
                            "baño cerrado"
                        }
                    }else{
                        null
                    },
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
                        Toast.makeText(requireContext(), "¡Publicado con éxito!", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.frame, FragmentMapa())
                            .addToBackStack(null)
                            .commit()
                    }
                    .addOnFailureListener { e ->
                        manejoCarga.ocultarCarga()
                        Toast.makeText(requireContext(), "Error al publicar: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("Firestore", "Error al publicar", e)
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
                Toast.makeText(context, "Añade un nombre", Toast.LENGTH_SHORT).show()
                false
            }
            ratingBar.rating < 0.5 -> {
                Toast.makeText(context, "La puntuación debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                false
            }
            horaApertura == null || horaCierre == null ->{
                Toast.makeText(context, "Añade un horario", Toast.LENGTH_SHORT).show()
                false
            }
            tipoUbiSeleccionado == null ->{
                Toast.makeText(context, "Añade un tipo de ubicación", Toast.LENGTH_SHORT).show()
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
        super.onDestroyView()
        manejoCarga.ocultarCarga()
    }


    fun compressAndEncodeImageToBase64(file: File, maxSize: Int): String {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return ""

        // Redimensionar la imagen si es demasiado grande
        val width = bitmap.width
        val height = bitmap.height
        val ratio = width.toFloat() / height.toFloat()

        var newWidth = 800 // Ancho máximo
        var newHeight = (newWidth / ratio).toInt()

        if (width > height) {
            newHeight = (newWidth / ratio).toInt()
        } else {
            newWidth = (newHeight * ratio).toInt()
        }

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

        // Comprimir la imagen con calidad baja
        val byteArrayOutputStream = ByteArrayOutputStream()
        var quality = 80
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)

        while (byteArrayOutputStream.size() > maxSize) {
            byteArrayOutputStream.reset() // Limpiar el flujo de bytes
            quality -= 10 // Disminuir la calidad
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        }
        val byteArray = byteArrayOutputStream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    }

    private fun mostrarConfirmacionDeSalida() {
        val mensaje = "¿Estás seguro de que quieres salir?\n\nSe perderá la información no guardada."
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