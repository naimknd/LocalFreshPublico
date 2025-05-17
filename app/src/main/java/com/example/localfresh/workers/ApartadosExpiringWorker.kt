package com.example.localfresh.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.localfresh.network.RetrofitInstance
import com.example.localfresh.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ApartadosExpiringWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ApartadosWorker"
        private const val NOTIFY_WITHIN_MINUTES = 60 // Notificar cuando falten 60 minutos o menos
        private const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Worker iniciado - ${System.currentTimeMillis()}")
        try {
            // Obtener ID del usuario desde SharedPreferences
            val prefs = applicationContext.getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            val userId = prefs.getInt("USER_ID", -1)
            val notificationsEnabled = prefs.getBoolean("NOTIF_RECORDATORIOS", true)

            Log.d(TAG, "Usuario ID: $userId, Notificaciones habilitadas: $notificationsEnabled")

            if (userId == -1) {
                Log.d(TAG, "No hay usuario logueado, worker completado sin acción")
                return@withContext Result.success()
            }

            // Verificar si el usuario tiene habilitadas las notificaciones de recordatorios
            if (!notificationsEnabled) {
                Log.d(TAG, "Notificaciones de recordatorios desactivadas por el usuario")
                return@withContext Result.success()
            }

            // Llamar al endpoint existente de apartados
            Log.d(TAG, "Iniciando petición a API: userId=$userId, status=Pendiente")
            val response = RetrofitInstance.compradorApiService.getReservations(
                userId = userId,
                status = "Pendiente"
            ).execute()

            if (response.isSuccessful) {
                Log.d(TAG, "Respuesta exitosa: ${response.code()}")
                val data = response.body()
                if (data != null) {
                    Log.d(TAG, "Apartados recibidos: ${data.count} (status=${data.status})")

                    if (data.reservations.isNotEmpty()) {
                        Log.d(TAG, "Verificando ${data.reservations.size} apartados pendientes")
                        checkForExpiringReservations(data.reservations)
                    } else {
                        Log.d(TAG, "No hay apartados pendientes para verificar")
                    }
                } else {
                    Log.e(TAG, "Respuesta vacía (body=null)")
                }
            } else {
                Log.e(TAG, "Error en la petición: ${response.code()} - ${response.errorBody()?.string()}")
            }

            Log.d(TAG, "Worker completado exitosamente")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error en worker: ${e.message}", e)
            Result.retry()
        }
    }

    private fun checkForExpiringReservations(reservations: List<com.example.localfresh.model.comprador.apartados.ReservationData>) {
        val now = Date()
        val notificationHelper = NotificationHelper.getInstance(applicationContext)
        val dateFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())

        Log.d(TAG, "Hora actual: $now")

        reservations.forEach { reservation ->
            try {
                Log.d(TAG, "Verificando apartado #${reservation.reservation_id} - Tienda: ${reservation.seller.store_name}")

                // Verificar si el apartado está próximo a expirar
                val expirationDate = try {
                    dateFormat.parse(reservation.expiration_date)
                } catch (e: Exception) {
                    Log.e(TAG, "Error al parsear fecha: ${reservation.expiration_date} - ${e.message}")
                    null
                }

                if (expirationDate != null) {
                    // Calcular tiempo restante en minutos
                    val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(expirationDate.time - now.time).toInt()
                    Log.d(TAG, "Apartado #${reservation.reservation_id} - Expira en: $minutesLeft minutos - Fecha: ${reservation.expiration_date}")

                    // Notificar para apartados próximos a expirar o ya expirados
                    if (minutesLeft <= NOTIFY_WITHIN_MINUTES && minutesLeft > 0) {
                        // Evitar múltiples notificaciones - solo notificar en ciertos umbrales
                        if (minutesLeft <= 15 || minutesLeft == 30 || minutesLeft == 60) {
                            Log.d(TAG, "Enviando notificación para apartado #${reservation.reservation_id}, expira en $minutesLeft minutos")

                            notificationHelper.showApartadoExpiringNotification(
                                reservation.reservation_id,
                                reservation.seller.store_name,
                                minutesLeft,
                                reservation.total_price
                            )
                        } else {
                            Log.d(TAG, "Omitiendo notificación para apartado #${reservation.reservation_id} ($minutesLeft minutos no es un umbral de notificación)")
                        }
                    } else if (minutesLeft <= 0) {
                        // Notificar si el apartado ha expirado recientemente (en las últimas 2 horas)
                        if (minutesLeft >= -120) {
                            Log.d(TAG, "Enviando notificación para apartado EXPIRADO #${reservation.reservation_id}")

                            notificationHelper.showApartadoExpiringNotification(
                                reservation.reservation_id,
                                reservation.seller.store_name,
                                0, // Indicar expiración con 0 minutos
                                reservation.total_price
                            )
                        } else {
                            Log.d(TAG, "Apartado #${reservation.reservation_id} expiró hace más de 2 horas, omitiendo notificación")
                        }
                    } else {
                        Log.d(TAG, "Apartado #${reservation.reservation_id} - No necesita notificación aún ($minutesLeft > $NOTIFY_WITHIN_MINUTES minutos)")
                    }
                } else {
                    Log.e(TAG, "No se pudo determinar la fecha de expiración para apartado #${reservation.reservation_id}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al procesar apartado #${reservation.reservation_id}: ${e.message}")
            }
        }

        Log.d(TAG, "Verificación completada para ${reservations.size} apartados")
    }
}