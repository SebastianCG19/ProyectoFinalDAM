package com.example.proyectofinaldam.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "almohadas_table")
data class Almohadas (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var nomProducto: String,
    var stock: String,
    var tamanio: String,
    //var imageUrl: String

    /*
    var descripcion: String,
    var precio: Double,
    var tipo: String
    */
)