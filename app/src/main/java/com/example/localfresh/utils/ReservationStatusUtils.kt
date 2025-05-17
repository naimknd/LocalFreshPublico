package com.example.localfresh.utils

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.localfresh.R

/**
 * Utilidad para manejar los estados de los apartados y su representación visual
 */
object ReservationStatusUtils {

    // Constantes de los estados posibles
    const val STATUS_PENDING = "Pendiente"
    const val STATUS_COMPLETED = "Completado"
    const val STATUS_EXPIRED = "Expirado"
    const val STATUS_CANCELLED = "Cancelado"

    /**
     * Obtiene el color correspondiente a un estado específico
     * @return ID del recurso de color
     */
    fun getStatusColorResource(status: String): Int {
        return when (status) {
            STATUS_PENDING -> R.color.warning_color
            STATUS_COMPLETED -> R.color.green
            STATUS_EXPIRED, STATUS_CANCELLED -> R.color.red
            else -> R.color.text_color
        }
    }

    /**
     * Obtiene el ícono correspondiente al estado
     * @return ID del recurso drawable
     */
    fun getStatusIconResource(status: String): Int {
        return when (status) {
            STATUS_PENDING -> R.drawable.ic_clock
            STATUS_COMPLETED -> R.drawable.ic_check_circle
            STATUS_EXPIRED -> R.drawable.ic_expired
            STATUS_CANCELLED -> R.drawable.ic_cancelled
            else -> R.drawable.ic_info
        }
    }

    /**
     * Determina si un apartado puede ser cancelado
     * @param status Estado actual del apartado
     * @param expirationDate Fecha de expiración en formato string
     * @return true si el apartado puede ser cancelado
     */
    fun canCancelReservation(status: String, expirationDate: String): Boolean {
        return status == STATUS_PENDING && !DateUtils.fechaExpirada(expirationDate)
    }

    /**
     * Determina si un apartado necesita mostrar temporizador
     */
    fun needsTimer(status: String, expirationDate: String): Boolean {
        return status == STATUS_PENDING && !DateUtils.fechaExpirada(expirationDate)
    }

    /**
     * Obtiene un mensaje descriptivo según el estado
     */
    fun getStatusMessage(status: String): String {
        return when (status) {
            STATUS_PENDING -> "Pendiente de recoger"
            STATUS_COMPLETED -> "Apartado completado"
            STATUS_EXPIRED -> "Este apartado ha expirado"
            STATUS_CANCELLED -> "Apartado cancelado"
            else -> "Estado desconocido"
        }
    }

    /**
     * Aplica el estilo completo al TextView de estado (texto y fondo)
     */
    fun applyStatusStyle(textView: TextView, status: String) {
        // Establecer el texto
        textView.text = status

        // Obtener el color según el estado
        val colorResId = getStatusColorResource(status)
        val color = ContextCompat.getColor(textView.context, colorResId)

        // Obtener el drawable existente y modificar su color
        val background = textView.background
        if (background is GradientDrawable) {
            background.setColor(color)
        } else {
            // Si no existe un drawable o no es un GradientDrawable, crear uno nuevo
            val drawable = GradientDrawable().apply {
                cornerRadius = textView.context.resources.getDimensionPixelSize(R.dimen.status_corner_radius).toFloat()
                setColor(color)
            }
            textView.background = drawable
        }

        // Establecer el color del texto
        textView.setTextColor(ContextCompat.getColor(textView.context, android.R.color.white))
    }
}