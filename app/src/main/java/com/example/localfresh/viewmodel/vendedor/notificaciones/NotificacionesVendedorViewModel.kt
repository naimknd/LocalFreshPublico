package com.example.localfresh.viewmodel.vendedor.notificaciones

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.localfresh.model.vendedor.notificaciones.SellerNotification
import com.example.localfresh.model.vendedor.notificaciones.SellerNotificationsResponse
import com.example.localfresh.repository.vendedor.NotificationVendedorRepository
import kotlinx.coroutines.launch

class NotificacionesVendedorViewModel(application: Application) : AndroidViewModel(application) {
    val notificaciones = MutableLiveData<List<SellerNotification>>(emptyList())
    val unreadCount = MutableLiveData<Int>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()
    var offset = 0
    var limit = 10
    var allLoaded = false
    var currentType: String? = null
    var currentIsRead: Int? = null
    var currentSortBy: String = "created_at"
    var currentSortDirection: String = "DESC"

    private val repository = NotificationVendedorRepository()

    fun loadNotifications(
        reset: Boolean = false,
        type: String? = null,
        isRead: Int? = null,
        sortBy: String = currentSortBy,
        sortDirection: String = currentSortDirection
    ) {
        if (isLoading.value == true) return
        isLoading.value = true

        if (reset) {
            offset = 0
            allLoaded = false
            notificaciones.value = emptyList()
            currentType = type
            currentIsRead = isRead
        }

        val sellerId = getSellerIdFromPrefs()
        if (sellerId == -1) {
            isLoading.value = false
            error.value = "No se pudo obtener el ID del vendedor"
            return
        }

        viewModelScope.launch {
            val result = repository.getSellerNotifications(
                sellerId = sellerId,
                limit = limit,
                offset = offset,
                type = type,
                isRead = isRead,
                sortBy = sortBy,
                sortDirection = sortDirection
            )
            isLoading.value = false
            result.fold(
                onSuccess = { response ->
                    unreadCount.value = response.unread_count
                    val currentList = if (reset) emptyList() else notificaciones.value ?: emptyList()
                    val newList = currentList + response.notifications
                    notificaciones.value = newList
                    if (response.notifications.size < limit) allLoaded = true
                },
                onFailure = { error.value = it.message }
            )
        }
    }

    fun loadNextPage() {
        if (!allLoaded && isLoading.value != true) {
            offset += limit
            loadNotifications(reset = false)
        }
    }

    fun refresh() {
        loadNotifications(reset = true, type = currentType, isRead = currentIsRead)
    }

    private fun getSellerIdFromPrefs(): Int {
        val prefs = getApplication<Application>().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
        return prefs.getInt("SELLER_ID", -1)
    }
}