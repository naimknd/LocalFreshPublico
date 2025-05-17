package com.example.localfresh.utils

import com.example.localfresh.R
import android.widget.TextView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class CustomInfoWindow(resId: Int, mapView: MapView) : InfoWindow(resId, mapView) {
    override fun onOpen(item: Any?) {
        val marker = item as Marker
        val view = mView ?: return

        val title = view.findViewById<TextView>(R.id.infoWindowTitle)
        val description = view.findViewById<TextView>(R.id.infoWindowDescription)

        title.text = marker.title
        description.text = marker.snippet
    }

    override fun onClose() {
        // You can override this if needed
    }
}