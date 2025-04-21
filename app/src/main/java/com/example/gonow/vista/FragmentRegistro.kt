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
import com.example.gonow.viewModel.AuthenticationState
import com.example.gonow.viewModel.userViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.result.contract.ActivityResultContracts
import com.example.gonow.data.AuthSingleton
import com.example.gonow.tfg.R
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider


class FragmentRegistro : Fragment(R.layout.fragment_registro){

    private val userViewModel: userViewModel by activityViewModels()
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val botonRegistrar = view.findViewById<Button>(R.id.buttonRegistrarme)
        val textoIniciarSesion = view.findViewById<TextView>(R.id.textViewIniciarSesion)
        val correo = view.findViewById<EditText>(R.id.Correo)
        val contraseña = view.findViewById<EditText>(R.id.Contraseña)
        val contraseña2 = view.findViewById<EditText>(R.id.contraseña)
        val googleIdButton = view.findViewById<Button>(R.id.buttonGoogle)
        firebaseAuth = AuthSingleton.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext() , gso)

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
                                getString(R.string.bienvenido_con_correo, user.email),
                                Toast.LENGTH_SHORT
                            ).show()
                            iniciarMapa()
                        } else {
                            PopUpContenidoGeneral.newInstance(FragmentPopUpVerificate()).show(parentFragmentManager, "popUp")
                        }
                    }
                }
                is AuthenticationState.Error -> {
                    Toast.makeText(requireContext(), getString(R.string.error_correo_existe), Toast.LENGTH_SHORT).show()
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
            signInGoogle()
        }

        botonRegistrar.setOnClickListener {
            if (correo.text.isNotEmpty() && contraseña.text.isNotEmpty() && contraseña2.text.isNotEmpty() && contraseña2.text.toString() == contraseña.text.toString()) {
                userViewModel.createUserWithEmailAndPassword(correo.text.toString(), contraseña.text.toString())
            } else if (!correo.text.toString().isValidEmail()) {
                Toast.makeText(requireContext(), getString(R.string.correo_no_valido), Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(requireContext(), getString(R.string.completar_campos), Toast.LENGTH_SHORT).show()
            }
        }
        textoIniciarSesion.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragmentIniciar())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun signInGoogle(){
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
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

    fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()



}
