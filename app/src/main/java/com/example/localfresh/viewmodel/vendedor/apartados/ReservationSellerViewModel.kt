package com.example.localfresh.viewmodel.vendedor.apartados

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localfresh.model.vendedor.apartados.MarkReservationCompleteResponse
import com.example.localfresh.model.vendedor.apartados.ReservationDetailSellerResponse
import com.example.localfresh.model.vendedor.apartados.ReservationListSellerResponse
import com.example.localfresh.repository.vendedor.ReservationSellerRepository
import kotlinx.coroutines.launch

class ReservationSellerViewModel : ViewModel() {
    private val repository = ReservationSellerRepository()

    private val _reservationsListState = MutableLiveData<ReservationsListSellerState>()
    val reservationsListState: LiveData<ReservationsListSellerState> = _reservationsListState

    private val _reservationDetailState = MutableLiveData<ReservationDetailSellerState>()
    val reservationDetailState: LiveData<ReservationDetailSellerState> = _reservationDetailState

    private val _markCompleteState = MutableLiveData<MarkCompleteState>()
    val markCompleteState: LiveData<MarkCompleteState> = _markCompleteState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getSellerReservations(
        sellerId: Int,
        status: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        buyerSearch: String? = null,
        isHistory: Boolean = false
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getSellerReservations(
                    sellerId, status, dateFrom, dateTo, buyerSearch, isHistory
                )
                result.fold(
                    onSuccess = { response ->
                        _reservationsListState.postValue(ReservationsListSellerState.Success(response))
                    },
                    onFailure = { error ->
                        _reservationsListState.postValue(
                            ReservationsListSellerState.Error(error.message ?: "Error desconocido")
                        )
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getSellerReservationDetails(reservationId: Int, sellerId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getSellerReservationDetails(reservationId, sellerId)
                result.fold(
                    onSuccess = { response ->
                        _reservationDetailState.postValue(ReservationDetailSellerState.Success(response))
                    },
                    onFailure = { error ->
                        _reservationDetailState.postValue(ReservationDetailSellerState.Error(error.message ?: "Error desconocido"))
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markReservationComplete(reservationId: Int, sellerId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.markReservationComplete(reservationId, sellerId)
                result.fold(
                    onSuccess = { response ->
                        _markCompleteState.postValue(MarkCompleteState.Success(response))
                    },
                    onFailure = { error ->
                        _markCompleteState.postValue(MarkCompleteState.Error(error.message ?: "Error desconocido"))
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}

sealed class ReservationsListSellerState {
    data class Success(val response: ReservationListSellerResponse) : ReservationsListSellerState()
    data class Error(val message: String) : ReservationsListSellerState()
    object Loading : ReservationsListSellerState()
}

sealed class ReservationDetailSellerState {
    data class Success(val response: ReservationDetailSellerResponse) : ReservationDetailSellerState()
    data class Error(val message: String) : ReservationDetailSellerState()
}

sealed class MarkCompleteState {
    data class Success(val response: MarkReservationCompleteResponse) : MarkCompleteState()
    data class Error(val message: String) : MarkCompleteState()
}