package com.example.localfresh.adapters.comprador

import android.graphics.Paint
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.localfresh.R
import com.example.localfresh.databinding.ItemVerApartadosBinding
import com.example.localfresh.model.comprador.apartados.ReservationData
import com.example.localfresh.utils.DateUtils
import com.example.localfresh.utils.ReservationStatusUtils
import com.example.localfresh.utils.ReservationTimerUtils

class VerApartadosAdapter(
    private val onTimerExpired: () -> Unit,
    private val onItemClick: (ReservationData) -> Unit,
    private val statusUtils: ReservationStatusUtils,
    private val timerUtils: ReservationTimerUtils
) : ListAdapter<ReservationData, VerApartadosAdapter.ViewHolder>(ViewHolder.Companion.DiffCallback()) {

    // Mapa para almacenar los timers y poder limpiarlos después
    private val timers = mutableMapOf<Int, CountDownTimer?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVerApartadosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onTimerExpired, onItemClick, statusUtils, timerUtils)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        // Guardar el timer si existe
        timers[item.reservation_id] = holder.currentTimer
    }

    fun releaseResources() {
        // Cancelar todos los timers cuando se destruye el adapter
        timers.values.forEach { timer ->
            timer?.cancel()
        }
        timers.clear()
    }

    class ViewHolder(
        private val binding: ItemVerApartadosBinding,
        private val onTimerExpired: () -> Unit,
        private val onItemClick: (ReservationData) -> Unit,
        private val statusUtils: ReservationStatusUtils,
        private val timerUtils: ReservationTimerUtils
    ) : RecyclerView.ViewHolder(binding.root) {

        // Mantener referencia al timer activo
        var currentTimer: CountDownTimer? = null

        fun bind(item: ReservationData) {


            // Configurar datos básicos
            binding.textViewReservationId.text = "Apartado NO. ${item.reservation_id}"
            binding.textViewReservationDate.text = DateUtils.formatearFecha(item.reservation_date)
            binding.textViewStoreName.text = item.seller.store_name

            // Configurar imagen de la tienda
            Glide.with(binding.root.context)
                .load(item.seller.store_logo)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(binding.imageViewStore)

            // Formatear y configurar los precios
            if (item.original_price > 0) {
                // Mostrar el precio original tachado
                binding.textViewOriginalPrice.apply {
                    text = "$${String.format("%.2f", item.original_price)}"
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG // Esto tacha el texto
                    visibility = View.VISIBLE
                }

                // Mostrar el precio con descuento en color destacado
                binding.textViewDiscountedPrice.apply {
                    text = "$${String.format("%.2f", item.total_price)}"
                    visibility = View.VISIBLE
                }
            } else {
                // Si no hay descuento, mostrar solo el precio total
                binding.textViewOriginalPrice.visibility = View.GONE
                binding.textViewDiscountedPrice.apply {
                    text = "$${String.format("%.2f", item.total_price)}"
                    visibility = View.VISIBLE
                }
            }

            // Mostrar estado con el nuevo utility
            binding.textViewStatus.text = item.status

            // Fondo del estado
            val statusBackground = ContextCompat.getDrawable(binding.root.context, R.drawable.status_background)?.mutate()
            val colorRes = statusUtils.getStatusColorResource(item.status)
            DrawableCompat.setTint(statusBackground!!, ContextCompat.getColor(binding.root.context, colorRes))
            binding.textViewStatus.background = statusBackground
            binding.textViewStatus.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.white))

            // Mostrar timer para apartados pendientes con el nuevo utility
            if (statusUtils.needsTimer(item.status, item.expiration_date)) {
                // Hacer visible tanto el timer como el ícono
                binding.textViewRemainingTime.visibility = View.VISIBLE
                binding.imageViewClock?.visibility = View.VISIBLE  // Si existe este elemento

                // Cancelar timer anterior si existe
                currentTimer?.cancel()

                // Iniciar nuevo timer
                currentTimer = timerUtils.startCountDownTimer(
                    expirationDate = item.expiration_date,
                    textView = binding.textViewRemainingTime,
                    onTimerFinished = {
                        onTimerExpired()
                    }
                )
            } else {
                // La visibilidad del textView ya se maneja en setStaticReservationStatus
                binding.imageViewClock?.visibility = View.GONE  // Si existe este elemento
                timerUtils.setStaticReservationStatus(binding.textViewRemainingTime, item.status)
            }


            // Configurar clic para ver detalles
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }

        companion object {
            class DiffCallback : DiffUtil.ItemCallback<ReservationData>() {
                override fun areItemsTheSame(oldItem: ReservationData, newItem: ReservationData): Boolean {
                    return oldItem.reservation_id == newItem.reservation_id
                }

                override fun areContentsTheSame(oldItem: ReservationData, newItem: ReservationData): Boolean {
                    return oldItem == newItem
                }
            }
        }
    }
}