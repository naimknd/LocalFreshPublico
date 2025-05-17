package com.example.localfresh.activitys.vendedor.paginaprincipal.fragments

import com.example.localfresh.R
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localfresh.activitys.vendedor.paginaprincipal.filtros.FiltroUnidadesBottomSheet
import com.example.localfresh.adapters.vendedor.UnitAdapterListener
import com.example.localfresh.adapters.vendedor.UnitAdapterVendedor
import com.example.localfresh.databinding.VerUnidadesVendedorLayoutBinding
import com.example.localfresh.model.vendedor.unidad.GetUnitsProductSellerResponse
import com.example.localfresh.utils.DateUtils
import com.example.localfresh.viewmodel.vendedor.paginaprincipal.VerUnidadesViewModel

class VerUnidadesVendedorFragment : Fragment(), UnitAdapterListener {

    private var _binding: VerUnidadesVendedorLayoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VerUnidadesViewModel by viewModels()
    private var productId: Int = -1
    private var currentSelectedDate = ""
    private var currentSelectedStatus = ""
    private val pendingDeletions = mutableMapOf<Int, Int>() // unitId -> position
    private lateinit var adapter: UnitAdapterVendedor

    companion object {
        private const val ARG_PRODUCT_ID = "product_id"
        private const val PREF_SELLER_ID = "SELLER_ID"
        private const val STATUS_DISPONIBLE = "disponible"
        private const val STATUS_APARTADO = "apartado"
        private const val STATUS_EXPIRADO_CONSUMO = "expirado_consumo"
        private const val STATUS_EXPIRADO_CADUCADO = "expirado_caducado"

        fun newInstance(productId: Int) = VerUnidadesVendedorFragment().apply {
            arguments = Bundle().apply { putInt(ARG_PRODUCT_ID, productId) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = arguments?.getInt(ARG_PRODUCT_ID, -1) ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        VerUnidadesVendedorLayoutBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (productId == -1) {
            showToast("Error al obtener el ID del producto")
            parentFragmentManager.popBackStack()
            return
        }

        setupViews()
        setupObservers()
        viewModel.getUnitsProduct(productId)
    }

    private fun setupViews() = with(binding) {
        // Navegación
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() { parentFragmentManager.popBackStack() }
            }
        )

        // Componentes UI
        recyclerViewUnidades.layoutManager = LinearLayoutManager(requireContext())
        buttonFiltros.setOnClickListener { mostrarBottomSheetFiltros() }
        fabAgregarUnidad.setOnClickListener { navigateToAddUnit() }
        btnAddFirstUnit.setOnClickListener { navigateToAddUnit() }
        btnClearFilters.setOnClickListener {
            currentSelectedDate = ""
            currentSelectedStatus = ""
            actualizarIndicadorFiltros()
            aplicarFiltros()
        }

        // Estado inicial
        cardActiveFilters.visibility = View.GONE
    }

    override fun onUnitDeleted(unitId: Int, position: Int) {
        pendingDeletions[unitId] = position
    }

