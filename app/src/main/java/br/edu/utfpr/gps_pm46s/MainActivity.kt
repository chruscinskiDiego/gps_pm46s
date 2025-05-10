package br.edu.utfpr.gps_pm46s

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mapView: MapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        primaryFab = findViewById(R.id.primary_fab)
        fabAction1 = findViewById(R.id.fab_action_1)
        fabAction2 = findViewById(R.id.fab_action_2)

        fabAction1.visibility = View.GONE
        fabAction2.visibility = View.GONE

        primaryFab.setOnClickListener {
            if (isFabExpanded) {
                collapseFab()
            } else {
                expandFab()
            }
        }

        fabAction1.setOnClickListener {
            // Perform action 1
            collapseFab()
        }

        fabAction2.setOnClickListener {
            // Perform action 2
            collapseFab()
        }


        val toolbar: Toolbar = findViewById(R.id.my_toolbar)
        toolbar.title = "Pontos Turísticos"
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu) // Replace main_menu with your menu file name
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Open your settings activity
                val intent = Intent (this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun expandFab() {
        isFabExpanded = true
        primaryFab.setImageResource(R.drawable.settings) // Change icon to close

        // Animate secondary FABs and backdrop
        fabAction1.visibility = View.VISIBLE
        fabAction2.visibility = View.VISIBLE

        // Example animation (you'll need to adjust positions and timings)
        ObjectAnimator.ofFloat(fabAction1, "translationY", -resources.getDimension(R.dimen.fab_margin_large)).start()
        ObjectAnimator.ofFloat(fabAction2, "translationY", -resources.getDimension(R.dimen.fab_margin_extra_large)).start()

    }

    private fun collapseFab() {
        isFabExpanded = false
        primaryFab.setImageResource(R.drawable.settings) // Change icon back to add

        // Animate secondary FABs and backdrop back
        ObjectAnimator.ofFloat(fabAction1, "translationY", 0f).start()
        ObjectAnimator.ofFloat(fabAction2, "translationY", 0f).start()

        fabAction1.visibility = View.GONE
        fabAction2.visibility = View.GONE
        // Hide after animation completes
        // You might use an AnimatorListener to hide the views and backdrop
        // when the collapse animations finish.
    }
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            getLastKnownLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                if (androidx.core.app.ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED || androidx.core.app.ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@registerForActivityResult
                }
                enableMyLocation()
            }
        }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLastKnownLocation() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLocation = LatLng(it.latitude, it.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                map.addMarker(MarkerOptions().position(userLocation).title("You are here"))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<MapView>(R.id.mapView).onResume()
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
        val maps_types = sharedPreferences.getString("tipo", "Satélite")
        val zoom = sharedPreferences.getString("zoom", "Médio")
        map.mapType = when (maps_types) {
            "Rodovias" -> GoogleMap.MAP_TYPE_NORMAL
            "Satélite" -> GoogleMap.MAP_TYPE_SATELLITE
            "Híbrido" -> GoogleMap.MAP_TYPE_HYBRID
            else -> GoogleMap.MAP_TYPE_NORMAL
        }
        val zoomLevel = when (zoom) {
            "Perto" -> 20.0f
            "Médio" -> 15.0f
            "Distante" -> 10.0f
            else -> 12.0f
        }
        //Ajustar aqui para colocar a latitude e longitude atual
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLatitude, currentLongitude), zoomLevel))
    }
}