package com.example.gonow.vista

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gonow.data.AuthSingleton
import com.example.gonow.data.FirestoreSingleton
import com.example.gonow.modelo.Urinario
import com.example.gonow.modelo.UrinarioAdapter
import com.example.gonow.tfg.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore

class FragmetMisBanios : Fragment(R.layout.fragment_mis_banios) {

    val auth = AuthSingleton.auth
    val idUsuario = auth.currentUser?.uid

    private lateinit var recyclerView: RecyclerView
    private lateinit var urinarioAdapter: UrinarioAdapter
    private val urinariosList = mutableListOf<Urinario>()
    private lateinit var ubicacionActual: LatLng
    private var googleMap: GoogleMap? = null

    private val posicion: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }


    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.listaBanios)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        posicion.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                ubicacionActual = LatLng(it.latitude, it.longitude)  // Inicialización correcta de la propiedad
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15f))
            }
        }.addOnFailureListener {
            Toast.makeText(context, getString(R.string.error_obtener_ubicacion), Toast.LENGTH_SHORT).show()
        }

        urinarioAdapter = UrinarioAdapter(urinariosList) { urinario ->
            val fragment = FragmentResumenBanio.newInstance(
                nombre = urinario.nombre ?: getString(R.string.nombre_no_disponible),
                tipo = urinario.tipoUbi ?: "Sin tipo",
                descripcion = urinario.descripcion ?: getString(R.string.sin_descripcion),
                horario = urinario.horario,
                puntuacion = urinario.puntuacion ?: 0.0,
                sinhorario = urinario.sinhorario,
                etiquetas = urinario.etiquetas ?: emptyList(),
                cordenadasbanio = LatLng(
                    urinario.localizacion?.latitude ?: 0.0,
                    urinario.localizacion?.longitude ?: 0.0
                ),
                ubicacionUsuario = ubicacionActual,
                imagen = urinario.foto,
                creador = urinario.creador,
                idDocumento = urinario.idDocumento,
                mediaPuntuacion = urinario.mediaPuntuacion ?: 0.0,
                totalPuntuaciones = urinario.totalCalificaciones ?: 0
            )

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame, fragment)
                .addToBackStack("resumen_banio")
                .commit()
        }

        recyclerView.adapter = urinarioAdapter

        cargarUrinarios()

    }

    private fun cargarUrinarios() {

        FirestoreSingleton.db.collection("urinarios")
            .whereEqualTo("creador", idUsuario)
            .get()
            .addOnSuccessListener { result ->
                urinariosList.clear()
                for (document in result) {
                    val urinario = document.toObject(Urinario::class.java)
                    val id = document.id
                    urinariosList.add(urinario.copy(idDocumento = id))
                }
                urinarioAdapter.notifyDataSetChanged()

                if(urinariosList.isEmpty()){
                    PopUpContenidoGeneral.newInstance(FragmetnPopUpCreaBanios()).show(parentFragmentManager, "popUp")
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), getString(R.string.error_cargar_banios), Toast.LENGTH_SHORT).show()
            }
    }

}
