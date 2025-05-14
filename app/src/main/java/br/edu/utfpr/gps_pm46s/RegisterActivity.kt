package br.edu.utfpr.gps_pm46s

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import br.edu.utfpr.gps_pm46s.database.GpsDbHelper

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etLatitude: TextInputEditText
    private lateinit var etLongitude: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var ivPhoto: ImageView
    private var photoUri: Uri? = null
    private lateinit var dbHelper: GpsDbHelper

    //img da galeria
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUri = it
            ivPhoto.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        //views
        etName        = findViewById(R.id.etName)
        etLatitude    = findViewById(R.id.etLatitude)
        etLongitude   = findViewById(R.id.etLongitude)
        etDescription = findViewById(R.id.etDescription)
        ivPhoto       = findViewById(R.id.ivPhoto)
        val btnPick   = findViewById<Button>(R.id.btnPickImage)
        val btnSave   = findViewById<Button>(R.id.btnSave)

        intent.extras?.let { extras ->
            extras.getDouble("lat") .takeIf { it != 0.0 }?.let { etLatitude.setText(it.toString()) }
            extras.getDouble("lng") .takeIf { it != 0.0 }?.let { etLongitude.setText(it.toString()) }
        }

        btnPick.setOnClickListener {

            pickImageLauncher.launch("image/*")
        }

        dbHelper = GpsDbHelper(this)

        btnSave.setOnClickListener {
            savePoint()
        }
    }

    /*private fun savePoint() {
        val name = etName.text.toString().trim()
        val lat  = etLatitude.text.toString().toDoubleOrNull()
        val lng  = etLongitude.text.toString().toDoubleOrNull()
        val desc = etDescription.text.toString().trim()

        if (name.isEmpty() || lat == null || lng == null || desc.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos e escolha uma imagem", Toast.LENGTH_SHORT).show()
            return
        }

        // ações com os dados, salvar em um bd, etc...
        val data = Intent().apply {
            putExtra("name", name)
            putExtra("lat", lat)
            putExtra("lng", lng)
            putExtra("desc", desc)
            putExtra("photoUri", photoUri.toString())
        }
        setResult(Activity.RESULT_OK, data)
        finish()
    }*/

    private fun savePoint() {
        val name = etName.text.toString().trim()
        val lat  = etLatitude.text.toString().toDoubleOrNull()
        val lng  = etLongitude.text.toString().toDoubleOrNull()
        val desc = etDescription.text.toString().trim()

        if (name.isEmpty() || lat == null || lng == null || desc.isEmpty() || photoUri == null) {
            Toast.makeText(this, "Preencha todos os campos e escolha uma imagem", Toast.LENGTH_SHORT).show()
            return
        }

        photoUri?.let { uri ->
            grantUriPermission(
                "br.edu.utfpr.gps_pm46s",
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        val id = dbHelper.insertPoint(
            name = name,
            lat = lat,
            lng = lng,
            desc = desc,
            photoUri = photoUri.toString(),

        )

        if (id > 0) {
            Toast.makeText(this, "Ponto salvo com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Erro ao salvar o ponto.", Toast.LENGTH_SHORT).show()
        }
    }
}
