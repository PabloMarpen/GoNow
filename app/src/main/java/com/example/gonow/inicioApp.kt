package com.example.gonow

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class inicioApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Configuración global de la persistencia de Firebase
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}
