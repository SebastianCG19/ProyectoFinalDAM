package com.example.proyectofinaldam

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinaldam.model.Almohadas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListadoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var almohadasAdapter: AlmohadasAdapter
    private var listaAlmohadas: MutableList<Almohadas> = mutableListOf()
    private lateinit var btnLogout: Button // Declara el botón
    private lateinit var etBuscar: EditText // Declara el campo de búsqueda
    private lateinit var btnBuscar: Button // Declara el botón de búsqueda

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado)

        // Inicializa los elementos de la vista
        recyclerView = findViewById(R.id.rv)
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnLogout = findViewById(R.id.btnLogout)
        etBuscar = findViewById(R.id.etBuscar)
        btnBuscar = findViewById(R.id.btnBuscar)

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
            finish() // Opcional: terminar la actividad actual
        }

        // Cargar datos desde la base de datos
        obtenerAlmohadasExistentes()
    }

    private fun obtenerAlmohadasExistentes() {
        // Usa Coroutine para acceder a la base de datos
        CoroutineScope(Dispatchers.IO).launch {
            listaAlmohadas = AlmohadasDatabase.getDatabase(applicationContext).almohadasDAO().getAllAlmohadas().toMutableList()

            withContext(Dispatchers.Main) {
                almohadasAdapter = AlmohadasAdapter(listaAlmohadas, { almohada -> eliminarAlmohada(almohada) }, { almohada -> editarAlmohada(almohada) })
                recyclerView.adapter = almohadasAdapter // Configura el adaptador con la lista
            }
        }
    }

    private fun buscarAlmohadas(query: String) {
        val filteredList = listaAlmohadas.filter { almohada ->
            almohada.nomProducto.contains(query, ignoreCase = true) // Cambia 'nombre' al campo adecuado en tu modelo Almohadas
        }
        almohadasAdapter.updateAlmohadas(filteredList) // Asume que tienes un método updateList en tu adaptador para actualizar los datos
    }

    private fun eliminarAlmohada(almohada: Almohadas) {
        // Usa Coroutine para eliminar la almohada en la base de datos
        CoroutineScope(Dispatchers.IO).launch {
            // Eliminar la almohada de la base de datos
            AlmohadasDatabase.getDatabase(applicationContext).almohadasDAO().delete(almohada)

            // Actualizar la lista en la interfaz de usuario
            withContext(Dispatchers.Main) {
                listaAlmohadas.remove(almohada)  // Eliminar de la lista en memoria
                almohadasAdapter.notifyDataSetChanged()  // Notificar cambios en la UI
            }
        }
    }


    private fun editarAlmohada(almohada: Almohadas) {
        // Implementa la lógica para editar la almohada
        // Por ejemplo, abrir un diálogo para editar la almohada y luego actualizar la lista
    }
}
