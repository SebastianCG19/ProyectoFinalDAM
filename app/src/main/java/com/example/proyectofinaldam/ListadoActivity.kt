package com.example.proyectofinaldam

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Usa el binding correcto para el layout activity_listado
        binding = ActivityListadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa los elementos de la vista usando binding
        recyclerView = binding.rv
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnLogout = binding.btnLogout
        etBuscar = binding.etBuscar
        btnBuscar = binding.btnBuscar

        // Configura el botón de agregar nuevo producto
        binding.btnDirAgregar.setOnClickListener {
            val intent = Intent(this, UserDestino::class.java)
            startActivity(intent)
        }

        // Configura el botón de búsqueda
        btnBuscar.setOnClickListener {
            buscarAlmohadas(etBuscar.text.toString())
        }

        // Inicializa el botón de cerrar sesión
        btnLogout.setOnClickListener {
            // Limpiar la sesión
            UtilsSharePreferences.clearSession(this)

            // Volver a la MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        // Cargar datos desde la base de datos
        obtenerAlmohadasExistentes()
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
        // Implementa la lógica para editar la almohada
    }
}
