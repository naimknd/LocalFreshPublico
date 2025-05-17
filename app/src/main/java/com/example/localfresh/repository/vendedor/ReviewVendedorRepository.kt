package com.example.localfresh.repository.vendedor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.localfresh.model.comprador.reviews.SellerReviewsResponse
import com.example.localfresh.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewVendedorRepository {
    private val _reviewsResponse = MutableLiveData<SellerReviewsResponse>()
    val reviewsResponse: LiveData<SellerReviewsResponse> get() = _reviewsResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun getSellerReviews(sellerId: Int, limit: Int = 3) {
        _isLoading.value = true
        RetrofitInstance.compradorApiService.getSellerReviews(sellerId, 1, limit)
            .enqueue(object : Callback<SellerReviewsResponse> {
                override fun onResponse(
                    call: Call<SellerReviewsResponse>,
                    response: Response<SellerReviewsResponse>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        _reviewsResponse.value = response.body()
                    } else {
                        _errorMessage.value = "Error: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<SellerReviewsResponse>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = "Error de conexión: ${t.message}"
                }
            })
    }
}