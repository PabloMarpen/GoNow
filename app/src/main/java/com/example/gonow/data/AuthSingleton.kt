package com.example.gonow.data

import com.google.firebase.auth.FirebaseAuth

object AuthSingleton {
    // Singleton para Firebase Authentication
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
}