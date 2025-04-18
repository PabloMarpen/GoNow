package com.example.gonow.vista

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentManager

class ManejoDeCarga(
    private val fragmentManager: FragmentManager,
    private val timeoutMillis: Long = 20000L, // Por defecto, 20 segundos
    private val onTimeout: (() -> Unit)? = null
) {
    private var loadingDialog: LoadingDialog? = null
    private val handler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable {
        ocultarCarga()
        onTimeout?.invoke()
    }

    fun mostrarCarga() {
        ocultarCarga() // Por si ya había uno activo

        loadingDialog = LoadingDialog()
        loadingDialog?.show(fragmentManager, "loading")

        handler.postDelayed(timeoutRunnable, timeoutMillis)
    }

    fun ocultarCarga() {
        handler.removeCallbacks(timeoutRunnable)
        loadingDialog?.dismiss()
        loadingDialog = null
    }
}