package com.example.localfresh.viewmodel.vendedor.paginaprincipal

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.localfresh.model.vendedor.unidad.EditUnitRequest
import com.example.localfresh.model.vendedor.unidad.EditUnitResponse
import com.example.localfresh.model.vendedor.unidad.GetUnitResponse
import com.example.localfresh.repository.vendedor.UnitVendedorRepository

class EditarUnidadViewModel : ViewModel() {

    private val repository = UnitVendedorRepository()

    val editUnitResponse: LiveData<EditUnitResponse> get() = repository.editUnitResponse
    val getUnitResponse: LiveData<GetUnitResponse> get() = repository.getUnitResponse

    fun editUnit(request: EditUnitRequest) {
        repository.editUnit(request)
    }

    fun getUnit(unitId: Int) {
        repository.getUnit(unitId)
    }
}