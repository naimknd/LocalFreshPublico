package com.example.localfresh.adapters.vendedor

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.localfresh.R
import com.example.localfresh.databinding.ItemNotificacionVendedorBinding
import com.example.localfresh.model.vendedor.notificaciones.MarkNotificationReadRequest
import com.example.localfresh.model.vendedor.notificaciones.SellerNotification
import com.example.localfresh.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class NotificacionesVendedorAdapter :
    ListAdapter<SellerNotification, NotificacionesVendedorAdapter.NotificacionViewHolder>(DiffCallback()) {

    // Interfaz para manejar clics en notificaciones
    interface OnNotificationClickListener {
        fun onNotificationClick(notification: SellerNotification)
    }

    private var clickListener: OnNotificationClickListener? = null

    fun setOnNotificationClickListener(listener: OnNotificationClickListener) {
        this.clickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val binding = ItemNotificacionVendedorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificacionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificacionViewHolder(private val binding: ItemNotificacionVendedorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: SellerNotification) {
            binding.apply {
                tvTitulo.text = notification.title
                tvMensaje.text = notification.message

                // Formatear y mostrar fecha/hora
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                try {
                    val date = dateFormat.parse(notification.created_at)
                    tvFecha.text = outputFormat.format(date!!)
                } catch (e: Exception) {
                    tvFecha.text = notification.created_at
                }

                // Estado de lectura
                if (notification.is_read == 0) {
                    tvTitulo.setTypeface(null, Typeface.BOLD)
                    tvMensaje.setTypeface(null, Typeface.BOLD)
                    badgeNoLeida.visibility = View.VISIBLE
                } else {
                    tvTitulo.setTypeface(null, Typeface.NORMAL)
                    tvMensaje.setTypeface(null, Typeface.NORMAL)
                    badgeNoLeida.visibility = View.GONE
                }

                // Color de fondo según tipo de notificación
                val bgColor = when (notification.type) {
                    "alerta_caducidad" -> R.color.light_warning_bg
                    "producto_expirado" -> R.color.light_error_bg  // Mismo color que para expirado
                    "cancelado" -> R.color.light_error_bg
                    "caducado" -> R.color.light_error_bg
                    "nuevo_apartado" -> R.color.light_success_bg
                    else -> R.color.green
                }
                root.setCardBackgroundColor(ContextCompat.getColor(root.context, bgColor))

                // Click listener
                root.setOnClickListener {
                    // Marcar como leída si es necesario
                    if (notification.is_read == 0) {
                        markAsRead(notification.notification_id, bindingAdapterPosition)
                    }
                    clickListener?.onNotificationClick(notification)
                }
            }
        }

        private fun markAsRead(notificationId: Int, position: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = MarkNotificationReadRequest(notification_id = notificationId)
                    val response = RetrofitInstance.vendedorApiService.markNotificationAsRead(request).execute()
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body()?.status == "success") {
                            // Actualizar item en la lista
                            val updatedItem = getItem(position).copy(is_read = 1)
                            val currentList = currentList.toMutableList()
                            currentList[position] = updatedItem
                            submitList(currentList)
                        }
                    }
                } catch (e: Exception) {
                    // Manejar error silenciosamente
                }
            }
        }
    }

    fun updateData(nuevaLista: List<SellerNotification>) {
        submitList(nuevaLista)
    }

    class DiffCallback : DiffUtil.ItemCallback<SellerNotification>() {
        override fun areItemsTheSame(oldItem: SellerNotification, newItem: SellerNotification): Boolean {
            return oldItem.notification_id == newItem.notification_id
        }

        override fun areContentsTheSame(oldItem: SellerNotification, newItem: SellerNotification): Boolean {
            return oldItem == newItem
        }
    }
}