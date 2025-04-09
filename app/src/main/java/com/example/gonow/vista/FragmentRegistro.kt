package com.example.gonow.vista

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.gonow.R
import com.example.gonow.viewModel.AuthenticationState
import com.example.gonow.viewModel.userViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.gonow.viewModel.GoogleSignInUtils


class FragmentRegistro : Fragment(R.layout.fragment_registro){

    private val userViewModel: userViewModel by activityViewModels()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val botonRegistrar = view.findViewById<Button>(R.id.buttonRegistrarme)
        val textoIniciarSesion = view.findViewById<TextView>(R.id.textViewIniciarSesion)
        val correo = view.findViewById<EditText>(R.id.Correo)
        val contraseña = view.findViewById<EditText>(R.id.Contraseña)
        val contraseña2 = view.findViewById<EditText>(R.id.contraseña)
        val googleIdButton = view.findViewById<ImageView>(R.id.imageViewGoogle)
        firebaseAuth = FirebaseAuth.getInstance()

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                GoogleSignInUtils.doGoogleSignIn(
                    context = requireContext(),
                    scope = lifecycleScope,
                    launcher = null,
                    login = {
                        Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

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

        botonRegistrar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primaryVariant))
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.primary))
            }
            false
        }

        googleIdButton.setOnClickListener {

            GoogleSignInUtils.doGoogleSignIn(
                context = requireContext(),
                scope = lifecycleScope,
                launcher = googleSignInLauncher,
                login = {
                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                }
            )

        }



        botonRegistrar.setOnClickListener {
            if (correo.text.isNotEmpty() && contraseña.text.isNotEmpty() && contraseña2.text.isNotEmpty() && contraseña2.text.toString() == contraseña.text.toString()) {
                userViewModel.createUserWithEmailAndPassword(correo.text.toString(), contraseña.text.toString())
            } else if (!correo.text.toString().isValidEmail()) {
                Toast.makeText(requireContext(), "Correo no válido", Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(requireContext(), "Por favor, complete todos los campos correctamente.", Toast.LENGTH_SHORT).show()
            }
        }
        textoIniciarSesion.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmentIniciar())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun iniciarMapa() {
        val intent = Intent(requireContext(), generalActivity::class.java)
        intent.putExtra("abrirMapa", true)
        startActivity(intent)
    }

    fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()



}
