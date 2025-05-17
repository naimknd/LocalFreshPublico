package com.example.localfresh.adapters.comprador

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.localfresh.R
import com.example.localfresh.activitys.comprador.paginaprincipal.fragments.DetalleProductoCompradorFragment
import com.example.localfresh.databinding.ItemProductoCompradorBinding
import com.example.localfresh.model.comprador.paginaprincipal.ProductoData
import com.example.localfresh.model.comprador.paginaprincipal.UnitData
import com.example.localfresh.utils.DateUtils
import com.example.localfresh.utils.DiscountUtils
import com.example.localfresh.viewmodel.comprador.paginaprincipal.TiendaInfoViewModel
import java.text.SimpleDateFormat
import java.util.*

class ProductosAdapterComprador(
    private var unidades: List<ProductoConUnidad>,
    private val viewModel: TiendaInfoViewModel,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<ProductosAdapterComprador.ProductoViewHolder>() {

    data class ProductoConUnidad(
        val producto: ProductoData,
        val unidad: UnitData
    )

    inner class ProductoViewHolder(private val binding: ItemProductoCompradorBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(productoConUnidad: ProductoConUnidad) {
            val producto = productoConUnidad.producto
            val unidad = productoConUnidad.unidad

            // Usar DateUtils para calcular los días restantes
            val diasRestantes = DateUtils.calcularDiasRestantes(unidad.expiry_date)

            binding.txtNombreProducto.text = producto.product_name
            binding.txtCategoria.text = producto.product_category
            binding.txtCantidad.text = "Disponible: ${unidad.quantity}"

            // Cargar imagen con Glide
            Glide.with(itemView.context)
                .load(producto.product_image)
                .placeholder(R.drawable.ic_error_image)
                .error(R.drawable.ic_error_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(200, 200)
                .format(DecodeFormat.PREFER_RGB_565)
                .into(binding.imgProducto)

            // Usar el metodo centralizado de DiscountUtils para configurar precios
            DiscountUtils.setupPriceDisplay(
                originalPrice = producto.product_price,
                expiryDate = unidad.expiry_date,
                txtPrecioOriginal = binding.txtPrecioOriginal,
                txtPrecioDescuento = binding.txtPrecioDescuento
            )

            // Configurar información de tipo de caducidad y tiempo restante
            binding.txtTipoCaducidad.text = if (producto.expiry_type == "consumo_preferente") {
                "Consumo preferente:"
            } else {
                "Fecha de caducidad:"
            }

            // Establecer texto de día de caducidad con formato
            binding.txtFechaExpiracion.text = DateUtils.formatearFecha(unidad.expiry_date)

            val (clockDrawable, textoRestante, colorId) = DateUtils.getInfoTiempoRestante(
                itemView.context,
                unidad.expiry_date,
                producto.expiry_type
            )

            binding.txtDiasRestantes.apply {
                text = textoRestante
                setTextColor(ContextCompat.getColor(context, colorId))
                clockDrawable?.setTint(ContextCompat.getColor(context, colorId))
                setCompoundDrawablesWithIntrinsicBounds(clockDrawable, null, null, null)
            }

            // Mostrar info del producto al hacer clic
            itemView.setOnClickListener {
                val fragment = DetalleProductoCompradorFragment.newInstance(unidad.unit_id)
                fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoCompradorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(unidades[position])
    }

    override fun getItemCount(): Int = unidades.size

    fun updateData(nuevasUnidades: List<ProductoConUnidad>) {
        unidades = nuevasUnidades
        notifyDataSetChanged()
    }

    fun getCurrentItems(): List<ProductoConUnidad> {
        return unidades
    }
}