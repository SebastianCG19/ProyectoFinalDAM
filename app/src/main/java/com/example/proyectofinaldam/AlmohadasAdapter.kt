package com.example.proyectofinaldam

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinaldam.databinding.ItemDestinoBinding
import com.example.proyectofinaldam.model.Almohadas
import com.squareup.picasso.Picasso

class AlmohadasAdapter(
    private var lstAlmohadas: List<Almohadas>,
    private var actionDelete: (almohadas: Almohadas) -> Unit,
    private var actionUpdate: (almohadas: Almohadas) -> Unit
) : RecyclerView.Adapter<AlmohadasAdapter.AlmohadasViewHolder>() {

    class AlmohadasViewHolder(val binding: ItemDestinoBinding) : RecyclerView.ViewHolder(binding.root)

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
        holder.binding.txtId.text = almohadas.id.toString()
        holder.binding.txtNombre.text = almohadas.nomProducto
        holder.binding.txtTamaO.text = almohadas.tamanio
        holder.binding.txtStock.text = almohadas.stock

        // Cargar la imagen usando Picasso
        if (almohadas.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(almohadas.imageUrl)
                .placeholder(R.drawable.uploadimg) // Reemplaza con tu recurso de imagen
                .into(holder.binding.img) // Asegúrate de que tu ItemDestinoBinding tenga un ImageView
        }

        // Acciones para eliminar
        holder.binding.btnEliminar.setOnClickListener {
            actionDelete(almohadas)
        }

        // Acciones para editar
        holder.binding.btnEditar.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, UserDestino::class.java).apply {

                putExtra("ID", almohadas.id)
                putExtra("NOMBRE_PRODUCTO", almohadas.nomProducto)
                putExtra("TAMANIO", almohadas.tamanio)
                putExtra("STOCK", almohadas.stock)
                putExtra("IMAGE_URL", almohadas.imageUrl)
            }
            context.startActivity(intent)

        }
    }



    // Función para actualizar la lista de almohadas
    fun updateAlmohadas(newList: List<Almohadas>) {
        lstAlmohadas = newList.toMutableList() // Convert to mutable list
        notifyDataSetChanged()
    }


    // Método para filtrar almohadas por nombre
    fun filterAlmohadas(query: String) {
        val filteredList = if (query.isEmpty()) {
            lstAlmohadas // Si la búsqueda está vacía, retorna la lista original
        } else {
            lstAlmohadas.filter { it.nomProducto.contains(query, ignoreCase = true) }
        }
        updateAlmohadas(filteredList) // Actualiza la lista con los resultados filtrados
    }

    // Método para obtener una almohada en una posición específica
    fun getAlmohadaAt(position: Int): Almohadas {
        return lstAlmohadas[position]
    }


}
