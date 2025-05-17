package com.example.localfresh.model.comprador.notificaciones

data class NotificationPreferences(
    val user_id: Int,
    val frequency: String = "diaria",
    val notif_ofertas: Int = 1,
    val notif_nuevos_productos: Int = 1,
    val notif_recordatorios: Int = 1
)

data class NotificationPreferencesRequest(
    val user_id: Int,
    val frequency: String,
    val notif_ofertas: Int,
    val notif_nuevos_productos: Int,
    val notif_recordatorios: Int
)

data class NotificationPreferencesResponse(
    val status: String,
    val message: String? = null,
    val preferences: NotificationPreferences? = null
)