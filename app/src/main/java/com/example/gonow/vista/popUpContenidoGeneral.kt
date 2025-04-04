package com.example.gonow.vista

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.gonow.R

class popUpContenidoGeneral : DialogFragment() {

    companion object {
        private const val ARG_MESSAGE = "message"

        fun newInstance(message: String): popUpContenidoGeneral {
            val fragment = popUpContenidoGeneral()
            val args = Bundle()
            args.putString(ARG_MESSAGE, message)
            fragment.arguments = args
            return fragment
        }
    }

    private var listener: (() -> Unit)? = null

    fun setOnAcceptListener(callback: () -> Unit) {
        listener = callback
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(requireContext()).create()
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_pop_up_contenido_general, null)



        dialog.setView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return dialog
    }
}