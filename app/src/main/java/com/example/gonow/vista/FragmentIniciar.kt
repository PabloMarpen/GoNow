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
import com.example.gonow.R
import com.example.gonow.vista.BottomSheet.Companion.TAG
import com.google.firebase.auth.FirebaseAuth

class FragmentIniciar : Fragment(R.layout.fragment_login) {

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
        val botonIniciar = view.findViewById<Button>(R.id.buttonIniciarSesion)
        val textoOlvidoContrasena = view.findViewById<TextView>(R.id.TextOlvidoContrasena)
        val textoRegistrarme = view.findViewById<TextView>(R.id.textViewRegistrarme)
        val correo = view.findViewById<EditText>(R.id.Correo)
        val contraseña = view.findViewById<EditText>(R.id.Contraseña)
        val auth = FirebaseAuth.getInstance()

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
            val email = correo.text.toString()
            val contraseña = contraseña.text.toString()


            if (!email.isValidEmail()) {
                Toast.makeText(requireContext(), "Correo no válido", Toast.LENGTH_SHORT).show()
            }
            else if(contraseña.isEmpty()) {
                Toast.makeText(requireContext(), "Contraseña vacia", Toast.LENGTH_SHORT).show()
            }
            else {
                //fuente https://firebase.google.com/docs/auth/android/password-auth?hl=es-419
                // esto es para iniciar sesion en google auth en firebase
                auth.signInWithEmailAndPassword(email, contraseña)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            auth.currentUser

                            val intent = Intent(requireContext(), generalActivity::class.java)
                            intent.putExtra("abrirMapa", true) // Pasar una señal para abrir el fragmento
                            startActivity(intent)

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                requireContext(),
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()

                        }
                    }
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
}