package com.example.gonow.modelo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gonow.tfg.R

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
