package com.example.localfresh.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.localfresh.R
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"

    /**
     * Calcula los días restantes entre la fecha de expiración y hoy
     * @return Número de días restantes (negativo si ya expiró)
     */
    fun calcularDiasRestantes(expiryDate: String): Int {
        return try {
            val formato = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

            val fechaExpiracion = formato.parse(expiryDate)?.let { date ->
                Calendar.getInstance().apply {
                    time = date
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
            }

            val hoy = getHoyStartOfDay()

            fechaExpiracion?.let { exp ->
                ((exp.time - hoy.time) / (1000 * 60 * 60 * 24)).toInt()
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Formatea una fecha de formato "yyyy-MM-dd" a "dd MMM yyyy"
     */
    fun formatearFecha(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val fechaDate = inputFormat.parse(date)
            outputFormat.format(fechaDate!!)
        } catch (e: Exception) {
            date
        }
    }

    /**
     * Obtiene la fecha actual con hora en 00:00:00
     */
    fun getHoyStartOfDay(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    /**
     * Devuelve información de tiempo restante para UI con formato adecuado
     * @return Triple(diasRestantes, textoMostrar, colorId)
     */
    /**
     * Devuelve información formateada sobre los días restantes para mostrar en la UI
     * @param context Contexto para acceder a recursos
     * @param expiryDate Fecha de caducidad en formato "yyyy-MM-dd"
     * @param expiryType Tipo de caducidad (caducidad o consumo_preferente)
     * @return Triple con (drawable, texto formateado, id del color)
     */
    fun getInfoTiempoRestante(context: Context, expiryDate: String, expiryType: String): Triple<Drawable?, String, Int> {
        val diasRestantes = calcularDiasRestantes(expiryDate)

        return when {
            diasRestantes > 10 -> Triple(
                ContextCompat.getDrawable(context, R.drawable.ic_clock),
                "$diasRestantes días restantes",
                R.color.text_color
            )
            diasRestantes > 5 -> Triple(
                ContextCompat.getDrawable(context, R.drawable.ic_clock),
                "$diasRestantes días restantes",
                R.color.warning_color
            )
            diasRestantes >= 1 -> Triple(
                ContextCompat.getDrawable(context, R.drawable.ic_warning),
                "$diasRestantes días restantes",
                R.color.warning_color
            )
            diasRestantes == 0 -> Triple(
                ContextCompat.getDrawable(context, R.drawable.ic_warning),
                "¡Último día!",
                R.color.red
            )
            else -> Triple(
                ContextCompat.getDrawable(context, R.drawable.ic_warning),
                if (expiryType == "consumo_preferente") "Consumo preferente expirado" else "Expirado",
                R.color.red
            )
        }
    }

    /**
     * Verifica si una fecha está dentro del rango de fechas especificado
     */
    fun estaDentroDeRango(fecha: String, minDate: String?, maxDate: String?): Boolean {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        try {
            val fechaProducto = dateFormat.parse(fecha)!!

            if (minDate != null) {
                val fechaMinima = dateFormat.parse(minDate)!!
                if (fechaProducto.before(fechaMinima)) return false
            }

            if (maxDate != null) {
                val fechaMaxima = dateFormat.parse(maxDate)!!
                if (fechaProducto.after(fechaMaxima)) return false
            }

            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Formatea una fecha para enviarla al servidor
     */
    fun formatDateForServer(date: String): String {
        val inputFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val outputFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val parsedDate: Date = inputFormat.parse(date) ?: Date()
        return outputFormat.format(parsedDate)
    }

    /**
     * Calcula los milisegundos restantes entre la fecha de expiración y la hora actual
     * @return Milisegundos restantes (0 si ya expiró)
     */
    fun calcularMilisegundosRestantes(expiryDate: String): Long {
        return try {
            val formato = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
            val fechaExpiracion = formato.parse(expiryDate) ?: return 0L
            val ahora = Date()

            val diferencia = fechaExpiracion.time - ahora.time
            if (diferencia < 0) 0L else diferencia
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Formatea milisegundos a un formato legible (Xd Xh Xm Xs)
     */
    fun formatearMilisegundos(milisegundos: Long): String {
        val segundos = (milisegundos / 1000) % 60
        val minutos = (milisegundos / (1000 * 60)) % 60
        val horas = (milisegundos / (1000 * 60 * 60)) % 24
        val dias = milisegundos / (1000 * 60 * 60 * 24)

        return when {
            dias > 0 -> "${dias}d ${horas}h ${minutos}m ${segundos}s"
            horas > 0 -> "${horas}h ${minutos}m ${segundos}s"
            else -> "${minutos}m ${segundos}s"
        }
    }

    /**
     * Determina el color adecuado para mostrar basado en el tiempo restante
     * @return ID del recurso de color
     */
    fun getColorPorTiempoRestante(milisegundos: Long): Int {
        return when {
            milisegundos < 30 * 60 * 1000 -> R.color.red // menos de 30 min
            milisegundos < 2 * 60 * 60 * 1000 -> R.color.warning_color // menos de 2h
            else -> R.color.text_color
        }
    }

    /**
     * Verifica si una fecha con hora ya expiró
     */
    fun fechaExpirada(fechaString: String): Boolean {
        return try {
            val formato = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
            val fechaExpiracion = formato.parse(fechaString) ?: return true
            val ahora = Date()

            fechaExpiracion.before(ahora)
        } catch (e: Exception) {
            true // Si hay error, asumimos que expiró para estar seguros
        }
    }

    /**
     * Calcula tiempo restante y devuelve información para mostrar en un timer
     * @return Pair(textoFormateado, colorID)
     */
    fun getInfoTimer(fechaString: String): Pair<String, Int> {
        if (fechaExpirada(fechaString)) {
            return Pair("Expirado", R.color.red)
        }

        val milisegundos = calcularMilisegundosRestantes(fechaString)
        val textoFormateado = formatearMilisegundos(milisegundos)
        val colorId = getColorPorTiempoRestante(milisegundos)

        return Pair(textoFormateado, colorId)
    }

    /**
     * Calcula el tiempo restante para una fecha de expiración
     * @return Triple(milisegundosRestantes, textoFormateado, haExpirado)
     */
    fun calcularTiempoRestante(fechaExpiracion: String): Triple<Long, String, Boolean> {
        val milisegundosRestantes = calcularMilisegundosRestantes(fechaExpiracion)
        val textoFormateado = formatearMilisegundos(milisegundosRestantes)
        val haExpirado = fechaExpirada(fechaExpiracion)

        return Triple(milisegundosRestantes, textoFormateado, haExpirado)
    }

    /**
     * Valida que una persona tenga al menos la edad mínima requerida
     * @param birthdate Fecha de nacimiento en formato yyyy-MM-dd
     * @param minYears Edad mínima requerida en años
     * @return true si cumple la edad mínima, false en caso contrario
     */
    fun validarEdadMinima(birthdate: String, minYears: Int): Boolean {
        try {
            val formato = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val fechaNacimiento = formato.parse(birthdate) ?: return false

            val calendarNacimiento = Calendar.getInstance()
            calendarNacimiento.time = fechaNacimiento

            val calendarHoy = Calendar.getInstance()

            // Añadir los años mínimos a la fecha de nacimiento
            calendarNacimiento.add(Calendar.YEAR, minYears)

            // Si la fecha resultante es después de hoy, no cumple la edad mínima
            return !calendarNacimiento.after(calendarHoy)
        } catch (e: Exception) {
            return false
        }
    }

    fun formatDate(year: Int, month: Int, day: Int): String {
        return String.format("%04d-%02d-%02d", year, month + 1, day)
    }
}