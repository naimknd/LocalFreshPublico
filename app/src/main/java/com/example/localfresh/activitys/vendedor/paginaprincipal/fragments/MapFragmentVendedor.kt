package com.example.localfresh.activitys.vendedor.paginaprincipal.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint
import com.example.localfresh.R
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class MapFragmentVendedor : Fragment() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configurar la biblioteca OSMDroid
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(Context.MODE_PRIVATE))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflar el layout del fragmento
        return inflater.inflate(R.layout.map_fragment_vendedor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.store_map)

        // Configurar el mapa
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // Obtener las coordenadas del vendedor
        val latitude = arguments?.getDouble("latitude") ?: 0.0
        val longitude = arguments?.getDouble("longitude") ?: 0.0

        // Agregar un marcador en la ubicación del vendedor
        val sellerLocation = GeoPoint(latitude, longitude)
        val marker = Marker(mapView)
        marker.position = sellerLocation

        // Establecer un icono personalizado para el marcador
        marker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker) // Reemplaza con tu recurso de imagen
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) // Ajustar el ancla del marcador
        
        // Habilitar el evento de clic en el marcador
        marker.setOnMarkerClickListener { _, _ -> true }
        // Agregar el marcador al mapa
        mapView.overlays.add(marker)

        // Mover la cámara a la ubicación del vendedor
        mapView.controller.setZoom(18.0) // Ajusta el nivel de zoom
        mapView.controller.setCenter(sellerLocation)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume() // Llamar a onResume en el ciclo de vida del mapa
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause() // Llamar a onPause en el ciclo de vida del mapa
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDetach() // Llamar a onDetach en el ciclo de vida del mapa
    }
}
