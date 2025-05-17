package com.example.localfresh.viewmodel.vendedor.paginaprincipal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localfresh.model.vendedor.producto.EditProductRequest
import com.example.localfresh.model.vendedor.producto.EditProductResponse
import com.example.localfresh.model.vendedor.producto.GetProductResponse
import com.example.localfresh.repository.vendedor.ProductVendedorRepository
import kotlinx.coroutines.launch

class EditarProductoViewModel(private val repository: ProductVendedorRepository = ProductVendedorRepository()) : ViewModel() {

    private val _productInfo = MutableLiveData<GetProductResponse?>()
    val productInfo: LiveData<GetProductResponse?> get() = _productInfo

    private val _editProductResponse = MutableLiveData<EditProductResponse?>()
    val editProductResponse: LiveData<EditProductResponse?> get() = _editProductResponse

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var sellerId: Int? = null

    fun loadProductInfo(productId: Int) {
        viewModelScope.launch {
            repository.getProduct(productId, { response ->
                response?.data?.let {
                    sellerId = it.seller_id
                }
                _productInfo.value = response
            }, { error ->
                _errorMessage.value = error
            })
        }
    }

    fun editProduct(editProductRequest: EditProductRequest) {
        viewModelScope.launch {
            val request = editProductRequest.copy(seller_id = sellerId ?: 0)
            repository.editProduct(request, { response ->
                _editProductResponse.value = response
            }, { error ->
                _errorMessage.value = error
            })
        }
    }
}
