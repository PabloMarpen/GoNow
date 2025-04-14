package com.example.gonow.vista

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Patterns
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
import androidx.fragment.app.setFragmentResultListener
import com.example.gonow.R
import com.example.gonow.modelo.Urinario
import java.util.Locale
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.gonow.viewModel.userViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import android.os.Handler
import android.os.Looper

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

    lateinit var banio : Urinario
    lateinit var ubicacionActual : LatLng
    var horaApertura : String? = null
    var horaCierre : String? = null
    var abiertoSiempre : Boolean = true
    var cerradoSiempre : Boolean = false
    var tieneHorario : Boolean = false
    var tipoUbiSeleccionado : String? = null
    val auth = FirebaseAuth.getInstance()
    private val loadingTimeoutMillis = 60000L // 1 minuto
    private var loadingTimeoutHandler: Handler? = null
    private val loadingTimeoutRunnable = Runnable {
        ocultarCarga()
        Toast.makeText(requireContext(), "La operación tardó demasiado", Toast.LENGTH_SHORT).show()
    }
    private var loadingDialog: LoadingDialog? = null
    val currentUser = auth.currentUser?.uid
    private val posicion: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().supportFragmentManager.setFragmentResultListener("horario", this) { _, bundle ->
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

        switchAcesibilidad = view.findViewById(R.id.switchAcesibilidad)
        switchUnisex = view.findViewById(R.id.switchUnisex)
        switchJabon = view.findViewById(R.id.switchJabon)
        switchPapel = view.findViewById(R.id.switchPapel)
        switchGratis = view.findViewById(R.id.switchGratis)
        switchCambiar = view.findViewById(R.id.switchCambiar)
        ubicacionActual = LatLng(0.0, 0.0)


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

        mostrarCarga()

        posicion.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                ubicacionActual = LatLng(it.latitude, it.longitude)

                val direccion = getFullAddress(requireContext(), ubicacionActual.latitude, ubicacionActual.longitude)
                val calle = direccion["calle"]
                val numero = direccion["numero"]
                val ciudad = direccion["ciudad"]

                textoNombre.setText("${getString(R.string.banio)}"+ " " +"$calle" + " " + "$numero" + " " + "$ciudad")

                ocultarCarga()
            }.run {
                ocultarCarga()
            }
        }.addOnFailureListener { e ->
            ocultarCarga()
            Toast.makeText(requireContext(), "Error al obtener ubicación", Toast.LENGTH_SHORT).show()
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

            val tipoUbicacionFragmento = FragmentPopUpTipoUbicacion.nuevaInstancia(
                tipoSeleccionado = tipoUbiSeleccionado
            )
            popUpContenidoGeneral.newInstance(tipoUbicacionFragmento).show(parentFragmentManager, "popUp")
        }

        botonAñadirHorario.setOnClickListener {

            val horarioFragment = FragmentPopUpHorario.nuevaInstancia(
                switchAbierto = abiertoSiempre,
                switchCerrado = cerradoSiempre,
                switchTieneHorario = tieneHorario,
                horaApertura = horaApertura,
                horaCierre = horaCierre
            )

            popUpContenidoGeneral.newInstance(horarioFragment).show(parentFragmentManager, "popUp")

        }

        botonPublicar.setOnClickListener {
            if (validarCampos()) {
                mostrarCarga()
                Toast.makeText(requireContext(), "Publicando...", Toast.LENGTH_SHORT).show()

                val horario = mapOf(
                    "apertura" to horaApertura,
                    "cierre" to horaCierre
                )

                val etiquetas = mutableListOf<String>(
                    if (switchAcesibilidad.isChecked) "accesible" else "no accesible",
                    if (switchUnisex.isChecked) "unisex" else "no unisex",
                    if (switchJabon.isChecked) "jabon" else "no jabon",
                    if (switchPapel.isChecked) "papel" else "no papel",
                    if (switchGratis.isChecked) "gratis" else "de pago",
                    if (switchCambiar.isChecked) "zona bebes" else "no zona bebes",
                )


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
                    creador = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    descripcion = textoDescripcion.text.toString(),
                    etiquetas = etiquetas,
                    foto = null,
                    horario = horario,
                    localizacion = GeoPoint(ubicacionActual.latitude, ubicacionActual.longitude),
                    tipoUbi = tipoUbiSeleccionado,
                    puntuacion = ratingBar.rating.toDouble(),
                )

                FirebaseFirestore.getInstance().collection("urinarios")
                    .add(banio)
                    .addOnSuccessListener { documentReference ->

                        ocultarCarga()
                        Toast.makeText(requireContext(), "¡Publicado con éxito!", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.frame, FragmentMapa())
                            .addToBackStack(null)
                            .commit()
                    }
                    .addOnFailureListener { e ->
                        // Error
                        ocultarCarga()
                        Toast.makeText(requireContext(), "Error al publicar", Toast.LENGTH_SHORT).show()
                        Log.e("Firestore", "Error al publicar", e)
                    }
            }

        }

    }

    fun getFullAddress(context: Context, lat: Double, lng: Double): Map<String, String> {
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

    fun mostrarCarga() {
        loadingTimeoutHandler?.removeCallbacks(loadingTimeoutRunnable)

        loadingDialog = LoadingDialog()
        loadingDialog?.show(parentFragmentManager, "loading")

        loadingTimeoutHandler = Handler(Looper.getMainLooper())
        loadingTimeoutHandler?.postDelayed(loadingTimeoutRunnable, loadingTimeoutMillis)
    }

    fun ocultarCarga() {
        loadingTimeoutHandler?.removeCallbacks(loadingTimeoutRunnable)
        loadingDialog?.dismiss()
    }

}