    private fun navigateToAddUnit() {
        val sellerId = requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            .getInt(PREF_SELLER_ID, -1)

        if (sellerId != -1) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AgregarUnidadVendedorFragment.newInstance(sellerId, productId))
                .addToBackStack(null)
                .commit()
        } else {
            showToast("Error al obtener el ID del vendedor")
        }
    }

    private fun mostrarBottomSheetFiltros() {
        FiltroUnidadesBottomSheet().apply {
            setInitialValues(currentSelectedDate, currentSelectedStatus)
            setOnFiltrosAplicadosListener { selectedDate, selectedStatus ->
                currentSelectedDate = selectedDate
                currentSelectedStatus = selectedStatus
                actualizarIndicadorFiltros()
                aplicarFiltros()
            }
        }.show(parentFragmentManager, "FiltroUnidadesBottomSheet")
    }

    private fun actualizarIndicadorFiltros() {
        val hayFiltrosActivos = currentSelectedDate.isNotEmpty() || currentSelectedStatus.isNotEmpty()

        binding.cardActiveFilters.isVisible = hayFiltrosActivos

        if (hayFiltrosActivos) {
            val filtros = mutableListOf<String>().apply {
                if (currentSelectedDate.isNotEmpty()) add("Fecha: ${DateUtils.formatearFecha(currentSelectedDate)}")
                if (currentSelectedStatus.isNotEmpty()) {
                    val estadoFormateado = when(currentSelectedStatus) {
                        STATUS_EXPIRADO_CONSUMO -> "Expirado (Consumo Pref.)"
                        STATUS_EXPIRADO_CADUCADO -> "Expirado (Caducado)"
                        else -> currentSelectedStatus.replaceFirstChar { it.uppercase() }
                    }
                    add("Estado: $estadoFormateado")
                }
            }
            binding.tvActiveFilters.text = "Filtros activos: ${filtros.joinToString(", ")}"
        }
    }

    private fun aplicarFiltros() {
        viewModel.getUnitsProductResponse.value?.takeIf { it.status == "success" }?.let { response ->
            response.units?.let { allUnits ->
                val filteredUnits = allUnits.filter { unit ->
                    val matchesDate = currentSelectedDate.isEmpty() || unit.expiry_date == currentSelectedDate

                    val matchesStatus = when (currentSelectedStatus) {
                        "" -> true  // Sin filtro de estado
                        STATUS_DISPONIBLE -> unit.status.equals(STATUS_DISPONIBLE, true) &&
                                DateUtils.calcularDiasRestantes(unit.expiry_date) >= 0
                        STATUS_EXPIRADO_CONSUMO -> {
                            // Expirado de consumo preferente: unidades disponibles, expiradas y de tipo consumo preferente
                            unit.status.equals(STATUS_DISPONIBLE, true) &&
                                    DateUtils.calcularDiasRestantes(unit.expiry_date) < 0 &&
                                    response.product?.expiry_type == "consumo_preferente"
                        }
                        STATUS_EXPIRADO_CADUCADO -> {
                            // Expirado de caducidad: unidades disponibles, expiradas y de tipo caducidad
                            unit.status.equals(STATUS_DISPONIBLE, true) &&
                                    DateUtils.calcularDiasRestantes(unit.expiry_date) < 0 &&
                                    response.product?.expiry_type == "caducidad"
                        }
                        else -> unit.status.equals(currentSelectedStatus, ignoreCase = true)
                    }

                    matchesDate && matchesStatus
                }.toMutableList()

                adapter.updateUnits(filteredUnits)
                updateEmptyState(filteredUnits.isEmpty())
                updateUnitCounters(response)
            }
        }
    }

    private fun updateUnitCounters(response: GetUnitsProductSellerResponse) {
        val units = response.units ?: return
        val expiryType = response.product?.expiry_type ?: "caducidad"

        // Contar disponibles (no expirados)
        val disponibles = units.count {
            it.status.equals(STATUS_DISPONIBLE, true) &&
                    DateUtils.calcularDiasRestantes(it.expiry_date) >= 0
        }

        // Contar expirados
        val expirados = units.count {
            it.status.equals(STATUS_DISPONIBLE, true) &&
                    DateUtils.calcularDiasRestantes(it.expiry_date) < 0
        }

        binding.apply {
            tvTotalUnits.text = units.size.toString()
            tvDisponibleUnits.text = disponibles.toString()
            tvReservedUnits.text = units.count { it.status.equals(STATUS_APARTADO, true) }.toString()
            tvExpiredUnits.text = expirados.toString()
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) = with(binding) {
        emptyStateLayout.isVisible = isEmpty
        recyclerViewUnidades.isVisible = !isEmpty
        cardSummary.isVisible = !isEmpty
    }

    private fun setupObservers() {
        viewModel.getUnitsProductResponse.observe(viewLifecycleOwner) { response ->
            if (response.status == "success") {
                val product = response.product
                val units = response.units?.toMutableList() ?: mutableListOf()

                // Actualizar título del toolbar
                product?.let { binding.toolbar.title = it.name }

                updateUnitCounters(response)
                updateEmptyState(units.isEmpty())

                if (units.isNotEmpty()) {
                    adapter = UnitAdapterVendedor(units, product, viewModel, this)
                    binding.recyclerViewUnidades.adapter = adapter

                    if (currentSelectedDate.isNotEmpty() || currentSelectedStatus.isNotEmpty()) {
                        aplicarFiltros()
                    }
                }
            } else {
                showToast(response.message ?: "Error al obtener unidades")
            }
        }

        viewModel.deleteUnitResponse.observe(viewLifecycleOwner) { response ->
            if (response.status == "success") {
                pendingDeletions.entries.toList().forEach { (unitId, position) ->
                    adapter.removeUnit(position)
                    pendingDeletions.remove(unitId)
                }

                if (pendingDeletions.isNotEmpty()) {
                    showToast("Unidad eliminada correctamente")
                    viewModel.getUnitsProduct(productId)
                }
            } else if (pendingDeletions.isNotEmpty()) {
                showToast(response.message ?: "Error al eliminar unidad")
                pendingDeletions.clear()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        if (productId != -1) viewModel.getUnitsProduct(productId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pendingDeletions.clear()
        _binding = null
    }
}