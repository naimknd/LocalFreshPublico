package com.example.localfresh.activitys.vendedor.registrovendedores

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.localfresh.adapters.AddressAdapter
import com.example.localfresh.api.NominatimApi
import com.example.localfresh.databinding.SelectAddressStoreSignUpLayoutBinding
import com.example.localfresh.model.general.LocationResult
import com.google.android.gms.location.LocationServices
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class SelectAddressStoreSignUp : AppCompatActivity() {

    companion object {
        private const val LOCATION_REQUEST_CODE = 1001
        const val EXTRA_ADDRESS = "address"
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
    }

    private lateinit var binding: SelectAddressStoreSignUpLayoutBinding
    private lateinit var adapter: AddressAdapter
    private var selectedLocation: LocationResult? = null
    private lateinit var mapMarker: Marker
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val api: NominatimApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApi::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SelectAddressStoreSignUpLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupMap()
        setupSearch()
        setupLocationList()
        binding.selectButton.setOnClickListener { returnSelectedLocation() }
        checkLocationPermission()
        addMapLongPressListener()
    }

    private fun setupMap() = with(binding.osmMap) {
        setMultiTouchControls(true)
        controller.setZoom(15.0)
        mapMarker = Marker(this).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = getDrawable(com.example.localfresh.R.drawable.ic_marker)
        }
        overlays.add(mapMarker)
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty() && s.length > 2) searchLocations(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupLocationList() {
        adapter = AddressAdapter(this, ArrayList())
        binding.locationListView.adapter = adapter
        binding.locationListView.setOnItemClickListener { _, _, pos, _ ->
            adapter.getItem(pos)?.let {
                selectedLocation = it
                updateMapLocation(it)
                adapter.updateLocations(emptyList())
            }
        }
    }

    private fun checkLocationPermission() {
        if (hasLocationPermission()) showCurrentLocation()
        else ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_REQUEST_CODE
        )
    }

    private fun hasLocationPermission() = ActivityCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            showCurrentLocation()
        } else {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let {
                    api.reverseGeocode(it.latitude.toString(), it.longitude.toString(), "json")
                        ?.enqueue(object : Callback<LocationResult?> {
                            override fun onResponse(call: Call<LocationResult?>, response: Response<LocationResult?>) {
                                response.body()?.let { result ->
                                    selectedLocation = result
                                    adapter.updateLocations(listOf(result))
                                    updateMapLocation(result)
                                }
                            }
                            override fun onFailure(call: Call<LocationResult?>, t: Throwable) {}
                        })
                }
            }
        } catch (_: SecurityException) {
            Toast.makeText(this, "Permisos de ubicación no concedidos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchLocations(query: String) {
        api.searchLocations(query, "json", 2, 3)?.enqueue(object : Callback<List<LocationResult>> {
            override fun onResponse(call: Call<List<LocationResult>>, response: Response<List<LocationResult>>) {
                adapter.updateLocations(response.body() ?: emptyList())
            }
            override fun onFailure(call: Call<List<LocationResult>>, t: Throwable) {}
        })
    }

    private fun updateMapLocation(location: LocationResult) {
        val lat = location.latitude.toDoubleOrNull() ?: return
        val lon = location.longitude.toDoubleOrNull() ?: return
        val point = GeoPoint(lat, lon)
        binding.osmMap.controller.setCenter(point)
        mapMarker.position = point
        mapMarker.title = location.displayName
        binding.osmMap.invalidate()
    }

    private fun addMapLongPressListener() {
        binding.osmMap.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint) = true
            override fun longPressHelper(p: GeoPoint): Boolean {
                selectedLocation = LocationResult(
                    p.latitude.toString(),
                    p.longitude.toString(),
                    "Ubicación seleccionada manualmente"
                )
                updateMapLocation(selectedLocation!!)
                reverseGeocode(p)
                return true
            }
        }))
    }

    private fun reverseGeocode(geoPoint: GeoPoint) {
        api.reverseGeocode(geoPoint.latitude.toString(), geoPoint.longitude.toString(), "json")
            ?.enqueue(object : Callback<LocationResult?> {
                override fun onResponse(call: Call<LocationResult?>, response: Response<LocationResult?>) {
                    response.body()?.let {
                        selectedLocation?.displayName = it.formattedAddress
                        mapMarker.title = it.formattedAddress
                        binding.osmMap.invalidate()
                    }
                }
                override fun onFailure(call: Call<LocationResult?>, t: Throwable) {}
            })
    }

    private fun returnSelectedLocation() {
        val loc = selectedLocation ?: adapter.getItem(0)
        val lat = loc?.latitude?.toDoubleOrNull()
        val lon = loc?.longitude?.toDoubleOrNull()
        if (loc?.displayName.isNullOrEmpty() || lat == null || lon == null) {
            Toast.makeText(this, "Selecciona una ubicación válida", Toast.LENGTH_SHORT).show()
            return
        }
        setResult(RESULT_OK, Intent().apply {
            putExtra(EXTRA_ADDRESS, loc.displayName)
            putExtra(EXTRA_LATITUDE, lat)
            putExtra(EXTRA_LONGITUDE, lon)
        })
        finish()
    }
}