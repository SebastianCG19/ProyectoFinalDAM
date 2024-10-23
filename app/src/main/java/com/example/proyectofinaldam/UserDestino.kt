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

        // Inicialización del adaptador
        adapter = AlmohadasAdapter(listOf(), { almohada ->
            deleteAlmohada(almohada)
        }, { almohada ->
            updateAlmohadaForEditing(almohada)
        })

        binding.rv.adapter = adapter

        binding.btnLogout.setOnClickListener {
            // Lógica para manejar el logout, como cerrar sesión y volver a la actividad de inicio de sesión.
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Opcional: termina esta actividad si no deseas que el usuario regrese a ella al presionar el botón de retroceso
        }

        // Configurar botón para agregar almohadas
        binding.btnAgregar.setOnClickListener {
            val selectedTamanio = when (binding.rgTamanio.checkedRadioButtonId) {
                R.id.rbGrande -> "Grande"
                R.id.rbMediano -> "Mediano"
                R.id.rbPequenio -> "Pequeño"
                else -> ""
            }

            if (almohadaData != null) {
                almohadaData.let {
                    it?.nomProducto = binding.etNombre.text.toString()
                    it?.tamanio = selectedTamanio // Asignamos el tamaño seleccionado
                    it?.stock = binding.etStock.text.toString()
                }
                updateAlmohadas(almohadaData)
            } else {
                val nuevaAlmohada = Almohadas(
                    nomProducto = binding.etNombre.text.toString(),
                    tamanio = selectedTamanio,  // Asignamos el tamaño seleccionado
                    stock = binding.etStock.text.toString()
                )
                createAlmohada(nuevaAlmohada)
            }

            // Limpiar campos después de agregar
            clearInputFields()
        }

        // Configurar botón para buscar almohadas
        binding.btnBuscar.setOnClickListener {
            val query = binding.etBuscar.text.toString()
            filterAlmohadas(query)
        }

        // Cargar datos iniciales
        updateData()
    }

    private fun clearInputFields() {
        binding.etNombre.text.clear()
        binding.rgTamanio.clearCheck()  // Limpiar selección de RadioButtons
        binding.etStock.text.clear()
        binding.etNombre.requestFocus()
    }

    private fun filterAlmohadas(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val filteredList = almohadasDao.getAllAlmohadas().filter {
                it.nomProducto.contains(query, ignoreCase = true)
            }
            withContext(Dispatchers.Main) {
                adapter.filterList(filteredList)
            }
        }
    }

    private fun updateAlmohadaForEditing(almohada: Almohadas) {
        almohadaData = almohada
        binding.etNombre.setText(almohada.nomProducto)
        when (almohada.tamanio) {
            "Grande" -> binding.rbGrande.isChecked = true
            "Mediano" -> binding.rbMediano.isChecked = true
            "Pequeño" -> binding.rbPequenio.isChecked = true
        }
        binding.etStock.setText(almohada.stock)
    }


    fun createAlmohada(almohadas: Almohadas) {
        CoroutineScope(Dispatchers.IO).launch {
            almohadasDao.insert(almohadas)
            updateData()
        }
    }

    fun updateAlmohadas(almohadas: Almohadas?) {
        CoroutineScope(Dispatchers.IO).launch {
            almohadas?.let {
                almohadasDao.update(it)
            }
            updateData()
            almohadaData = null
        }
    }

    fun updateData() {
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
            updateData()
        }
    }
}
