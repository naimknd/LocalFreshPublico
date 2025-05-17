package com.example.localfresh.activitys.comprador.paginaprincipal.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.localfresh.R
import com.example.localfresh.activitys.comprador.reviews.ReportReviewDialogFragment
import com.example.localfresh.activitys.comprador.reviews.VerReviewsCompradorFragment
import com.example.localfresh.adapters.comprador.ReviewsAdapter
import com.example.localfresh.databinding.BottomSheetStoreInfoBinding
import com.example.localfresh.databinding.ItemProductoBottomSheetBinding
import com.example.localfresh.model.comprador.favoritos.StoreFavorite
import com.example.localfresh.model.comprador.paginaprincipal.ProductoData
import com.example.localfresh.model.comprador.paginaprincipal.TiendaData
import com.example.localfresh.model.comprador.paginaprincipal.TiendasPaginaPrincipal
import com.example.localfresh.network.RetrofitClient
import com.example.localfresh.viewmodel.comprador.favoritos.FavoritesViewModel
import com.example.localfresh.viewmodel.comprador.paginaprincipal.TiendaInfoViewModel
import com.example.localfresh.viewmodel.comprador.reviews.ReviewViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class StoreInfoBottomSheetFragment(private val store: TiendasPaginaPrincipal) : BottomSheetDialogFragment() {
    private val viewModel: TiendaInfoViewModel by viewModels()
    private val favoritesViewModel: FavoritesViewModel by viewModels()
    private val reviewViewModel: ReviewViewModel by viewModels()

    private var _binding: BottomSheetStoreInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var reviewsAdapter: ReviewsAdapter
    private val osrmService by lazy { RetrofitClient.getOsrmService() }
    private val userId by lazy {
        requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            .getInt("USER_ID", -1)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetStoreInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.scrollView.isNestedScrollingEnabled = true
        setupUI()
        loadData()
    }

    private fun setupUI() {
        setupReviewsRecyclerView()
        setupObservers()
        setupButtons()
        setupFavoriteButton()
        requestLocationAndCalculateDistance()
    }

    private fun loadData() {
        viewModel.obtenerTiendaData(store.seller_id)
        viewModel.obtenerProductosTienda(store.seller_id)
        reviewViewModel.getSellerReviews(store.seller_id, 1)
    }

    private fun setupReviewsRecyclerView() {
        binding.linearLayoutResenas.visibility = View.VISIBLE

        reviewsAdapter = ReviewsAdapter().apply {
            setOnReportClickListener(object : ReviewsAdapter.OnReviewReportClickListener {
                override fun onReportClick(reviewId: Int) {
                    showReportDialog(reviewId)
                }
            })
        }

        binding.recyclerViewReviews.apply {
            adapter = reviewsAdapter
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
            itemAnimator = null
        }
    }

    private fun showReportDialog(reviewId: Int) {
        // Verificar que el fragmento esté adjunto antes de mostrar el diálogo
        if (!isAdded) return

        val reportDialog = ReportReviewDialogFragment.newInstance(reviewId)
        reportDialog.setViewModel(reviewViewModel)

        // Usar parentFragmentManager en lugar de childFragmentManager
        reportDialog.show(parentFragmentManager, "report_review")
    }

    private fun setupFavoriteButton() {
        if (userId != -1) {
            favoritesViewModel.checkFavoriteStatus(userId, sellerId = store.seller_id)

            // Observar cambios en el estado de favorito
            favoritesViewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
                binding.btnAdd.apply {
                    isSelected = isFavorite
                    setImageResource(if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite)
                }
            }

            // Click listener
            binding.btnAdd.setOnClickListener {
                it.startAnimation(AnimationUtils.loadAnimation(context, R.anim.favorite_selected))
                favoritesViewModel.toggleFavorite(userId, store.seller_id)
            }

            // Respuesta del servidor
            favoritesViewModel.favoriteStatus.observe(viewLifecycleOwner) { response ->
                response?.let {
                    showToast(it.message)
                    if (it.status == "error") favoritesViewModel.checkFavoriteStatus(userId, store.seller_id)
                }
            }

            // Estado de carga
            favoritesViewModel.isLoading.observe(viewLifecycleOwner) {
                binding.btnAdd.isEnabled = !it
            }
        } else {
            binding.btnAdd.apply {
                isEnabled = false
                setOnClickListener { showToast("Debes iniciar sesión para agregar a favoritos") }
            }
        }
    }

    private fun setupObservers() = with(binding) {
        // Datos de la tienda
        viewModel.tiendaData.observe(viewLifecycleOwner) { it?.let { updateStoreInfo(it) } }

        // Datos de productos
        viewModel.productosData.observe(viewLifecycleOwner) { it?.let { updateProductsList(it) } }

        // Reseñas
        reviewViewModel.reviews.observe(viewLifecycleOwner) { updateReviewsUI(it) }

        // Resumen de reseñas
        reviewViewModel.reviewSummary.observe(viewLifecycleOwner) { summary ->
            summary?.let { (average, total, _) ->
                ratingBarStore.rating = average
                textViewRating.text = String.format("%.1f", average)
                textViewResenas.text = "Reseñas ($total)"
            } ?: run {
                ratingBarStore.rating = 0f
                textViewRating.text = "N/A"
                textViewResenas.text = "Reseñas (0)"
            }
        }
    }

    private fun updateReviewsUI(reviews: List<com.example.localfresh.model.comprador.reviews.ReviewItem>?) {
        binding.apply {
            // Estado vacío o con contenido
            val isEmpty = reviews.isNullOrEmpty()
            textViewNoReviews.visibility = if (isEmpty) View.VISIBLE else View.GONE
            imageViewNoReviews.visibility = if (isEmpty) View.VISIBLE else View.GONE
            recyclerViewReviews.visibility = if (isEmpty) View.GONE else View.VISIBLE

            // Actualizar lista
            if (!isEmpty) {
                // Limitar a 3 reseñas para la vista previa
                val limitedReviews = reviews.take(3)
                reviewsAdapter.submitList(limitedReviews)
            }
        }
    }

    private fun setupButtons() {
        // Ver todos los productos
        binding.buttonVerProductos.setOnClickListener {
            navigateToFragment(ProductsCompradorFragment().apply {
                arguments = Bundle().apply { putInt("SELLER_ID", store.seller_id) }
            })
        }

        // Cómo llegar
        binding.buttonComoLlegar.setOnClickListener { openMapsNavigation() }

        // Ver todas las reseñas
        binding.textViewVerResenas.setOnClickListener {
            navigateToFragment(VerReviewsCompradorFragment.newInstance(store.seller_id))
        }
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right, R.anim.fade_out,
                R.anim.fade_in, R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
        dismiss()
    }

    private fun openMapsNavigation() {
        try {
            val gmmIntentUri = Uri.parse("google.navigation:q=${store.latitude},${store.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            startActivity(mapIntent)
        } catch (e: Exception) {
            // Fallback si Google Maps no está instalado
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${store.latitude},${store.longitude}")
            ))
        }
    }

    private fun requestLocationAndCalculateDistance() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            calculateHaversineDistance(0.0, 0.0)
            return
        }

        fusedLocationClient.lastLocation.apply {
            addOnSuccessListener { location ->
                if (location != null) {
                    tryGetRouteDistance(location.latitude, location.longitude)
                } else {
                    calculateHaversineDistance(0.0, 0.0)
                }
            }
            addOnFailureListener { calculateHaversineDistance(0.0, 0.0) }
        }
    }

    private fun tryGetRouteDistance(lat: Double, lon: Double) {
        lifecycleScope.launch {
            try {
                val response = osrmService.getRoute(
                    "${lon},${lat}",
                    "${store.longitude},${store.latitude}"
                )

                if (response.code != "no_route") {
                    calculateHaversineDistance(lat, lon)
                    return@launch
                }

                val distance = response.routes[0].distance / 1000 // km
                binding.textViewDistancia.text = String.format("%.1f km", distance)
            } catch (e: Exception) {
                calculateHaversineDistance(lat, lon)
            }
        }
    }

    private fun calculateHaversineDistance(lat1: Double, lon1: Double) {
        val earthRadius = 6371.0 // km
        val lat2 = store.latitude
        val lon2 = store.longitude

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadius * c

        _binding?.textViewDistancia?.text = String.format("%.1f km (directa)", distance)
    }

    private fun updateStoreInfo(tiendaData: TiendaData) = with(binding) {
        textViewNombreTienda.text = tiendaData.store_name
        textViewHorario.text = "${tiendaData.opening_time} - ${tiendaData.closing_time}"
        textViewContacto.text = tiendaData.store_phone
        textViewCategorias.text = tiendaData.store_type
        textViewUbicacion.text = tiendaData.store_address
    }

    private fun updateProductsList(productos: List<ProductoData>) = with(binding) {
        // Limpiar contenedor y mostrar/ocultar según corresponda
        linearLayoutProductosList.removeAllViews()

        // Tomar solo los primeros 4 productos
        val productosLimitados = productos.take(4)
        linearLayoutProductos.visibility = if (productosLimitados.isEmpty()) View.GONE else View.VISIBLE

        if (productosLimitados.isEmpty()) return

        // Crear y añadir las vistas de productos
        productosLimitados.forEach { producto ->
            // Usar ItemProductoBottomSheetBinding en lugar de inflater con findViewById
            val productoBinding = ItemProductoBottomSheetBinding.inflate(
                layoutInflater, linearLayoutProductosList, false)

            with(productoBinding) {
                // Configurar datos
                textViewNombreProducto.text = producto.product_name
                textViewPrecioProducto.text = getString(R.string.price_format, producto.product_price)

                // Configurar click listener
                root.setOnClickListener {
                    navigateToFragment(DetalleProductoCompradorFragment.newInstance(producto.units[0].unit_id))
                }

                // Cargar imagen
                loadProductImage(producto.product_image, imageViewProducto)

                // Añadir al contenedor
                linearLayoutProductosList.addView(root)
            }
        }

        // Mostrar botón "Ver todos" solo si hay más de 4 productos
        buttonVerProductos.visibility = if (productos.size > 4) View.VISIBLE else View.GONE
    }

    // Metodo para cargar imágenes
    private fun loadProductImage(imageUrl: String, imageView: ImageView) {
        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(imageView)
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(store: StoreFavorite): StoreInfoBottomSheetFragment {
            return StoreInfoBottomSheetFragment(
                TiendasPaginaPrincipal(
                    seller_id = store.seller_id,
                    store_name = store.store_name,
                    latitude = store.latitude ?: 0.0,
                    longitude = store.longitude ?: 0.0,
                    opening_time = store.opening_time ?: "",
                    closing_time = store.closing_time ?: "",
                    organic_products = 0,
                    store_type = ""
                )
            )
        }
    }
}