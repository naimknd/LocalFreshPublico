package com.example.localfresh.viewmodel.comprador.favoritos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.localfresh.model.comprador.favoritos.FavoriteRequest
import com.example.localfresh.model.comprador.favoritos.FavoritesResponse
import com.example.localfresh.model.comprador.favoritos.GetFavoritesResponse
import com.example.localfresh.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FavoritesViewModel : ViewModel() {
    private val _favoriteStatus = MutableLiveData<FavoritesResponse>()
    val favoriteStatus: LiveData<FavoritesResponse> = _favoriteStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

   private val _favorites = MutableLiveData<GetFavoritesResponse>()
    val favorites: LiveData<GetFavoritesResponse> = _favorites

    fun checkFavoriteStatus(userId: Int, sellerId: Int? = null, productId: Int? = null) {
        _isLoading.value = true
        RetrofitInstance.compradorApiService.checkIsFavorite(userId, sellerId, productId)
            .enqueue(object : Callback<FavoritesResponse> {
                override fun onResponse(
                    call: Call<FavoritesResponse>,
                    response: Response<FavoritesResponse>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val isFavorite = when {
                            sellerId != null -> {
                                response.body()?.data?.stores?.any { it.seller_id == sellerId } == true
                            }
                            productId != null -> {
                                response.body()?.data?.products?.any { it.product_id == productId } == true
                            }
                            else -> false
                        }
                        _isFavorite.value = isFavorite
                    } else {
                        _isFavorite.value = false
                    }
                }

                override fun onFailure(call: Call<FavoritesResponse>, t: Throwable) {
                    _isLoading.value = false
                    _isFavorite.value = false
                    _favoriteStatus.value = FavoritesResponse(
                        "error",
                        "Error de conexión: ${t.message}"
                    )
                }
            })
    }

    fun toggleFavorite(userId: Int, sellerId: Int? = null, productId: Int? = null) {
        _isLoading.value = true
        val request = FavoriteRequest(userId, sellerId, productId)

        val call = if (_isFavorite.value == true) {
            RetrofitInstance.compradorApiService.removeFromFavorites(request)
        } else {
            RetrofitInstance.compradorApiService.addToFavorites(request)
        }

        call.enqueue(object : Callback<FavoritesResponse> {
            override fun onResponse(
                call: Call<FavoritesResponse>,
                response: Response<FavoritesResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _favoriteStatus.value = response.body()
                    response.body()?.let {
                        if (it.status == "success") {
                            _isFavorite.value = !(_isFavorite.value ?: false)
                        }
                    }
                } else {
                    _favoriteStatus.value = FavoritesResponse(
                        "error",
                        "Error en la respuesta del servidor"
                    )
                }
            }

            override fun onFailure(call: Call<FavoritesResponse>, t: Throwable) {
                _isLoading.value = false
                _favoriteStatus.value = FavoritesResponse(
                    "error",
                    "Error de conexión: ${t.message}"
                )
            }
        })
    }

    fun getFavorites(userId: Int, type: String? = null) {
        _isLoading.value = true
        RetrofitInstance.compradorApiService.getFavorites(userId, type)
            .enqueue(object : Callback<GetFavoritesResponse> {
                override fun onResponse(
                    call: Call<GetFavoritesResponse>,
                    response: Response<GetFavoritesResponse>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        _favorites.value = response.body()
                    } else {
                        _favorites.value = GetFavoritesResponse(
                            "error",
                            "Error en la respuesta del servidor: ${response.code()}"
                        )
                    }
                }

                override fun onFailure(call: Call<GetFavoritesResponse>, t: Throwable) {
                    _isLoading.value = false
                    _favorites.value = GetFavoritesResponse(
                        "error",
                        "Error de conexión: ${t.message}"
                    )
                }
            })
    }
}