package com.example.practica12

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.cloudinary.Cloudinary
import com.cloudinary.api.ApiResponse
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    val database =
        FirebaseDatabase.getInstance("https://pokemon-ff997-default-rtdb.firebaseio.com/")
    val pokemones = database.getReference("pokemones")
    val config = HashMap<String, String>()

    private lateinit var listView: ListView
    private lateinit var pokemonList: MutableList<Pokemon>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Configuración de Cloudinary
        config["cloud_name"] = "dmt2hd7q6"
        config["api_key"] = "735469152226555"
        config["api_secret"] = "_8Tu-1wy5v67rcsMoJ9tpdEOX_4"
        val cloudinary = Cloudinary(config)

        listView = findViewById(R.id.listViewPokemon)
        pokemonList = mutableListOf()

        // Cargar los Pokémon desde Firebase
        fetchPokemonData(cloudinary)

        val addPokemonButton = findViewById<Button>(R.id.btnAddPokemon)
        addPokemonButton.setOnClickListener() {
            val intent = Intent(this, CreatePokemonActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchPokemonData(cloudinary: Cloudinary) {
        // Obtener los datos de Firebase
        pokemones.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pokemonList.clear()

                for (pokemonSnapshot in snapshot.children) {
                    val pokemon = pokemonSnapshot.getValue(Pokemon::class.java)
                    if (pokemon != null) {
                        pokemonList.add(pokemon)
                    }
                }

                // Crear el adaptador y establecerlo en el ListView
                val adapter = PokemonAdapter(this@MainActivity, pokemonList, cloudinary)
                listView.adapter = adapter

                // Notificar al adaptador que los datos han cambiado
                adapter.notifyDataSetChanged()  // Asegura que el ListView se actualice
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al obtener los datos de Firebase", error.toException())
            }
        })
    }



    class PokemonAdapter(private val context: Context, private val pokemonList: List<Pokemon>, private val cloudinary: Cloudinary) : BaseAdapter() {

        override fun getCount(): Int {
            return pokemonList.size
        }

        override fun getItem(position: Int): Any {
            return pokemonList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val pokemon = pokemonList[position]

            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.pokemon_list_item, parent, false)

            val pokemonImageView = view.findViewById<ImageView>(R.id.imgPokemon)
            val pokemonNameTextView = view.findViewById<TextView>(R.id.tvPokemonName)
            val pokemonNumberTextView = view.findViewById<TextView>(R.id.tvPokemonNumber)

            // Consulta la imagen de Cloudinary usando el public_id
            val pokemonImageUrl = pokemon.image

            // Cargar la imagen con Glide usando la URL obtenida de Cloudinary
            Glide.with(context)
                .load(pokemonImageUrl) // Usa la URL obtenida de Cloudinary
                .override(100, 100)  // Redimensionar la imagen (opcional)
                .into(pokemonImageView) // Mostrar la imagen en el ImageView


            // Establecer el nombre y el número del Pokémon en los TextViews
            pokemonNameTextView.text = pokemon.name
            pokemonNumberTextView.text = "#${pokemon.number}"

            return view
        }

    }

}