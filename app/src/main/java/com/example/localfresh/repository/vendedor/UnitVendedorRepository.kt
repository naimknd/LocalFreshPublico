package com.example.localfresh.repository.vendedor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.localfresh.model.vendedor.unidad.AddUnitRequest
import com.example.localfresh.model.vendedor.unidad.AddUnitResponse
import com.example.localfresh.model.vendedor.producto.GetSellerProductsResponse
import com.example.localfresh.model.vendedor.unidad.DeleteUnitResponse
import com.example.localfresh.model.vendedor.unidad.GetUnitsProductSellerResponse
import com.example.localfresh.model.vendedor.unidad.EditUnitRequest
import com.example.localfresh.model.vendedor.unidad.EditUnitResponse
import com.example.localfresh.model.vendedor.unidad.GetUnitResponse
import com.example.localfresh.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UnitVendedorRepository {

    private val _addUnitResponse = MutableLiveData<AddUnitResponse>()
    val addUnitResponse: LiveData<AddUnitResponse> get() = _addUnitResponse

    private val _getSellerProductsResponse = MutableLiveData<GetSellerProductsResponse>()
    val getSellerProductsResponse: LiveData<GetSellerProductsResponse> get() = _getSellerProductsResponse

    private val _getUnitsProductResponse = MutableLiveData<GetUnitsProductSellerResponse>()
    val getUnitsProductResponse: LiveData<GetUnitsProductSellerResponse> get() = _getUnitsProductResponse

    private val _editUnitResponse = MutableLiveData<EditUnitResponse>()
    val editUnitResponse: LiveData<EditUnitResponse> get() = _editUnitResponse

    private val _getUnitResponse = MutableLiveData<GetUnitResponse>()
    val getUnitResponse: LiveData<GetUnitResponse> get() = _getUnitResponse

    private val _deleteUnitResponse = MutableLiveData<DeleteUnitResponse>()
    val deleteUnitResponse: LiveData<DeleteUnitResponse> get() = _deleteUnitResponse

    fun addUnit(request: AddUnitRequest) {
        val call = RetrofitInstance.vendedorApiService.addUnit(request)
        call.enqueue(object : Callback<AddUnitResponse> {
            override fun onResponse(call: Call<AddUnitResponse>, response: Response<AddUnitResponse>) {
                if (response.isSuccessful) {
                    _addUnitResponse.value = response.body()
                } else {
                    _addUnitResponse.value = AddUnitResponse("error", "Error al añadir la unidad del producto")
                }
            }

            override fun onFailure(call: Call<AddUnitResponse>, t: Throwable) {
                _addUnitResponse.value = AddUnitResponse("error", "Error de red: ${t.message}")
            }
        })
    }

    fun getSellerProducts(sellerId: Int) {
        val call = RetrofitInstance.vendedorApiService.getSellerProducts(sellerId)
        call.enqueue(object : Callback<GetSellerProductsResponse> {
            override fun onResponse(call: Call<GetSellerProductsResponse>, response: Response<GetSellerProductsResponse>) {
                if (response.isSuccessful) {
                    _getSellerProductsResponse.value = response.body()
                } else {
                    _getSellerProductsResponse.value = GetSellerProductsResponse("error", "Error al obtener productos", null)
                }
            }

            override fun onFailure(call: Call<GetSellerProductsResponse>, t: Throwable) {
                _getSellerProductsResponse.value = GetSellerProductsResponse("error", "Error de red: ${t.message}", null)
            }
        })
    }

    fun getUnitsProduct(productId: Int) {
        val call = RetrofitInstance.vendedorApiService.getUnitsProduct(productId)
        call.enqueue(object : Callback<GetUnitsProductSellerResponse> {
            override fun onResponse(call: Call<GetUnitsProductSellerResponse>, response: Response<GetUnitsProductSellerResponse>) {
                if (response.isSuccessful) {
                    _getUnitsProductResponse.value = response.body()
                } else {
                    _getUnitsProductResponse.value = GetUnitsProductSellerResponse("error", null, null, "Error al obtener las unidades y la información del producto")
                }
            }

            override fun onFailure(call: Call<GetUnitsProductSellerResponse>, t: Throwable) {
                _getUnitsProductResponse.value = GetUnitsProductSellerResponse("error", null, null, "Error de red: ${t.message}")
            }
        })
    }

    fun editUnit(request: EditUnitRequest) {
        val call = RetrofitInstance.vendedorApiService.editUnit(request)
        call.enqueue(object : Callback<EditUnitResponse> {
            override fun onResponse(call: Call<EditUnitResponse>, response: Response<EditUnitResponse>) {
                if (response.isSuccessful) {
                    _editUnitResponse.value = response.body()
                } else {
                    _editUnitResponse.value = EditUnitResponse("error", "Error al editar la unidad del producto")
                }
            }

            override fun onFailure(call: Call<EditUnitResponse>, t: Throwable) {
                _editUnitResponse.value = EditUnitResponse("error", "Error de red: ${t.message}")
            }
        })
    }

    fun getUnit(unitId: Int) {
        val call = RetrofitInstance.vendedorApiService.getUnit(unitId)
        call.enqueue(object : Callback<GetUnitResponse> {
            override fun onResponse(call: Call<GetUnitResponse>, response: Response<GetUnitResponse>) {
                if (response.isSuccessful) {
                    _getUnitResponse.value = response.body()
                } else {
                    _getUnitResponse.value = GetUnitResponse("error", null, "Error al obtener los datos de la unidad")
                }
            }

            override fun onFailure(call: Call<GetUnitResponse>, t: Throwable) {
                _getUnitResponse.value = GetUnitResponse("error", null, "Error de red: ${t.message}")
            }
        })
    }

    fun deleteUnit(unitId: Int) {
        val call = RetrofitInstance.vendedorApiService.deleteUnit(unitId)
        call.enqueue(object : Callback<DeleteUnitResponse> {
            override fun onResponse(call: Call<DeleteUnitResponse>, response: Response<DeleteUnitResponse>) {
                if (response.isSuccessful) {
                    _deleteUnitResponse.value = response.body()
                } else {
                    _deleteUnitResponse.value = DeleteUnitResponse("error", "Error al eliminar la unidad")
                }
            }

            override fun onFailure(call: Call<DeleteUnitResponse>, t: Throwable) {
                _deleteUnitResponse.value = DeleteUnitResponse("error", "Error de red: ${t.message}")
            }
        })
    }

}
