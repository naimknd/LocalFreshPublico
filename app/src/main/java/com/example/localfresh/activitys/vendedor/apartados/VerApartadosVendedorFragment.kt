package com.example.localfresh.activitys.vendedor.apartados

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localfresh.R
import com.example.localfresh.adapters.vendedor.ListaApartadosVendedorAdapter
import com.example.localfresh.databinding.FragmentVerApartadosVendedorBinding
import com.example.localfresh.utils.ReservationStatusUtils
import com.example.localfresh.utils.ReservationTimerUtils
import com.example.localfresh.viewmodel.vendedor.apartados.ReservationSellerViewModel
import com.example.localfresh.viewmodel.vendedor.apartados.ReservationsListSellerState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

class VerApartadosVendedorFragment : Fragment() {
    private var _binding: FragmentVerApartadosVendedorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReservationSellerViewModel by viewModels()

    private lateinit var adapter: ListaApartadosVendedorAdapter
    private var isHistoryMode = false

    // Variables para filtros agrupadas en un objeto para mejor manejo
    private data class FilterState(
        var status: String? = null,
        var dateFrom: String? = null,
        var dateTo: String? = null,
        var buyerSearch: String? = null
    )

    private val filters = FilterState()
    private val sellerId by lazy {
        requireContext().getSharedPreferences("LocalFreshPrefs", 0).getInt("SELLER_ID", -1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerApartadosVendedorBinding.inflate(inflater, container, false)
        isHistoryMode = arguments?.getBoolean("SHOW_HISTORY", false) ?: false

        // Configurar interfaz según el modo
        with(binding) {
            title.text = if (isHistoryMode) "Historial de Apartados" else "Apartados"
            backButton.apply {
                visibility = if (isHistoryMode) View.VISIBLE else View.GONE
                setOnClickListener { parentFragmentManager.popBackStack() }
            }
            cameraButton.visibility = if (isHistoryMode) View.GONE else View.VISIBLE
            historyButton.visibility = if (isHistoryMode) View.GONE else View.VISIBLE
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupListeners()
        loadReservations()
    }

    private fun setupRecyclerView() {
        adapter = ListaApartadosVendedorAdapter(
            onItemClick = { navigateToDetail(it.reservation_id) },
            onTimerExpired = { if (isAdded) loadReservations() },
            statusUtils = ReservationStatusUtils,
            timerUtils = ReservationTimerUtils
        )

        binding.recyclerView.apply {
            adapter = this@VerApartadosVendedorFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        viewModel.reservationsListState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = View.GONE

            when (state) {
                is ReservationsListSellerState.Success -> {
                    val reservations = state.response.reservations
                    adapter.submitList(reservations)

                    if (reservations.isEmpty()) {
                        showEmptyState(if (isHistoryMode)
                            "No hay registros en el historial de apartados"
                        else
                            "No hay apartados pendientes"
                        )
                    } else {
                        hideEmptyState()
                    }
                }
                is ReservationsListSellerState.Error -> {
                    showEmptyState("Error: ${state.message}")
                }
                is ReservationsListSellerState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupListeners() {
        with(binding) {
            filterButton.setOnClickListener { showFilterOptions() }
            historyButton.setOnClickListener { navigateToHistory() }
            cameraButton.setOnClickListener { navigateToQRScanner() }
        }
    }

    private fun loadReservations() {
        if (sellerId != -1) {
            binding.progressBar.visibility = View.VISIBLE
            binding.textViewEmpty.visibility = View.GONE
            updateFilterStatusText()

            viewModel.getSellerReservations(
                sellerId,
                filters.status,
                filters.dateFrom,
                filters.dateTo,
                filters.buyerSearch,
                isHistoryMode
            )
        } else {
            showEmptyState("No se pudo obtener el ID del vendedor")
        }
    }

    private fun updateFilterStatusText() {
        val activeFilters = mutableListOf<String>().apply {
            filters.status?.let { add("Estado: $it") }
            filters.dateFrom?.let { add("Desde: $it") }
            filters.dateTo?.let { add("Hasta: $it") }
            filters.buyerSearch?.let { add("Comprador: $it") }
        }

        binding.filterStatusText.text = if (activeFilters.isEmpty()) {
            if (isHistoryMode) "Mostrando historial de apartados" else "Mostrando apartados pendientes"
        } else {
            "Filtros: ${activeFilters.joinToString(", ")}"
        }
    }

    private fun showFilterOptions() {
        val options = arrayOf("Estado", "Fecha", "Comprador", "Limpiar filtros")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtrar por")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showStatusFilter()
                    1 -> showDateFilter()
                    2 -> showBuyerFilter()
                    3 -> clearFilters()
                }
            }
            .show()
    }

    private fun clearFilters() {
        filters.apply {
            status = null
            dateFrom = null
            dateTo = null
            buyerSearch = null
        }
        updateFilterStatusText()
        loadReservations()
    }

    private fun showStatusFilter() {
        val options = if (isHistoryMode) {
            arrayOf("Completado", "Cancelado", "Expirado")
        } else {
            arrayOf("Pendiente", "Completado", "Cancelado", "Expirado")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtrar por estado")
            .setItems(options) { _, which ->
                filters.status = options[which]
                updateFilterStatusText()
                loadReservations()
            }
            .show()
    }

    private fun showDateFilter() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_date_filter, null)

        // Setup date selectors
        dialogView.findViewById<View>(R.id.btnFromDate).setOnClickListener {
            showDatePicker { date ->
                dialogView.findViewById<android.widget.TextView>(R.id.tvFromDate).text = date
                filters.dateFrom = date
            }
        }

        dialogView.findViewById<View>(R.id.btnToDate).setOnClickListener {
            showDatePicker { date ->
                dialogView.findViewById<android.widget.TextView>(R.id.tvToDate).text = date
                filters.dateTo = date
            }
        }

        // Set current values
        filters.dateFrom?.let { dialogView.findViewById<android.widget.TextView>(R.id.tvFromDate).text = it }
        filters.dateTo?.let { dialogView.findViewById<android.widget.TextView>(R.id.tvToDate).text = it }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtrar por fechas")
            .setView(dialogView)
            .setPositiveButton("Aplicar") { _, _ ->
                updateFilterStatusText()
                loadReservations()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                onDateSelected(String.format("%04d-%02d-%02d", year, month + 1, day))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showBuyerFilter() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_search_buyer, null)
        val editTextBuyer = dialogView.findViewById<android.widget.EditText>(R.id.editTextBuyerSearch)

        filters.buyerSearch?.let { editTextBuyer.setText(it) }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Buscar por comprador")
            .setView(dialogView)
            .setPositiveButton("Buscar") { _, _ ->
                val buyerName = editTextBuyer.text.toString().trim()
                filters.buyerSearch = buyerName.takeIf { it.isNotEmpty() }
                updateFilterStatusText()
                loadReservations()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun navigateToDetail(reservationId: Int) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DetalleApartadoVendedorFragment().apply {
                arguments = Bundle().apply {
                    putInt("reservation_id", reservationId)
                }
            })
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToHistory() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, VerApartadosVendedorFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("SHOW_HISTORY", true)
                }
            })
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToQRScanner() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, QRScannerVendedorFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun showEmptyState(message: String) {
        binding.recyclerView.visibility = View.GONE
        binding.textViewEmpty.visibility = View.VISIBLE
        binding.textViewEmpty.text = message
    }

    private fun hideEmptyState() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.textViewEmpty.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (binding.recyclerView.adapter as? ListaApartadosVendedorAdapter)?.releaseResources()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        loadReservations()
    }
}