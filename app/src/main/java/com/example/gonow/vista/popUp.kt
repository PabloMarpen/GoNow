package com.example.gonow.vista

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.gonow.R


class popUp : DialogFragment() {

    companion object {
        private const val ARG_MESSAGE = "message"

        fun newInstance(message: String): popUp {
            val fragment = popUp()
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(requireContext()).create()
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_pop_up_confirmacion, null)

        val texto = view.findViewById<TextView>(R.id.tvMensaje)
        val btnAceptar = view.findViewById<Button>(R.id.btnSi)
        val btnNop = view.findViewById<Button>(R.id.btnNo)

        val message = arguments?.getString(ARG_MESSAGE) ?: "Mensaje por defecto"
        texto.text = message

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


