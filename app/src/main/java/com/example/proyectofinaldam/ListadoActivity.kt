package com.example.proyectofinaldam

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinaldam.databinding.ActivityListadoBinding
import com.example.proyectofinaldam.model.Almohadas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListadoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListadoBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var almohadasAdapter: AlmohadasAdapter
    private var listaAlmohadas: MutableList<Almohadas> = mutableListOf()
    private lateinit var btnLogout: Button
    private lateinit var etBuscar: EditText
    private lateinit var btnBuscar: Button

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_listado)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityListadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = binding.rv
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnLogout = binding.btnLogout
        etBuscar = binding.etBuscar
        btnBuscar = binding.btnBuscar

        // Configura el botón de agregar nuevo producto
        binding.btnDirAgregar.setOnClickListener {
            // Lógica para agregar o editar un producto
            val intent = Intent(this, UserDestino::class.java)
            startActivity(intent)
        }

        // Configura el botón de búsqueda
        btnBuscar.setOnClickListener {
            buscarAlmohadas(etBuscar.text.toString())
        }

        // Inicializa el botón de cerrar sesión
        btnLogout.setOnClickListener {
            UtilsSharePreferences.clearSession(this)
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        // Cargar datos desde la base de datos
        obtenerAlmohadasExistentes()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK) {
            loadAlmohadas() // Refresh the list after the update
        }
    }



    private fun obtenerAlmohadasExistentes() {
        CoroutineScope(Dispatchers.IO).launch {
            listaAlmohadas = AlmohadasDatabase.getDatabase(applicationContext).almohadasDAO().getAllAlmohadas().toMutableList()

            withContext(Dispatchers.Main) {
                almohadasAdapter = AlmohadasAdapter(listaAlmohadas, { almohada -> eliminarAlmohada(almohada) }, { almohada -> editarAlmohada(almohada) })
                recyclerView.adapter = almohadasAdapter
            }
        }
    }

    private fun buscarAlmohadas(query: String) {
        val filteredList = listaAlmohadas.filter { almohada ->
            almohada.nomProducto.contains(query, ignoreCase = true)
        }
        almohadasAdapter.updateAlmohadas(filteredList)
    }

    private fun eliminarAlmohada(almohada: Almohadas) {
        CoroutineScope(Dispatchers.IO).launch {
            AlmohadasDatabase.getDatabase(applicationContext).almohadasDAO().delete(almohada)

            withContext(Dispatchers.Main) {
                listaAlmohadas.remove(almohada)
                almohadasAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun editarAlmohada(almohada: Almohadas) {
            val intent = Intent(this, UserDestino::class.java)
        intent.putExtra("ID", almohada.id)
        startActivityForResult(intent, 200)  // Cambiar a startActivityForResult
    }


    private fun loadAlmohadas() {
        CoroutineScope(Dispatchers.IO).launch {
            listaAlmohadas = AlmohadasDatabase.getDatabase(applicationContext).almohadasDAO().getAllAlmohadas().toMutableList()
            withContext(Dispatchers.Main) {
                almohadasAdapter.updateAlmohadas(listaAlmohadas)
            }
        }
    }


}
/*
        val intent = Intent(this, UserDestino::class.java).apply {
            putExtra("ALMOHADA_ID", almohada.id)
            putExtra("NOMBRE_PRODUCTO", almohada.nomProducto)
            putExtra("TAMANIO", almohada.tamanio)
            putExtra("STOCK", almohada.stock)
            putExtra("IMAGE_URL", almohada.imageUrl)
        }
        startActivity(intent)
    }
}
*/