package com.example.localfresh.viewmodel.vendedor.paginaprincipal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.localfresh.model.vendedor.paginaprincipal.GetSellerInfoResponse
import com.example.localfresh.repository.vendedor.SellerRepository

class PaginaPrincipalVendedorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SellerRepository()
    val sellerInfo = MutableLiveData<GetSellerInfoResponse>()
    val errorMessage = MutableLiveData<String>()
    val isLoading = MutableLiveData<Boolean>()

    fun fetchSellerInfo(sellerId: Int) {
        isLoading.value = true
        repository.getSellerInfo(sellerId, { response ->
            isLoading.value = false
            response?.let {
                sellerInfo.value = it
            } ?: run {
                errorMessage.value = "No se encontró información del vendedor"
            }
        }, { error ->
            isLoading.value = false
            errorMessage.value = error
        })
    }
}