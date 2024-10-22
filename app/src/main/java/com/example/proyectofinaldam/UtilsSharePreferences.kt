package com.example.proyectofinaldam

import android.content.Context
import android.content.Context.MODE_PRIVATE

class UtilsSharePreferences {
    companion object {
        fun createSession(context: Context) {
            val sharePref = context.getSharedPreferences("com.midominio.miaplication", MODE_PRIVATE)
            val editor = sharePref.edit()
            editor.putBoolean("login", true)
            editor.apply()
        }

        fun getSession(context: Context): Boolean {
            var sharedPref = context.getSharedPreferences("com.midominio.miaplication", MODE_PRIVATE)
            return sharedPref.getBoolean("login", false)
        }

        // Cerrar sesión
        fun clearSession(context: Context) {
            val sharedPref = context.getSharedPreferences("com.midominio.miaplication", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.clear()  // Eliminar todas las preferencias almacenadas
            editor.apply()
        }
    }
}