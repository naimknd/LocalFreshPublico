package com.example.localfresh.services

import android.util.Log
import com.example.localfresh.utils.NotificationHelper
import com.example.localfresh.utils.TokenManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class LocalFreshMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "FCM_Service"
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensaje recibido desde: ${remoteMessage.from}")

        // Delegar la gestión de notificaciones al NotificationHelper
        NotificationHelper.getInstance(this).handleRemoteMessage(remoteMessage)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM recibido: $token")

        // Delegar la gestión de tokens al TokenManager
        TokenManager.getInstance(this).handleNewToken(token)
    }
}
