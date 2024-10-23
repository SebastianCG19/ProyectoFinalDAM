package com.example.proyectofinaldam

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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

    private lateinit var selectedImageUri: Uri // Variable para almacenar la URI de la imagen

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



        // Configurar el ImageView para seleccionar la imagen
        binding.uploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 100)
        }

        // Configuración del botón de agregar
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
                    it?.imageUrl = selectedImageUri.toString() // Guardar la URI de la imagen
                }
                updateAlmohadas(almohadaData)
            } else {
                val nuevaAlmohada = Almohadas(
                    nomProducto = binding.etNombre.text.toString(),
                    tamanio = selectedTamanio,  // Asignamos el tamaño seleccionado
                    stock = binding.etStock.text.toString(),
                    imageUrl = binding.uploadImage.toString()
                )
                createAlmohada(nuevaAlmohada)
            }

            // Limpiar campos después de agregar
            clearInputFields()
        }

        binding.btnLogout.setOnClickListener {
            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()  // Borra las credenciales o sesión almacenada
            editor.apply()

            // Redirige al LoginActivity o la actividad de inicio de sesión
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()  // Finaliza la actividad actual para evitar volver a ella
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            data?.data?.let {
                selectedImageUri = it
                binding.uploadImage.setImageURI(selectedImageUri) // Mostrar la imagen seleccionada
            }
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
