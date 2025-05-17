package com.example.localfresh.model.vendedor.notificaciones

data class SellerNotification(
    val notification_id: Int,
    val type: String,
    val title: String,
    val message: String,
    val data: String?,
    val created_at: String,
    val is_read: Int
)

data class SellerNotificationsResponse(
    val status: String,
    val notifications: List<SellerNotification>,
    val unread_count: Int,
    val offset: Int,
    val limit: Int
)