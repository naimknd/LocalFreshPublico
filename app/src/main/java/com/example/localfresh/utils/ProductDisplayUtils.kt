package com.example.localfresh.utils

import android.content.Context
import android.graphics.Paint
import android.view.View
import android.widget.TextView
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.localfresh.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Clase utilitaria para operaciones comunes en la visualización de productos
 * tanto en el carrito como en los detalles de apartados.
 */
object ProductDisplayUtils {

    /**
     * Calcula los días restantes hasta la fecha de expiración y devuelve información formateada
     * @param expiryDate Fecha de expiración en formato YYYY-MM-DD
     * @return Triple con (días restantes, texto a mostrar, color a usar)
     */
    fun calculateRemainingDays(expiryDate: String): Triple<Int, String, Int> {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val expDate = formatter.parse(expiryDate)?.time ?: today
        val days = (expDate - today) / (1000 * 60 * 60 * 24)

        return Triple(days.toInt(), when {
            days > 5 -> "${days.toInt()} días restantes"
            days > 1 -> "${days.toInt()} días restantes"
            days == 1L -> "1 día restante"
            days == 0L -> "¡Último día!"
            else -> "Expirado"
        }, when {
            days > 5 -> R.color.text_color
            days > 0 -> R.color.warning_color
            else -> R.color.red
        })
    }

    /**
     * Configura la visualización de precios según si hay descuento o no
     */
    fun setupPrices(
        originalPrice: Double,
        discountPrice: Double?,
        discountPercentage: Int,
        originalPriceView: TextView,
        discountPriceView: TextView,
        discountPercentageView: TextView
    ) {
        val hasDiscount = discountPercentage > 0 && discountPrice != null && discountPrice < originalPrice

        // Precio original (tachado si hay descuento)
        originalPriceView.apply {
            text = formatCurrency(originalPrice)
            paintFlags = if (hasDiscount)
                paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            else
                paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        // Precio con descuento
        discountPriceView.apply {
            visibility = if (hasDiscount) View.VISIBLE else View.GONE
            if (hasDiscount) text = formatCurrency(discountPrice!!)
        }

        // Porcentaje de descuento
        discountPercentageView.apply {
            visibility = if (hasDiscount) View.VISIBLE else View.GONE
            if (hasDiscount) text = "-${discountPercentage}%"
        }
    }

    /**
     * Configura la información de fechas de expiración
     */
    fun setupExpiryInfo(
        context: Context,
        expiryDate: String,
        dateTextView: TextView,
        daysTextView: TextView
    ) {
        dateTextView.text = "Fecha límite: ${formatDateForDisplay(expiryDate)}"

        calculateRemainingDays(expiryDate).let { (_, text, colorRes) ->
            daysTextView.apply {
                this.text = "($text)"
                setTextColor(ContextCompat.getColor(context, colorRes))
            }
        }
    }

    /**
     * Carga una imagen con Glide
     */
    fun loadImage(context: Context, imageUrl: String, imageView: ImageView) {
        Glide.with(context)
            .load(imageUrl)
            .apply(RequestOptions()
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder))
            .into(imageView)
    }

    /**
     * Formatea una fecha de YYYY-MM-DD a DD/MM/YYYY
     */
    fun formatDateForDisplay(dateString: String): String {
        try {
            val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val targetFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = originalFormat.parse(dateString)
            return date?.let { targetFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            return dateString
        }
    }

    /**
     * Formatea un valor a formato de moneda
     */
    fun formatCurrency(value: Double): String {
        return "$${String.format("%.2f", value)}"
    }
}