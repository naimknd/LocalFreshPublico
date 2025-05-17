package com.example.localfresh.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.localfresh.R
import com.example.localfresh.activitys.LogInActivity
import com.example.localfresh.activitys.comprador.paginaprincipal.PaginaPrincipalCompradorActivity
import com.google.firebase.messaging.RemoteMessage

class NotificationHelper private constructor(private val context: Context) {

    companion object {
        private const val TAG = "NotificationHelper"
        private const val CHANNEL_ID = "LocalFreshNotifications"
        private const val CHANNEL_NAME = "Notificaciones LocalFresh"
        private const val APARTADOS_CHANNEL_ID = "LocalFreshApartados"
        private const val APARTADOS_CHANNEL_NAME = "Apartados LocalFresh"

        @Volatile
        private var instance: NotificationHelper? = null

        fun getInstance(context: Context): NotificationHelper {
            return instance ?: synchronized(this) {
                instance ?: NotificationHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Procesa un mensaje remoto y muestra la notificación apropiada
     */
    fun handleRemoteMessage(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Recibido mensaje remoto: ${remoteMessage.messageId}")
        // Título y mensaje
        val title = remoteMessage.notification?.title ?: "LocalFresh"
        val message = remoteMessage.notification?.body ?: "Tienes una nueva notificación"

        // Mostrar la notificación (siempre abre LogInActivity)
        showNotification(title, message)
    }

    fun showNotification(title: String, message: String, pendingIntent: PendingIntent? = null) {
        Log.d(TAG, "Mostrando notificación: '$title'")
        // Configurar sonido de notificación
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Usar intent proporcionado o crear uno por defecto
        val finalIntent = pendingIntent ?: createDefaultIntent()

        // Crear la notificación
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(finalIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Para Android Oreo y versiones posteriores, crear un canal de notificación
        createNotificationChannel(notificationManager)

        // Mostrar la notificación (usando el timestamp como ID único)
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d(TAG, "Notificación enviada con ID: $notificationId")
    }

    /**
     * Muestra una notificación específica para apartados próximos a expirar
     */
    fun showApartadoExpiringNotification(
        reservationId: Int,
        storeName: String,
        minutesLeft: Int,
        totalPrice: Double
    ) {
        Log.d(TAG, "Creando notificación para apartado #$reservationId (Tienda: $storeName, Minutos: $minutesLeft)")

        // Crear intent para abrir directamente el detalle del apartado
        val intent = Intent(context, PaginaPrincipalCompradorActivity::class.java).apply {
            putExtra("FRAGMENT_TO_LOAD", "DetalleApartadoFragment")
            putExtra("reservationId", reservationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reservationId, // Usar reservationId como requestCode para que sean distintos
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Determinar urgencia y mensaje según tiempo restante
        val (title, message) = when {
            minutesLeft <= 0 -> {
                Log.d(TAG, "Nivel de urgencia: EXPIRADO")
                Pair(
                    "Apartado expirado",
                    "Tu apartado #$reservationId en $storeName ha expirado"
                )
            }
            minutesLeft <= 15 -> {
                Log.d(TAG, "Nivel de urgencia: URGENTE (≤15 minutos)")
                Pair(
                    "¡URGENTE! Tu apartado expira muy pronto",
                    "El apartado #$reservationId en $storeName expira en $minutesLeft minutos"
                )
            }
            minutesLeft <= 30 -> {
                Log.d(TAG, "Nivel de urgencia: ALTA (≤30 minutos)")
                Pair(
                    "Apartado a punto de expirar",
                    "Tu apartado #$reservationId en $storeName expira en menos de 30 minutos"
                )
            }
            else -> {
                Log.d(TAG, "Nivel de urgencia: NORMAL (>30 minutos)")
                Pair(
                    "Recordatorio de apartado",
                    "Tu apartado #$reservationId en $storeName expira pronto"
                )
            }
        }

        // Construir mensaje completo con precio
        val fullMessage = "$message\nValor: $${String.format("%.2f", totalPrice)}"
        Log.d(TAG, "Mensaje configurado: title='$title', message='$fullMessage'")

        // Configurar sonido de notificación
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Crear la notificación
        val notificationBuilder = NotificationCompat.Builder(context, APARTADOS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fullMessage))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // Añadir colores de acuerdo a la urgencia
            .setColor(if (minutesLeft <= 30)
                context.resources.getColor(R.color.red, null)
            else
                context.resources.getColor(R.color.green, null))

        Log.d(TAG, "NotificationBuilder configurado con prioridad alta")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal específico para apartados
        createApartadosNotificationChannel(notificationManager)
        Log.d(TAG, "Canal de notificaciones APARTADOS_CHANNEL_ID configurado")

        // Mostrar la notificación con ID reservationId para que no se repita
        notificationManager.notify(reservationId, notificationBuilder.build())
        Log.d(TAG, "Notificación enviada con ID: $reservationId")
    }

    private fun createDefaultIntent(): PendingIntent {
        val intent = Intent(context, LogInActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal de notificaciones LocalFresh"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createApartadosNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                APARTADOS_CHANNEL_ID,
                APARTADOS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de apartados próximos a expirar"
                enableLights(true)
                lightColor = context.resources.getColor(R.color.red, null)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}