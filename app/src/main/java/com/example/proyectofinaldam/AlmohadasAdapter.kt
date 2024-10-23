package com.example.proyectofinaldam

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinaldam.databinding.ActivityUserDestinoBinding
import com.example.proyectofinaldam.databinding.ItemDestinoBinding
import com.example.proyectofinaldam.model.Almohadas

class AlmohadasAdapter(var lstAlmohadas: List<Almohadas>,private var actionDelete:(almohadas : Almohadas,)->Unit,private var actionUpdate:(almohadas : Almohadas,)->Unit,): RecyclerView.Adapter<AlmohadasAdapter.AlmohadasViewHolder>() {
    class AlmohadasViewHolder (val binding: ItemDestinoBinding):RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlmohadasViewHolder {
       val binding =ItemDestinoBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return AlmohadasViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return lstAlmohadas.size
    }

    override fun onBindViewHolder(holder: AlmohadasViewHolder, position: Int) {
       val almohadas =lstAlmohadas[position]

        holder.binding.txtNombre.text =almohadas.nomProducto
        holder.binding.txtTamaO.text =almohadas.tamanio
        holder.binding.txtStock.text = almohadas.stock
        holder.binding.btnEliminar.setOnClickListener {
            actionDelete(almohadas)
        }

        fun updateAlmohadas(newList:List<Almohadas>){

            lstAlmohadas = newList
            notifyDataSetChanged()
        }
    }
}