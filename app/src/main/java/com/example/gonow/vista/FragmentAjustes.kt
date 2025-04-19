package com.example.gonow.vista

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.gonow.data.AuthSingleton
import com.example.gonow.data.FirestoreSingleton
import com.example.gonow.tfg.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.database.FirebaseDatabase

class FragmentAjustes : Fragment(R.layout.fragment_ajustes) {

    val auth = AuthSingleton.auth
    val idUsuario = auth.currentUser?.uid
    val user = auth.currentUser

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
        val botonBorrarCuenta = view.findViewById<Button>(R.id.buttonBorrarCuenta)
        val botonCerrarSesion = view.findViewById<Button>(R.id.buttonCerrarSesion)
        val botonCambiarCorreo = view.findViewById<Button>(R.id.buttonCambiarCorreo)
        val botonCambiarContrasena = view.findViewById<Button>(R.id.buttonCambiarContraseña)
        val botonAyuda = view.findViewById<Button>(R.id.buttonAyuda)
        val correoUsuario = view.findViewById<TextView>(R.id.textViewNombre)
        val textViewTotalBaños = view.findViewById<TextView>(R.id.textViewTotalBaños)
        val textViewTotalBañosCreados = view.findViewById<TextView>(R.id.textViewTotalBañosCreados)
        // pal refresh
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        val sharedPrefs = requireContext().getSharedPreferences("refreshPrefs", Context.MODE_PRIVATE)
        val lastRefreshTime = sharedPrefs.getLong("lastRefresh", 0L)
        val tiempoActual = System.currentTimeMillis()
        val tiempoMinimoEntreRefresh = 60 * 1000 // 1 minuto
        // para las estadisticas
        val prefs = requireContext().getSharedPreferences("estadisticas", Context.MODE_PRIVATE)
        val yaGuardadoPuntuados = prefs.getInt("totalPuntuados", -1)
        val yaGuardadoCreados = prefs.getInt("totalCreados", -1)
        // para saber si es de google o no
        var esGoogle = false

        user?.providerData?.forEach { profile ->
            if (profile.providerId == "google.com") {
                esGoogle = true
            }
        }

        if (yaGuardadoPuntuados != -1) {
            textViewTotalBaños.text = yaGuardadoPuntuados.toString()
        } else {
            recargarEstadisticas {
                swipeRefresh.isRefreshing = false
            }
        }

        if (yaGuardadoCreados != -1) {
            textViewTotalBañosCreados.text = yaGuardadoCreados.toString()
        } else {
            recargarEstadisticas {
                swipeRefresh.isRefreshing = false
            }
        }

        swipeRefresh.setOnRefreshListener {
            if (tiempoActual - lastRefreshTime > tiempoMinimoEntreRefresh) {

                recargarEstadisticas {
                    swipeRefresh.isRefreshing = false
                }
                sharedPrefs.edit().putLong("lastRefresh", tiempoActual).apply()
            } else {
                Toast.makeText(requireContext(), "Espera un poco antes de refrescar", Toast.LENGTH_SHORT).show()
                swipeRefresh.isRefreshing = false
            }
        }

        correoUsuario.text = auth.currentUser?.email

        val touchListener = View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.primaryVariant)
                )
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.primary)
                )
            }
            false
        }

        listOf(
            botonBorrarCuenta,
            botonCerrarSesion,
            botonCambiarCorreo,
            botonCambiarContrasena,
            botonAyuda
        ).forEach { it.setOnTouchListener(touchListener) }

        botonBorrarCuenta.setOnClickListener {
            val mensaje = "borrar"
            val popup = PopUp.newInstance(mensaje)
            popup.setOnAcceptListener { isConfirmed ->
                if (isConfirmed) {
                    user?.delete()
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Cuenta eliminada correctamente.", Toast.LENGTH_SHORT).show()
                                val intent = Intent(requireContext(), MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                requireActivity().finishAffinity()
                            } else {
                                val exception = task.exception
                                if (exception is FirebaseAuthRecentLoginRequiredException) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Por seguridad, por favor cierra sesión y vuelve a iniciar para borrar tu cuenta.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(requireContext(), "Error al eliminar la cuenta.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                }
            }
            popup.show(parentFragmentManager, "PopUp")
        }

        botonCerrarSesion.setOnClickListener {
            val mensaje = "¿Seguro que quieres cerrar sesión?"
            val popup = PopUp.newInstance(mensaje)
            popup.setOnAcceptListener { isConfirmed ->
                if (isConfirmed) {

                    FirebaseAuth.getInstance().signOut()

                    FirebaseDatabase.getInstance().goOffline()

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)


                    // Cerrar todas las actividades y evitar que el usuario regrese a la actividad anterior
                    requireActivity().finishAffinity()
                }
            }
            popup.show(parentFragmentManager, "popUp")


        }

        if(esGoogle){
            botonCambiarCorreo.isEnabled = false
            botonCambiarCorreo.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.supportVariant))
            botonCambiarContrasena.isEnabled = false
            botonCambiarContrasena.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.supportVariant))

        }

        botonCambiarCorreo.setOnClickListener {
            //cargamos el popup seleccionando nuestra interfaz
            PopUpContenidoGeneral.newInstance(FragmentPopUpCambiarCorreo()).show(parentFragmentManager, "popUp")
        }

        botonCambiarContrasena.setOnClickListener {
            //cargamos el popup seleccionando nuestra interfaz
            val fragmento = FragmentPopUpCambiarContraseña()
            val popup = PopUpContenidoGeneral.newInstance(fragmento)
            popup.show(parentFragmentManager, "popUp")
        }




    }

    private fun recargarEstadisticas(onFinish: () -> Unit) {
        val prefs = requireContext().getSharedPreferences("estadisticas", Context.MODE_PRIVATE)
        var consultasPendientes = 2

        val onConsultaTermina: () -> Unit = {
            consultasPendientes--
            if (consultasPendientes == 0) {
                onFinish()
            }
        }

        // Consultar calificaciones
        FirestoreSingleton.db.collection("calificaciones")
            .whereEqualTo("idUsuario", idUsuario)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val totalPuntuados = querySnapshot.size()
                prefs.edit().putInt("totalPuntuados", totalPuntuados).apply()
                view?.findViewById<TextView>(R.id.textViewTotalBaños)?.text = totalPuntuados.toString()
                onConsultaTermina() // Llamar al método cuando termine la consulta
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al obtener las calificaciones", Toast.LENGTH_SHORT).show()
                onConsultaTermina() // Llamar al método cuando termine la consulta (aunque falle)
            }

        // Consultar urinarios
        FirestoreSingleton.db.collection("urinarios")
            .whereEqualTo("creador", idUsuario)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val totalCreados = querySnapshot.size()
                prefs.edit().putInt("totalCreados", totalCreados).apply()
                view?.findViewById<TextView>(R.id.textViewTotalBañosCreados)?.text = totalCreados.toString()
                onConsultaTermina() // Llamar al método cuando termine la consulta
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al obtener los urinarios creados", Toast.LENGTH_SHORT).show()
                onConsultaTermina() // Llamar al método cuando termine la consulta (aunque falle)
            }
    }
}
