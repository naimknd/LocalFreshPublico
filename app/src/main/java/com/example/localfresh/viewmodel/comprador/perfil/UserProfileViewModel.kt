package com.example.localfresh.viewmodel.comprador.perfil

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localfresh.model.comprador.perfil.UpdateUserProfileRequest
import com.example.localfresh.model.comprador.perfil.UserProfileResponse
import com.example.localfresh.repository.comprador.UserProfileRepository
import com.example.localfresh.utils.Event
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {
    private val repository = UserProfileRepository()
    val userProfile: LiveData<UserProfileResponse?> = repository.userProfile

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isUpdating = MutableLiveData<Boolean>()
    val isUpdating: LiveData<Boolean> = _isUpdating

    private val _updateSuccess = MutableLiveData<Event<String>>()
    val updateSuccess: LiveData<Event<String>> = _updateSuccess

    private val _updateError = MutableLiveData<Event<String>>()
    val updateError: LiveData<Event<String>> = _updateError

    fun fetchUserProfile(userId: Int) {
        Log.d("UserProfileViewModel", "Fetching profile for user: $userId")
        _isLoading.value = true
        repository.fetchUserProfile(userId)
    }

    // Add this method to handle errors in the repository if needed
    fun setError(message: String) {
        _errorMessage.value = message
        _isLoading.value = false
    }

    fun updateUserProfile(request: UpdateUserProfileRequest) {
        _isUpdating.value = true
        repository.updateUserProfile(request)

        val observer = object : Observer<Result<String>> {
            override fun onChanged(t: Result<String>) {
                t.fold(
                    onSuccess = { status ->
                        _updateSuccess.value = Event(status)
                        _isUpdating.value = false
                        repository.updateResult.removeObserver(this)
                    },
                    onFailure = { exception ->
                        _updateError.value = Event(exception.message ?: "Error desconocido")
                        _isUpdating.value = false
                        repository.updateResult.removeObserver(this)
                    }
                )
            }
        }

        repository.updateResult.observeForever(observer)
    }
}