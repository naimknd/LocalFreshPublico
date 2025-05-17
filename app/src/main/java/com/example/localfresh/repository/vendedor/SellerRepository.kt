package com.example.localfresh.repository.vendedor

import com.example.localfresh.model.vendedor.paginaprincipal.GetSellerInfoResponse
import com.example.localfresh.model.vendedor.paginaprincipal.UpdateSellerInfoRequest
import com.example.localfresh.model.vendedor.paginaprincipal.UpdateSellerInfoResponse
import com.example.localfresh.network.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SellerRepository {

    // Metodo para obtener la información del vendedor
    fun getSellerInfo(sellerId: Int, onResult: (GetSellerInfoResponse?) -> Unit, onError: (String) -> Unit) {
        RetrofitInstance.vendedorApiService.getSellerInfo(sellerId).enqueue(object : Callback<GetSellerInfoResponse> {
            override fun onResponse(call: Call<GetSellerInfoResponse>, response: Response<GetSellerInfoResponse>) {
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onError("Error al obtener información del vendedor")
                }
            }

            override fun onFailure(call: Call<GetSellerInfoResponse>, t: Throwable) {
                onError("Fallo de conexión: ${t.message}")
            }
        })
    }

    // Metodo para actualizar la información del vendedor
    fun updateSellerInfo(sellerInfo: UpdateSellerInfoRequest, onResult: (UpdateSellerInfoResponse?) -> Unit, onError: (String) -> Unit) {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

        // Añadir los campos de texto
        builder.addFormDataPart("seller_id", sellerInfo.seller_id.toString())
        sellerInfo.store_name?.let { builder.addFormDataPart("store_name", it) }
        sellerInfo.store_description?.let { builder.addFormDataPart("store_description", it) }
        sellerInfo.store_phone?.let { builder.addFormDataPart("store_phone", it) }
        sellerInfo.store_address?.let { builder.addFormDataPart("store_address", it) }
        sellerInfo.opening_time?.let { builder.addFormDataPart("opening_time", it) }
        sellerInfo.closing_time?.let { builder.addFormDataPart("closing_time", it) }

        // Añadir latitud y longitud
        builder.addFormDataPart("latitude", sellerInfo.latitude?.toString() ?: "0.0") // Asegúrate de que no sea nulo
        builder.addFormDataPart("longitude", sellerInfo.longitude?.toString() ?: "0.0") // Asegúrate de que no sea nulo

        // Añadir la imagen si existe
        sellerInfo.store_logo?.let { file ->
            val requestFile = file.asRequestBody("image/*".toMediaType()) // Usar la función de extensión
            builder.addFormDataPart("store_logo", file.name, requestFile)
        }

        val requestBody = builder.build()

        RetrofitInstance.vendedorApiService.updateSellerInfoWithImage(requestBody)
            .enqueue(object : Callback<UpdateSellerInfoResponse> {
                override fun onResponse(call: Call<UpdateSellerInfoResponse>, response: Response<UpdateSellerInfoResponse>) {
                    if (response.isSuccessful) {
                        onResult(response.body())
                    } else {
                        onError("Error al actualizar la información")
                    }
                }

                override fun onFailure(call: Call<UpdateSellerInfoResponse>, t: Throwable) {
                    onError("Fallo de conexión: ${t.message}")
                }
            })
    }
}
