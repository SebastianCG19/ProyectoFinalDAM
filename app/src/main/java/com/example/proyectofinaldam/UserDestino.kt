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
    private var selectedImageUri: Uri = Uri.EMPTY // Valor inicial para evitar verificaciones
    private val REQUEST_CODE_STORAGE_PERMISSION = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserDestinoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Solicitar permisos de almacenamiento si no están concedidos
        checkStoragePermission()

        // Configurar ventana para edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Configurar la base de datos y el adaptador
        initDatabaseAndAdapter()

        // Verificar si estamos en modo edición y cargar datos si es necesario
        if (intent.hasExtra("NOMBRE_PRODUCTO")) {
            loadDataForEditing()
        }

        // Configurar eventos de los botones
        setupEventHandlers()
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE_PERMISSION)
        }
    }

    // Inicializar la base de datos y el adaptador del RecyclerView
    private fun initDatabaseAndAdapter() {
        database = AlmohadasDatabase.getDatabase(this)
        almohadasDao = database.almohadasDAO()
        adapter = AlmohadasAdapter(listOf(), { deleteAlmohada(it) }, { updateAlmohadaForEditing(it) })
    }

    private fun loadDataForEditing() {


        // Obtener datos del Intent
        val id = intent.getLongExtra("ID", -1) // Cambiado de getIntExtra a getLongExtra
        val nombreProducto = intent.getStringExtra("NOMBRE_PRODUCTO") ?: ""
        val tamanio = intent.getStringExtra("TAMANIO") ?: ""
        val stock = intent.getStringExtra("STOCK") ?: ""
        val imageUrl = intent.getStringExtra("IMAGE_URL") ?: ""



        // Cargar datos en campos de entrada
        if (id != -1L) { // Asegúrate de usar -1L para la comparación
            binding.etID.setText(id.toString())
            binding.etNombre.setText(nombreProducto)
            binding.etStock.setText(stock)
            // Seleccionar el tamaño correcto basado en los datos
            when (intent.getStringExtra("TAMANIO")) {
                "Grande" -> binding.rbGrande.isChecked = true
                "Mediano" -> binding.rbMediano.isChecked = true
                "Pequeño" -> binding.rbPequenio.isChecked = true
            }

            // Cargar la imagen con Picasso
            intent.getStringExtra("IMAGE_URL")?.let {
                if (it.isNotEmpty()) Picasso.get().load(it).into(binding.uploadImage)
            }
            almohadaData = Almohadas(id = id, nomProducto = nombreProducto, tamanio = tamanio, stock = stock, imageUrl = imageUrl)
        }

        /*
        binding.etID.setText(intent.getStringExtra("ID")) // Cargar el ID en el EditText
        binding.etNombre.setText(intent.getStringExtra("NOMBRE_PRODUCTO"))
        binding.etStock.setText(intent.getStringExtra("STOCK"))

        // Seleccionar el tamaño correcto basado en los datos
        when (intent.getStringExtra("TAMANIO")) {
            "Grande" -> binding.rbGrande.isChecked = true
            "Mediano" -> binding.rbMediano.isChecked = true
            "Pequeño" -> binding.rbPequenio.isChecked = true
        }

        // Cargar la imagen con Picasso
        intent.getStringExtra("IMAGE_URL")?.let {
            if (it.isNotEmpty()) Picasso.get().load(it).into(binding.uploadImage)
        }

        // Almacenar datos actuales para actualizar después
        almohadaData = Almohadas(
            id = binding.etID.text.toString().toLongOrNull() ?: 0L, // Conversión segura a Long
            nomProducto = binding.etNombre.text.toString(),
            tamanio = binding.rgTamanio.checkedRadioButtonId.toString(),
            stock = binding.etStock.text.toString(),
            imageUrl = intent.getStringExtra("IMAGE_URL") ?: ""
        )*/
    }

    private fun setupEventHandlers() {
        binding.uploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 100)
        }

        binding.btnAgregar.setOnClickListener { handleAddOrUpdate() }
        binding.btnLogout.setOnClickListener { logout() }
        binding.btnListado.setOnClickListener {
            startActivity(Intent(this, ListadoActivity::class.java))
        }
    }

    private fun handleAddOrUpdate() {
        val idAlmohada = binding.etID.text.toString().toLongOrNull() ?: 0L // Convertir a Long o nulo
        val nombreProducto = binding.etNombre.text.toString()
        val stock = binding.etStock.text.toString()
        val selectedTamanio = when (binding.rgTamanio.checkedRadioButtonId) {
            R.id.rbGrande -> "Grande"
            R.id.rbMediano -> "Mediano"
            R.id.rbPequenio -> "Pequeño"
            else -> ""
        }

        // Validación de campos
        if (nombreProducto.isEmpty() || stock.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos requeridos.", Toast.LENGTH_SHORT).show()
            return
        }

        val imageUri = selectedImageUri.toString() // Usar la URI seleccionada o una vacía

        if (almohadaData != null) { // Modo edición
            almohadaData?.apply {
                id = idAlmohada // Usa el ID ingresado
                nomProducto = nombreProducto
                tamanio = selectedTamanio
                this.stock = stock
                imageUrl = imageUri
            }
            updateAlmohada(almohadaData)
            Toast.makeText(this, "Se actualizó correctamente", Toast.LENGTH_SHORT).show()
        } else { // Modo creación
            createAlmohada(
                Almohadas(nomProducto = nombreProducto, tamanio = selectedTamanio, stock = stock, imageUrl = imageUri)
            )
            Toast.makeText(this, "Se agregó correctamente", Toast.LENGTH_SHORT).show()
        }

        clearInputFields()
    }


    private fun clearInputFields() {
        binding.etID.text.clear()
        binding.etNombre.text.clear()
        binding.rgTamanio.clearCheck()
        binding.etStock.text.clear()
        binding.uploadImage.setImageResource(R.drawable.uploadimg)
        binding.etNombre.requestFocus()
        selectedImageUri = Uri.EMPTY
    }

    private fun logout() {
        UtilsSharePreferences.clearSession(this)
        Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(this)
        }
        finish()
    }

    private fun updateAlmohadaForEditing(almohada: Almohadas) {
        almohadaData = almohada
        binding.etID.setText(almohada.id.toString())
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
            updateData()
        }
    }

    private fun updateAlmohada(almohadas: Almohadas?) {
        almohadas?.let {
            CoroutineScope(Dispatchers.IO).launch {
                almohadasDao.update(it)
                updateData()
            }
        }
    }

    private fun deleteAlmohada(almohada: Almohadas) {
        CoroutineScope(Dispatchers.IO).launch {
            almohadasDao.delete(almohada)
            updateData()
        }
    }

    private fun updateData() {
        CoroutineScope(Dispatchers.IO).launch {
            val almohadasList = almohadasDao.getAllAlmohadas()
            withContext(Dispatchers.Main) {
                adapter.updateAlmohadas(almohadasList)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            data?.data?.let {
                selectedImageUri = it
                binding.uploadImage.setImageURI(selectedImageUri)
            }
        }
    }
}
