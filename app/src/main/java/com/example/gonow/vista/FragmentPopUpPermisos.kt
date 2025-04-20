package com.example.gonow.vista

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.gonow.tfg.R

class FragmentPopUpPermisos : Fragment(R.layout.fragment_pop_up_permisos) {

    private val REQUEST_LOCATION_PERMISSION = 1001

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val permisosButton = view.findViewById<Button>(R.id.buttonDarPermisos)

        permisosButton.setOnClickListener {
            val activity = requireActivity()

            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }
}