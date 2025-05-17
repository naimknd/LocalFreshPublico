package com.example.localfresh.activitys.vendedor.paginaprincipal.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.localfresh.R
import com.example.localfresh.activitys.LogInActivity
import com.example.localfresh.databinding.FragmentPaginaPrincipalVendedorBinding
import com.example.localfresh.model.vendedor.paginaprincipal.GetSellerInfoResponse
import com.example.localfresh.viewmodel.LogoutViewModel
import com.example.localfresh.viewmodel.vendedor.paginaprincipal.PaginaPrincipalVendedorViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localfresh.activitys.comprador.reviews.VerReviewsCompradorFragment
import com.example.localfresh.adapters.vendedor.ReviewsVendedorAdapter
import com.example.localfresh.viewmodel.vendedor.reviews.ReviewVendedorViewModel

class PaginaPrincipalVendedorFragment : Fragment() {

    private var _binding: FragmentPaginaPrincipalVendedorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaginaPrincipalVendedorViewModel by viewModels()
    private val logoutViewModel: LogoutViewModel by viewModels()

    private var sellerId: Int = -1
    private var selectedLatitude: Double = 0.0
    private var selectedLongitude: Double = 0.0
    private var storeName: String = ""

    private lateinit var reviewsAdapter: ReviewsVendedorAdapter
    private val reviewViewModel: ReviewVendedorViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaginaPrincipalVendedorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("LocalFreshPrefs", AppCompatActivity.MODE_PRIVATE)
        sellerId = sharedPreferences.getInt("SELLER_ID", -1)
        val token = sharedPreferences.getString("TOKEN", null)

        if (sellerId != -1) {
            viewModel.fetchSellerInfo(sellerId)
            reviewViewModel.loadSellerReviews(sellerId)
        } else {
            Toast.makeText(requireContext(), "ID de vendedor no válido", Toast.LENGTH_SHORT).show()
        }

        setupObservers()
        setupButtons()
        setupLogoutButton(token)
        setupReviewsSection()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.scrollView.visibility = View.GONE
                binding.buttonsLayout.visibility = View.GONE
                binding.mapContainer.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.scrollView.visibility = View.VISIBLE
                binding.buttonsLayout.visibility = View.VISIBLE
                binding.mapContainer.visibility = View.VISIBLE
            }
        }

        viewModel.sellerInfo.observe(viewLifecycleOwner) { response ->
            response?.let {
                displaySellerStoreInfo(it)
                setupMap(it.data.latitude, it.data.longitude)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMap(latitude: Double, longitude: Double) {
        val mapFragment = MapFragmentVendedor()
        val bundle = Bundle()
        bundle.putDouble("latitude", latitude)
        bundle.putDouble("longitude", longitude)
        mapFragment.arguments = bundle

        childFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()
    }

    private fun setupButtons() {
        binding.editStoreButton.setOnClickListener {
            val fragment = EditarInformacionTiendaFragment.newInstance(sellerId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnDirections.setOnClickListener {
            openGoogleMaps(storeName)
        }
    }

    private fun setupLogoutButton(token: String?) {
        binding.logoutFab.setOnClickListener {
            if (token != null) {
                logoutViewModel.logout(token)
            }
        }

        logoutViewModel.logoutResponse.observe(viewLifecycleOwner) { response ->
            if (response != null && response.status == "success") {
                val editor = requireActivity().getSharedPreferences("LocalFreshPrefs", AppCompatActivity.MODE_PRIVATE).edit()
                editor.remove("TOKEN")
                editor.apply()

                val intent = Intent(requireContext(), LogInActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
            }
        }

        logoutViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupReviewsSection() {
        // Inicializar adaptador
        reviewsAdapter = ReviewsVendedorAdapter()
        binding.reviewsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reviewsAdapter
        }

        // Configurar botón "Ver todas"
        binding.verTodasReviews.setOnClickListener {
            val verReviewsFragment = VerReviewsCompradorFragment.newInstance(sellerId, esVendedor = true)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, verReviewsFragment)
                .addToBackStack(null)
                .commit()
        }

        // Observar cambios en reseñas
        reviewViewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            if (reviews.isEmpty()) {
                binding.noReviewsText.visibility = View.VISIBLE
                binding.reviewsRecyclerView.visibility = View.GONE
            } else {
                binding.noReviewsText.visibility = View.GONE
                binding.reviewsRecyclerView.visibility = View.VISIBLE
                reviewsAdapter.updateReviews(reviews)
            }
        }

        // Observar calificación promedio
        reviewViewModel.averageRating.observe(viewLifecycleOwner) { rating ->
            binding.ratingBarResumen.rating = rating
            binding.ratingValueText.text = String.format("%.1f", rating)
        }

        // Observar número total de reseñas
        reviewViewModel.totalReviews.observe(viewLifecycleOwner) { count ->
            binding.reviewCountText.text = "($count)"
        }

        // Observar errores
        reviewViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg.isNotEmpty()) {
                binding.noReviewsText.text = "No se pudieron cargar las reseñas"
                binding.noReviewsText.visibility = View.VISIBLE
                binding.reviewsRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun openGoogleMaps(storeName: String) {
        if (selectedLatitude != 0.0 && selectedLongitude != 0.0) {
            val uri = "geo:${selectedLatitude},${selectedLongitude}?q=${selectedLatitude},${selectedLongitude}($storeName)"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "Coordenadas no disponibles", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displaySellerStoreInfo(sellerInfoResponse: GetSellerInfoResponse) {
        val seller = sellerInfoResponse.data

        binding.storeName.text = seller.store_name
        binding.storeDescription.text = seller.store_description
        binding.tvTelefono.text = "Teléfono: ${seller.store_phone}"
        binding.tvHorario.text = "Horario: ${seller.opening_time} - ${seller.closing_time}"
        binding.storeType.text = seller.store_type
        binding.storeAddress.text = "Ubicación: ${seller.store_address}"
        seller.store_rating?.let { binding.storeRatingBar.rating = it.toFloat() }
        binding.storeRatingValue.text = String.format("%.1f", seller.store_rating)

        selectedLatitude = seller.latitude
        selectedLongitude = seller.longitude
        storeName = seller.store_name
    }

    override fun onResume() {
        super.onResume()
        // Actualizar la información del vendedor al regresar a la actividad
        if (sellerId != -1) {
            viewModel.fetchSellerInfo(sellerId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
