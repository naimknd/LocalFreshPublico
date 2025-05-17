package com.example.localfresh.adapters.comprador.favoritos

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.localfresh.R
import com.example.localfresh.databinding.ItemFavoriteProductBinding
import com.example.localfresh.model.comprador.favoritos.ProductFavorite

class FavoritesProductAdapter(
    private val onItemClick: (ProductFavorite) -> Unit
) : ListAdapter<ProductFavorite, FavoritesProductAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavoriteProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemFavoriteProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(product: ProductFavorite) {
            binding.apply {
                tvProductName.text = product.product_name
                tvStoreName.text = product.store_name ?: ""
                tvCategory.text = product.category ?: ""

                // Configurar precios y descuentos
                if (product.has_discount == true && product.min_discount_price != null) {
                    // Mostrar precio original tachado y el precio con descuento
                    tvOriginalPrice.apply {
                        visibility = View.VISIBLE
                        text = String.format("$%.2f", product.product_price)
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }
                    tvPrice.text = String.format("$%.2f", product.min_discount_price)
                } else {
                    // Mostrar sólo el precio normal
                    tvOriginalPrice.visibility = View.GONE
                    tvPrice.text = String.format("$%.2f", product.product_price)
                }

                // Indicar disponibilidad
                tvAvailability.apply {
                    text = if (product.available == true) "Disponible" else "No disponible"
                    setTextColor(resources.getColor(
                        if (product.available == true) R.color.green else R.color.red,
                        null
                    ))
                }

                // Cargar imagen con Glide
                Glide.with(ivProductImage.context)
                    .load(product.product_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivProductImage)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ProductFavorite>() {
            override fun areItemsTheSame(oldItem: ProductFavorite, newItem: ProductFavorite): Boolean {
                return oldItem.product_id == newItem.product_id
            }

            override fun areContentsTheSame(oldItem: ProductFavorite, newItem: ProductFavorite): Boolean {
                return oldItem == newItem
            }
        }
    }
}
