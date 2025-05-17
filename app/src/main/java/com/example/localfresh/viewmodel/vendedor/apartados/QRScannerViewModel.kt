package com.example.localfresh.viewmodel.vendedor.apartados

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localfresh.model.vendedor.apartados.QRVerificationResponse
import com.example.localfresh.repository.vendedor.ReservationSellerRepository
import kotlinx.coroutines.launch

class QRScannerViewModel : ViewModel() {
    private val repository = ReservationSellerRepository()

    private val _qrVerificationState = MutableLiveData<QRVerificationState>()
    val qrVerificationState: LiveData<QRVerificationState> = _qrVerificationState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun verifyReservationQR(qrCode: String, sellerId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.verifyReservationQR(qrCode, sellerId)
                result.fold(
                    onSuccess = { response ->
                        _qrVerificationState.postValue(QRVerificationState.Success(response))
                    },
                    onFailure = { error ->
                        Log.e("QRScannerViewModel", "Error verifying QR: ${error.message}")
                        _qrVerificationState.postValue(QRVerificationState.Error(error.message ?: "Error desconocido"))
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}

sealed class QRVerificationState {
    data class Success(val response: QRVerificationResponse) : QRVerificationState()
    data class Error(val message: String) : QRVerificationState()
}
