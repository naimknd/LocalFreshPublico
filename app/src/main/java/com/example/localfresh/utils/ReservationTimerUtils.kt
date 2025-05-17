package com.example.localfresh.utils

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.localfresh.R

/**
 * Utilidad para manejar temporizadores de apartados
 */
object ReservationTimerUtils {

    /**
     * Inicia un CountDownTimer para un TextView
     * @return El CountDownTimer creado (para poder cancelarlo después)
     */
    fun startCountDownTimer(
        expirationDate: String,
        textView: TextView,
        onTimerTick: (String, Int) -> Unit = { _, _ -> },
        onTimerFinished: () -> Unit = {}
    ): CountDownTimer? {
        if (DateUtils.fechaExpirada(expirationDate)) {
            textView.text = "Expirado"
            textView.setTextColor(ContextCompat.getColor(textView.context, R.color.red))
            onTimerFinished()
            return null
        }

        val milisRestantes = DateUtils.calcularMilisegundosRestantes(expirationDate)

        return object : CountDownTimer(milisRestantes, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val textoFormateado = DateUtils.formatearMilisegundos(millisUntilFinished)
                val colorId = DateUtils.getColorPorTiempoRestante(millisUntilFinished)

                textView.text = textoFormateado
                textView.setTextColor(ContextCompat.getColor(textView.context, colorId))

                onTimerTick(textoFormateado, colorId)
            }

            override fun onFinish() {
                textView.text = "Expirado"
                textView.setTextColor(ContextCompat.getColor(textView.context, R.color.red))
                onTimerFinished()
            }
        }.apply { start() }
    }

    /**
     * Inicia una actualización periódica con un Handler
     * @return El Runnable creado (para poder cancelarlo después)
     */
    fun startHandlerTimer(
        expirationDate: String,
        textView: TextView,
        handler: Handler = Handler(Looper.getMainLooper()),
        onTimerFinished: () -> Unit = {}
    ): Runnable {
        val updateRunnable = object : Runnable {
            override fun run() {
                val (texto, colorId) = DateUtils.getInfoTimer(expirationDate)

                textView.text = texto
                textView.setTextColor(ContextCompat.getColor(textView.context, colorId))

                // Si no ha expirado, programar otra actualización
                if (texto != "Expirado") {
                    handler.postDelayed(this, 1000)
                } else {
                    onTimerFinished()
                }
            }
        }

        handler.post(updateRunnable)
        return updateRunnable
    }

    /**
     * Formatea un apartado estático (completado, expirado, etc.)
     * También controla la visibilidad del elemento según sea necesario
     */
    fun setStaticReservationStatus(textView: TextView, status: String) {
        // Para estados distintos de Pendiente, ocultamos el timer
        if (status != ReservationStatusUtils.STATUS_PENDING) {
            textView.visibility = View.GONE
            return
        }

        // Si es un estado pendiente pero expirado, mostrar como expirado
        val (text, colorId) = when (status) {
            ReservationStatusUtils.STATUS_COMPLETED -> "Completado" to R.color.green
            ReservationStatusUtils.STATUS_EXPIRED -> "Expirado" to R.color.red
            ReservationStatusUtils.STATUS_CANCELLED -> "Cancelado" to R.color.red
            else -> "Estado desconocido" to R.color.text_color
        }

        // Asegurar que el elemento sea visible
        textView.visibility = View.VISIBLE
        textView.text = text
        textView.setTextColor(ContextCompat.getColor(textView.context, colorId))
    }
}