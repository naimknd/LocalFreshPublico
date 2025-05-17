package com.example.localfresh.adapters.vendedor

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.localfresh.R
import com.example.localfresh.databinding.ItemDetalleApartadoProductoVendedorBinding
import com.example.localfresh.model.vendedor.apartados.QRProduct

class QRProductsAdapter(private val products: List<QRProduct>) :
    RecyclerView.Adapter<QRProductsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetalleApartadoProductoVendedorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]

        // Mostrar datos básicos
        holder.tvProductName.text = product.product_name
        holder.tvCategoria.text = product.category

        // Mostrar precios con consideración de cantidad
        holder.tvPrecioOriginal.apply {
            text = "$${String.format("%.2f", product.original_total)}"
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }

        holder.tvPrecioDescuento.text = "$${String.format("%.2f", product.item_total)}"
        holder.tvDescuento.text = "${product.discount_percentage}% de descuento"

        // Mostrar cantidad
        holder.tvCantidad.text = "Cantidad: ${product.quantity}"

        // Fecha límite
        holder.tvFechaLimite.text = "Fecha límite: ${product.expiration_date}"

        // Cargar imagen
        Glide.with(holder.itemView.context)
            .load(product.image_url)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(holder.imgProducto)
    }

    class ViewHolder(binding: ItemDetalleApartadoProductoVendedorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imgProducto = binding.imgProducto
        val tvProductName = binding.tvNombreProducto
        val tvCategoria = binding.tvCategoria
        val tvPrecioOriginal = binding.tvPrecioOriginal
        val tvPrecioDescuento = binding.tvPrecioDescuento
        val tvDescuento = binding.tvDescuento
        val tvCantidad = binding.tvCantidad
        val tvFechaLimite = binding.tvFechaLimite
    }

    override fun getItemCount() = products.size
}