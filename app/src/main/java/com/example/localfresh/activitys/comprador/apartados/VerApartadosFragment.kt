package com.example.localfresh.activitys.comprador.apartados

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localfresh.R
import com.example.localfresh.adapters.comprador.VerApartadosAdapter
import com.example.localfresh.databinding.FragmentVerApartadosBinding
import com.example.localfresh.model.comprador.apartados.ReservationData
import com.example.localfresh.utils.ReservationStatusUtils
import com.example.localfresh.utils.ReservationTimerUtils
import com.example.localfresh.viewmodel.comprador.apartados.ReservationViewModel
import com.example.localfresh.viewmodel.comprador.apartados.ReservationsListState

class VerApartadosFragment : Fragment() {
    private var _binding: FragmentVerApartadosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReservationViewModel by viewModels()
    private lateinit var adapter: VerApartadosAdapter

    private var isHistoryMode = false
    private var currentStatus: String? = null
    private var currentSortBy: String = "reservation_date"
    private var currentSortDirection: String = "DESC"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVerApartadosBinding.inflate(inflater, container, false)
        isHistoryMode = arguments?.getBoolean("SHOW_HISTORY", false) ?: false
        setupHeader()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        setupObservers()
        updateFilterStatus()
        loadReservations()
    }

    private fun setupHeader() = with(binding) {
        if (isHistoryMode) {
            tvTitle.text = "Historial de Apartados"
            btnBack.visibility = View.VISIBLE
            btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        }
    }

    private fun setupRecyclerView() = with(binding) {
        rvApartados.layoutManager = LinearLayoutManager(context)
        adapter = VerApartadosAdapter(
            onTimerExpired = { if (isAdded) loadReservations() },
            onItemClick = { navigateToDetail(it) },
            statusUtils = ReservationStatusUtils,
            timerUtils = ReservationTimerUtils
        )
        rvApartados.adapter = adapter
    }

    private fun setupListeners() = with(binding) {
        btnFilter.setOnClickListener { showFilterDialog() }
        btnSort.setOnClickListener { showSortDialog() }
        swipeRefreshLayout.setOnRefreshListener { loadReservations() }
    }

    private fun setupObservers() {
        viewModel.reservationsListState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefreshLayout.isRefreshing = false
            when (state) {
                is ReservationsListState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.response.reservations.isEmpty()) showEmptyState()
                    else {
                        binding.textViewEmpty.visibility = View.GONE
                        adapter.submitList(state.response.reservations)
                    }
                }
                is ReservationsListState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.textViewEmpty.apply {
                        visibility = View.VISIBLE
                        text = state.message
                    }
                }
                is ReservationsListState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.textViewEmpty.visibility = View.GONE
                }
            }
        }
    }

    private fun updateFilterStatus() = with(binding) {
        filterStatusText.visibility = View.VISIBLE
        filterStatusText.text = when {
            isHistoryMode && currentStatus != null -> "Historial: ${getReadableStatus(currentStatus!!)}"
            isHistoryMode -> "Mostrando todo el historial"
            currentStatus != null -> "Apartados: ${getReadableStatus(currentStatus!!)}"
            else -> "Mostrando apartados pendientes"
        }
    }

    private fun getReadableStatus(status: String) = when (status) {
        "Pendiente" -> "Pendientes"
        "Completado" -> "Completados"
        "Expirado" -> "Expirados"
        "Cancelado" -> "Cancelados"
        else -> status
    }

    private fun navigateToDetail(reservation: ReservationData) {
        val detalleFragment = DetalleApartadoFragment().apply {
            arguments = Bundle().apply { putInt("reservationId", reservation.reservation_id) }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detalleFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showFilterDialog() {
        val options = if (isHistoryMode)
            arrayOf("Todos", "Completados", "Expirados", "Cancelados")
        else
            arrayOf("Pendientes", "Completados", "Expirados", "Cancelados")

        AlertDialog.Builder(requireContext())
            .setTitle("Filtrar por estado")
            .setItems(options) { _, which ->
                currentStatus = if (isHistoryMode) {
                    when (which) {
                        0 -> null
                        1 -> "Completado"
                        2 -> "Expirado"
                        3 -> "Cancelado"
                        else -> null
                    }
                } else {
                    when (which) {
                        0 -> "Pendiente"
                        1 -> "Completado"
                        2 -> "Expirado"
                        3 -> "Cancelado"
                        else -> "Pendiente"
                    }
                }
                updateFilterStatus()
                loadReservations()
            }
            .show()
    }

    private fun showSortDialog() {
        val options = arrayOf(
            "Más recientes primero",
            "Más antiguos primero",
            "Precio (mayor a menor)",
            "Precio (menor a mayor)"
        )
        AlertDialog.Builder(requireContext())
            .setTitle("Ordenar apartados")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { currentSortBy = "reservation_date"; currentSortDirection = "DESC" }
                    1 -> { currentSortBy = "reservation_date"; currentSortDirection = "ASC" }
                    2 -> { currentSortBy = "total_price"; currentSortDirection = "DESC" }
                    3 -> { currentSortBy = "total_price"; currentSortDirection = "ASC" }
                }
                loadReservations()
            }
            .show()
    }

    private fun showEmptyState() = with(binding.textViewEmpty) {
        visibility = View.VISIBLE
        text = if (isHistoryMode) "No hay registros en el historial de apartados"
        else getString(R.string.no_reservations_available)
    }

    private fun loadReservations() {
        val userId = requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            .getInt("USER_ID", -1)
        if (userId != -1) {
            binding.progressBar.visibility = View.VISIBLE
            binding.textViewEmpty.visibility = View.GONE
            val statusToUse = if (!isHistoryMode && currentStatus == null) "Pendiente" else currentStatus
            viewModel.getReservations(
                userId = userId,
                status = statusToUse,
                sortBy = currentSortBy,
                sortDirection = currentSortDirection,
                isHistory = isHistoryMode
            )
        } else {
            Toast.makeText(context, "No se pudo obtener el ID de usuario", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (binding.rvApartados.adapter as? VerApartadosAdapter)?.releaseResources()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        loadReservations()
    }
}