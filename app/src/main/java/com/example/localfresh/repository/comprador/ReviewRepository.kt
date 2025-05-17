package com.example.localfresh.repository.comprador

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.localfresh.model.comprador.reviews.ReportReviewRequest
import com.example.localfresh.model.comprador.reviews.ReportReviewResponse
import com.example.localfresh.model.comprador.reviews.ReviewRequest
import com.example.localfresh.model.comprador.reviews.ReviewResponse
import com.example.localfresh.model.comprador.reviews.SellerReviewsResponse
import com.example.localfresh.network.RetrofitInstance
import com.example.localfresh.utils.Event
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewRepository {
    private val _sellerReviews = MutableLiveData<Event<Result<SellerReviewsResponse>>>()
    val sellerReviews: LiveData<Event<Result<SellerReviewsResponse>>> = _sellerReviews

    private val _submitReviewResult = MutableLiveData<Event<Result<ReviewResponse>>>()
    val submitReviewResult: LiveData<Event<Result<ReviewResponse>>> = _submitReviewResult

    private val _reportReviewResult = MutableLiveData<Event<Result<ReportReviewResponse>>>()
    val reportReviewResult: LiveData<Event<Result<ReportReviewResponse>>> = _reportReviewResult

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Variables para manejar la paginación
    private var currentPage = 1
    private var totalPages = 1
    private var isLoadingMore = false

    fun submitReview(reviewRequest: ReviewRequest) {
        _isLoading.value = true
        RetrofitInstance.compradorApiService.submitReview(reviewRequest)
            .enqueue(object : Callback<ReviewResponse> {
                override fun onResponse(call: Call<ReviewResponse>, response: Response<ReviewResponse>) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        response.body()?.let {
                            _submitReviewResult.value = Event(Result.success(it))
                        } ?: run {
                            _submitReviewResult.value = Event(Result.failure(Exception("Respuesta vacía")))
                        }
                    } else {
                        _submitReviewResult.value = Event(Result.failure(Exception("Error: ${response.code()}")))
                    }
                }

                override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                    _isLoading.value = false
                    _submitReviewResult.value = Event(Result.failure(t))
                }
            })
    }

    fun getSellerReviews(sellerId: Int, page: Int = 1, limit: Int = 5) {
        // Si estamos cargando la primera página o una página específica
        if (page == 1) {
            _isLoading.value = true
        } else {
            isLoadingMore = true
        }

        currentPage = page

        Log.d("ReviewsDebug", "Repository: Iniciando llamada a API para página $page")

        RetrofitInstance.compradorApiService.getSellerReviews(sellerId, page, limit)
            .enqueue(object : Callback<SellerReviewsResponse> {
                override fun onResponse(call: Call<SellerReviewsResponse>, response: Response<SellerReviewsResponse>) {
                    _isLoading.value = false
                    isLoadingMore = false

                    if (response.isSuccessful) {
                        response.body()?.let {
                            totalPages = it.total_pages
                            currentPage = it.current_page
                            Log.d("ReviewsDebug", "Respuesta exitosa: ${it.reviews.size} reseñas, página $currentPage de $totalPages")
                            _sellerReviews.value = Event(Result.success(it))
                        } ?: run {
                            _sellerReviews.value = Event(Result.failure(Exception("Respuesta vacía")))
                        }
                    } else {
                        val errorMsg = "Error en la respuesta: ${response.code()}"
                        Log.e("ReviewsDebug", errorMsg)
                        _sellerReviews.value = Event(Result.failure(Exception(errorMsg)))
                    }
                }

                override fun onFailure(call: Call<SellerReviewsResponse>, t: Throwable) {
                    _isLoading.value = false
                    isLoadingMore = false
                    Log.e("ReviewsDebug", "Error en la llamada: ${t.message}")
                    _sellerReviews.value = Event(Result.failure(t))
                }
            })
    }

    fun reportReview(request: ReportReviewRequest) {
        Log.d("ReviewRepository", "Reportando reseña: ${request.review_id} por usuario: ${request.user_id}")
        _isLoading.value = true

        RetrofitInstance.compradorApiService.reportReview(request)
            .enqueue(object : Callback<ReportReviewResponse> {
                override fun onResponse(call: Call<ReportReviewResponse>, response: Response<ReportReviewResponse>) {
                    _isLoading.value = false

                    if (response.isSuccessful) {
                        response.body()?.let {
                            Log.d("ReviewRepository", "Reporte exitoso: ${it.message}")
                            _reportReviewResult.value = Event(Result.success(it))
                        } ?: run {
                            _reportReviewResult.value = Event(Result.failure(Exception("Respuesta vacía")))
                        }
                    } else {
                        // Intentar parsear el error del cuerpo de la respuesta
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ReportReviewResponse::class.java)
                            _reportReviewResult.value = Event(Result.failure(Exception(errorResponse.message)))
                        } catch (e: Exception) {
                            _reportReviewResult.value = Event(Result.failure(Exception("Error: ${response.code()}")))
                        }
                    }
                }

                override fun onFailure(call: Call<ReportReviewResponse>, t: Throwable) {
                    _isLoading.value = false
                    _reportReviewResult.value = Event(Result.failure(t))
                }
            })
    }

    fun loadNextPage(sellerId: Int) {
        if (currentPage < totalPages && !isLoadingMore) {
            getSellerReviews(sellerId, currentPage + 1)
        }
    }

    fun loadPreviousPage(sellerId: Int) {
        if (currentPage > 1 && !isLoadingMore) {
            getSellerReviews(sellerId, currentPage - 1)
        }
    }

    fun hasMorePages(): Boolean {
        return currentPage < totalPages
    }

    fun hasPreviousPages(): Boolean {
        return currentPage > 1
    }

    fun isLoadingMorePages(): Boolean {
        return isLoadingMore
    }

    fun getCurrentPage(): Int {
        return currentPage
    }

    fun getTotalPages(): Int {
        return totalPages
    }
}