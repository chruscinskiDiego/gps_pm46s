package br.edu.utfpr.gps_pm46s

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var isFabExpanded = false
    private lateinit var primaryFab: FloatingActionButton
    private lateinit var fabAction1: FloatingActionButton
    private lateinit var fabAction2: FloatingActionButton

    private lateinit var map: GoogleMap

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    private val registerPointLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val name = data.getStringExtra("name") ?: return@let
                val lat  = data.getDoubleExtra("lat", 0.0)
                val lng  = data.getDoubleExtra("lng", 0.0)
                val desc = data.getStringExtra("desc") ?: ""
                val uri  = Uri.parse(data.getStringExtra("photoUri"))
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(lat, lng))
                        .title(name)
                        .snippet(desc)
                )
                // Aqui você pode armazenar `uri` em um repositório/localDB, se quiser.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Mapa
        val mapView: MapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // FABs
        primaryFab = findViewById(R.id.primary_fab)
        fabAction1 = findViewById(R.id.fab_action_1)
        fabAction2 = findViewById(R.id.fab_action_2)

        // Esconde e desabilita até o mapa carregar
        fabAction1.visibility = View.GONE
        fabAction1.isEnabled = false
        fabAction2.visibility = View.GONE

        primaryFab.setOnClickListener {
            if (isFabExpanded) collapseFab() else expandFab()
        }

        fabAction1.setOnClickListener {
            collapseFab()
            if (!::map.isInitialized) {
                Toast.makeText(this, "Aguarde o carregamento do mapa…", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val center = map.cameraPosition.target
            val intent = Intent(this, RegisterActivity::class.java)
                .putExtra("lat", center.latitude)
                .putExtra("lng", center.longitude)
            registerPointLauncher.launch(intent)
        }

        fabAction2.setOnClickListener {
            collapseFab()

            val intent = Intent(this, ListPontosTuristicosActivity::class.java)
            startActivity(intent)
        }


        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.my_toolbar)
        toolbar.title = "Pontos Turísticos"
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun expandFab() {
        isFabExpanded = true
        primaryFab.setImageResource(R.drawable.settings)
        fabAction1.visibility = View.VISIBLE
        fabAction2.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(fabAction1, "translationY", -resources.getDimension(R.dimen.fab_margin_large)).start()
        ObjectAnimator.ofFloat(fabAction2, "translationY", -resources.getDimension(R.dimen.fab_margin_extra_large)).start()
    }

    private fun collapseFab() {
        isFabExpanded = false
        primaryFab.setImageResource(R.drawable.settings)
        ObjectAnimator.ofFloat(fabAction1, "translationY", 0f).start()
        ObjectAnimator.ofFloat(fabAction2, "translationY", 0f).start()
        fabAction1.visibility = View.GONE
        fabAction2.visibility = View.GONE
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
        // libera o FAB de cadastro
        fabAction1.isEnabled = true
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            getLastKnownLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) enableMyLocation()
        }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation
            .addOnSuccessListener { loc: Location? ->
                loc?.let {
                    currentLatitude = it.latitude
                    currentLongitude = it.longitude

                    val userLoc = LatLng(it.latitude, it.longitude)
                    //map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 15f))
                    map.addMarker(MarkerOptions().position(userLoc).title("Você está aqui"))

                    setConfig()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        findViewById<MapView>(R.id.mapView).onResume()
        if (::map.isInitialized) {
            if (currentLatitude != 0.0 || currentLongitude != 0.0) {
                setConfig()
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    enableMyLocation()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        findViewById<MapView>(R.id.mapView).onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        findViewById<MapView>(R.id.mapView).onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        findViewById<MapView>(R.id.mapView).onLowMemory()
    }

    private fun setConfig(){
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val mapsTypes = sharedPreferences.getString("mapsTypes", "Rodovias")
        val zoom = sharedPreferences.getString("zoom", "Médio")
        map.mapType = when (mapsTypes) {
            "Rodovias" -> GoogleMap.MAP_TYPE_NORMAL
            "Satélite" -> GoogleMap.MAP_TYPE_SATELLITE
            "Híbrido" -> GoogleMap.MAP_TYPE_HYBRID
            else -> GoogleMap.MAP_TYPE_SATELLITE
        }
        val zoomLevel = when (zoom) {
            "Perto" -> 20.0f
            "Médio" -> 15.0f
            "Distante" -> 10.0f
            else -> 15.0f
        }
        //Ajustar aqui para colocar a latitude e longitude atual
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLatitude, currentLongitude), zoomLevel))
    }
}
