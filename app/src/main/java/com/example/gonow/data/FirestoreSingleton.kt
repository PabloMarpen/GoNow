package com.example.gonow.data

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreSingleton {
    // Singleton para Firebase Firestore
    val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}