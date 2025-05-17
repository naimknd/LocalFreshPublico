package com.example.localfresh.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.localfresh.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.concurrent.TimeUnit

class LocationPermissionUtils(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null
    private val updateInterval = TimeUnit.SECONDS.toMillis(10)

    fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun createLocationRequest(): LocationRequest {
        return LocationRequest.Builder(updateInterval)
            .setMinUpdateIntervalMillis(updateInterval / 2)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setWaitForAccurateLocation(false)
            .build()
    }

    fun startLocationUpdates(callback: LocationCallback) {
        if (!hasLocationPermissions()) {
            Log.e("LocationPermissionUtils", "No location permissions")
            return
        }

        stopLocationUpdates()

        val locationRequest = createLocationRequest()
        locationCallback = callback

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            Log.d("LocationUpdates", "Solicitud de actualizaciones de ubicación iniciada")
        } catch (securityException: SecurityException) {
            Log.e("LocationPermissionUtils", "Error requesting location updates", securityException)
        }
    }

    fun stopLocationUpdates() {
        try {
            locationCallback?.let {
                fusedLocationClient.removeLocationUpdates(it)
                Log.d("LocationUpdates", "Actualizaciones de ubicación detenidas")
            }
        } catch (exception: Exception) {
            Log.e("LocationPermissionUtils", "Error al detener actualizaciones", exception)
        } finally {
            locationCallback = null
        }
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun showLocationAlertDialog(fragment: Fragment, isAlertDialogShown: Boolean, onPositiveClick: () -> Unit) {
        if (isAlertDialogShown) return

        val dialogView = fragment.layoutInflater.inflate(R.layout.custom_alert_dialog, null)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val positiveButton = dialogView.findViewById<Button>(R.id.dialogPositiveButton)
        val negativeButton = dialogView.findViewById<Button>(R.id.dialogNegativeButton)

        dialogTitle.text = "Enable Location"
        dialogMessage.text = "The app needs you to enable location to function properly."
        positiveButton.text = "Enable"
        negativeButton.text = "Cancel"

        val alertDialog = AlertDialog.Builder(fragment.requireContext())
            .setView(dialogView)
            .create()

        positiveButton.setOnClickListener {
            onPositiveClick()
            alertDialog.dismiss()
        }

        negativeButton.setOnClickListener {
            Toast.makeText(fragment.requireContext(), "The app cannot function without location.", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    fun showSettingsAlertDialog(fragment: Fragment) {
        val dialogView = fragment.layoutInflater.inflate(R.layout.custom_alert_dialog, null)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val positiveButton = dialogView.findViewById<Button>(R.id.dialogPositiveButton)
        val negativeButton = dialogView.findViewById<Button>(R.id.dialogNegativeButton)

        dialogTitle.text = "Se necesita permiso de ubicación"
        dialogMessage.text = "La aplicación necesita permisos de ubicación para funcionar correctamente. Por favor, actívalos en la configuración de la aplicación."
        positiveButton.text = "Configuración"
        negativeButton.text = "Cancelar"

        val alertDialog = AlertDialog.Builder(fragment.requireContext())
            .setView(dialogView)
            .create()

        positiveButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
            alertDialog.dismiss()
        }

        negativeButton.setOnClickListener {
            Toast.makeText(fragment.requireContext(), "La aplicación no puede funcionar sin la ubicación.", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}
