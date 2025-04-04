package com.example.practica12

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import android.Manifest
import android.widget.Toast

class CreatePokemonActivity : AppCompatActivity() {
    val cloudName = "234"
    val database = FirebaseDatabase.getInstance("https://pokemon-ff997-default-rtdb.firebaseio.com/")
    val pokemones = database.getReference("pokemones")
    val config = HashMap<String, String>()

    private var selectedImageUri: Uri? = null // Variable para almacenar la URI de la imagen seleccionada

    private val getImageLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            selectedImageUri = data?.data // Guardamos la URI seleccionada
            // Establecer la imagen seleccionada en el ImageView
            val imgPokemon = findViewById<ImageView>(R.id.imgPokemon)
            imgPokemon.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_pokemon)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        config["cloud_name"] = "dmt2hd7q6"
        config["api_key"] = "735469152226555"
        config["api_secret"] = "_8Tu-1wy5v67rcsMoJ9tpdEOX_4"

        val cloudinary = Cloudinary(config)

        val pokemon_number = findViewById<EditText>(R.id.etPokemonNumber)
        val pokemon_name = findViewById<EditText>(R.id.etPokemonName)
//        val pokemon_image = findViewById<ImageView>(R.id.imgPokemon)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        val btnSavePokemon = findViewById<Button>(R.id.btnSavePokemon)

        checkStoragePermission()

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getImageLauncher.launch(intent)
        }

        btnSavePokemon.setOnClickListener {
            val number = pokemon_number.text.toString().trim()
            val name = pokemon_name.text.toString().trim()

            if (number.isEmpty()) {
                Log.e("Validación", "El número del Pokémon es requerido.")
                pokemon_number.error = "Requerido"
                Toast.makeText(this, "El número del Pokémon es necesario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (name.isEmpty()) {
                Log.e("Validación", "El nombre del Pokémon es requerido.")
                pokemon_name.error = "Requerido"
                Toast.makeText(this, "El nombre del Pokémon es necesario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Log.e("Validación", "La imagen del Pokémon es requerida.")
                Toast.makeText(this, "La imagen del Pokémon es necesaria", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Si todo está correcto, continuar
            val imageUrl = uploadImageToCloudinary(selectedImageUri!!, cloudinary)
            val newPokemon = Pokemon(number, name, imageUrl)

            pokemones.push().setValue(newPokemon)
                .addOnSuccessListener {
                    Log.d("Firebase", "Pokemon guardado exitosamente")
                }
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "Error al guardar el Pokémon", exception)
                }

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

        fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission", "Permiso concedido para leer imágenes.")
                } else {
                    Log.e("Permission", "Permiso denegado para leer imágenes.")
                }
            }
        }
    }

    private fun uploadImageToCloudinary(imageUri: Uri, cloudinary: Cloudinary): String {
        val file = File(getRealPathFromURI(imageUri))
        var image = ""

        val thread = Thread {
            try {
                val uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
                image = uploadResult["url"].toString().replace("http://", "https://")
                Log.d("Cloudinary", "Imagen subida con éxito. URL: $image")

                runOnUiThread {
                    Log.d("Cloudinary", "Imagen subida: $image")
                }

            } catch (e: Exception) {
                Log.e("Cloudinary", "Error al subir la imagen: ${e.message}")
            }
        }
        thread.start()
        thread.join() // Esto hace que el hilo principal espere

        return image
    }


    private fun getRealPathFromURI(contentURI: Uri): String {
        var result: String = ""
        val cursor = contentResolver.query(contentURI, null, null, null, null)
        cursor?.let {
            it.moveToFirst()
            val columnIndex = it.getColumnIndex("_data")
            result = it.getString(columnIndex)
            it.close()
        }
        return result
    }
}
