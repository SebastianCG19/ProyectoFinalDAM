package com.example.proyectofinaldam

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.proyectofinaldam.model.Almohadas

@Dao
interface AlmohadasDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(almohadas: Almohadas)

    @Update
    suspend fun update(almohadas: Almohadas)

    @Delete
    suspend fun delete(almohadas: Almohadas)

    @Query("SELECT * FROM almohadas_table")
    suspend fun getAllAlmohadas(): List<Almohadas>

    @Query("SELECT * FROM almohadas_table WHERE id = :id")
    suspend fun getById(id: Long): Almohadas?

    @Query("SELECT * FROM almohadas_table")
    suspend fun getAll(): List<Almohadas>
}
