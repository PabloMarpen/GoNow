package com.example.gonow.vista

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.compose.material3.RadioButton
import androidx.fragment.app.DialogFragment
import com.example.gonow.R
import com.example.gonow.databinding.ItemBottomsheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheet : BottomSheetDialogFragment() {
    private var _binding: ItemBottomsheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var radioButtonBanioPublico: RadioButton
    private lateinit var radioButtonBar: RadioButton
    private lateinit var radioButtonTienda: RadioButton
    private lateinit var radioButtonRestaurante: RadioButton
    private lateinit var radioButtonGasolinera: RadioButton
    private lateinit var radioButtonOtro: RadioButton
    private lateinit var radioGroup: RadioGroup

    interface TipoBanioListener {
        fun onTipoSeleccionado(tipo: String)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ItemBottomsheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var listener: TipoBanioListener? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar los RadioButtons usando ViewBinding
        radioButtonBanioPublico = binding.radioButtonBanioPublico
        radioButtonBar = binding.radioButtonBar
        radioButtonTienda = binding.radioButtonTienda
        radioButtonRestaurante = binding.radioButtonRestaurante
        radioButtonGasolinera = binding.radioButtonGasolinera
        radioButtonOtro = binding.radioButtonOtro
        radioGroup = binding.radioGroup

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val tipo = when (checkedId) {
                R.id.radioButtonBanioPublico -> "baño público"
                R.id.radioButtonBar -> "bar"
                R.id.radioButtonTienda -> "tienda"
                R.id.radioButtonRestaurante -> "restaurante"
                R.id.radioButtonGasolinera -> "gasolinera"
                R.id.radioButtonOtro -> "otro"
                else -> return@setOnCheckedChangeListener
            }

            listener?.onTipoSeleccionado(tipo)
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return BottomSheetDialog(requireContext()).apply {
            setOnShowListener {
                val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.let {
                    val behavior = BottomSheetBehavior.from(it)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? TipoBanioListener ?:
                parentFragment as? TipoBanioListener
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ModalBottomSheetDialog"
    }
}