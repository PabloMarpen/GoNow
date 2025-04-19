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
        // Asegurarse de que solo se muestra una vez
        ocultarCarga()

        loadingDialog = LoadingDialog()
        if (fragmentManager.isStateSaved) return

        loadingDialog?.show(fragmentManager, "loading")

        handler.postDelayed(timeoutRunnable, timeoutMillis)
    }

    fun ocultarCarga() {
        handler.removeCallbacks(timeoutRunnable)

        loadingDialog?.let { dialog ->
            if (dialog.isAdded && !dialog.parentFragmentManager.isStateSaved) {
                dialog.dismiss()
            }
        }

        loadingDialog = null
    }

    fun estaCargando(): Boolean {
        return loadingDialog?.isAdded == true
    }

    // Esta función podría ser útil en onStart() o onResume para reiniciar la carga
    fun reiniciarCargaSiEsNecesario() {
        if (fragmentManager.isStateSaved) {
            return
        }
        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, timeoutMillis)
    }
}
