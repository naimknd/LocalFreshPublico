package com.example.localfresh.viewmodel.vendedor.paginaprincipal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.localfresh.model.vendedor.paginaprincipal.GetSellerInfoResponse
import com.example.localfresh.model.vendedor.paginaprincipal.UpdateSellerInfoRequest
import com.example.localfresh.repository.vendedor.SellerRepository

class EditarInformacionTiendaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SellerRepository()
    val sellerInfo = MutableLiveData<GetSellerInfoResponse?>()
    val updateSuccess = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    // LiveData para manejar mensajes informativos
    val infoMessage = MutableLiveData<String>()

    // Metodo para obtener la información del vendedor
    fun fetchSellerInfo(sellerId: Int) {
        repository.getSellerInfo(sellerId, { response ->
            sellerInfo.value = response // Asignar la respuesta a LiveData
        }, { error ->
            errorMessage.value = error
        })
    }

    // Metodo para actualizar la información del vendedor
    fun updateSellerInfo(sellerInfo: UpdateSellerInfoRequest) {
        repository.updateSellerInfo(sellerInfo, { response ->
            if (response != null) {
                when (response.status) {
                    "success" -> {
                        updateSuccess.value = true // Indica que la actualización fue exitosa
                    }
                    "info" -> {
                        infoMessage.value = "No se realizaron cambios en la información de la tienda." // Mensaje informativo
                    }
                    "error" -> {
                        errorMessage.value = "Error al actualizar la información: ${response.message}"
                    }
                }
            } else {
                errorMessage.value = "Respuesta nula del servidor."
            }
        }, { error ->
            errorMessage.value = error
        })
    }
}
