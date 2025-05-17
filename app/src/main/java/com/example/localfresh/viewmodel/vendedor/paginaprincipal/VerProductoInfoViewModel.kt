package com.example.localfresh.viewmodel.vendedor.paginaprincipal

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.localfresh.model.vendedor.producto.GetSellerProductsResponse
import com.example.localfresh.model.vendedor.producto.InfoProductSeller
import com.example.localfresh.repository.vendedor.ProductVendedorRepository

class VerProductoInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductVendedorRepository()
    val productInfo = MutableLiveData<GetSellerProductsResponse?>()
    val errorMessage = MutableLiveData<String>()
    val deleteSuccess = MutableLiveData<Boolean>()
    val filteredProducts = MutableLiveData<List<InfoProductSeller>>()
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun getProductInfo(sellerId: Int) {
        _isLoading.value = true
        repository.getSellerProducts(sellerId, { response ->
            _isLoading.value = false
            if (response?.status == "success") {
                productInfo.value = response
                filteredProducts.value = response.data ?: emptyList()
            } else {
                errorMessage.value = response?.message ?: "Error desconocido"
            }
        }, { error ->
            _isLoading.value = false
            errorMessage.value = error
        })
    }

    fun deleteProduct(productId: Int) {
        _isLoading.value = true
        repository.deleteProduct(productId, { success ->
            _isLoading.value = false
            if (success) {
                deleteSuccess.value = true
            } else {
                errorMessage.value = "Error al eliminar el producto"
            }
        }, { error ->
            _isLoading.value = false
            errorMessage.value = error
            Log.e("VerProductoInfoViewModel", "Error deleting product: $error")
        })
    }
}
