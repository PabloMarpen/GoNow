package com.example.gonow.vista

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.gonow.tfg.R


class PopUp : DialogFragment() {

    // Este popup muestra un cuadro de diálogo de confirmación personalizado con un mensaje que se le pasa como argumento.
    // Si el mensaje es "borrar", adapta el texto y colores para confirmar el borrado de algo. Tiene dos botones:
    // uno para aceptar (invoca un callback con `true`) y otro para cancelar (callback con `false`).


    companion object {
        private const val ARG_MESSAGE = "message"

        fun newInstance(message: String): PopUp {
            val fragment = PopUp()
            val args = Bundle()
            args.putString(ARG_MESSAGE, message)
            fragment.arguments = args
            return fragment
        }
    }

    private var listener: ((Boolean) -> Unit)? = null

    fun setOnAcceptListener(callback: (Boolean) -> Unit) {
        listener = callback
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(requireContext()).create()
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_pop_up_confirmacion, null)

        val texto = view.findViewById<TextView>(R.id.tvMensaje)
        val btnAceptar = view.findViewById<Button>(R.id.btnSi)
        val btnNop = view.findViewById<Button>(R.id.btnNo)

        val message = arguments?.getString(ARG_MESSAGE) ?: getString(R.string.mensaje_por_defecto)
        if(message == "borrar"){
            texto.text = getString(R.string.confirmar_borrado)
            btnAceptar.text = getString(R.string.boton_borrar)
            btnNop.text = getString(R.string.boton_cancelar)
            btnAceptar.setBackgroundColor(Color.parseColor("#ff4c4c"))
        }else if (message == "llegar") {
            texto.text = getString(R.string.ruta_sistema_opcion)
            btnAceptar.text = "GoNow"
            btnNop.text = "Google Maps"
            btnNop.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
        }else{
            texto.text = message
        }


        btnAceptar.setOnClickListener {
            listener?.invoke(true)
            dismiss()
        }

        btnNop.setOnClickListener {
            listener?.invoke(false)
            dismiss()
        }

        dialog.setView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return dialog
    }

}


