package com.example.localfresh.repository.comprador

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.localfresh.model.comprador.perfil.UpdateUserProfileRequest
import com.example.localfresh.model.comprador.perfil.UpdateUserProfileResponse
import com.example.localfresh.model.comprador.perfil.UserProfileResponse
import com.example.localfresh.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserProfileRepository {
    private val _userProfile = MutableLiveData<UserProfileResponse?>()
    val userProfile: LiveData<UserProfileResponse?> = _userProfile

    private val _updateResult = MutableLiveData<Result<String>>()
    val updateResult: LiveData<Result<String>> = _updateResult

    fun fetchUserProfile(userId: Int) {
        Log.d("UserProfileRepository", "Fetching profile for userId: $userId")

        RetrofitInstance.compradorApiService.getUserProfile(userId).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { userResponse ->
                        Log.d("UserProfileRepository", "Success: ${userResponse.user.username}")
                        _userProfile.postValue(userResponse)
                    }
                } else {
                    Log.e("UserProfileRepository", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                    _userProfile.postValue(null)
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Log.e("UserProfileRepository", "Network error", t)
                _userProfile.postValue(null)
            }
        })
    }

    fun updateUserProfile(request: UpdateUserProfileRequest) {
        RetrofitInstance.compradorApiService.updateUserProfile(request)
            .enqueue(object : Callback<UpdateUserProfileResponse> {
                override fun onResponse(call: Call<UpdateUserProfileResponse>, response: Response<UpdateUserProfileResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { updateResponse ->
                            _updateResult.postValue(Result.success(updateResponse.status))
                            // Si actualizamos correctamente, también actualizamos el perfil local
                            fetchUserProfile(request.user_id)
                        } ?: _updateResult.postValue(Result.failure(Exception("Respuesta vacía")))
                    } else {
                        _updateResult.postValue(Result.failure(Exception("Error: ${response.code()}")))
                    }
                }

                override fun onFailure(call: Call<UpdateUserProfileResponse>, t: Throwable) {
                    _updateResult.postValue(Result.failure(t))
                }
            })
    }
}