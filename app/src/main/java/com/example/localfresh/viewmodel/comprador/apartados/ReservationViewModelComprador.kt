package com.example.localfresh.viewmodel.comprador.apartados

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.localfresh.model.comprador.apartados.CancelReservationRequest
import com.example.localfresh.model.comprador.apartados.CancelReservationResponse
import com.example.localfresh.model.comprador.apartados.ReservationDetailResponse
import com.example.localfresh.model.comprador.apartados.ReservationListResponse
import com.example.localfresh.model.comprador.apartados.ReservationResponse
import com.example.localfresh.repository.comprador.ReservationRepositoryComprador
import kotlinx.coroutines.launch

class ReservationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReservationRepositoryComprador()

    private val _reservationState = MutableLiveData<ReservationState>()
    val reservationState: LiveData<ReservationState> = _reservationState

    private val _reservationsListState = MutableLiveData<ReservationsListState>()
    val reservationsListState: LiveData<ReservationsListState> = _reservationsListState

    private val _reservationDetailState = MutableLiveData<ReservationDetailState>()
    val reservationDetailState: LiveData<ReservationDetailState> = _reservationDetailState

    private val _cancelReservationState = MutableLiveData<CancelReservationState>()
    val cancelReservationState: LiveData<CancelReservationState> = _cancelReservationState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun confirmReservation(userId: Int, cartId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.confirmReservation(userId, cartId)
                result.fold(
                    onSuccess = { response ->
                        _reservationState.postValue(ReservationState.Success(response))
                    },
                    onFailure = { error ->
                        _reservationState.postValue(ReservationState.Error(error.message ?: "Error desconocido"))
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getReservations(
        userId: Int,
        status: String? = null,
        sortBy: String = "reservation_date",
        sortDirection: String = "DESC",
        isHistory: Boolean = false
    ) {
        _reservationsListState.value = ReservationsListState.Loading
        viewModelScope.launch {
            try {
                val result = repository.getReservations(
                    userId = userId,
                    status = status,
                    sortBy = sortBy,
                    sortDirection = sortDirection,
                    history = if (isHistory) "true" else null
                )
                result.fold(
                    onSuccess = { response ->
                        _reservationsListState.postValue(ReservationsListState.Success(response))
                    },
                    onFailure = { error ->
                        _reservationsListState.postValue(ReservationsListState.Error(error.message ?: "Error desconocido"))
                    }
                )
            } catch (e: Exception) {
                _reservationsListState.postValue(ReservationsListState.Error(e.message ?: "Error desconocido"))
            }
        }
    }

    fun getReservationDetails(reservationId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getReservationDetails(reservationId)
                result.fold(
                    onSuccess = { response ->
                        _reservationDetailState.postValue(ReservationDetailState.Success(response))
                    },
                    onFailure = { error ->
                        _reservationDetailState.postValue(ReservationDetailState.Error(error.message ?: "Error desconocido"))
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelReservation(reservationId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userId = getUserIdFromPreferences()
                if (userId != -1) {
                    val result = repository.cancelReservation(userId, reservationId)
                    result.fold(
                        onSuccess = { response ->
                            _cancelReservationState.postValue(CancelReservationState.Success(response))
                        },
                        onFailure = { error ->
                            _cancelReservationState.postValue(CancelReservationState.Error(error.message ?: "Error desconocido"))
                        }
                    )
                } else {
                    _cancelReservationState.postValue(CancelReservationState.Error("No se pudo obtener el ID de usuario"))
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getUserIdFromPreferences(): Int {
        return getApplication<Application>().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            .getInt("USER_ID", -1)
    }
}

sealed class ReservationState {
    data class Success(val response: ReservationResponse) : ReservationState()
    data class Error(val message: String) : ReservationState()
}

sealed class ReservationsListState {
    data class Success(val response: ReservationListResponse) : ReservationsListState()
    data class Error(val message: String) : ReservationsListState()
    object Loading : ReservationsListState()
}

sealed class ReservationDetailState {
    data class Success(val response: ReservationDetailResponse) : ReservationDetailState()
    data class Error(val message: String) : ReservationDetailState()
}

sealed class CancelReservationState {
    data class Success(val response: CancelReservationResponse) : CancelReservationState()
    data class Error(val message: String) : CancelReservationState()
}