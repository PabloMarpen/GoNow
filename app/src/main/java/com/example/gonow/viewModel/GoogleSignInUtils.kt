package com.example.gonow.viewModel


import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.gonow.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GoogleSignInUtils {

    companion object {
        fun doGoogleSignIn(
            context: Context,
            scope: CoroutineScope,
            launcher: ActivityResultLauncher<Intent>?,
            login: () -> Unit
        ) {
            val credentialManager = CredentialManager.create(context)

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(getCredentialOptions(context))
                .build()
            scope.launch {
                try {
                    val result = credentialManager.getCredential(context,request)
                    when(result.credential){
                        is CustomCredential ->{
                            if(result.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                                val googleTokenId = googleIdTokenCredential.idToken
                                val authCredential = GoogleAuthProvider.getCredential(googleTokenId,null)
                                try {
                                    val user = Firebase.auth.signInWithCredential(authCredential).await().user
                                    user?.let {
                                        if (!it.isAnonymous) {
                                            login.invoke()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("GoogleSignIn", "Error al hacer signInWithCredential: ${e.message}")
                                    Toast.makeText(context, "Error de autenticación", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        else ->{
                            Toast.makeText(context, "No se pudo obtener credencial de Google", Toast.LENGTH_SHORT).show()
                            Log.d("GoogleSignIn", "Credencial inesperada: ${result.credential}")
                        }
                    }
                }catch (e:NoCredentialException){
                    Log.e("GoogleSignIn", "NoCredentialException lanzada: ${e.message}")
                    Toast.makeText(context, "Lanzando selección de cuenta", Toast.LENGTH_SHORT).show()
                    launcher?.launch(getIntent())
                }catch (e:GetCredentialException){
                    e.printStackTrace()
                }
            }
        }

        private fun getIntent():Intent{
            return Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
            }
        }

        private fun getCredentialOptions(context: Context):CredentialOption{
            return GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .build()
        }
    }
}