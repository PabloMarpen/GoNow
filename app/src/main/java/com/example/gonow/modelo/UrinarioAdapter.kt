package com.example.gonow.modelo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gonow.tfg.R

/*
* Este es un adaptador para un RecyclerView que muestra una lista de objetos 'Urinario'.
* El adaptador utiliza un ViewHolder para almacenar las vistas de cada item de la lista.
* En el método 'onCreateViewHolder', se infla el layout para cada item de la lista.
* En 'onBindViewHolder', se asignan los valores correspondientes del 'Urinario' a las vistas, como el nombre y la puntuación.
* Si la puntuación media es 0.0, se utiliza la puntuación individual, de lo contrario, se muestra la puntuación media.
* Se asigna un listener al hacer clic en el item, que ejecuta el callback 'onItemClick' con el 'Urinario' correspondiente.
* Finalmente, 'getItemCount' devuelve el número de elementos en la lista 'urinarios'.
*/


class UrinarioAdapter(
    private val urinarios: List<Urinario>,
    private val onItemClick: (Urinario) -> Unit
) : RecyclerView.Adapter<UrinarioAdapter.UrinarioViewHolder>() {

    class UrinarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.item_name)
        val ratingBar: RatingBar = itemView.findViewById(R.id.item_rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrinarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_banio_item, parent, false)
        return UrinarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UrinarioViewHolder, position: Int) {
        val urinario = urinarios[position]
        holder.nombre.text = urinario.nombre ?: "Sin nombre"


        if (urinario.mediaPuntuacion == 0.0) {
            holder.ratingBar.rating = (urinario.puntuacion ?: 0.0).toFloat()
        }else{
            holder.ratingBar.rating = (urinario.mediaPuntuacion ?: 0.0).toFloat()
        }

        holder.itemView.setOnClickListener {
            onItemClick(urinario)
        }
    }

    override fun getItemCount() = urinarios.size
}
