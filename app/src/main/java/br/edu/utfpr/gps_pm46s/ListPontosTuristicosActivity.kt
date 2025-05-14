package br.edu.utfpr.gps_pm46s

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.gps_pm46s.adapter.PontosAdapter
import br.edu.utfpr.gps_pm46s.database.GpsDbHelper
import br.edu.utfpr.gps_pm46s.entity.PontoTuristico

class ListPontosTuristicosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PontosAdapter
    private lateinit var dbHelper: GpsDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_pontos_turisticos_activity)

        setSupportActionBar(findViewById(R.id.my_toolbar))

        recyclerView = findViewById(R.id.rvPontosTuristicos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        dbHelper = GpsDbHelper(this)
        carregarPontos()
    }

    private fun carregarPontos() {
        val cursor = dbHelper.list()
        val pontos = mutableListOf<PontoTuristico>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val lat = cursor.getDouble(1)
                val lng = cursor.getDouble(2)
                val nome = cursor.getString(3)
                val desc = cursor.getString(4)
                val foto = cursor.getString(5)

                pontos.add(PontoTuristico(id, lat, lng, nome, desc, foto))
            } while (cursor.moveToNext())
        }

        cursor.close()

        adapter = PontosAdapter(pontos)
        recyclerView.adapter = adapter
    }
}
