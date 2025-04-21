package com.example.gonow.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class userViewModel : ViewModel() {

    private val _authenticationState = MutableLiveData<AuthenticationState>()
    val authenticationState: LiveData<AuthenticationState> get() = _authenticationState

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Método para iniciar sesión con correo y contraseña
    fun signInWithEmailAndPassword(email: String, password: String) {
        _authenticationState.value = AuthenticationState.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authenticationState.value = AuthenticationState.Success(auth.currentUser)
                } else {
                    _authenticationState.value = AuthenticationState.Error(task.exception?.message)
                }
            }
    }

    // Método para crear un nuevo usuario con correo y contraseña
    fun createUserWithEmailAndPassword(email: String, password: String) {
        _authenticationState.value = AuthenticationState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {
                                Log.d("Auth", "Correo de verificación enviado a ${user.email}")

                            } else {
                                Log.e(
                                    "Auth",
                                    "Error al enviar verificación: ${verifyTask.exception?.message}"
                                )
                            }
                        }

                    _authenticationState.value = AuthenticationState.Success(user)
                } else {
                    _authenticationState.value = AuthenticationState.Error(task.exception?.message)
                }
            }
    }
}


// Define el estado de autenticación
sealed class AuthenticationState {
    object Loading : AuthenticationState()
    data class Success(val user: FirebaseUser?) : AuthenticationState()
    data class Error(val errorMessage: String?) : AuthenticationState()
}
