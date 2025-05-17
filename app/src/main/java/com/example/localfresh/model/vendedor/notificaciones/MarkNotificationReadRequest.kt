package com.example.localfresh.model.vendedor.notificaciones

data class MarkNotificationReadRequest(
    val notification_id: Int
)

data class MarkNotificationReadResponse(
    val status: String,
    val message: String
)