package com.example.gonow.vista

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.gonow.tfg.R

class LoadingDialog : DialogFragment() {

    private var mensaje: String? = null

    // Clase LoadingDialog que extiende de DialogFragment y representa un diálogo de carga con un mensaje personalizado.
    // Utiliza el patrón de fábrica para crear una nueva instancia del diálogo pasando un mensaje como argumento.
    // Los métodos principales son:
    // - newInstance: Crea una nueva instancia de LoadingDialog con el mensaje proporcionado y lo pasa a los argumentos del diálogo.
    // - onCreateDialog: Configura el diálogo utilizando un AlertDialog.Builder, inflando una vista personalizada (frame_carga) y mostrando el mensaje en un TextView.
    // El diálogo es no cancelable para evitar que el usuario lo cierre durante la carga.


    companion object {
        // Método para crear una nueva instancia del diálogo con un mensaje
        fun newInstance(mensaje: String): LoadingDialog {
            val dialog = LoadingDialog()
            val args = Bundle()
            args.putString("mensaje", mensaje)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mensaje = arguments?.getString("mensaje")

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.frame_carga, null)

        // Suponiendo que tienes un TextView con ID textoCarga
        view.findViewById<TextView>(R.id.textoCarga)?.text = mensaje

        builder.setView(view)
        isCancelable = false
        return builder.create()
    }
}

