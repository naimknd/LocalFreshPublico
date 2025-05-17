package com.example.localfresh.viewmodel.vendedor.reviews

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.localfresh.model.comprador.reviews.ReviewItem
import com.example.localfresh.repository.vendedor.ReviewVendedorRepository

class ReviewVendedorViewModel : ViewModel() {
    private val repository = ReviewVendedorRepository()

    val isLoading: LiveData<Boolean> = repository.isLoading

    private val _reviews = MutableLiveData<List<ReviewItem>>()
    val reviews: LiveData<List<ReviewItem>> = _reviews

    private val _averageRating = MutableLiveData<Float>()
    val averageRating: LiveData<Float> = _averageRating

    private val _totalReviews = MutableLiveData<Int>()
    val totalReviews: LiveData<Int> = _totalReviews

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        observeRepository()
    }

    private fun observeRepository() {
        repository.reviewsResponse.observeForever { response ->
            if (response != null && response.status == "success") {
                _reviews.value = response.reviews ?: emptyList()
                _averageRating.value = response.average_rating ?: 0f
                _totalReviews.value = response.total_reviews ?: 0
            } else {
                _reviews.value = emptyList()
            }
        }

        repository.errorMessage.observeForever { message ->
            _error.value = message ?: "Error desconocido"
        }
    }

    fun loadSellerReviews(sellerId: Int, limit: Int = 3) {
        repository.getSellerReviews(sellerId, limit)
    }
}