package com.example.proyectofinaldam

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectofinaldam.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var etPassword: EditText
    private lateinit var ivShowPassword: ImageView
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (UtilsSharePreferences.getSession(this)) {
            startActivity(
                Intent(this, Menu::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        } else {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Inicializa las vistas aquí
            etPassword = binding.etPassword // Asegúrate de que este ID sea correcto
            ivShowPassword = binding.ivShowPassword // Asegúrate de que este ID sea correcto

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            binding.btnLog.setOnClickListener {
                val user = binding.etUser.text.toString()
                val password = binding.etPassword.text.toString()

                // Validar que los campos no estén vacíos
                if (user.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                } else {
                    // Validar las credenciales
                    if (user == "user" && password == "123") {
                        // Crear la sesión y redirigir a Menu
                        UtilsSharePreferences.createSession(this)
                        startActivity(
                            Intent(this, Menu::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                    } else {
                        // Mostrar error de credenciales
                        Toast.makeText(this, "Error en las credenciales", Toast.LENGTH_SHORT).show()
                        binding.etUser.setText("")
                        binding.etPassword.setText("")
                        binding.etUser.requestFocus()
                    }
                }
            }

            // Asigna el listener para mostrar/ocultar la contraseña
            ivShowPassword.setOnClickListener {
                togglePasswordVisibility()
            }
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            ivShowPassword.setImageResource(R.drawable.baseline_remove_red_eye_24) // Cambia al ícono de "ojo cerrado"
        } else {
            etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            ivShowPassword.setImageResource(R.drawable.baseline_remove_red_eye_24) // Cambia al ícono de "ojo abierto"
        }
        etPassword.setSelection(etPassword.text.length) // Mantiene el cursor al final
        isPasswordVisible = !isPasswordVisible
    }
}

