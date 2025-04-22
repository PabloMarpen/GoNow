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

    // Clase ManejoDeCarga que gestiona la visualización y ocultación de un diálogo de carga en un fragmento.
    // Incluye un mecanismo de temporizador para ocultar el diálogo si no se ha completado una operación en un tiempo especificado (por defecto, 20 segundos).
    // Los métodos principales son:
    // - mostrarCarga: Muestra el diálogo de carga con un mensaje opcional, asegurándose de que solo se muestre una vez y gestionando el tiempo de espera.
    // - ocultarCarga: Oculta el diálogo de carga y maneja correctamente la eliminación del diálogo de la vista en función del estado del fragmento.
    // - estaCargando: Devuelve un booleano indicando si el diálogo de carga está actualmente visible.
    // - reiniciarCargaSiEsNecesario: Reinicia el temporizador si el fragmento está activo y no guardado.


    fun mostrarCarga(mensaje: String = "") {
        // Asegurarse de que solo se muestra una vez
        ocultarCarga()

        loadingDialog = LoadingDialog.newInstance(mensaje)
        if (fragmentManager.isStateSaved) return

        loadingDialog?.show(fragmentManager, "loading")

        handler.postDelayed(timeoutRunnable, timeoutMillis)
    }

    fun ocultarCarga() {
        handler.removeCallbacks(timeoutRunnable)

        loadingDialog?.let { dialog ->
            // Forzar el dismiss si el diálogo es visible
            if (dialog.isAdded && dialog.isVisible && !dialog.parentFragmentManager.isStateSaved) {
                dialog.dismiss()
            } else {
                // Si el dialog no es visible, elimínalo de inmediato
                fragmentManager.beginTransaction().remove(dialog).commitAllowingStateLoss()
            }
        }

        loadingDialog = null
    }

    fun estaCargando(): Boolean {
        return loadingDialog?.isAdded == true
    }

    fun reiniciarCargaSiEsNecesario() {
        if (fragmentManager.isStateSaved) {
            return
        }
        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, timeoutMillis)
    }
}

