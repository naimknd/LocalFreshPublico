package com.example.localfresh.adapters.comprador

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.localfresh.databinding.ItemVerDetalleApartadoBinding
import com.example.localfresh.model.comprador.apartados.ReservedProduct
import com.example.localfresh.utils.ProductDisplayUtils

class DetalleApartadoAdapter : ListAdapter<ReservedProduct, DetalleApartadoAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ReservedProduct>() {
        override fun areItemsTheSame(oldItem: ReservedProduct, newItem: ReservedProduct) =
            oldItem.unit_id == newItem.unit_id
        override fun areContentsTheSame(oldItem: ReservedProduct, newItem: ReservedProduct) =
            oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemVerDetalleApartadoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemVerDetalleApartadoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReservedProduct) = with(binding) {
            // Datos del producto
            tvNombreProducto.text = item.product_name
            tvCategoria.text = item.category
            tvCantidad.text = "Cantidad: ${item.quantity}"
            tvItemTotal.text = "Total: " + ProductDisplayUtils.formatCurrency(item.total_price)

            // Configurar precios
            ProductDisplayUtils.setupPrices(
                originalPrice = item.total_original_price,
                discountPrice = item.total_price,
                discountPercentage = item.discount_percentage,
                originalPriceView = tvPrecioOriginal,
                discountPriceView = tvPrecioDescuento,
                discountPercentageView = tvDescuento
            )

            // Configurar información de expiración
            ProductDisplayUtils.setupExpiryInfo(
                context = root.context,
                expiryDate = item.expiry_date,
                dateTextView = tvFechaLimite,
                daysTextView = tvDiasRestantes
            )

            // Cargar imagen del producto
            ProductDisplayUtils.loadImage(root.context, item.product_image, imgProducto)
        }
    }
}