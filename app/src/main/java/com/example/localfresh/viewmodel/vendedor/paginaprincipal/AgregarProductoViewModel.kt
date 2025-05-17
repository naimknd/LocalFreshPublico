package com.example.localfresh.viewmodel.vendedor.paginaprincipal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.localfresh.model.vendedor.producto.AddProductRequest
import com.example.localfresh.repository.vendedor.ProductVendedorRepository

class AgregarProductoViewModel : ViewModel() {
    private val repository = ProductVendedorRepository()
    val addProductSuccess = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    fun addProduct(productRequest: AddProductRequest, token: String) {
        repository.addProduct(token, productRequest, { response ->
            if (response != null && response.status == "success") {
                addProductSuccess.value = true
            } else {
                errorMessage.value = response?.message ?: "Error desconocido"
            }
        }, { error ->
            errorMessage.value = error
        })
    }
}