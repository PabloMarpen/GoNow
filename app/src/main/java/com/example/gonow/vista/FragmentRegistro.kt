package com.example.gonow.vista

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth

class FragmentRegistro : Fragment(R.layout.fragment_registro){

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
        val botonRegistrar = view.findViewById<Button>(R.id.buttonRegistrarme)
        val textoIniciarSesion = view.findViewById<TextView>(R.id.textViewIniciarSesion)
        val correo = view.findViewById<EditText>(R.id.Correo)
        val contraseña = view.findViewById<EditText>(R.id.Contraseña)
        val contraseña2 = view.findViewById<EditText>(R.id.contraseña)
        val auth = FirebaseAuth.getInstance()

        botonRegistrar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primaryVariant))
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.primary))
            }
            false
        }

        botonRegistrar.setOnClickListener {
            if(!correo.text.toString().isValidEmail()){
                Toast.makeText(requireContext(), "Correo no válido", Toast.LENGTH_SHORT).show()
            }else{
                if(contraseña.text.toString() != contraseña2.text.toString()){
                    Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }else{

                    auth.createUserWithEmailAndPassword(correo.text.toString(), contraseña.text.toString())
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                val user = auth.currentUser
                                Toast.makeText(
                                    requireContext(),
                                    "Authentication success.",
                                    Toast.LENGTH_SHORT,
                                ).show()

                                val intent = Intent(requireContext(), generalActivity::class.java)
                                intent.putExtra("abrirMapa", true) // Pasar una señal para abrir el fragmento
                                startActivity(intent)
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(
                                    requireContext(),
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                }
            }
        }
        textoIniciarSesion.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmentIniciar())
                .addToBackStack(null)
                .commit()
    }
}
    }