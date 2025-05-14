package br.edu.utfpr.gps_pm46s.adapter

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.gps_pm46s.entity.PontoTuristico
import br.edu.utfpr.gps_pm46s.R
import com.bumptech.glide.Glide

class PontosAdapter(private val lista: List<PontoTuristico>) :
    RecyclerView.Adapter<PontosAdapter.PontoViewHolder>() {

    inner class PontoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nome: TextView = itemView.findViewById(R.id.tvNome)
        val latitude: TextView = itemView.findViewById(R.id.tvLatitude)
        val longitude: TextView = itemView.findViewById(R.id.tvLongitude)
        val imagem: ImageView = itemView.findViewById(R.id.ivMidia)
        val caminhoImagem: TextView = itemView.findViewById(R.id.tvCaminhoImagem)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PontoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_ponto_turistico, parent, false)
        return PontoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PontoViewHolder, position: Int) {
        val ponto = lista[position]

        holder.nome.text = ponto.nome
        holder.latitude.text = ponto.latitude.toString()
        holder.longitude.text = ponto.longitude.toString()

        holder.caminhoImagem.text = ponto.fotoUri

        val imageUri = Uri.parse(ponto.fotoUri)

        Glide.with(holder.itemView.context)
            .load(imageUri)
            .into(holder.imagem)
    }

    override fun getItemCount(): Int = lista.size
}
