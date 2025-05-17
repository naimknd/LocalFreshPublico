package com.example.localfresh.adapters.comprador

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.localfresh.R
import com.example.localfresh.activitys.comprador.paginaprincipal.fragments.DetalleProductoCompradorFragment
import com.example.localfresh.databinding.ItemRecomendacionCompradorBinding
import com.example.localfresh.model.comprador.recomendaciones.RecommendedProduct
import java.util.Locale

class RecommendationsAdapter(
    private var recommendations: List<RecommendedProduct> = emptyList(),
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<RecommendationsAdapter.RecommendationViewHolder>() {

    inner class RecommendationViewHolder(private val binding: ItemRecomendacionCompradorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: RecommendedProduct) {
            with(binding) {
                // Datos básicos del producto
                txtProductName.text = product.product_name
                txtStoreName.text = product.store_name
                txtCategory.text = product.product_category

                // Configurar precios
                if (product.discount_percentage > 0) {
                    txtOriginalPrice.apply {
                        visibility = View.VISIBLE
                        text = String.format(Locale.getDefault(), "$%.2f", product.product_price)
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }
                    txtDiscountPrice.text = String.format(Locale.getDefault(), "$%.2f", product.discount_price)
                    txtDiscountBadge.visibility = View.VISIBLE
                    txtDiscountBadge.text = "-%${product.discount_percentage}"
                } else {
                    txtOriginalPrice.visibility = View.GONE
                    txtDiscountPrice.text = String.format(Locale.getDefault(), "$%.2f", product.product_price)
                    txtDiscountBadge.visibility = View.GONE
                }

                // Mostrar días restantes
                val daysText = when (product.days_until_expiry) {
                    0 -> "¡Último día!"
                    1 -> "¡Último día!"
                    else -> "Expira en ${product.days_until_expiry} días"
                }
                txtExpiryDate.text = daysText

                // Color según urgencia
                val colorRes = when {
                    product.days_until_expiry <= 1 -> R.color.red
                    product.days_until_expiry <= 3 -> R.color.warning_color
                    else -> R.color.green
                }
                txtExpiryDate.setTextColor(ContextCompat.getColor(itemView.context, colorRes))

                // Mostrar razón de recomendación
                txtReason.text = product.recommendation_reason

                // Cargar imagen
                Glide.with(itemView.context)
                    .load(product.product_image)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgProduct)

                // Configurar click para ver detalles
                root.setOnClickListener {
                    val fragment = DetalleProductoCompradorFragment.newInstance(product.unit_id)
                    fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val binding = ItemRecomendacionCompradorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecommendationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        holder.bind(recommendations[position])
    }

    override fun getItemCount(): Int = recommendations.size

    fun updateData(newData: List<RecommendedProduct>) {
        recommendations = newData
        notifyDataSetChanged()
    }
}