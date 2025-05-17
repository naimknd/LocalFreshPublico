package com.example.localfresh.adapters.comprador

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.localfresh.databinding.ItemProductoCarritoBinding
import com.example.localfresh.model.comprador.apartados.CartItem
import com.example.localfresh.utils.ProductDisplayUtils

class CartItemAdapter : ListAdapter<CartItem, CartItemAdapter.CartItemViewHolder>(
    object : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem.item_id == newItem.item_id
        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CartItemViewHolder(
        ItemProductoCarritoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartItemViewHolder(private val binding: ItemProductoCarritoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItem) = with(binding) {
            // Datos básicos
            tvProductName.text = item.product_name
            tvProductCategory.text = item.category
            tvQuantity.text = "Cantidad: ${item.quantity}"
            tvItemTotal.text = "Total: " + ProductDisplayUtils.formatCurrency(item.item_total)

            // Precios según descuento
            ProductDisplayUtils.setupPrices(
                originalPrice = item.original_price,
                discountPrice = item.price,
                discountPercentage = item.discount_percentage,
                originalPriceView = tvOriginalPrice,
                discountPriceView = tvDiscountPrice,
                discountPercentageView = tvDiscountPercentage
            )

            // Imagen
            ProductDisplayUtils.loadImage(itemView.context, item.image_url, ivProductImage)

            // Fecha y tiempo restante
            ProductDisplayUtils.setupExpiryInfo(
                context = itemView.context,
                expiryDate = item.expiry_date,
                dateTextView = tvDate,
                daysTextView = tvDays
            )
        }
    }
}