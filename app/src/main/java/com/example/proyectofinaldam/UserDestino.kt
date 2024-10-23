package com.example.proyectofinaldam

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectofinaldam.databinding.ActivityUserDestinoBinding
import com.example.proyectofinaldam.model.Almohadas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserDestino : AppCompatActivity() {
    private lateinit var binding: ActivityUserDestinoBinding
    private lateinit var almohadasDao: AlmohadasDAO
    private var almohadaData: Almohadas? = null
    private lateinit var database: AlmohadasDatabase
    private lateinit var adapter: AlmohadasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserDestinoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de la base de datos y el DAO
        database = AlmohadasDatabase.getDatabase(this)
        almohadasDao = database.almohadasDAO()

        // Configuración del RecyclerView
        binding.rv.layoutManager = LinearLayoutManager(this)

        //icialización del adaptador
        adapter = AlmohadasAdapter(listOf(), { almohada ->
            deleteAlmohada(almohada)
        }, { almohada ->
            updateAlmohadas(almohada)
        })


        binding.rv.adapter = adapter

        // Configuración del botón de agregar
        binding.btnAgregar.setOnClickListener {
            if (almohadaData != null) {
                almohadaData.let {
                    it?.nomProducto = binding.etNombre.text.toString()
                    it?.tamanio = binding.etTamaO.text.toString()
                    it?.stock = binding.etStock.text.toString()
                }
                updateAlmohadas(almohadaData)
            } else {
                val nuevaAlmohada = Almohadas(
                    nomProducto = binding.etNombre.text.toString(),
                    tamanio = binding.etTamaO.text.toString(),
                    stock = binding.etStock.text.toString()
                )
                createAlmohada(nuevaAlmohada)
            }
            binding.etNombre.text.clear()
            binding.etTamaO.text.clear()
            binding.etStock.text.clear()

            binding.etNombre.requestFocus()
        }

        // Cargar datos iniciales
        updateDate()
    }

    fun createAlmohada(almohadas: Almohadas) {
        CoroutineScope(Dispatchers.IO).launch {
            almohadasDao.insert(almohadas)
            updateDate()
        }
    }

    fun updateAlmohadas(almohadas: Almohadas?) {
        CoroutineScope(Dispatchers.IO).launch {
            almohadas?.let {
                almohadasDao.update(it)
            }
            updateDate()
            almohadaData = null
        }
    }

    fun updateDate() {
        CoroutineScope(Dispatchers.IO).launch {
            val almohadas = almohadasDao.getAllAlmohadas()
            withContext(Dispatchers.Main) {
                adapter.updateAlmohadas(almohadas)
            }
        }
    }

    fun deleteAlmohada(almohadas: Almohadas) {
        CoroutineScope(Dispatchers.IO).launch {
            almohadasDao.delete(almohadas)
            updateDate()
        }
    }
}
