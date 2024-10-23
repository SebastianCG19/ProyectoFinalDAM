package com.example.proyectofinaldam

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.proyectofinaldam.model.Almohadas

@Database(entities = [Almohadas::class], version = 2, exportSchema = false)
abstract class AlmohadasDatabase : RoomDatabase() { // Hereda de RoomDatabase
    abstract fun almohadasDAO(): AlmohadasDAO // Método abstracto para obtener el DAO

    companion object {
        @Volatile
        private var INSTANCE: AlmohadasDatabase? = null

        fun getDatabase(context: Context): AlmohadasDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlmohadasDatabase::class.java,
                    "almohadas_database"
                )
                    .fallbackToDestructiveMigration() // Permitir la destrucción de la base de datos en caso de migraciones
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}