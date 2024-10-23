package com.example.proyectofinaldam

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.proyectofinaldam.model.Almohadas

@Database(entities = [Almohadas::class], version = 1, exportSchema = false)
abstract class AlmohadasDatabase : RoomDatabase() { // Hereda de RoomDatabase
    abstract fun almohadasDAO(): AlmohadasDAO // Método abstracto para obtener el DAO

    companion object {
        @Volatile
        private var INSTANCE: AlmohadasDatabase? = null // Instancia de la base de datos

        fun getDatabase(context: Context): AlmohadasDatabase { // Método para obtener la instancia
            return INSTANCE ?: synchronized(this) { // Si no existe una instancia
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlmohadasDatabase::class.java,
                    "almohadas_database" // Nombre de la base de datos
                ).build()
                INSTANCE = instance // Asignar la nueva instancia
                instance // Retornar la instancia
            }
        }
    }
}
