package com.example.localfresh.activitys.comprador.paginaprincipal.fragments

import com.example.localfresh.R
import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.localfresh.activitys.comprador.paginaprincipal.fragments.filtros.FiltrosMapaBottomSheetFragment
import com.example.localfresh.databinding.FragmentPaginaPrincipalCompradorBinding
import com.example.localfresh.model.comprador.paginaprincipal.PaginaPrincipalCompradorResponse
import com.example.localfresh.utils.LocationMapUtils
import com.example.localfresh.utils.LocationPermissionUtils
import com.example.localfresh.viewmodel.comprador.paginaprincipal.PaginaPrincipalViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localfresh.adapters.comprador.RecommendationsAdapter
import com.example.localfresh.viewmodel.comprador.recomendaciones.RecommendationsViewModel

class PaginaPrincipalFragment : Fragment() {

    private var _binding: FragmentPaginaPrincipalCompradorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaginaPrincipalViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationMapUtils: LocationMapUtils
    private lateinit var locationPermissionUtils: LocationPermissionUtils
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var locationSettingsLauncher: ActivityResultLauncher<IntentSenderRequest>

    // Variables para controlar los diálogos de permisos y configuración de ubicación
    private var isAlertDialogShown = false
    private var hasShownLocationRequest = false
    private var hasShownSettingsDialog = false
    private var hasShownPermissionRationale = false
    private var permanentlyDenied = false

    // Variables para los filtros
    private var tipoTiendaSeleccionado: String? = null
    private var distanciaMaxima: Int? = null
    private var soloOrganicos: Boolean = false

