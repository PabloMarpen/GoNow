package com.example.gonow.vista

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
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
        val botonCambiarContrasena = view.findViewById<Button>(R.id.buttonCambiarContraseña)
        val botonMisBanios = view.findViewById<Button>(R.id.buttonMisBanios)
        val botonAyuda = view.findViewById<Button>(R.id.buttonAyuda)
        val correoUsuario = view.findViewById<TextView>(R.id.textViewNombre)
        val textViewTotalBaños = view.findViewById<TextView>(R.id.textViewTotalBaños)
        val textViewTotalBañosCreados = view.findViewById<TextView>(R.id.textViewTotalBañosCreados)
        // pal refresh
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        val sharedPrefs = requireContext().getSharedPreferences("refreshPrefs", Context.MODE_PRIVATE)
        val lastRefreshTime = sharedPrefs.getLong("lastRefresh", 0L)
        val tiempoActual = System.currentTimeMillis()
        val tiempoMinimoEntreRefresh = 5000  // 5 segundos
        // para las estadisticas
        val prefs = requireContext().getSharedPreferences("estadisticas", Context.MODE_PRIVATE)
        val yaGuardadoPuntuados = prefs.getInt("totalPuntuados", -1)
        val yaGuardadoCreados = prefs.getInt("totalCreados", -1)
        // para saber si es de google el usuario o no
        var esGoogle = false

        // Verificar si el usuario está autenticado con Google
        user?.providerData?.forEach { profile ->
            if (profile.providerId == "google.com") {
                esGoogle = true
            }
        }

        // Configuración de las estadísticas y el swipe refresh
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

        // Configuración del SwipeRefreshLayout para que no se hagan peticiones contantes
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
                                Toast.makeText(requireContext(), getString(R.string.cuenta_eliminada), Toast.LENGTH_SHORT).show()
                                val intent = Intent(requireContext(), MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                requireActivity().finishAffinity()
                            } else {
                                val exception = task.exception
                                if (exception is FirebaseAuthRecentLoginRequiredException) {
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.seguridad_eliminar),
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(requireContext(), getString(R.string.error_eliminar_cuenta), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                }
            }
            popup.show(parentFragmentManager, "PopUp")
        }

        botonCerrarSesion.setOnClickListener {
            val mensaje = getString(R.string.cerrarsesion)
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

        // si es google la cuenta se cancela el boton para cambiar contraseña
        if(esGoogle){

            botonCambiarContrasena.isEnabled = false
            botonCambiarContrasena.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.supportVariant))

        }

        botonCambiarContrasena.setOnClickListener {
            //cargamos el popup seleccionando nuestra interfaz
            val fragmento = FragmentPopUpCambiarContraseña()
            val popup = PopUpContenidoGeneral.newInstance(fragmento)
            popup.show(parentFragmentManager, "popUp")
        }

        botonAyuda.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pablommp.myvnc.com/gonow.html"))
            startActivity(browserIntent)
        }

        botonMisBanios.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmetMisBanios())
                .addToBackStack(null)
                .commit()
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
                Toast.makeText(requireContext(), getString(R.string.error_obtener_califi), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), getString(R.string.error_obtener_urinarios_creados), Toast.LENGTH_SHORT).show()
                onConsultaTermina() // Llamar al método cuando termine la consulta (aunque falle)
            }
    }
}
