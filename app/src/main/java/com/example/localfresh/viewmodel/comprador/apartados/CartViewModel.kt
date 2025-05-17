package com.example.localfresh.viewmodel.comprador.apartados

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localfresh.model.comprador.apartados.AddToCartResponse
import com.example.localfresh.model.comprador.apartados.CartResponse
import com.example.localfresh.repository.comprador.CartRepository
import com.example.localfresh.model.comprador.apartados.CartItem
import com.example.localfresh.model.comprador.apartados.RemoveFromCartResponse
import com.example.localfresh.utils.Event
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {
    private val repository = CartRepository()

    private val _cartState = MutableLiveData<CartState>()
    val cartState: LiveData<CartState> = _cartState

    private val _addToCartResult = MutableLiveData<Event<Result<AddToCartResponse>>>()
    val addToCartResult: LiveData<Event<Result<AddToCartResponse>>> = _addToCartResult

    private val _removeFromCartResult = MutableLiveData<Result<RemoveFromCartResponse>>()
    val removeFromCartResult: LiveData<Result<RemoveFromCartResponse>> = _removeFromCartResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    fun getCart(userId: Int, cartId: Int? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getCart(userId, cartId)
                result.fold(
                    onSuccess = { response ->
                        _cartState.postValue(CartState.Success(response))
                        _cartItems.postValue(response.items)

                        //  Actualizar el total de items en el carrito
                        _cartItemCount.postValue(response.cart?.total_items ?: 0)
                    },
                    onFailure = { error ->
                        _cartState.postValue(CartState.Error(error.message ?: "Error desconocido"))
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToCart(userId: Int, unitId: Int, quantity: Int = 1) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.addToCart(userId, unitId, quantity)
                _addToCartResult.postValue(Event(result)) // Encapsular en Event
                if (result.isSuccess) {
                    getCart(userId)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeFromCart(userId: Int, unitId: Int, quantity: Int? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.removeFromCart(userId, unitId, quantity)
                _removeFromCartResult.postValue(result)
                result.fold(
                    onSuccess = { response ->
                        if (response.items_remaining == 0) {
                            _cartState.postValue(CartState.Success(
                                CartResponse(
                                    status = "success",
                                    message = "El usuario no tiene un carrito activo",
                                    cart = null,
                                    items = emptyList()
                                )
                            ))
                            _cartItems.postValue(emptyList())
                        } else {
                            getCart(userId)
                        }
                    },
                    onFailure = { error ->
                        _cartState.postValue(CartState.Error(error.message ?: "Error desconocido"))
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Metodo para obtener el total de items en el carrito
    private val _cartItemCount = MutableLiveData<Int>()
    val cartItemCount: LiveData<Int> = _cartItemCount

    // Method to get cart item count
    fun getCartItemCount(userId: Int) {
        viewModelScope.launch {
            try {
                val result = repository.getCart(userId)
                result.fold(
                    onSuccess = { response ->
                        // Calculate total items in cart
                        val count = response.cart?.total_items ?: 0
                        _cartItemCount.postValue(count)
                    },
                    onFailure = { error ->
                        // On error, set count to 0
                        _cartItemCount.postValue(0)
                        Log.e("CartViewModel", "Error fetching cart count: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _cartItemCount.postValue(0)
                Log.e("CartViewModel", "Exception fetching cart count: ${e.message}")
            }
        }
    }

    fun refreshCart(userId: Int) {
        getCart(userId)
    }

    fun clearCart() {
        _cartState.value = CartState.Success(
            CartResponse(
                status = "success",
                message = "El usuario no tiene un carrito activo",
                cart = null,
                items = emptyList()
            )
        )
        _cartItems.value = emptyList()
    }
}

sealed class CartState {
    data class Success(val response: CartResponse) : CartState()
    data class Error(val message: String) : CartState()
}