    // Variables para recomendaciones
    private lateinit var recommendationsViewModel: RecommendationsViewModel
    private lateinit var recommendationsAdapter: RecommendationsAdapter

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation ?: return
            locationMapUtils.onUserLocationChanged(
                location.latitude,
                location.longitude,
                viewModel
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                checkLocationSettings()
            } else {
                permanentlyDenied = true
                showSettingsAlertDialog()
            }
        }
        locationSettingsLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) {
            if (it.resultCode == -1) {
                getLastKnownLocation()
            } else {
                permanentlyDenied = true
                showSettingsAlertDialog()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaginaPrincipalCompradorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar las clases utilitarias
        locationMapUtils = LocationMapUtils(requireContext(), binding.map)
        locationPermissionUtils = LocationPermissionUtils(requireContext())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Cargar filtros guardados
        cargarFiltrosGuardados()
        // Configurar el RecyclerView para recomendaciones
        setupRecommendations()

        // Configurar el mapa y la ubicación
        setupMap()
        setupLocation()
        setupListeners()
        observeViewModel()
    }

    private fun setupMap() {
        binding.map.setMultiTouchControls(true)
        binding.map.minZoomLevel = 12.0
        binding.map.maxZoomLevel = 20.0
    }

    private fun setupLocation() {
        if (!locationPermissionUtils.hasLocationPermissions()) {
            requestLocationPermissions()
        } else {
            checkLocationSettings()
        }
    }

    private fun setupListeners() {
        binding.settingsButton.setOnClickListener {
            mostrarFiltrosBottomSheet()
        }

        binding.recommendationsButton.setOnClickListener {
            toggleRecommendationsVisibility()
        }
    }

    private fun toggleRecommendationsVisibility() {
        val prefs = requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
        val hideRecommendations = prefs.getBoolean("HIDE_RECOMMENDATIONS", false)

        if (hideRecommendations) {
            // Si están ocultas, mostrarlas
            prefs.edit().putBoolean("HIDE_RECOMMENDATIONS", false).apply()

            // Cambiar el tinte del icono para indicar que las recomendaciones están activas
            binding.recommendationsButton.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.green),
                PorterDuff.Mode.SRC_IN
            )

            // Cargar recomendaciones y OBSERVAR los resultados
            val userId = prefs.getInt("USER_ID", -1)
            if (userId != -1) {
                // Mostrar indicador de carga
                binding.progressBarRecommendations.visibility = View.VISIBLE

                // Cargar recomendaciones
                recommendationsViewModel.loadRecommendations(userId)

                // Asegurar que el observer esté configurado
                recommendationsViewModel.recommendations.observe(viewLifecycleOwner) { response ->
                    binding.progressBarRecommendations.visibility = View.GONE

                    if (response.status == "success" && response.recommendations.isNotEmpty()) {
                        // Mostrar con animación suave
                        binding.recommendationsCardView.alpha = 0f
                        binding.recommendationsCardView.visibility = View.VISIBLE
                        binding.recommendationsCardView.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start()

                        recommendationsAdapter.updateData(response.recommendations)
                    } else {
                        // Mostrar Toast informativo si no hay recomendaciones
                        Toast.makeText(
                            requireContext(),
                            "No hay recomendaciones disponibles en este momento",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            // Si están visibles, ocultarlas
            prefs.edit().putBoolean("HIDE_RECOMMENDATIONS", true).apply()

            // Animar la desaparición
            binding.recommendationsCardView.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.recommendationsCardView.visibility = View.GONE
                }
                .start()

            // Cambiar el tinte del icono para indicar que las recomendaciones están desactivadas
            binding.recommendationsButton.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.text_color),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun setupRecommendations() {
        // Inicializar ViewModel
        recommendationsViewModel = ViewModelProvider(this).get(RecommendationsViewModel::class.java)

        // Configurar RecyclerView horizontal para recomendaciones
        binding.recyclerViewRecommendations.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recommendationsAdapter = RecommendationsAdapter(emptyList(), parentFragmentManager)
            adapter = recommendationsAdapter
        }

        // Configurar botón para cerrar las recomendaciones
        binding.btnCloseRecommendations.setOnClickListener {
            binding.recommendationsCardView.visibility = View.GONE

            // Guardar preferencia de usuario para no mostrar recomendaciones
            val prefs = requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("HIDE_RECOMMENDATIONS", true).apply()

            // Actualizar el tinte del botón de recomendaciones
            binding.recommendationsButton.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.text_color),
                PorterDuff.Mode.SRC_IN
            )
        }

        // Verificar si el usuario quiere ver recomendaciones
        val prefs = requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
        val hideRecommendations = prefs.getBoolean("HIDE_RECOMMENDATIONS", false)

        // Configurar el tinte inicial del botón de recomendaciones
        binding.recommendationsButton.setColorFilter(
            ContextCompat.getColor(requireContext(),
                if (hideRecommendations) R.color.text_color else R.color.green),
            PorterDuff.Mode.SRC_IN
        )

        // Si el usuario no ha desactivado las recomendaciones, cargarlas
        if (!hideRecommendations) {
            // Observar cambios en las recomendaciones
            recommendationsViewModel.recommendations.observe(viewLifecycleOwner) { response ->
                if (response.status == "success" && response.recommendations.isNotEmpty()) {
                    binding.recommendationsCardView.visibility = View.VISIBLE
                    recommendationsAdapter.updateData(response.recommendations)
                } else {
                    binding.recommendationsCardView.visibility = View.GONE
                }
            }

            recommendationsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                binding.progressBarRecommendations.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

            // Cargar recomendaciones
            val userId = prefs.getInt("USER_ID", -1)
            if (userId != -1) {
                recommendationsViewModel.loadRecommendations(userId)
            }
        } else {
            binding.recommendationsCardView.visibility = View.GONE
        }
    }

    private fun checkLocationSettings() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationPermissionUtils.createLocationRequest())

        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            getLastKnownLocation()
        }.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && !hasShownSettingsDialog) {
                try {
                    hasShownSettingsDialog = true
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    locationSettingsLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e("LocationUpdates", "Error al solicitar habilitar ubicación", sendEx)
                }
            }
        }
    }

    private fun requestLocationPermissions() {
        when {
            locationPermissionUtils.hasLocationPermissions() -> {
                checkLocationSettings()
            }
            !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    !hasShownPermissionRationale && !permanentlyDenied -> {
                // Primera vez que se piden los permisos
                hasShownPermissionRationale = true
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            !hasShownLocationRequest -> {
                // Mostrar diálogo explicando por qué necesitamos permisos
                hasShownLocationRequest = true
                showLocationAlertDialog()
            }
            else -> {
                // Usuario rechazó permisos permanentemente
                showSettingsAlertDialog()
            }
        }
    }

    private fun getLastKnownLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestLocationPermissions()
                return
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    locationMapUtils.updateUserLocationOnMap(it.latitude, it.longitude)
                    locationPermissionUtils.startLocationUpdates(locationCallback)
                } ?: run {
                    // Si lastLocation es null, iniciar actualizaciones de ubicación para obtener una
                    locationPermissionUtils.startLocationUpdates(locationCallback)
                }
            }.addOnFailureListener { e ->
                Log.e("Location", "Error getting location", e)
                locationPermissionUtils.startLocationUpdates(locationCallback)
            }
        } catch (e: SecurityException) {
            Log.e("Location", "SecurityException when getting location", e)
        }
    }

    private fun observeViewModel() {
        viewModel.tiendasResponse.observe(viewLifecycleOwner) { response ->
            binding.loadingIndicator.visibility = View.GONE
            if (response != null) {
                handleTiendasResponse(response)
            } else {
                Log.e("PaginaPrincipalFragment", "La respuesta de tiendas es nula")
                Toast.makeText(
                    requireContext(),
                    "Error al cargar las tiendas. Intenta nuevamente.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Observar el estado de carga explícitamente
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observar errores explícitamente
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            binding.loadingIndicator.visibility = View.GONE
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleTiendasResponse(response: PaginaPrincipalCompradorResponse) {
        val tiendas = response.tiendas
        val radioKm = if (response.rango_distancia == 0) 1.0 else response.rango_distancia.toDouble()
        locationMapUtils.setStoresAndRadius(tiendas, radioKm)

        if (!locationPermissionUtils.hasLocationPermissions()) {
            requestLocationPermissions()
            return
        }

        try {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestLocationPermissions()
                return
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val tiendasEnRango = viewModel.filtrarTiendasEnRango(
                        location.latitude,
                        location.longitude,
                        response.rango_distancia.toDouble(),
                        response.tiendas
                    )
                    locationMapUtils.setupMap(tiendasEnRango)
                    locationMapUtils.drawCircleOnMap(
                        location.latitude,
                        location.longitude,
                        response.rango_distancia.toDouble()
                    )
                }
            }.addOnFailureListener { e ->
                Log.e("Location", "Error getting location", e)
            }
        } catch (e: SecurityException) {
            Log.e("Location", "SecurityException when getting location", e)
            requestLocationPermissions()
        }
    }

    fun refreshStoresData() {
        binding.loadingIndicator.visibility = View.VISIBLE

        val token = arguments?.getString("TOKEN") ?:
        requireActivity().getSharedPreferences("LocalFreshPrefs", 0).getString("TOKEN", null)

        token?.let {
            // Convertir soloOrganicos (Boolean) a Int para la API
            val organicInt = if (soloOrganicos) 1 else null

            viewModel.obtenerTiendasYDistancia(
                token = it,
                storeType = tipoTiendaSeleccionado,
                organic = organicInt,
                distance = distanciaMaxima
            )
        } ?: run {
            Log.e("PaginaPrincipalFragment", "Token no encontrado")
            binding.loadingIndicator.visibility = View.GONE
        }
    }

    private fun showLocationAlertDialog() {
        if (isAlertDialogShown) return

        locationPermissionUtils.showLocationAlertDialog(this, false) {
            hasShownLocationRequest = false
            requestLocationPermissions()
        }

        isAlertDialogShown = true
    }

    private fun showSettingsAlertDialog() {
        locationPermissionUtils.showSettingsAlertDialog(this)
    }

    private fun mostrarFiltrosBottomSheet() {
        val bottomSheet = FiltrosMapaBottomSheetFragment()

        // Establecer los valores iniciales con los filtros guardados
        bottomSheet.setFiltrosIniciales(
            tipoTienda = tipoTiendaSeleccionado,
            distanciaMaxima = distanciaMaxima ?: 3,
            soloOrganicos = soloOrganicos
        )

        // Configurar el listener para recibir los filtros seleccionados
        bottomSheet.setFiltrosListener(object : FiltrosMapaBottomSheetFragment.OnFiltrosMapaSeleccionadosListener {
            override fun onFiltrosSeleccionados(
                tiposTienda: List<String>,
                distanciaMaxima: Int,
                soloOrganicos: Boolean
            ) {
                aplicarFiltrosAlMapa(tiposTienda, distanciaMaxima, soloOrganicos)
            }
        })

        bottomSheet.show(parentFragmentManager, "FiltrosMapaBottomSheetFragment")
    }

    private fun aplicarFiltrosAlMapa(tiposTienda: List<String>, distanciaMaxima: Int, soloOrganicos: Boolean) {
        // Guardar los filtros seleccionados
        this.tipoTiendaSeleccionado = if (tiposTienda.isEmpty()) null else tiposTienda.first()
        this.distanciaMaxima = distanciaMaxima
        this.soloOrganicos = soloOrganicos

        // Guardar los filtros en SharedPreferences
        guardarFiltros()

        // Mostrar información sobre filtros aplicados
        val filtrosAplicados = listOfNotNull(
            tipoTiendaSeleccionado?.let { "Tipo: $it" },
            if (soloOrganicos) "Solo orgánicos" else null,
            "Distancia: $distanciaMaxima km"
        ).joinToString(", ")

        // Mostrar toast con los filtros aplicados
        if (filtrosAplicados.isNotEmpty()) {
            Toast.makeText(requireContext(), "Filtros: $filtrosAplicados", Toast.LENGTH_SHORT).show()
        }

        // Recargar datos con los nuevos filtros
        refreshStoresData()
    }

    // Metodo para guardar los filtros en SharedPreferences
    private fun guardarFiltros() {
        val sharedPreferences = requireActivity().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("FILTER_TIPO_TIENDA", tipoTiendaSeleccionado)
            putInt("FILTER_DISTANCIA_MAXIMA", distanciaMaxima ?: 3)
            putBoolean("FILTER_SOLO_ORGANICOS", soloOrganicos)
        }.apply()
    }

    // Metodo para cargar los filtros guardados
    private fun cargarFiltrosGuardados() {
        val sharedPreferences = requireActivity().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
        tipoTiendaSeleccionado = sharedPreferences.getString("FILTER_TIPO_TIENDA", null)
        distanciaMaxima = sharedPreferences.getInt("FILTER_DISTANCIA_MAXIMA", 3)
        soloOrganicos = sharedPreferences.getBoolean("FILTER_SOLO_ORGANICOS", false)
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()

        if (locationPermissionUtils.hasLocationPermissions() &&
            locationPermissionUtils.isLocationEnabled()) {
            locationPermissionUtils.startLocationUpdates(locationCallback)
            arguments?.getString("TOKEN")?.let { token ->
                viewModel.obtenerTiendasYDistancia(token)
            }
        } else {
            requestLocationPermissions()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
        locationPermissionUtils.stopLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        locationPermissionUtils.stopLocationUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationPermissionUtils.stopLocationUpdates()
        binding.map.onDetach()
        _binding = null
    }
}