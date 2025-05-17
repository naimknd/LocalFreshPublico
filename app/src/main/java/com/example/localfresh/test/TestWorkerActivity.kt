package com.example.localfresh.test

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.localfresh.R
import com.example.localfresh.utils.NotificationHelper
import com.example.localfresh.workers.ApartadosExpiringWorker

class TestWorkerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_worker)

        val btnTestNotification = findViewById<Button>(R.id.btn_test_notification)
        val btnTestWorker = findViewById<Button>(R.id.btn_test_worker)
        val tvStatus = findViewById<TextView>(R.id.tv_status)

        // Botón para probar una notificación directamente
        btnTestNotification.setOnClickListener {
            tvStatus.text = "Enviando notificación de prueba..."
            val notificationHelper = NotificationHelper.getInstance(this)
            notificationHelper.showApartadoExpiringNotification(
                reservationId = 12345, // Usa un ID real de tu BD
                storeName = "Tienda de Prueba",
                minutesLeft = 15,
                totalPrice = 250.50
            )
            tvStatus.text = "Notificación enviada."
        }

        // Botón para ejecutar el worker manualmente
        btnTestWorker.setOnClickListener {
            tvStatus.text = "Ejecutando Worker..."
            val workRequest = OneTimeWorkRequestBuilder<ApartadosExpiringWorker>()
                .build()

            WorkManager.getInstance(this).enqueue(workRequest)

            // Observar el estado del worker
            WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(workRequest.id)
                .observe(this) { workInfo ->
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED ->
                            tvStatus.text = "Worker completado exitosamente"
                        WorkInfo.State.FAILED ->
                            tvStatus.text = "Worker falló"
                        WorkInfo.State.RUNNING ->
                            tvStatus.text = "Worker ejecutándose..."
                        else ->
                            tvStatus.text = "Estado del worker: ${workInfo.state}"
                    }
                }
        }
    }
}