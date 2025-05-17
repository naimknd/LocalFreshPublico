package com.example.localfresh.adapters.vendedor

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.localfresh.R
import com.example.localfresh.databinding.ItemDetalleApartadoProductoVendedorBinding
import com.example.localfresh.model.vendedor.apartados.ReservedProductSeller

class DetalleApartadoVendedorAdapter : ListAdapter<ReservedProductSeller, DetalleApartadoVendedorAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetalleApartadoProductoVendedorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemDetalleApartadoProductoVendedorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReservedProductSeller) {
            binding.apply {
                tvNombreProducto.text = item.product_name
                tvCategoria.text = item.category

                // Usar el precio total original (considerar cantidad)
                tvPrecioOriginal.apply {
                    text = "$${String.format("%.2f", item.total_original_price)}"
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }

                // Usar el precio total con descuento
                tvPrecioDescuento.text = "$${String.format("%.2f", item.total_price)}"
                tvDescuento.text = "${item.discount_percentage}% de descuento"

                // Mostrar la cantidad
                tvCantidad.text = "Cantidad: ${item.quantity}"
                tvFechaLimite.text = "Fecha límite: ${item.formatted_expiry}"

                // Cargar imagen con Glide
                Glide.with(itemView.context)
                    .load(item.product_image)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(imgProducto)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ReservedProductSeller>() {
        override fun areItemsTheSame(oldItem: ReservedProductSeller, newItem: ReservedProductSeller): Boolean =
            oldItem.unit_id == newItem.unit_id

        override fun areContentsTheSame(oldItem: ReservedProductSeller, newItem: ReservedProductSeller): Boolean =
            oldItem == newItem
    }
}