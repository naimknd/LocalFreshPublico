package com.example.localfresh

import android.app.Application
import android.util.Log
import androidx.work.*
import com.example.localfresh.workers.ApartadosExpiringWorker
import com.google.firebase.FirebaseApp
import org.osmdroid.config.Configuration
import java.util.concurrent.TimeUnit
import com.example.localfresh.network.RetrofitInstance

class LocalFreshApplication : Application() {
    companion object {
        private const val TAG = "LocalFreshApp"
    }



    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Inicializando LocalFreshApplication")

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        Log.d(TAG, "Firebase inicializado")

        // Configurar osmdroid
        Configuration.getInstance().apply {
            userAgentValue = "LocalFresh/1.0 (Android)"
            osmdroidBasePath = getPrivateStorageDirectory()
            osmdroidTileCache = getPrivateStorageDirectory()
        }
        Log.d(TAG, "OSMDroid configurado")

        // Configurar el worker para monitorear apartados
        setupApartadosWorker()
        Log.d(TAG, "Configuración completa de la aplicación")

        // Inicializar RetrofitInstance con el contexto de la aplicación
        RetrofitInstance.initialize(applicationContext)
    }

    private fun getPrivateStorageDirectory() = filesDir

    private fun setupApartadosWorker() {
        Log.d(TAG, "Configurando worker de apartados...")

        // Crear restricciones - el worker solo se ejecutará cuando haya conexión a internet
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        Log.d(TAG, "Restricciones configuradas: requiere conexión a internet")

        // Configurar trabajo periódico
        val workRequest = PeriodicWorkRequestBuilder<ApartadosExpiringWorker>(
            15, TimeUnit.MINUTES, // Verificar cada 15 minutos
            5, TimeUnit.MINUTES   // Con flexibilidad de 5 minutos
        )
            .setConstraints(constraints)
            .build()
        Log.d(TAG, "WorkRequest creado: intervalo=15min, flexibilidad=5min")

        // Registrar el trabajo con WorkManager
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "apartados_expiring_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        Log.d(TAG, "Worker registrado con ID: apartados_expiring_worker (policy=KEEP)")

        // Verificar workers programados
        val workInfosFuture = WorkManager.getInstance(this).getWorkInfosByTag(ApartadosExpiringWorker::class.java.name)
        Log.d(TAG, "ℹWorker tag para consulta: ${ApartadosExpiringWorker::class.java.name}")
    }

}