package com.example.localfresh.utils

import android.graphics.Paint
import android.view.View
import android.widget.TextView
import java.util.*

object DiscountUtils {
    /**
     * Calcula el precio con descuento basado en los días restantes antes de la caducidad.
     *
     * @param originalPrice El precio original del producto.
     * @param expiryDate La fecha de caducidad del producto.
     * @return El precio con descuento.
     */
    fun calculateDiscountPrice(originalPrice: Double, expiryDate: String): Double {
        val discountPercentage = getDiscountPercentage(expiryDate)
        return originalPrice * (1 - discountPercentage)
    }

    /**
     * Calcula el porcentaje de descuento basado en los días restantes.
     * Útil para mostrar el porcentaje en la interfaz.
     *
     * @param expiryDate La fecha de caducidad del producto.
     * @return El porcentaje de descuento (0.0 = 0%, 0.05 = 5%, 0.15 = 15%, etc.)
     */
    fun getDiscountPercentage(expiryDate: String): Double {
        val daysRemaining = DateUtils.calcularDiasRestantes(expiryDate)

        return when {
            daysRemaining > 15 -> 0.0   // Sin descuento para productos con más de 15 días
            daysRemaining > 10 -> 0.05  // 5%
            daysRemaining >= 5 -> 0.15  // 15%
            daysRemaining > 2 -> 0.30   // 30%
            daysRemaining >= 1 -> 0.50  // 50% para productos que caducan en 1-2 días
            else -> 0.50               // 50% para productos caducados
        }
    }

    /**
     * Obtiene el porcentaje de descuento como string formateado.
     *
     * @param expiryDate La fecha de caducidad del producto.
     * @return El porcentaje de descuento como string (ej: "5%", "15%", etc.)
     */
    fun getFormattedDiscountPercentage(expiryDate: String): String {
        val percentage = getDiscountPercentage(expiryDate) * 100
        return "${percentage.toInt()}%"
    }

    /**
     * Configura la visualización de precios según si hay descuento o no.
     *
     * @param originalPrice Precio original del producto
     * @param expiryDate Fecha de caducidad
     * @param txtPrecioOriginal TextView para mostrar el precio original
     * @param txtPrecioDescuento TextView para mostrar el precio con descuento
     * @param txtDescuentoInfo TextView opcional para mostrar porcentaje de descuento
     */
    fun setupPriceDisplay(
        originalPrice: Double,
        expiryDate: String,
        txtPrecioOriginal: TextView,
        txtPrecioDescuento: TextView,
        txtDescuentoInfo: TextView? = null
    ) {
        val discountPercentage = getDiscountPercentage(expiryDate)

        if (discountPercentage > 0) {
            // Si hay descuento, mostrar precio original tachado y precio con descuento
            txtPrecioOriginal.apply {
                visibility = View.VISIBLE
                text = String.format(Locale.getDefault(), "$%.2f", originalPrice)
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }

            val precioConDescuento = calculateDiscountPrice(originalPrice, expiryDate)
            txtPrecioDescuento.apply {
                visibility = View.VISIBLE
                text = String.format(Locale.getDefault(), "$%.2f", precioConDescuento)
            }

            // Si se proporcionó un TextView para mostrar el porcentaje de descuento
            txtDescuentoInfo?.apply {
                visibility = View.VISIBLE
                text = "${(discountPercentage * 100).toInt()}% de descuento"
            }
        } else {
            // Si no hay descuento, mostrar solo el precio original sin tachar
            txtPrecioOriginal.apply {
                visibility = View.VISIBLE
                text = String.format(Locale.getDefault(), "$%.2f", originalPrice)
                paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() // Quitar tachado
            }

            // Ocultar elementos de descuento
            txtPrecioDescuento.visibility = View.GONE
            txtDescuentoInfo?.visibility = View.GONE
        }
    }
}