package com.example.localfresh.activitys.comprador.reviews

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localfresh.adapters.comprador.ReviewsAdapter
import com.example.localfresh.databinding.FragmentVerReviewsCompradorBinding
import com.example.localfresh.viewmodel.comprador.reviews.ReviewViewModel

class VerReviewsCompradorFragment : Fragment() {
    private var _binding: FragmentVerReviewsCompradorBinding? = null
    private val binding get() = _binding!!

    val viewModel: ReviewViewModel by viewModels()
    private var sellerId: Int = 0
    private var esVendedor: Boolean = false
    private lateinit var reviewsAdapter: ReviewsAdapter

    companion object {
        private const val ARG_SELLER_ID = "seller_id"
        private const val ARG_ES_VENDEDOR = "es_vendedor"

        fun newInstance(sellerId: Int, esVendedor: Boolean = false) = VerReviewsCompradorFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_SELLER_ID, sellerId)
                putBoolean(ARG_ES_VENDEDOR, esVendedor)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sellerId = it.getInt(ARG_SELLER_ID)
            esVendedor = it.getBoolean(ARG_ES_VENDEDOR, false)
        }
        Log.d("ReviewsDebug", "Fragment iniciado con sellerId: $sellerId, esVendedor: $esVendedor")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerReviewsCompradorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupPaginationControls()

        // Solicitar las reseñas para el vendedor
        Log.d("ReviewsDebug", "Solicitando reseñas para vendedor: $sellerId")
        viewModel.getSellerReviews(sellerId)
    }

    private fun setupRecyclerView() {
        // Solo mostrar el botón de reportar si NO es vendedor
        reviewsAdapter = ReviewsAdapter(showReportButton = !esVendedor)

        reviewsAdapter.setOnReportClickListener(object : ReviewsAdapter.OnReviewReportClickListener {
            override fun onReportClick(reviewId: Int) {
                Log.d("VerReviewsFragment", "Solicitud de reportar reseña con ID: $reviewId")
                showReportDialog(reviewId)
            }
        })

        binding.reviewsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reviewsAdapter
        }

        // Botón back usando popBackStack()
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun showReportDialog(reviewId: Int) {
        Log.d("VerReviewsFragment", "Mostrando diálogo para reportar reseña: $reviewId")
        val reportDialog = ReportReviewDialogFragment.newInstance(reviewId)
        reportDialog.show(childFragmentManager, "report_review")
    }

    private fun setupObservers() {
        // Observar el estado de carga principal
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observar el estado de carga de más páginas
        viewModel.isLoadingMore.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingMore.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observar la lista de reseñas de la página actual
        viewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            Log.d("ReviewsDebug", "Actualizando lista de reseñas con ${reviews.size} elementos")
            reviewsAdapter.submitList(reviews)

            // Mostrar u ocultar el estado vacío
            if (reviews.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.reviewsRecyclerView.visibility = View.GONE
                binding.paginationControls.root.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.reviewsRecyclerView.visibility = View.VISIBLE
                binding.paginationControls.root.visibility = View.VISIBLE
            }
        }

        // Observar el resumen de reseñas (para actualizar la parte superior)
        viewModel.reviewSummary.observe(viewLifecycleOwner) { (average, total, distribution) ->
            // Actualizar los elementos dentro del layout incluido
            val ratingsSummary = binding.ratingsSummary

            ratingsSummary.tvRatingAverage.text = average.toString()
            ratingsSummary.ratingBar.rating = average
            ratingsSummary.tvTotalReviews.text = "$total reseñas"

            updateRatingDistribution(distribution)
        }

        // Observar información de paginación
        viewModel.currentPage.observe(viewLifecycleOwner) { currentPage ->
            viewModel.totalPages.value?.let { totalPages ->
                updatePaginationControls(currentPage, totalPages)
            }
        }

        viewModel.totalPages.observe(viewLifecycleOwner) { totalPages ->
            viewModel.currentPage.value?.let { currentPage ->
                updatePaginationControls(currentPage, totalPages)
            }
        }

        // Observar resultado de reportes
        viewModel.reportReviewResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.fold(
                onSuccess = { response ->
                    Log.d("VerReviewsFragment", "Reporte enviado con éxito: ${response.message}")
                    // Mostrar un mensaje de éxito
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                },
                onFailure = { error ->
                    Log.e("VerReviewsFragment", "Error al reportar: ${error.message}")
                    Toast.makeText(requireContext(), error.message ?: "Error al reportar reseña", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun setupPaginationControls() {
        // Botón Anterior
        binding.paginationControls.btnPrevious.setOnClickListener {
            viewModel.loadPreviousPage()
        }

        // Botón Siguiente
        binding.paginationControls.btnNext.setOnClickListener {
            viewModel.loadNextPage()
        }
    }

    private fun updatePaginationControls(currentPage: Int, totalPages: Int) {
        with(binding.paginationControls) {
            tvPageInfo.text = "Página $currentPage de $totalPages"
            btnPrevious.isEnabled = viewModel.canLoadPreviousPage()
            btnPrevious.visibility = if (viewModel.canLoadPreviousPage()) View.VISIBLE else View.INVISIBLE
            btnNext.isEnabled = viewModel.canLoadNextPage()
            btnNext.visibility = if (viewModel.canLoadNextPage()) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun updateRatingDistribution(distribution: Map<Int, Int>) {
        // Usar los elementos del layout incluido ratingsSummary
        val ratingsSummary = binding.ratingsSummary

        // Actualizar las barras de progreso de distribución de calificaciones
        val totalReviews = distribution.values.sum().toFloat()
        if (totalReviews > 0) {
            ratingsSummary.progressBar5.progress = ((distribution[5] ?: 0) * 100 / totalReviews).toInt()
            ratingsSummary.progressBar4.progress = ((distribution[4] ?: 0) * 100 / totalReviews).toInt()
            ratingsSummary.progressBar3.progress = ((distribution[3] ?: 0) * 100 / totalReviews).toInt()
            ratingsSummary.progressBar2.progress = ((distribution[2] ?: 0) * 100 / totalReviews).toInt()
            ratingsSummary.progressBar1.progress = ((distribution[1] ?: 0) * 100 / totalReviews).toInt()

            // Actualizar los contadores de cada puntuación
            ratingsSummary.txtCount5.text = (distribution[5] ?: 0).toString()
            ratingsSummary.txtCount4.text = (distribution[4] ?: 0).toString()
            ratingsSummary.txtCount3.text = (distribution[3] ?: 0).toString()
            ratingsSummary.txtCount2.text = (distribution[2] ?: 0).toString()
            ratingsSummary.txtCount1.text = (distribution[1] ?: 0).toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}