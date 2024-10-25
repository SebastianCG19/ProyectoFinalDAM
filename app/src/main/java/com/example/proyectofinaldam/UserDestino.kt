package com.example.proyectofinaldam

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectofinaldam.databinding.ActivityUserDestinoBinding
import com.example.proyectofinaldam.model.Almohadas
import com.squareup.picasso.Picasso
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
    private val REQUEST_CODE_STORAGE_PERMISSION = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserDestinoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar y solicitar permisos
        checkStoragePermission()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Verificar si hay datos en el Intent (si venimos desde el botón "Editar")
        val nombreProducto = intent.getStringExtra("NOMBRE_PRODUCTO")
        val tamanio = intent.getStringExtra("TAMANIO")
        val stock = intent.getStringExtra("STOCK")
        val imageUrl = intent.getStringExtra("IMAGE_URL")

        // Si los datos no son nulos, significa que estamos en modo edición
        if (nombreProducto != null && tamanio != null && stock != null && imageUrl != null) {
            // Rellenar los campos con los datos recibidos
            binding.etNombre.setText(nombreProducto)
            binding.etStock.setText(stock)

            // Seleccionar el tamaño correcto basado en los datos
            when (tamanio) {
                "Grande" -> binding.rbGrande.isChecked = true
                "Mediano" -> binding.rbMediano.isChecked = true
                "Pequeño" -> binding.rbPequenio.isChecked = true
            }

            // Cargar la imagen si existe una URL válida
            if (imageUrl.isNotEmpty()) {
                Picasso.get().load(imageUrl).into(binding.uploadImage)
            }

            // Almacenar los datos en la variable almohadaData para poder actualizarlos luego
            almohadaData = Almohadas(
                nomProducto = nombreProducto,
                tamanio = tamanio,
                stock = stock,
                imageUrl = imageUrl
            )
        }

        // Inicialización de la base de datos y el DAO
        database = AlmohadasDatabase.getDatabase(this)
        almohadasDao = database.almohadasDAO()

        // Inicialización del adaptador
        adapter = AlmohadasAdapter(listOf(), { almohada -> deleteAlmohada(almohada) }, { almohada -> updateAlmohadaForEditing(almohada) })

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

            // Recoger el nombre y el stock, asegurándose de que no estén vacíos
            val nombreProducto = binding.etNombre.text.toString()
            val stock = binding.etStock.text.toString()

            if (nombreProducto.isEmpty() || stock.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos requeridos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Evita continuar si hay campos vacíos
            }

            // Manejar la URI de la imagen
            val imageUri = if (::selectedImageUri.isInitialized) {
                selectedImageUri.toString()
            } else {
                "" // Usar una cadena vacía si no se ha inicializado
            }

            // Si ya existe una almohada para actualizar
            if (almohadaData != null) {
                almohadaData?.let {
                    it.nomProducto = nombreProducto
                    it.tamanio = selectedTamanio // Asignamos el tamaño seleccionado
                    it.stock = stock // Mantener stock como String
                    it.imageUrl = imageUri // Usar la URI de la imagen
                }
                updateAlmohadas(almohadaData) // Actualiza la almohada existente
                Toast.makeText(this,"Se actualizó correctamente",Toast.LENGTH_SHORT).show()
            } else {
                // Crear una nueva almohada
                val nuevaAlmohada = Almohadas(
                    nomProducto = nombreProducto,
                    tamanio = selectedTamanio, // Asignamos el tamaño seleccionado
                    stock = stock, // Mantener stock como String
                    imageUrl = imageUri // Usar la URI de la imagen
                )
                createAlmohada(nuevaAlmohada) // Crea una nueva almohada
                Toast.makeText(this,"Se agregó correctamente",Toast.LENGTH_SHORT).show()
            }

            // Limpiar campos después de agregar
            clearInputFields()
        }

        binding.btnLogout.setOnClickListener {
            // Limpiar la sesión
            UtilsSharePreferences.clearSession(this)

            // Volver a la MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish() // Opcional: terminar la actividad actual
        }

        // Configurar botón para redirigir a la lista de almohadas (nueva vista)
        binding.btnListado.setOnClickListener {
            val intent = Intent(this, ListadoActivity::class.java)
            startActivity(intent)
        }

        // Cargar datos iniciales
        updateData()
    }

    // Método para verificar y solicitar permisos
    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE_PERMISSION)
        }
    }

    // Manejar la respuesta de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, puedes continuar
            } else {
                // El permiso fue denegado, informa al usuario
                Toast.makeText(this, "Permiso denegado para acceder a la galería.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearInputFields() {
        binding.etNombre.text.clear()
        binding.rgTamanio.clearCheck()  // Limpiar selección de RadioButtons
        binding.etStock.text.clear()
        binding.uploadImage.setImageResource(R.drawable.uploadimg) // Limpiar el campo de subida de la imagen
        binding.etNombre.requestFocus()
    }

    private fun filterAlmohadas(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val filteredList = almohadasDao.getAllAlmohadas().filter {
                it.nomProducto.contains(query, ignoreCase = true)
            }
            withContext(Dispatchers.Main) {
                adapter.filterAlmohadas(query) // Cambiado de filterList a filterAlmohadas
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

    private fun createAlmohada(almohadas: Almohadas) {
        CoroutineScope(Dispatchers.IO).launch {
            almohadasDao.insert(almohadas)
            updateData() // Asegúrate de actualizar la lista después de agregar
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data!! // Guardar la URI de la imagen seleccionada
            binding.uploadImage.setImageURI(selectedImageUri) // Mostrar la imagen seleccionada
        }
    }

    private fun deleteAlmohada(almohada: Almohadas) {
        CoroutineScope(Dispatchers.IO).launch {
            almohadasDao.delete(almohada)
            updateData() // Asegúrate de actualizar la lista después de eliminar
        }
    }

    private fun updateAlmohadas(almohadas: Almohadas?) {
        almohadas?.let {
            CoroutineScope(Dispatchers.IO).launch {
                almohadasDao.update(it)
                updateData() // Asegúrate de actualizar la lista después de actualizar
            }
        }
    }

    private fun updateData() {
        CoroutineScope(Dispatchers.IO).launch {
            val almohadasList = almohadasDao.getAllAlmohadas()
            withContext(Dispatchers.Main) {
                adapter.updateAlmohadas(almohadasList) // Actualiza la lista en el adaptador
            }
        }
    }
}
