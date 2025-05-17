package com.example.localfresh.adapters.vendedor

import com.example.localfresh.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.localfresh.databinding.ItemProductoNotificacionBinding
import com.example.localfresh.model.vendedor.notificaciones.ProductExpiryInfo

class ProductosNotificacionAdapter(
    private val productos: List<ProductExpiryInfo>,
    private val onEditClick: (Int) -> Unit
) : RecyclerView.Adapter<ProductosNotificacionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductoNotificacionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = productos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    inner class ViewHolder(private val binding: ItemProductoNotificacionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(producto: ProductExpiryInfo) {
            binding.tvNombreProducto.text = producto.name
            binding.tvCategoria.text = producto.category

            val diasRestantes = com.example.localfresh.utils.DateUtils.calcularDiasRestantes(producto.expiry_date)
            binding.tvFechaCaducidad.text = when {
                producto.expiry_type == "consumo_preferente" -> "Consumo preferente hasta: ${producto.expiry_date}"
                diasRestantes < 0 -> "Caducado: ${producto.expiry_date}"
                diasRestantes == 0 -> "¡Último día!: ${producto.expiry_date}"
                else -> "Caduca: ${producto.expiry_date}"
            }


            // Cambia color si está caducado
            val context = binding.root.context
            val isExpired = com.example.localfresh.utils.DateUtils.fechaExpirada(producto.expiry_date)
            if (isExpired) {
                binding.tvNombreProducto.setTextColor(ContextCompat.getColor(context, R.color.red))
                binding.tvFechaCaducidad.setTextColor(ContextCompat.getColor(context, R.color.red))
                binding.btnEditar.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
                binding.icWarning.visibility = View.VISIBLE
            } else {
                binding.tvNombreProducto.setTextColor(ContextCompat.getColor(context, R.color.text_color))
                binding.tvFechaCaducidad.setTextColor(ContextCompat.getColor(context, R.color.warning_color))
                binding.btnEditar.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
                binding.icWarning.visibility = View.GONE
            }

            binding.btnEditar.setOnClickListener { onEditClick(producto.product_id) }
        }
    }
}