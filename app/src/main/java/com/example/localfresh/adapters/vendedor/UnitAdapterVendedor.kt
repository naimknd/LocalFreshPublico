package com.example.localfresh.adapters.vendedor

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Paint
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.localfresh.R
import com.example.localfresh.databinding.ItemUnidadVendedorBinding
import com.example.localfresh.model.vendedor.unidad.ProductUnitSellerDetails
import com.example.localfresh.model.vendedor.unidad.UnitSellerDetails
import com.example.localfresh.utils.DateUtils
import com.example.localfresh.viewmodel.vendedor.paginaprincipal.VerUnidadesViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// Interfaz para comunicar eventos de eliminación al fragmento
interface UnitAdapterListener {
    fun onUnitDeleted(unitId: Int, position: Int)
}

class UnitAdapterVendedor(
    private var units: MutableList<UnitSellerDetails>,
    private val product: ProductUnitSellerDetails?,
    private val viewModel: VerUnidadesViewModel,
    private val listener: UnitAdapterListener? = null
) : RecyclerView.Adapter<UnitAdapterVendedor.UnitViewHolder>() {

    private fun formatStatus(status: String): String {
        return when (status.lowercase()) {
            "disponible" -> "Disponible"
            "apartado" -> "Apartado"
            "vendido" -> "Vendido"
            else -> status.replaceFirstChar { it.uppercase() }
        }
    }

    inner class UnitViewHolder(private val binding: ItemUnidadVendedorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(unit: UnitSellerDetails, product: ProductUnitSellerDetails?, position: Int) {
            product?.let {
                setupProductInfo(it)
                loadProductImage(it.image_url)
            }

            setupUnitInfo(unit)
            setupButtons(unit, position)
        }

        private fun setupProductInfo(product: ProductUnitSellerDetails) {
            with(binding) {
                txtNombreProducto.text = product.name
                txtCategoria.text = product.category
                txtPrecioOriginal.apply {
                    text = "${product.price}$"
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                txtTipoCaducidad.apply {
                    text = if (product.expiry_type == "consumo_preferente") {
                        "Consumo preferente"
                    } else {
                        "Fecha de caducidad"
                    }
                    setTextColor(ContextCompat.getColor(itemView.context, R.color.text_color))
                }
            }
        }

        private fun loadProductImage(imageUrl: String?) {
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(240, 240)
                .format(DecodeFormat.PREFER_RGB_565)
                .error(R.drawable.ic_placeholder)
                .into(binding.imgProducto)
        }

        private fun setupUnitInfo(unit: UnitSellerDetails) {
            with(binding) {
                val context = itemView.context

                // Verificar si la unidad está expirada
                val diasRestantes = DateUtils.calcularDiasRestantes(unit.expiry_date)
                val isExpired = diasRestantes < 0
                val expiryType = product?.expiry_type ?: "caducidad"

                // Determinar si es un producto consumo pref. expirado pero aún disponible
                val isConsumoPreferenteExpirado = isExpired &&
                        expiryType == "consumo_preferente" &&
                        unit.status.equals("disponible", ignoreCase = true)

                // Determinar si es un producto caducado que no es consumo preferente
                val isCaducado = isExpired &&
                        expiryType != "consumo_preferente" &&
                        unit.status.equals("disponible", ignoreCase = true)

                // Texto de estado con lógica especial para expirados
                txtEstadoProducto.text = when {
                    isConsumoPreferenteExpirado -> "Cons. Pref. Expirado"
                    isCaducado -> "Caducado"
                    else -> formatStatus(unit.status)
                }

                // Color del estado
                txtEstadoProducto.setTextColor(ContextCompat.getColor(context, when {
                    isConsumoPreferenteExpirado -> R.color.warning_color
                    isCaducado -> R.color.red
                    unit.status.equals("disponible", ignoreCase = true) -> R.color.green
                    unit.status.equals("apartado", ignoreCase = true) -> R.color.warning_color
                    else -> R.color.text_color
                }))

                // Datos básicos
                txtPrecioDescuento.text = unit.discount_price?.let { "${it}$" } ?: "N/A"
                txtFechaProducto.text = DateUtils.formatearFecha(unit.expiry_date)
                txtCantidad.text = "Cantidad: ${unit.quantity}"

                // Configurar icono de información para unidades caducadas
                icInfo.visibility = if (isCaducado) View.VISIBLE else View.GONE

                // Configurar tiempo restante y barra de progreso
                setupRemainingTime(unit, expiryType)
            }
        }

        private fun setupRemainingTime(unit: UnitSellerDetails, expiryType: String) {
            val context = itemView.context
            val diasRestantes = DateUtils.calcularDiasRestantes(unit.expiry_date)
            val isExpired = diasRestantes < 0

            // Obtener información completa sobre tiempo restante (con icono y color)
            val timeInfo = DateUtils.getInfoTiempoRestante(
                context,
                unit.expiry_date,
                expiryType
            )

            // Configurar texto de tiempo restante con icono
            binding.txtTiempoRestante.apply {
                // Establecer texto y color
                text = timeInfo.second
                setTextColor(ContextCompat.getColor(context, timeInfo.third))

                // Establecer icono principal
                timeInfo.first?.let { drawable ->
                    drawable.setTint(ContextCompat.getColor(context, timeInfo.third))
                    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                    compoundDrawablePadding = context.resources.getDimensionPixelSize(R.dimen.text_drawable_padding)
                }

                // Configuración especial para consumo preferente expirado
                if (isExpired && expiryType == "consumo_preferente" &&
                    unit.status.equals("disponible", ignoreCase = true)) {

                    // Añadir icono de información
                    setCompoundDrawablesWithIntrinsicBounds(
                        timeInfo.first, null,
                        ContextCompat.getDrawable(context, R.drawable.ic_info), null
                    )

                    // Hacer clickable para mostrar info
                    setOnClickListener { mostrarInfoConsumoPreferente(context) }
                } else {
                    // Configuración normal
                    setOnClickListener(null)
                }

                // Si es caducado (no consumo preferente), desactivar botones
                if (isExpired && expiryType != "consumo_preferente" &&
                    unit.status.equals("disponible", ignoreCase = true)) {
                    disableButtons()
                    showExpiryWarningIcon(context)
                }
            }

            // Configurar barra de progreso
            binding.progressTiempoRestante.apply {
                // Calcular progreso (0-100)
                progress = when {
                    diasRestantes <= 0 -> 0
                    diasRestantes >= 30 -> 100
                    else -> ((diasRestantes * 100) / 30).toInt()
                }

                // Aplicar color según tiempo restante
                progressTintList = ContextCompat.getColorStateList(
                    context,
                    when {
                        diasRestantes > 10 -> R.color.green
                        diasRestantes > 5 -> R.color.warning_color
                        diasRestantes > 0 -> R.color.warning_color
                        else -> if (expiryType == "consumo_preferente")
                            R.color.warning_color else R.color.red
                    }
                )
            }
        }

        private fun mostrarInfoConsumoPreferente(context: Context) {
            MaterialAlertDialogBuilder(context)
                .setTitle("Información sobre Consumo Preferente")
                .setIcon(R.drawable.ic_info)
                .setMessage(
                    "• El alimento sigue siendo seguro si:\n" +
                            "  - Se respetan las instrucciones de conservación\n" +
                            "  - El envase no está dañado\n\n" +
                            "• Se aplica a alimentos:\n" +
                            "  - Refrigerados y congelados\n" +
                            "  - Desecados (pasta, arroz)\n" +
                            "  - Enlatados\n" +
                            "  - Otros (aceite, chocolate)\n\n" +
                            "• Antes de desechar:\n" +
                            "  - Verificar aspecto, olor y sabor\n" +
                            "  - Comprobar integridad del envase\n\n" +
                            "• Después de abrir:\n" +
                            "  - Seguir instrucciones específicas del envase"
                )
                .setPositiveButton("Entendido", null)
                .show()
        }

        private fun showExpiryWarningIcon(context: Context) {
            with(binding) {
                icInfo.visibility = View.VISIBLE
                icInfo.setOnClickListener {
                    AlertDialog.Builder(context)
                        .setTitle("Producto Caducado")
                        .setMessage("Este producto ha caducado y ya no se ofrece a los compradores.")
                        .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                        .setIcon(R.drawable.ic_warning)
                        .show()
                }
            }
        }

        private fun setupButtons(unit: UnitSellerDetails, position: Int) {
            with(binding) {
                // Configurar botón Editar
                btnEditar.apply {
                    // Determinar si se puede editar (no se pueden editar productos caducados que no son de consumo preferente)
                    val isEditable = !(product?.expiry_type != "consumo_preferente" &&
                            DateUtils.calcularDiasRestantes(unit.expiry_date) < 0)

                    // Establecer estado habilitado/deshabilitado
                    isEnabled = isEditable
                    alpha = if (isEnabled) 1.0f else 0.5f

                    // Configurar click listener
                    setOnClickListener {
                        if (isEditable) {
                            launchEditFragment(unit.unit_id, product?.name)
                        }
                    }
                }

                // Configurar botón Eliminar
                btnEliminar.setOnClickListener {
                    showDeleteConfirmationDialog(unit.unit_id, position)
                }
            }
        }

        private fun disableButtons() {
            binding.btnEditar.apply {
                isEnabled = false
                alpha = 0.5f  // Visual feedback de deshabilitado
            }
        }

        private fun showDeleteConfirmationDialog(unitId: Int, position: Int) {
            AlertDialog.Builder(itemView.context)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar esta unidad?")
                .setPositiveButton("Eliminar") { _, _ -> deleteUnit(unitId, position) }
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_warning)
                .show()
        }

        private fun launchEditFragment(unitId: Int, productName: String?) {
            val fragment = com.example.localfresh.activitys.vendedor.paginaprincipal.fragments.EditarUnidadVendedorFragment.newInstance(unitId, productName)
            (itemView.context as? androidx.fragment.app.FragmentActivity)?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, fragment)
                ?.addToBackStack(null)
                ?.commit()
        }

        private fun deleteUnit(unitId: Int, position: Int) {
            // Enviar solicitud de eliminación
            viewModel.deleteUnit(unitId)

            // Deshabilitar temporalmente el botón para evitar múltiples eliminaciones
            binding.btnEliminar.isEnabled = false

            // Notificar al fragmento sobre la eliminación para su manejo posterior
            listener?.onUnitDeleted(unitId, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val binding = ItemUnidadVendedorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UnitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        holder.bind(units[position], product, position)
    }

    override fun getItemCount() = units.size

    // Metodo para eliminar una unidad del adaptador
    fun removeUnit(position: Int) {
        if (position < units.size) {
            units.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, units.size)
        }
    }

    // Metodo para actualizar la lista completa de unidades
    fun updateUnits(newUnits: MutableList<UnitSellerDetails>) {
        units = newUnits
        notifyDataSetChanged()
    }
}