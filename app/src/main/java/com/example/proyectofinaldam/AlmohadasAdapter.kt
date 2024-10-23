package com.example.proyectofinaldam

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinaldam.databinding.ItemDestinoBinding
import com.example.proyectofinaldam.model.Almohadas
import com.squareup.picasso.Picasso // Importa la librería Picasso

class AlmohadasAdapter(
    private var lstAlmohadas: List<Almohadas>,
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

        // Cargar la imagen usando Picasso
        if (almohadas.imageUrl.isNotEmpty()) { // Verifica que la URL no esté vacía
            Picasso.get()
                .load(almohadas.imageUrl)
                //.placeholder(R.drawable.placeholder) // Reemplaza con un recurso de imagen de carga
                //.error(R.drawable.error_image) // Reemplaza con un recurso de imagen de error
                .into(holder.binding.img) // Asegúrate de que tu ItemDestinoBinding tenga un ImageView
        }

        // Acciones para eliminar
        holder.binding.btnEliminar.setOnClickListener {
            actionDelete(almohadas)
        }

        // Acciones para editar
        holder.binding.btnEditar.setOnClickListener {
            actionUpdate(almohadas)
        }
    }

    // Función para actualizar la lista de almohadas
    fun updateAlmohadas(newList: List<Almohadas>) {
        lstAlmohadas = newList
        notifyDataSetChanged() // Notifica que la lista ha cambiado
    }

    // Método para filtrar almohadas por nombre
    fun filterList(filteredAlmohadas: List<Almohadas>) {
        lstAlmohadas = filteredAlmohadas
        notifyDataSetChanged()
    }

    // Método para obtener una almohada en una posición específica
    fun getAlmohadaAt(position: Int): Almohadas {
        return lstAlmohadas[position]
    }
}
