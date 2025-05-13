package br.edu.utfpr.gps_pm46s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.gps_pm46s.entity.PontoTuristico
import br.edu.utfpr.gps_pm46s.R

class PontosAdapter(private val lista: List<PontoTuristico>) :
    RecyclerView.Adapter<PontosAdapter.PontoViewHolder>() {

    inner class PontoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foto = itemView.findViewById<ImageView>(R.id.ivMidia)
        val nome = itemView.findViewById<TextView>(R.id.tvNome)
        val latitude = itemView.findViewById<TextView>(R.id.tvLatitude)
        val longitude = itemView.findViewById<TextView>(R.id.tvLongitude)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PontoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_ponto_turistico, parent, false)
        return PontoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PontoViewHolder, position: Int) {
        val ponto = lista[position]
        holder.nome.text = ponto.nome
    }

    override fun getItemCount(): Int = lista.size
}
