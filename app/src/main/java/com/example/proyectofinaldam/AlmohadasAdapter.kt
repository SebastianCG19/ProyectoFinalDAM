package com.example.proyectofinaldam

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinaldam.databinding.ActivityUserDestinoBinding
import com.example.proyectofinaldam.databinding.ItemDestinoBinding
import com.example.proyectofinaldam.model.Almohadas

class AlmohadasAdapter(
    var lstAlmohadas: List<Almohadas>,
    private var actionDelete: (almohadas: Almohadas) -> Unit,
    private var actionUpdate: (almohadas: Almohadas) -> Unit
) : RecyclerView.Adapter<AlmohadasAdapter.AlmohadasViewHolder>() {

    class AlmohadasViewHolder(val binding: ItemDestinoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlmohadasViewHolder {
        val binding = ItemDestinoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlmohadasViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return lstAlmohadas.size
    }

    override fun onBindViewHolder(holder: AlmohadasViewHolder, position: Int) {
        val almohadas = lstAlmohadas[position]

        // Vincula los datos de la almohada a las vistas del ViewHolder
        holder.binding.txtNombre.text = almohadas.nomProducto
        holder.binding.txtTamaO.text = almohadas.tamanio
        holder.binding.txtStock.text = almohadas.stock

        // Acciones para eliminar
        holder.binding.btnEliminar.setOnClickListener {
            actionDelete(almohadas)
        }

        // Acciones para actualizar (si tienes un botón o acción para ello)
        holder.binding.btnEditar.setOnClickListener {
            actionUpdate(almohadas)
        }
    }

    // Función para actualizar la lista de almohadas
    fun updateAlmohadas(newList: List<Almohadas>) {
        lstAlmohadas = newList
        notifyItemInserted(lstAlmohadas.size - 1) // Notifica que un nuevo elemento fue insertado al final
    }



}