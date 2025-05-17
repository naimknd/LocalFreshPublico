package com.example.localfresh.adapters.vendedor

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.localfresh.R
import com.example.localfresh.activitys.vendedor.paginaprincipal.fragments.EditarProductoVendedorFragment
import com.example.localfresh.activitys.vendedor.paginaprincipal.fragments.VerUnidadesVendedorFragment
import com.example.localfresh.databinding.ItemVerProductoVendedorBinding
import com.example.localfresh.model.vendedor.producto.InfoProductSeller
import com.example.localfresh.viewmodel.vendedor.paginaprincipal.VerProductoInfoViewModel

class ProductoAdapterVendedor(
    private val editProductLauncher: ActivityResultLauncher<Intent>,
    private val viewModel: VerProductoInfoViewModel,
    private val fragmentManager: FragmentManager // Añadir FragmentManager
) : RecyclerView.Adapter<ProductoAdapterVendedor.ProductoViewHolder>() {

    private var products: MutableList<InfoProductSeller> = mutableListOf()

    inner class ProductoViewHolder(private val binding: ItemVerProductoVendedorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(product: InfoProductSeller) {
            with(binding) {
                // Configurar textos básicos
                txtNombreProducto.apply {
                    text = product.name
                    isSelected = true
                }
                txtCategoria.text = product.category
                txtPrecio.text = "${product.price} $"

                // Información de stock directa
                txtUnitCount.text = when (product.unit_count) {
                    0 -> "Sin lotes"
                    1 -> "1 lote"
                    else -> "${product.unit_count} lotes"
                }

                // Contadores de stock
                txtStockTotal.text = "Total: ${product.total_quantity}"
                txtStockDisponible.text = "Disp: ${product.available_quantity}"
                txtStockApartado.text = "Apar: ${product.reserved_quantity ?: 0}"
                txtStockExpirado.text = "Exp: ${product.expired_quantity ?: 0}"

                // Determinar el estado predominante y configurar el TextView
                val background = txtEstadoStock.background.mutate()

                when {
                    product.available_quantity > 0 -> {
                        // Si hay unidades disponibles, mostrar "En stock"
                        txtEstadoStock.text = "En stock"
                        DrawableCompat.setTint(background, ContextCompat.getColor(root.context, R.color.green))
                    }
                    (product.reserved_quantity ?: 0) > 0 -> {
                        // Si hay unidades apartadas, mostrar "Apartado"
                        txtEstadoStock.text = "Apartado"
                        DrawableCompat.setTint(background, ContextCompat.getColor(root.context, R.color.warning_color))
                    }
                    (product.expired_quantity ?: 0) > 0 -> {
                        // Si hay unidades expiradas, mostrar "Expirado"
                        txtEstadoStock.text = "Expirado"
                        DrawableCompat.setTint(background, ContextCompat.getColor(root.context, R.color.red))
                    }
                    else -> {
                        // Si no hay unidades, mostrar "Sin stock"
                        txtEstadoStock.text = "Sin stock"
                        DrawableCompat.setTint(background, ContextCompat.getColor(root.context, R.color.red))
                    }
                }

                txtEstadoStock.background = background

                // Configurar ProgressBar
                progressBarStock.apply {
                    max = product.total_quantity
                    progress = product.available_quantity
                    progressTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context,
                            when {
                                product.available_quantity == 0 -> R.color.red
                                product.available_quantity < (product.total_quantity * 0.3) -> R.color.warning_color
                                else -> R.color.green
                            }
                        )
                    )
                }

                // Cargar imagen
                Glide.with(imgProducto.context)
                    .load(product.image_url)
                    .placeholder(R.drawable.ic_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(240, 240)
                    .format(DecodeFormat.PREFER_RGB_565)
                    .error(R.drawable.ic_placeholder)
                    .into(imgProducto)

                // Configuración de botones
                btnEditarProducto.setOnClickListener {
                    val fragment = EditarProductoVendedorFragment.newInstance(product.product_id)
                    fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                btnEliminar.setOnClickListener {
                    showDeleteConfirmationDialog(product.product_id)
                }

                // Click en el item
                root.setOnClickListener {
                    navigateToUnidades(product)
                }
            }
        }

        private fun showDeleteConfirmationDialog(productId: Int) {
            AlertDialog.Builder(binding.root.context)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar este producto?")
                .setPositiveButton("Eliminar") { _, _ ->
                    viewModel.deleteProduct(productId)
                    removeProduct(bindingAdapterPosition)
                }
                .setNegativeButton("Cancelar", null)
                .setCancelable(false)
                .show()
        }

        private fun navigateToUnidades(product: InfoProductSeller) {
            // Usar el fragment en lugar de la activity
            val fragment = VerUnidadesVendedorFragment.newInstance(product.product_id)
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemVerProductoVendedorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun setProducts(newProducts: List<InfoProductSeller>) {
        with(products) {
            clear()
            addAll(newProducts)
        }
        notifyDataSetChanged()
    }

    fun removeProduct(position: Int) {
        if (position in 0 until products.size) {
            products.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, products.size)
        }
    }

    fun addProduct(product: InfoProductSeller) {
        products.add(product)
        notifyItemInserted(products.size - 1)
    }

    fun updateProduct(updatedProduct: InfoProductSeller) {
        val position = products.indexOfFirst { it.product_id == updatedProduct.product_id }
        if (position != -1) {
            products[position] = updatedProduct
            notifyItemChanged(position)
        }
    }

    fun getProducts(): List<InfoProductSeller> = products.toList()

    companion object {
        const val EDIT_PRODUCT_REQUEST = 1
    }
}