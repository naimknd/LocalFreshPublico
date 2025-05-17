package com.example.localfresh.activitys.vendedor.notificaciones

import com.example.localfresh.R
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localfresh.adapters.vendedor.NotificacionesVendedorAdapter
import com.example.localfresh.databinding.FragmentNotificacionesVendedorBinding
import com.example.localfresh.model.vendedor.notificaciones.SellerNotification
import com.example.localfresh.viewmodel.vendedor.notificaciones.NotificacionesVendedorViewModel

class NotificacionesVendedorFragment : Fragment() {

    private var _binding: FragmentNotificacionesVendedorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificacionesVendedorViewModel by viewModels()
    private lateinit var adapter: NotificacionesVendedorAdapter

    // Variables para filtrado y ordenamiento
    private var currentSortBy: String = "created_at"
    private var currentSortDirection: String = "DESC"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificacionesVendedorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NotificacionesVendedorAdapter()
        binding.rvNotificaciones.layoutManager = LinearLayoutManager(context)
        binding.rvNotificaciones.adapter = adapter

        // Listener para abrir el detalle de la notificación
        adapter.setOnNotificationClickListener(object : NotificacionesVendedorAdapter.OnNotificationClickListener {
            override fun onNotificationClick(notification: SellerNotification) {
                val detalleFragment = DetalleNotificacionFragment.newInstance(notification)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detalleFragment)
                    .addToBackStack(null)
                    .commit()
            }
        })

        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }

        binding.btnSort.setOnClickListener {
            showSortDialog()
        }

        // Scroll infinito para paginación
        binding.rvNotificaciones.addOnScrollListener(object :
            androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (totalItemCount <= lastVisibleItem + 2 && !viewModel.allLoaded && viewModel.isLoading.value != true) {
                    viewModel.loadNextPage()
                }
            }
        })

        // Observers
        viewModel.notificaciones.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
        }
        viewModel.unreadCount.observe(viewLifecycleOwner) { count ->
            binding.tvUnreadCount.text = "Sin leer: $count"
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        }
        // Primera carga
        viewModel.loadNotifications(reset = true)
    }

    private fun showFilterDialog() {
        val tipos = arrayOf("Todos", "Alerta caducidad", "Nuevo apartado", "Expirado", "Cancelado", "Producto expirado")
        val tipoValues = arrayOf(null, "alerta_caducidad", "nuevo_apartado", "caducado", "cancelado", "producto_expirado")
        val estados = arrayOf("Todas", "No leídas", "Leídas")
        val estadoValues = arrayOf(null, 0, 1)

        var selectedTipo = 0
        var selectedEstado = 0

        AlertDialog.Builder(requireContext())
            .setTitle("Filtrar por tipo")
            .setSingleChoiceItems(tipos, selectedTipo) { _, which -> selectedTipo = which }
            .setPositiveButton("Siguiente") { _, _ ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Filtrar por estado")
                    .setSingleChoiceItems(estados, selectedEstado) { _, which -> selectedEstado = which }
                    .setPositiveButton("Aplicar") { _, _ ->
                        // Aplica los filtros seleccionados
                        viewModel.loadNotifications(
                            reset = true,
                            type = tipoValues[selectedTipo],
                            isRead = estadoValues[selectedEstado]
                        )
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showSortDialog() {
        val options = arrayOf(
            "Más recientes primero",
            "Más antiguos primero",
            "Tipo A-Z",
            "Tipo Z-A"
        )
        AlertDialog.Builder(requireContext())
            .setTitle("Ordenar notificaciones")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Más recientes primero
                        currentSortBy = "created_at"
                        currentSortDirection = "DESC"
                    }
                    1 -> { // Más antiguos primero
                        currentSortBy = "created_at"
                        currentSortDirection = "ASC"
                    }
                    2 -> { // Tipo A-Z
                        currentSortBy = "type"
                        currentSortDirection = "ASC"
                    }
                    3 -> { // Tipo Z-A
                        currentSortBy = "type"
                        currentSortDirection = "DESC"
                    }
                }
                // Recargar notificaciones con el nuevo orden
                viewModel.loadNotifications(
                    reset = true,
                    type = viewModel.currentType,
                    isRead = viewModel.currentIsRead,
                    sortBy = currentSortBy,
                    sortDirection = currentSortDirection
                )
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}