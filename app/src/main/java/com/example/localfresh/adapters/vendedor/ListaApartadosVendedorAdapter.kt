package com.example.localfresh.adapters.vendedor

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.localfresh.R
import com.example.localfresh.databinding.ItemListaApartadosVendedorBinding
import com.example.localfresh.model.vendedor.apartados.ReservationSellerItem
import com.example.localfresh.utils.ReservationStatusUtils
import com.example.localfresh.utils.ReservationTimerUtils
import java.util.HashMap

class ListaApartadosVendedorAdapter(
    private val onItemClick: (ReservationSellerItem) -> Unit,
    private val onTimerExpired: () -> Unit = {}, // callback para cuando expire un timer
    private val statusUtils: ReservationStatusUtils = ReservationStatusUtils, // Utility de estados
    private val timerUtils: ReservationTimerUtils = ReservationTimerUtils // Utility de temporizadores
) : ListAdapter<ReservationSellerItem, ListaApartadosVendedorAdapter.ViewHolder>(DiffCallback()) {

    // Mapa para guardar los temporizadores activos
    private val timers = HashMap<Int, CountDownTimer?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListaApartadosVendedorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick, statusUtils, timerUtils, onTimerExpired)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservation = getItem(position)
        holder.bind(reservation)

        // Guardar referencia al timer si existe
        holder.currentTimer?.let { timer ->
            timers[reservation.reservation_id] = timer
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.cancelTimer()
    }

    // Metodo para limpiar todos los recursos (timers)
    fun releaseResources() {
        timers.values.forEach { timer -> timer?.cancel() }
        timers.clear()
    }

    class ViewHolder(
        private val binding: ItemListaApartadosVendedorBinding,
        private val onItemClick: (ReservationSellerItem) -> Unit,
        private val statusUtils: ReservationStatusUtils,
        private val timerUtils: ReservationTimerUtils,
        private val onTimerExpired: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        var currentTimer: CountDownTimer? = null

        fun bind(reservation: ReservationSellerItem) {
            binding.apply {
                textViewReservationId.text = "Apartado NO. ${reservation.reservation_id}"
                textViewReservationDate.text = reservation.formatted_date
                textViewBuyerName.text = reservation.user.full_name
                textViewStatus.text = reservation.status

                // Configurar color del estado usando la utilidad
                val colorRes = statusUtils.getStatusColorResource(reservation.status)
                textViewStatus.setTextColor(ContextCompat.getColor(itemView.context, colorRes))

                // Configurar el fondo del estado
                val statusBackground = ContextCompat.getDrawable(itemView.context, R.drawable.status_background)?.mutate()
                DrawableCompat.setTint(statusBackground!!, ContextCompat.getColor(itemView.context, colorRes))
                textViewStatus.background = statusBackground
                textViewStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                textViewStatus.setPadding(16, 4, 16, 4)

                // Configurar timer o estado estático según el status
                configureRemainingTime(reservation)

                // Configurar click listener
                root.setOnClickListener {
                    onItemClick(reservation)
                }
            }
        }

        private fun configureRemainingTime(reservation: ReservationSellerItem) {
            // Cancelar timer existente
            cancelTimer()

            // Verificar si necesita mostrar timer usando la utilidad centralizada
            if (statusUtils.needsTimer(reservation.status, reservation.expiration_date)) {
                // Hacer visibles los elementos del timer
                binding.imageViewClock.visibility = View.VISIBLE
                binding.textViewRemainingTime.visibility = View.VISIBLE

                // Iniciar nuevo timer utilizando ReservationTimerUtils
                currentTimer = timerUtils.startCountDownTimer(
                    expirationDate = reservation.expiration_date,
                    textView = binding.textViewRemainingTime,
                    onTimerTick = { _, colorId ->
                        // Sincronizar color del ícono con el texto
                        binding.imageViewClock.setColorFilter(
                            ContextCompat.getColor(itemView.context, colorId)
                        )
                    },
                    onTimerFinished = {
                        // Notificar que un timer ha expirado para refrescar datos
                        onTimerExpired()
                    }
                )
            } else {
                // Para estados no pendientes, usar representación estática
                timerUtils.setStaticReservationStatus(binding.textViewRemainingTime, reservation.status)

                // Configurar ícono según estado
                val colorRes = statusUtils.getStatusColorResource(reservation.status)
                binding.imageViewClock.setColorFilter(
                    ContextCompat.getColor(itemView.context, colorRes)
                )
                binding.imageViewClock.visibility = if (reservation.status == statusUtils.STATUS_PENDING) View.VISIBLE else View.GONE
            }
        }

        fun cancelTimer() {
            currentTimer?.cancel()
            currentTimer = null
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ReservationSellerItem>() {
        override fun areItemsTheSame(oldItem: ReservationSellerItem, newItem: ReservationSellerItem): Boolean {
            return oldItem.reservation_id == newItem.reservation_id
        }

        override fun areContentsTheSame(oldItem: ReservationSellerItem, newItem: ReservationSellerItem): Boolean {
            return oldItem == newItem
        }
    }
}