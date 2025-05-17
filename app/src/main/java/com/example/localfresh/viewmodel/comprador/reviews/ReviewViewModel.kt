package com.example.localfresh.viewmodel.comprador.reviews

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.localfresh.model.comprador.reviews.ReportReviewRequest
import com.example.localfresh.model.comprador.reviews.ReviewItem
import com.example.localfresh.model.comprador.reviews.ReviewRequest
import com.example.localfresh.repository.comprador.ReviewRepository

class ReviewViewModel : ViewModel() {
    val repository = ReviewRepository()

    // Para mostrar las reseñas actuales (solo de la página actual)
    private val _reviews = MutableLiveData<List<ReviewItem>>()
    val reviews: LiveData<List<ReviewItem>> = _reviews

    // Variables para datos de cabecera (no cambian con la paginación)
    private val _reviewSummary = MutableLiveData<Triple<Float, Int, Map<Int, Int>>>()
    val reviewSummary: LiveData<Triple<Float, Int, Map<Int, Int>>> = _reviewSummary

    // LiveData para controlar la carga de las páginas
    private val _isLoadingMore = MutableLiveData<Boolean>(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore
    val isLoading: LiveData<Boolean> = repository.isLoading

    // LiveData para la paginación
    private val _currentPage = MutableLiveData<Int>(1)
    val currentPage: LiveData<Int> = _currentPage

    private val _totalPages = MutableLiveData<Int>(1)
    val totalPages: LiveData<Int> = _totalPages

    // Exponer el submitReviewResult para el fragmento de calificar
    val submitReviewResult = repository.submitReviewResult
    // Exponer el reportReviewResult para el fragmento de reportar
    val reportReviewResult = repository.reportReviewResult

    private var currentSellerId: Int = -1

    fun submitReview(userId: Int, sellerId: Int, reservationId: Int, rating: Int, comment: String?) {
        val request = ReviewRequest(userId, sellerId, reservationId, rating, comment)
        repository.submitReview(request)
    }

    fun getSellerReviews(sellerId: Int, page: Int = 1) {
        currentSellerId = sellerId
        repository.getSellerReviews(sellerId, page)

        // Observar resultados
        repository.sellerReviews.observeForever { event ->
            event.getContentIfNotHandled()?.fold(
                onSuccess = { response ->
                    // Almacenar el resumen de reseñas (promedio, total, distribución)
                    _reviewSummary.value = Triple(
                        response.average_rating,
                        response.total_reviews,
                        response.distribution.mapKeys { it.key.toInt() }
                    )

                    // Actualizar las reseñas para esta página
                    _reviews.value = response.reviews

                    // Actualizar información de paginación
                    _currentPage.value = response.current_page
                    _totalPages.value = response.total_pages

                    _isLoadingMore.value = false
                },
                onFailure = {
                    _isLoadingMore.value = false
                }
            )
        }
    }

    fun reportReview(userId: Int, reviewId: Int, reason: String, additionalInfo: String? = null) {
        Log.d("ReviewViewModel", "Reportando reseña $reviewId con razón: $reason")
        val request = ReportReviewRequest(reviewId, userId, reason, additionalInfo)
        repository.reportReview(request)
    }

    fun loadNextPage() {
        if (repository.hasMorePages() && !repository.isLoadingMorePages()) {
            _isLoadingMore.value = true
            repository.loadNextPage(currentSellerId)
        }
    }

    fun loadPreviousPage() {
        if (repository.hasPreviousPages() && !repository.isLoadingMorePages()) {
            _isLoadingMore.value = true
            repository.loadPreviousPage(currentSellerId)
        }
    }

    fun canLoadNextPage(): Boolean {
        return repository.hasMorePages()
    }

    fun canLoadPreviousPage(): Boolean {
        return repository.hasPreviousPages()
    }
}