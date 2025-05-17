package com.example.localfresh.viewmodel.vendedor.paginaprincipal

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.localfresh.model.vendedor.producto.GetSellerProductsResponse
import com.example.localfresh.model.vendedor.unidad.AddUnitRequest
import com.example.localfresh.model.vendedor.unidad.AddUnitResponse
import com.example.localfresh.repository.vendedor.UnitVendedorRepository

class AgregarUnidadViewModel : ViewModel() {

    private val repository = UnitVendedorRepository()

    val addUnitResponse: LiveData<AddUnitResponse> get() = repository.addUnitResponse
    val getSellerProductsResponse: LiveData<GetSellerProductsResponse> get() = repository.getSellerProductsResponse

    fun addUnit(request: AddUnitRequest) {
        repository.addUnit(request)
    }

    fun getSellerProducts(sellerId: Int) {
        repository.getSellerProducts(sellerId)
    }
}
