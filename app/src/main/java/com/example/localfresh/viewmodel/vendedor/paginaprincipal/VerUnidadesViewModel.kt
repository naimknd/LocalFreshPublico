package com.example.localfresh.viewmodel.vendedor.paginaprincipal

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.localfresh.model.vendedor.unidad.DeleteUnitResponse
import com.example.localfresh.model.vendedor.unidad.GetUnitsProductSellerResponse
import com.example.localfresh.repository.vendedor.UnitVendedorRepository

class VerUnidadesViewModel : ViewModel() {

    private val repository = UnitVendedorRepository()

    val getUnitsProductResponse: LiveData<GetUnitsProductSellerResponse> get() = repository.getUnitsProductResponse
    val deleteUnitResponse: LiveData<DeleteUnitResponse> get() = repository.deleteUnitResponse

    fun getUnitsProduct(productId: Int) {
        repository.getUnitsProduct(productId)
    }

    fun deleteUnit(unitId: Int) {
        repository.deleteUnit(unitId)
    }
}
