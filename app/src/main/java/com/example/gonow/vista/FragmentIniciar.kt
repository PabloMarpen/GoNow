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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.gonow.tfg.R
import com.example.gonow.viewModel.AuthenticationState
import com.example.gonow.viewModel.userViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class FragmentIniciar : Fragment(R.layout.fragment_login) {

    private val userViewModel: userViewModel by activityViewModels()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
        val botonIniciar = view.findViewById<Button>(R.id.buttonIniciarSesion)
        val textoOlvidoContrasena = view.findViewById<TextView>(R.id.TextOlvidoContrasena)
        val textoRegistrarme = view.findViewById<TextView>(R.id.textViewRegistrarme)
        val correo = view.findViewById<EditText>(R.id.Correo)
        val contraseña = view.findViewById<EditText>(R.id.Contraseña)
        val botonGoogle = view.findViewById<Button>(R.id.buttonGoogle)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext() , gso)

        firebaseAuth = FirebaseAuth.getInstance()

        userViewModel.authenticationState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is AuthenticationState.Loading -> {
                    Toast.makeText(requireContext(), getString(R.string.cargando), Toast.LENGTH_SHORT).show()
                }
                is AuthenticationState.Success -> {
                    val user = state.user
                    user?.reload()?.addOnSuccessListener {
                        if (user.isEmailVerified) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.bienvenido_con_correo, user?.email),
                                Toast.LENGTH_SHORT
                            ).show()
                            iniciarMapa()
                        } else {
                            PopUpContenidoGeneral.newInstance(FragmentPopUpVerificate()).show(parentFragmentManager, "popUp")
                        }
                    }


                }
                is AuthenticationState.Error -> {
                    Toast.makeText(requireContext(), getString(R.string.error_credenciales), Toast.LENGTH_SHORT).show()
                }
            }
        })



        botonGoogle.setOnClickListener {
            signInGoogle()
        }

        botonIniciar.setOnTouchListener { v, event ->
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

        botonIniciar.setOnClickListener {
            if (!correo.text.toString().isValidEmail()) {
                Toast.makeText(requireContext(), getString(R.string.correo_no_valido), Toast.LENGTH_SHORT).show()
            } else if (contraseña.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.contrasena_vacia), Toast.LENGTH_SHORT).show()
            } else {
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

    /*
     * Este bloque de código gestiona el inicio de sesión con Google y su posterior autenticación en Firebase.
     *
     * - `signInGoogle()` lanza el intent de inicio de sesión con Google usando `googleSignInClient`.
     * - `launcher` recibe el resultado del intent y, si es exitoso, obtiene la cuenta de Google seleccionada y la pasa a `handleResults()`.
     * - `handleResults()` comprueba si la tarea fue exitosa, y si lo fue, llama a `updateUI()` con la cuenta de Google obtenida.
     * - `updateUI()` usa el `idToken` de la cuenta para crear una credencial de Firebase y autenticar al usuario.
     *   Si la autenticación es correcta, se inicia la actividad del mapa; si falla, muestra un mensaje de error.
     */

    private fun signInGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if (account != null){
                updateUI(account)
            }
        }else{
            Toast.makeText(requireContext(), task.exception.toString() , Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken , null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){
                iniciarMapa()
            }else{
                Toast.makeText(requireContext(), it.exception.toString() , Toast.LENGTH_SHORT).show()

            }
        }
    }


    private fun iniciarMapa() {
        val intent = Intent(requireContext(), GeneralActivity::class.java)
        intent.putExtra("abrirMapa", true)
        startActivity(intent)
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}