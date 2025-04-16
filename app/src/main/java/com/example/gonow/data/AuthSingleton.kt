package com.example.gonow.data

import com.google.firebase.auth.FirebaseAuth

object AuthSingleton {
    // Singleton para Firebase Authentication
    // uso val user = AuthSingleton.auth
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
}