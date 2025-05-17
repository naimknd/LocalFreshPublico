package com.example.localfresh.viewmodel.comprador.paginaprincipal

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localfresh.model.comprador.detalle.DetalleUnidad
import com.example.localfresh.model.comprador.detalle.DetalleUnidadResponse
import com.example.localfresh.model.comprador.paginaprincipal.ProductoData
import com.example.localfresh.model.comprador.paginaprincipal.ProductosResponse
import com.example.localfresh.model.comprador.paginaprincipal.TiendaData
import com.example.localfresh.model.comprador.paginaprincipal.TiendaResponse
import com.example.localfresh.network.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TiendaInfoViewModel : ViewModel() {
    private val _tiendaData = MutableLiveData<TiendaData>()
    val tiendaData: LiveData<TiendaData> get() = _tiendaData

    private val _productosData = MutableLiveData<List<ProductoData>>()
    val productosData: LiveData<List<ProductoData>> get() = _productosData

    private val _detalleUnidad = MutableLiveData<DetalleUnidad>()
    val detalleUnidad: LiveData<DetalleUnidad> = _detalleUnidad

    fun obtenerTiendaData(sellerId: Int) {
        viewModelScope.launch {
            RetrofitInstance.compradorApiService.obtenerInfoTienda(sellerId)
                .enqueue(object : Callback<TiendaResponse> {
                    override fun onResponse(
                        call: Call<TiendaResponse>,
                        response: Response<TiendaResponse>
                    ) {
                        if (response.isSuccessful) {
                            _tiendaData.value = response.body()?.data
                            Log.d("TiendaInfoViewModel", "TiendaData recibida: ${response.body()?.data}")
                        } else {
                            Log.e("TiendaInfoViewModel", "Error en la respuesta: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<TiendaResponse>, t: Throwable) {
                        Log.e("TiendaInfoViewModel", "Fallo en la llamada: ${t.message}")
                    }
                })
        }
    }

    fun obtenerProductosTienda(sellerId: Int) {
        viewModelScope.launch {
            RetrofitInstance.compradorApiService.obtenerProductosTienda(sellerId)
                .enqueue(object : Callback<ProductosResponse> {
                    override fun onResponse(
                        call: Call<ProductosResponse>,
                        response: Response<ProductosResponse>
                    ) {
                        if (response.isSuccessful) {
                            _productosData.value = response.body()?.productos
                            Log.d("TiendaInfoViewModel", "Productos recibidos: ${response.body()?.productos}")
                        } else {
                            Log.e("TiendaInfoViewModel", "Error en la respuesta: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<ProductosResponse>, t: Throwable) {
                        Log.e("TiendaInfoViewModel", "Fallo en la llamada: ${t.message}")
                    }
                })
        }
    }

    fun obtenerDetalleUnidad(unitId: Int) {
        viewModelScope.launch {
            RetrofitInstance.compradorApiService.obtenerDetalleUnidad(unitId)
                .enqueue(object : Callback<DetalleUnidadResponse> {
                    override fun onResponse(
                        call: Call<DetalleUnidadResponse>,
                        response: Response<DetalleUnidadResponse>
                    ) {
                        if (response.isSuccessful) {
                            _detalleUnidad.value = response.body()?.data?.unit
                            Log.d("TiendaInfoViewModel", "Detalle recibido: ${response.body()?.data?.unit}")
                        } else {
                            Log.e("TiendaInfoViewModel", "Error en la respuesta: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<DetalleUnidadResponse>, t: Throwable) {
                        Log.e("TiendaInfoViewModel", "Error al obtener detalle: ${t.message}")
                    }
                })
        }
    }
}