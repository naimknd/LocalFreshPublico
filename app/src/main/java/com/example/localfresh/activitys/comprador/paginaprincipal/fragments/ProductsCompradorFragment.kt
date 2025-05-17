package com.example.localfresh.activitys.comprador.paginaprincipal.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localfresh.R
import com.example.localfresh.adapters.comprador.ProductosAdapterComprador
import com.example.localfresh.databinding.FragmentProductsCompradorBinding
import com.example.localfresh.model.comprador.paginaprincipal.ProductoData
import com.example.localfresh.utils.CategoryFilterBottomSheet
import com.example.localfresh.utils.DateUtils
import com.example.localfresh.utils.DiscountUtils
import com.example.localfresh.viewmodel.comprador.paginaprincipal.TiendaInfoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductsCompradorFragment : Fragment() {
    private var _binding: FragmentProductsCompradorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TiendaInfoViewModel by viewModels()
    private lateinit var productosAdapter: ProductosAdapterComprador

    // Filtros y búsqueda
    private var originalProductos: List<ProductosAdapterComprador.ProductoConUnidad> = emptyList()
    private var searchQuery = ""
    private var selectedFilters = emptyList<String>()
    private var selectedMinDate: String? = null
    private var selectedMaxDate: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductsCompradorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        loadProducts()
    }

    private fun setupUI() = with(binding) {
        // RecyclerView
        recyclerViewProductos.layoutManager = LinearLayoutManager(context)

        // SearchView
        setupSearchView()

        // SwipeRefresh
        swipeRefreshLayout.setOnRefreshListener { loadProducts() }

        // Filtros
        buttonFiltros.setOnClickListener { mostrarBottomSheetFiltros() }

        buttonOrdenar.setOnClickListener { mostrarDialogoOrdenamiento() }
    }

    private fun setupSearchView() = with(binding.searchView) {

        queryHint = getString(R.string.search_hint)
        isIconified = false

        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Ocultar teclado al enviar búsqueda
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(windowToken, 0)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText ?: ""
                filtrarProductos()
                return true
            }
        })

        setOnCloseListener {
            searchQuery = ""
            if (hasActiveFilters()) {
                filtrarProductos()
            } else if (::productosAdapter.isInitialized) {
                productosAdapter.updateData(originalProductos)
                actualizarVisibilidad(originalProductos)
            }
            false
        }
    }

    private fun observeViewModel() {
        viewModel.productosData.observe(viewLifecycleOwner) { productos ->
            binding.swipeRefreshLayout.isRefreshing = false

            if (productos.isNullOrEmpty()) {
                updateVisibility(empty = true)
                return@observe
            }

            updateVisibility(empty = false)
            processProducts(productos)
        }
    }

    private fun updateVisibility(empty: Boolean) = with(binding) {
        recyclerViewProductos.isVisible = !empty
        emptyStateLayout.isVisible = empty
    }

    private fun processProducts(productos: List<ProductoData>) {
        // Organizar productos con unidades y ordenar por fecha
        val dateFormat = SimpleDateFormat(DateUtils.DATE_FORMAT, Locale.getDefault())
        val productosConUnidad = productos.flatMap { producto ->
            producto.units.map { ProductosAdapterComprador.ProductoConUnidad(producto, it) }
        }.sortedBy {
            try { dateFormat.parse(it.unidad.expiry_date) } catch (e: Exception) { Date(Long.MAX_VALUE) }
        }

        originalProductos = productosConUnidad

        // Inicializar o actualizar el adapter
        if (!::productosAdapter.isInitialized) {
            productosAdapter = ProductosAdapterComprador(productosConUnidad, viewModel, parentFragmentManager)
            binding.recyclerViewProductos.adapter = productosAdapter
        } else if (hasActiveFilters()) {
            filtrarProductos()
        } else {
            productosAdapter.updateData(productosConUnidad)
        }
    }

    private fun hasActiveFilters() =
        searchQuery.isNotEmpty() || selectedFilters.isNotEmpty() ||
                selectedMinDate != null || selectedMaxDate != null

    private fun loadProducts() {
        val sellerId = arguments?.getInt("SELLER_ID") ?: 0
        viewModel.obtenerProductosTienda(sellerId)
    }

    private fun mostrarBottomSheetFiltros() {
        CategoryFilterBottomSheet.newInstance(selectedFilters)
            .setOnCategoriesSelectedListener { filtros ->
                selectedFilters = filtros
                filtrarProductos()
            }
            .show(parentFragmentManager, "FiltrosCategorias")
    }

    private fun filtrarProductos() {
        if (!::productosAdapter.isInitialized) return

        // Aplicar filtros progresivamente
        var filtrados = originalProductos

        // Filtrar por búsqueda
        if (searchQuery.isNotEmpty()) {
            filtrados = filtrados.filter { item ->
                item.producto.run {
                    product_name.contains(searchQuery, ignoreCase = true) ||
                            product_category.contains(searchQuery, ignoreCase = true) ||
                            product_description?.contains(searchQuery, ignoreCase = true) ?: false
                }
            }
        }

        // Filtrar por categoría y fecha
        if (selectedFilters.isNotEmpty() || selectedMinDate != null || selectedMaxDate != null) {
            filtrados = filtrados.filter { item ->
                val cumpleFiltroCategoria = selectedFilters.isEmpty() ||
                        selectedFilters.contains(item.producto.product_category)
                val cumpleFechas = DateUtils.estaDentroDeRango(
                    item.unidad.expiry_date, selectedMinDate, selectedMaxDate
                )
                cumpleFiltroCategoria && cumpleFechas
            }
        }

        productosAdapter.updateData(filtrados)
        actualizarVisibilidad(filtrados)
    }

    private fun aplicarFiltros(filtros: List<String>, minDate: String?, maxDate: String?) {
        if (!::productosAdapter.isInitialized) return

        val filtrados = originalProductos.filter { item ->
            val cumpleFiltroCategoria = filtros.isEmpty() ||
                    filtros.contains(item.producto.product_category)
            val cumpleFechas = DateUtils.estaDentroDeRango(
                item.unidad.expiry_date, minDate, maxDate
            )
            cumpleFiltroCategoria && cumpleFechas
        }

        productosAdapter.updateData(filtrados)
        actualizarVisibilidad(filtrados)
    }

    private fun mostrarDialogoOrdenamiento() {
        val opciones = arrayOf(
            "Fecha de caducidad (más cercana)",
            "Fecha de caducidad (más lejana)",
            "Precio (mayor a menor)",
            "Precio (menor a mayor)",
            "Nombre (A-Z)",
            "Nombre (Z-A)"
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Ordenar productos")
            .setItems(opciones) { _, which ->
                ordenarProductos(which)
            }
            .show()
    }

    private fun ordenarProductos(opcion: Int) {
        if (!::productosAdapter.isInitialized || originalProductos.isEmpty()) return

        // Obtener los productos a ordenar (filtrados o todos)
        var productosAOrdenar = if (hasActiveFilters()) {
            // Si hay filtros activos, ordenar solo los productos filtrados
            productosAdapter.getCurrentItems()
        } else {
            originalProductos
        }

        // Ordenar según la opción seleccionada
        val productosOrdenados = when (opcion) {
            0 -> { // Fecha de caducidad (más cercana)
                productosAOrdenar.sortedBy {
                    DateUtils.calcularDiasRestantes(it.unidad.expiry_date)
                }
            }
            1 -> { // Fecha de caducidad (más lejana)
                productosAOrdenar.sortedByDescending {
                    DateUtils.calcularDiasRestantes(it.unidad.expiry_date)
                }
            }
            2 -> { // Precio (mayor a menor)
                productosAOrdenar.sortedByDescending {
                    DiscountUtils.calculateDiscountPrice(it.producto.product_price, it.unidad.expiry_date)
                }
            }
            3 -> { // Precio (menor a mayor)
                productosAOrdenar.sortedBy {
                    DiscountUtils.calculateDiscountPrice(it.producto.product_price, it.unidad.expiry_date)
                }
            }
            4 -> { // Nombre (A-Z)
                productosAOrdenar.sortedBy { it.producto.product_name }
            }
            5 -> { // Nombre (Z-A)
                productosAOrdenar.sortedByDescending { it.producto.product_name }
            }
            else -> productosAOrdenar
        }

        // Actualizar el adapter con los productos ordenados
        productosAdapter.updateData(productosOrdenados)
    }

    private fun actualizarVisibilidad(productos: List<ProductosAdapterComprador.ProductoConUnidad>) {
        updateVisibility(productos.isEmpty())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.productosData.removeObservers(viewLifecycleOwner)
        binding.recyclerViewProductos.adapter = null
        binding.swipeRefreshLayout.apply {
            isRefreshing = false
            setOnRefreshListener(null)
        }
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        _binding?.let {
            if (::productosAdapter.isInitialized) {
                it.recyclerViewProductos.adapter = productosAdapter
                it.recyclerViewProductos.isVisible = true
                it.emptyStateLayout.isVisible = false
                it.swipeRefreshLayout.isRefreshing = true
                loadProducts()
            } else {
                setupUI()
                it.swipeRefreshLayout.isRefreshing = true
                loadProducts()
            }
        }
    }
}