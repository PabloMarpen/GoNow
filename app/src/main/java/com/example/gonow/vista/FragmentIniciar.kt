package com.example.gonow.vista

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.gonow.R
import com.example.gonow.viewModel.AuthenticationState
import com.example.gonow.viewModel.userViewModel


class FragmentIniciar : Fragment(R.layout.fragment_login) {

    private val userViewModel: userViewModel by activityViewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
        val botonIniciar = view.findViewById<Button>(R.id.buttonIniciarSesion)
        val textoOlvidoContrasena = view.findViewById<TextView>(R.id.TextOlvidoContrasena)
        val textoRegistrarme = view.findViewById<TextView>(R.id.textViewRegistrarme)
        val correo = view.findViewById<EditText>(R.id.Correo)
        val contraseña = view.findViewById<EditText>(R.id.Contraseña)

        userViewModel.authenticationState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is AuthenticationState.Loading -> {
                    Toast.makeText(requireContext(), "cargando...", Toast.LENGTH_SHORT).show()
                }
                is AuthenticationState.Success -> {
                    val user = state.user
                    Toast.makeText(requireContext(), "Bienvenido, ${user?.email}", Toast.LENGTH_SHORT).show()
                    iniciarMapa()
                }
                is AuthenticationState.Error -> {
                    Toast.makeText(requireContext(), "Error: ${state.errorMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        })

        botonIniciar.setOnTouchListener { v, event ->
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

        botonIniciar.setOnClickListener {


            if (!correo.text.toString().isValidEmail()) {
                Toast.makeText(requireContext(), "Correo no válido", Toast.LENGTH_SHORT).show()
            }
            else if(contraseña.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Contraseña vacia", Toast.LENGTH_SHORT).show()
            } else{
                userViewModel.signInWithEmailAndPassword(correo.text.toString(), contraseña.text.toString())
            }

        }

        textoOlvidoContrasena.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmentRecuperacion())
                .addToBackStack(null)
                .commit()
        }
        textoRegistrarme.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmentRegistro())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun iniciarMapa() {
        val intent = Intent(requireContext(), generalActivity::class.java)
        intent.putExtra("abrirMapa", true)
        startActivity(intent)
    }
}