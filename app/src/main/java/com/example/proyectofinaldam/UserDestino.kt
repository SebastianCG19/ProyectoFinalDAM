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
    private var userData: Almohadas? = null
    private lateinit var database: AlmohadasDatabase
    private lateinit var adapter:AlmohadasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding =ActivityUserDestinoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = AlmohadasDatabase.getDatabase(this)
        almohadasDao = database.almohadasDAO()
        binding.rv.layoutManager = LinearLayoutManager(this)

        binding.rv.adapter = adapter

        binding.btnAgregar.setOnClickListener {
            if(userData != null){
                userData.let {
                    it?.nomProducto = binding.etNombre.text.toString()
                    it?.tamanio = binding.etTamaO.text.toString()
                    it?.stock= binding.etTamaO.text.toString()
                }
                almohadasDao(userData)
            }else{
                val userTest = Almohadas(nomProducto = binding.etNombre.text.toString(), tamanio = binding.etTamaO.text.toString(), stock = binding.etStock.text.toString())
                createUser(userTest)
            }
            binding.etNombre.text.clear()
            binding.etTamaO.text.clear()
            binding.etStock.text.clear()
        }

        /*
        binding.btnSalir.setOnClickListener {
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


        updateDate()

        */

    }
    fun createUser(almohadas : Almohadas){
        CoroutineScope(Dispatchers.IO).launch {
            almohadasDao.insert(almohadas)
            updateDate()
        }
    }

    fun updateAlmohadas(almohadas : Almohadas?){
        CoroutineScope(Dispatchers.IO).launch {
            almohadas?.let{
                almohadasDao.update(it)
            }
            updateDate()
            userData = null
        }
    }


    fun updateDate(){
        CoroutineScope(Dispatchers.IO).launch {
            val almohadas = almohadasDao.getAllAlmohadas()
            withContext(Dispatchers.Main){
                adapter.updateAlmohadas(almohadasDao)
            }
        }
    }

    fun deleteAlmohada(almohadas : Almohadas){
        CoroutineScope(Dispatchers.IO).launch {
            almohadasDao.delete(almohadas)
            updateDate()
        }
    }

    }
}