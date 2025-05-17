package com.example.localfresh.utils

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.view.MotionEvent
import org.osmdroid.util.GeoPoint

class ClickableAreaOverlay(
    private val geoPoint: GeoPoint,
    private val radiusInPixels: Int,
    private val sellerId: Int,  // Añadir el seller_id
    private val onClick: (Int) -> Unit  // Modificar para pasar el seller_id
) : Overlay() {

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        // Dibujar una área visual para debug
        if (!shadow) {
            val paint = Paint().apply {
                color = Color.GREEN // Hacerlo transparente o ajustar para visualizar durante el desarrollo
                style = Paint.Style.FILL
            }
            val screenPoint = mapView.projection.toPixels(geoPoint, null)
            canvas.drawCircle(screenPoint.x.toFloat(), screenPoint.y.toFloat(), radiusInPixels.toFloat(), paint)
        }
    }

    override fun onSingleTapConfirmed(event: MotionEvent, mapView: MapView): Boolean {
        val touchedPoint = Point(event.x.toInt(), event.y.toInt())
        val touchedGeoPoint = mapView.projection.fromPixels(touchedPoint.x, touchedPoint.y)

        if (geoPoint.distanceToAsDouble(touchedGeoPoint) <= radiusInPixels) {
            onClick(sellerId)
            return true
        }
        return false
    }
}
