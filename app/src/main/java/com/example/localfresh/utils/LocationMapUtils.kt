package com.example.localfresh.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.localfresh.R
import com.example.localfresh.activitys.comprador.paginaprincipal.fragments.StoreInfoBottomSheetFragment
import com.example.localfresh.model.comprador.paginaprincipal.TiendasPaginaPrincipal
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.io.File

class LocationMapUtils(private val context: Context, private val mapView: MapView) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var currentMarker: Marker? = null
    private var currentStores: List<TiendasPaginaPrincipal> = emptyList()
    private var currentRadiusKm: Double = 0.0
    var isFirstLocationUpdate = true

    fun setupMap(stores: List<TiendasPaginaPrincipal>) {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", 0))
            osmdroidTileCache = File(context.cacheDir, "osmdroid")
        }
        mapView.setMultiTouchControls(true)

        // Add MapEventsOverlay first
        val mapEventsReceiver = MapEventsReceiverImpl(context)
        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(mapEventsOverlay)

        // Then add store markers
        addStoreMarkers(stores)
    }

    fun updateUserLocationOnMap(latitude: Double, longitude: Double) {
        val userLocation = GeoPoint(latitude, longitude)

        try {
            currentMarker?.let {
                mapView.overlays.remove(it)
            }

            currentMarker = Marker(mapView).apply {
                position = userLocation
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(context, R.drawable.user_location_icon)
            }
            mapView.overlays.add(currentMarker)

            // Mover la cámara a la nueva ubicación
            mapView.controller.animateTo(userLocation)
            if (isFirstLocationUpdate) {
                mapView.controller.setZoom(18.0)
                isFirstLocationUpdate = false
            }

            mapView.invalidate()
        } catch (e: Exception) {
            Log.e("LocationMapUtils", "Error al actualizar ubicación en mapa", e)
        }
    }

    fun drawCircleOnMap(latitude: Double, longitude: Double, radiusInKm: Double) {
        val geoPoint = GeoPoint(latitude, longitude)
        val radiusInMeters = radiusInKm * 1000

        // Remove previous circles
        mapView.overlays.removeAll { it is CircleOverlay }

        // Create and add new circle
        val circleOverlay = CircleOverlay(geoPoint, radiusInMeters, context)

        // Add circle at the beginning of the overlays list
        mapView.overlays.add(0, circleOverlay)

        mapView.invalidate()
    }

    fun setStoresAndRadius(
        stores: List<TiendasPaginaPrincipal>,
        radiusKm: Double
    ) {
        currentStores = stores
        currentRadiusKm = radiusKm
    }

    fun onUserLocationChanged(
        latitude: Double,
        longitude: Double,
        viewModel: com.example.localfresh.viewmodel.comprador.paginaprincipal.PaginaPrincipalViewModel
    ) {
        updateUserLocationOnMap(latitude, longitude)
        drawCircleOnMap(latitude, longitude, currentRadiusKm)
        val tiendasEnRango = viewModel.filtrarTiendasEnRango(
            latitude, longitude, currentRadiusKm, currentStores
        )
        addStoreMarkers(tiendasEnRango)
    }

    fun addStoreMarkers(stores: List<TiendasPaginaPrincipal>) {
        if (mapView == null) return // Verifica si mapView es nulo
        // Limpiar marcadores anteriores
        mapView.overlays.removeAll { it is Marker && it != currentMarker }
        stores.forEach { store ->
            val geoPoint = GeoPoint(store.latitude, store.longitude)
            val marker = Marker(mapView).apply {
                position = geoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(context, R.drawable.ic_marker)
                title = store.store_name
                setOnMarkerClickListener { _, _ ->
                    showBottomSheetDialog(store)
                    true
                }
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    private fun showBottomSheetDialog(store: TiendasPaginaPrincipal) {
        val fragment = StoreInfoBottomSheetFragment(store)
        fragment.show((context as AppCompatActivity).supportFragmentManager, fragment.tag)
    }

    // Overlay para círculo de rango
    class CircleOverlay(
        private val center: GeoPoint,
        private val radius: Double,
        context: Context
    ) : Overlay() {
        private val paint: Paint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.green)
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        private val fillPaint: Paint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.green_transparent)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
            val projection = mapView.projection
            val centerPoint = projection.toPixels(center, null)
            val radiusPx = projection.metersToPixels(
                radius.toFloat(),
                center.latitude,
                mapView.zoomLevelDouble
            )

            canvas.drawCircle(centerPoint.x.toFloat(), centerPoint.y.toFloat(), radiusPx, fillPaint)
            canvas.drawCircle(centerPoint.x.toFloat(), centerPoint.y.toFloat(), radiusPx, paint)
        }
    }

    // Overlay para eventos en el mapa
    inner class MapEventsReceiverImpl(private val context: Context) : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
            p?.let {
                Log.d("MapClick", "Clicked at: ${it.latitude}, ${it.longitude}")
            }
            return true
        }

        override fun longPressHelper(p: GeoPoint?): Boolean {
            p?.let {
                Log.d("MapLongPress", "Long press at: ${it.latitude}, ${it.longitude}")
            }
            return true
        }
    }
}