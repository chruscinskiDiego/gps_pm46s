package br.edu.utfpr.gps_pm46s

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.textfield.TextInputEditText
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import br.edu.utfpr.gps_pm46s.database.GpsDbHelper
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etLatitude: TextInputEditText
    private lateinit var etLongitude: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var ivPhoto: ImageView
    private var photoUri: Uri? = null
    private lateinit var dbHelper: GpsDbHelper

    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var currentPhotoPath: String

    // Permissão para câmera
    private val cameraPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            dispatchTakePictureIntent()
        } else {
            Toast.makeText(this, "Permissão para câmera negada.", Toast.LENGTH_SHORT).show()
        }
    }

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
        val btnTakePhoto = findViewById<Button>(R.id.btnTakePhoto)
        val btnSave   = findViewById<Button>(R.id.btnSave)

        dbHelper = GpsDbHelper(this)

        // Inicializa o ActivityResultLauncher para tirar foto
        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                setPic()
            } else {
                Toast.makeText(this, "Falha ao capturar a foto.", Toast.LENGTH_SHORT).show()
            }
        }

        intent.extras?.let { extras ->
            extras.getDouble("lat") .takeIf { it != 0.0 }?.let { etLatitude.setText(it.toString()) }
            extras.getDouble("lng") .takeIf { it != 0.0 }?.let { etLongitude.setText(it.toString()) }
        }

        btnPick.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnTakePhoto.setOnClickListener {
            checkCameraPermission()
        }

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

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permissão não concedida, solicitar
            cameraPermissionRequest.launch(Manifest.permission.CAMERA)
        } else {
            // Permissão já concedida, abrir a câmera
            dispatchTakePictureIntent()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Cria um nome de arquivo de imagem com base no tempo
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir: File? = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Salva o caminho do arquivo para usar depois
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Garante que há um aplicativo de câmera para lidar com a intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Cria o arquivo onde a foto deve ir
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Erro ao criar o arquivo
                    null
                }
                // Continua apenas se o arquivo foi criado com sucesso
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "br.edu.utfpr.gps_pm46s.fileprovider", // Substitua pelo seu authority no AndroidManifest.xml
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePictureLauncher.launch(takePictureIntent)
                }
            }
        }
    }

    private fun setPic() {
        ivPhoto.setImageURI(Uri.fromFile(File(currentPhotoPath)))
        photoUri = Uri.fromFile(File(currentPhotoPath)) // Atualiza o photoUri para salvar no banco
    }

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
