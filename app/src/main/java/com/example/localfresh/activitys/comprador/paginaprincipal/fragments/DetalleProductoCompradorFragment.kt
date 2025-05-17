package com.example.localfresh.activitys.comprador.paginaprincipal.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.localfresh.R
import com.example.localfresh.activitys.comprador.paginaprincipal.PaginaPrincipalCompradorActivity
import com.example.localfresh.databinding.FragmentDetalleProductoCompradorBinding
import com.example.localfresh.model.comprador.detalle.DetalleUnidad
import com.example.localfresh.utils.DateUtils
import com.example.localfresh.utils.DiscountUtils
import com.example.localfresh.viewmodel.comprador.apartados.CartViewModel
import com.example.localfresh.viewmodel.comprador.favoritos.FavoritesViewModel
import com.example.localfresh.viewmodel.comprador.paginaprincipal.TiendaInfoViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DetalleProductoCompradorFragment : Fragment() {
    private var _binding: FragmentDetalleProductoCompradorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TiendaInfoViewModel by viewModels()
    private val favoritesViewModel: FavoritesViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()

    private var unitId: Int = 0
    private var productId: Int = 0
    private var selectedQuantity = 1
    private var maxAvailableQuantity = 0

    companion object {
        private const val ARG_UNIT_ID = "unit_id"
        private const val EXPIRY_TYPE_CONSUMO_PREFERENTE = "consumo_preferente"

        fun newInstance(unitId: Int) = DetalleProductoCompradorFragment().apply {
            arguments = Bundle().apply { putInt(ARG_UNIT_ID, unitId) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { unitId = it.getInt(ARG_UNIT_ID) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetalleProductoCompradorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        viewModel.obtenerDetalleUnidad(unitId)
    }

    private fun setupUI() = with(binding) {
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        btnIncrease.setOnClickListener { adjustQuantity(1) }
        btnDecrease.setOnClickListener { adjustQuantity(-1) }
    }

    private fun setupObservers() {
        viewModel.detalleUnidad.observe(viewLifecycleOwner, ::updateProductDetails)

        cartViewModel.addToCartResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.fold(
                onSuccess = { updateStockAfterCartAdd(it) },
                onFailure = { showToast(it.message ?: "Error al agregar al carrito") }
            )
        }

        cartViewModel.isLoading.observe(viewLifecycleOwner, ::updateLoadingState)
    }

    private fun updateProductDetails(detalle: DetalleUnidad?) {
        detalle?.let {
            productId = it.product_id
            maxAvailableQuantity = it.quantity
            selectedQuantity = 1

            loadProductImage(it.product_image)
            updateProductInfo(it)
            setupExpiryInfo(it)
            setupActionButtons(it)
            setupFavoriteButton()
            updateQuantityUI()
        }
    }

    private fun updateProductInfo(detalle: DetalleUnidad) = with(binding) {
        txtNombreProducto.text = detalle.product_name
        txtNombreTienda.text = detalle.store_name
        txtCategoria.apply {
            text = detalle.product_category
            setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
        }
        txtCantidad.text = "Cantidad disponible: ${detalle.quantity}"
        txtDescripcion.text = detalle.product_description

        // Configurar precios con helper centralizado
        DiscountUtils.setupPriceDisplay(
            originalPrice = detalle.product_price,
            expiryDate = detalle.expiry_date,
            txtPrecioOriginal = txtPrecioOriginal,
            txtPrecioDescuento = txtPrecioDescuento
        )
    }

    private fun loadProductImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(binding.imgProducto)
    }

    private fun setupExpiryInfo(detalle: DetalleUnidad) {
        binding.txtFechaExpiracion.text = "Fecha: ${DateUtils.formatearFecha(detalle.expiry_date)}"

        // Configurar tiempo restante
        val timeInfo = DateUtils.getInfoTiempoRestante(requireContext(), detalle.expiry_date, detalle.expiry_type)
        binding.txtTiempoRestante.apply {
            text = timeInfo.second
            setTextColor(ContextCompat.getColor(context, timeInfo.third))

            // Configurar icono
            timeInfo.first?.let { drawable ->
                drawable.setTint(ContextCompat.getColor(context, timeInfo.third))
                setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.text_drawable_padding)
            }

            // Caso especial para productos expirados de consumo preferente
            val isExpiredPreferredConsumption = detalle.expiry_type == EXPIRY_TYPE_CONSUMO_PREFERENTE &&
                    DateUtils.calcularDiasRestantes(detalle.expiry_date) <= 0

            if (isExpiredPreferredConsumption) {
                setCompoundDrawablesWithIntrinsicBounds(
                    timeInfo.first, null,
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_info), null
                )
                setOnClickListener { mostrarInfoConsumoPreferente() }
                binding.txtInfoConsumoPreferente.visibility = View.VISIBLE
            } else {
                setOnClickListener(null)
                binding.txtInfoConsumoPreferente.visibility = View.GONE
            }
        }

        // Configurar barra de progreso
        setupProgressBar(detalle.expiry_date, detalle.expiry_type)
    }

    private fun setupProgressBar(expiryDate: String, expiryType: String) {
        val diasRestantes = DateUtils.calcularDiasRestantes(expiryDate)
        binding.progressTiempoRestante.apply {
            progress = when {
                diasRestantes <= 0 -> 0
                diasRestantes >= 30 -> 100
                else -> ((diasRestantes * 100) / 30).toInt()
            }

            progressTintList = ContextCompat.getColorStateList(
                context,
                when {
                    diasRestantes > 10 -> R.color.green
                    diasRestantes > 5 -> R.color.warning_color
                    diasRestantes > 0 -> R.color.warning_color
                    else -> if (expiryType == EXPIRY_TYPE_CONSUMO_PREFERENTE)
                        R.color.warning_color else R.color.red
                }
            )
        }
    }

    private fun setupActionButtons(detalle: DetalleUnidad) {
        binding.btnComoLlegar.setOnClickListener { openMapsNavigation(detalle.latitude, detalle.longitude) }
        binding.btnAgregarCarrito.setOnClickListener { addToCart() }
    }

    private fun openMapsNavigation(latitude: Double, longitude: Double) {
        try {
            val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude")
            startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
            })
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
            ))
        }
    }

    private fun addToCart() {
        val userId = requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            .getInt("USER_ID", -1)

        when {
            userId == -1 -> showToast("Debes iniciar sesión primero")
            maxAvailableQuantity <= 0 -> showToast("No hay unidades disponibles")
            else -> cartViewModel.addToCart(userId, unitId, selectedQuantity)
        }
    }

    private fun updateStockAfterCartAdd(response: com.example.localfresh.model.comprador.apartados.AddToCartResponse) {
        showToast(response.message)
        (activity as? PaginaPrincipalCompradorActivity)?.refreshCartBadge()

        if (response.remaining_stock >= 0) {
            binding.txtCantidad.apply {
                text = "Cantidad disponible: ${response.remaining_stock}"
                startAnimation(AnimationUtils.loadAnimation(context, R.anim.pulse))
            }

            maxAvailableQuantity = response.remaining_stock
            selectedQuantity = 1
            updateQuantityUI()

            binding.btnAgregarCarrito.apply {
                isEnabled = response.remaining_stock > 0
                alpha = if (response.remaining_stock > 0) 1.0f else 0.5f
            }
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.apply {
            btnAgregarCarrito.isEnabled = !isLoading
            btnDecrease.isEnabled = !isLoading && selectedQuantity > 1
            btnIncrease.isEnabled = !isLoading && selectedQuantity < maxAvailableQuantity
        }
    }

    private fun adjustQuantity(amount: Int) {
        val newQuantity = selectedQuantity + amount
        when {
            amount > 0 && newQuantity > maxAvailableQuantity -> showToast("No hay más unidades disponibles")
            amount < 0 && newQuantity < 1 -> return
            else -> {
                selectedQuantity = newQuantity
                updateQuantityUI()
            }
        }
    }

    private fun updateQuantityUI() {
        binding.apply {
            txtQuantityValue.text = selectedQuantity.toString()

            // Actualizar estado de botones
            val canDecrease = selectedQuantity > 1
            val canIncrease = selectedQuantity < maxAvailableQuantity

            btnDecrease.isEnabled = canDecrease
            btnIncrease.isEnabled = canIncrease

            btnDecrease.alpha = if (canDecrease) 1.0f else 0.5f
            btnIncrease.alpha = if (canIncrease) 1.0f else 0.5f
        }
    }

    private fun setupFavoriteButton() {
        val userId = requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            .getInt("USER_ID", -1)

        if (userId != -1) {
            favoritesViewModel.checkFavoriteStatus(userId, productId)

            favoritesViewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
                binding.btnFavorito.apply {
                    isSelected = isFavorite
                    setImageResource(if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite)
                }
            }

            binding.btnFavorito.setOnClickListener {
                it.startAnimation(AnimationUtils.loadAnimation(context, R.anim.favorite_selected))
                favoritesViewModel.toggleFavorite(userId, productId)
                showToast(if (binding.btnFavorito.isSelected)
                    "Producto eliminado de favoritos" else "Producto añadido a favoritos")
            }
        } else {
            binding.btnFavorito.setOnClickListener { showToast("Debes iniciar sesión para guardar favoritos") }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun mostrarInfoConsumoPreferente() {
        MaterialAlertDialogBuilder(requireContext())
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

    override fun onDestroyView() {
        view?.let { Glide.with(this).clear(binding.imgProducto) }
        super.onDestroyView()
        _binding = null
    }
}