package com.example.gonow.vista

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.gonow.R

class popUpContenidoGeneral(private val fragmentoInterno: Fragment) : DialogFragment() {

    // Creamos el objeto para pasarle los parametros que son el fragment que queremos mostrar
    companion object {
        fun newInstance(fragmento: Fragment): popUpContenidoGeneral {
            return popUpContenidoGeneral(fragmento)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(requireContext()).create()
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_pop_up_contenido_general, null)

        dialog.setView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Esperamos a que la vista esté lista para cargar el fragmento
        view.post {
            childFragmentManager.beginTransaction()
                .replace(R.id.contenedor_fragmento, fragmentoInterno)
                .commit()
        }

        return dialog
    }
}
