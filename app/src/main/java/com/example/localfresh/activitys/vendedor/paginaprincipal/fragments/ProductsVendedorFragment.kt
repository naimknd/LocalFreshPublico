package com.example.localfresh.activitys.vendedor.paginaprincipal.fragments

import com.example.localfresh.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localfresh.adapters.vendedor.ProductoAdapterVendedor
import com.example.localfresh.databinding.FragmentVerProductosVendedorBinding
import com.example.localfresh.model.vendedor.producto.InfoProductSeller
import com.example.localfresh.utils.CategoryFilterBottomSheet
import com.example.localfresh.viewmodel.vendedor.paginaprincipal.VerProductoInfoViewModel
import com.google.android.material.snackbar.Snackbar

class ProductsVendedorFragment : Fragment() {

    private var _binding: FragmentVerProductosVendedorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VerProductoInfoViewModel by viewModels()
    private lateinit var adapter: ProductoAdapterVendedor
    private var sellerId: Int = -1
    private var selectedFilters: List<String> = emptyList()
    private var originalProductos: List<InfoProductSeller> = emptyList()
    private var searchQuery: String = ""

    private val editProductLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == AppCompatActivity.RESULT_OK) loadProducts()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVerProductosVendedorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sellerId = requireContext().getSharedPreferences("LocalFreshPrefs", AppCompatActivity.MODE_PRIVATE)
            .getInt("SELLER_ID", -1)

        setupUI()
        setupObservers()
        loadProducts()
    }

    private fun setupUI() {
        // RecyclerView
        adapter = ProductoAdapterVendedor(editProductLauncher, viewModel, parentFragmentManager)
        binding.recyclerViewProductos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewProductos.adapter = adapter

        // SearchView
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText ?: ""
                filtrarProductos()
                return true
            }
            override fun onQueryTextSubmit(query: String?) = false
        })

        binding.searchView.setOnCloseListener {
            searchQuery = ""
            if (selectedFilters.isNotEmpty()) filtrarProductos()
            else if (originalProductos.isNotEmpty()) adapter.setProducts(originalProductos)
            actualizarVisibilidad(originalProductos)
            false
        }

        // SwipeRefresh, Filtro y FAB
        binding.swipeRefreshLayout.setOnRefreshListener { loadProducts() }

        binding.buttonFiltros.setOnClickListener {
            CategoryFilterBottomSheet.newInstance(selectedFilters)
                .setCategoryListener(object : CategoryFilterBottomSheet.CategoryListener {
                    override fun onCategoriesSelected(categories: List<String>) {
                        selectedFilters = categories
                        filtrarProductos()
                    }
                })
                .show(childFragmentManager, "FiltrosCategorias")
        }

        binding.fabAgregarProducto.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AgregarProductoVendedorFragment.newInstance(sellerId))
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadProducts() {
        if (sellerId != -1) {
            binding.progressBar.visibility = View.VISIBLE
            viewModel.getProductInfo(sellerId)
        } else {
            Snackbar.make(binding.root, "Error al obtener ID del vendedor", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.loadingMessage.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.productInfo.observe(viewLifecycleOwner) { response ->
            binding.swipeRefreshLayout.isRefreshing = false

            if (response != null && response.status == "success" && !response.data.isNullOrEmpty()) {
                originalProductos = response.data
                adapter.setProducts(response.data)
                binding.buttonFiltros.visibility = View.VISIBLE
                binding.recyclerViewProductos.visibility = View.VISIBLE
                binding.emptyStateView.visibility = View.GONE

                if (searchQuery.isNotEmpty() || selectedFilters.isNotEmpty()) {
                    filtrarProductos()
                }
            } else {
                mostrarEstadoVacio(response?.message ?: "Error desconocido")
            }
        }

        viewModel.deleteSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Snackbar.make(binding.root, "Producto eliminado correctamente", Snackbar.LENGTH_SHORT).show()
                loadProducts()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun filtrarProductos() {
        if (!::adapter.isInitialized || originalProductos.isEmpty()) return

        val productosFiltrados = originalProductos.filter { producto ->
            // Combinar condiciones de búsqueda y filtros en una sola expresión
            (searchQuery.isEmpty() ||
                    (producto.name ?: "").contains(searchQuery, ignoreCase = true) ||
                    (producto.category ?: "").contains(searchQuery, ignoreCase = true) ||
                    (producto.description ?: "").contains(searchQuery, ignoreCase = true)) &&
                    (selectedFilters.isEmpty() ||
                            (producto.category != null && selectedFilters.contains(producto.category)))
        }

        adapter.setProducts(productosFiltrados)
        actualizarVisibilidad(productosFiltrados)
    }

    private fun actualizarVisibilidad(productos: List<InfoProductSeller>) {
        val isEmpty = productos.isEmpty()
        binding.recyclerViewProductos.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyStateView.visibility = if (isEmpty) View.VISIBLE else View.GONE

        if (isEmpty) {
            binding.emptyStateView.text = when {
                searchQuery.isNotEmpty() && selectedFilters.isNotEmpty() ->
                    "No encontramos productos que coincidan con \"$searchQuery\" en las categorías seleccionadas"
                searchQuery.isNotEmpty() ->
                    "No encontramos productos que coincidan con \"$searchQuery\""
                selectedFilters.isNotEmpty() ->
                    "No hay productos en las categorías seleccionadas"
                else -> "¡Empieza a subir tus productos!\n\nToca el botón + para agregar tu primer producto"
            }
        }
    }

    private fun mostrarEstadoVacio(mensaje: String) {
        binding.buttonFiltros.visibility = View.GONE
        binding.recyclerViewProductos.visibility = View.GONE
        binding.emptyStateView.visibility = View.VISIBLE
        binding.emptyStateView.text = when {
            mensaje.contains("Error") -> mensaje
            searchQuery.isNotEmpty() && selectedFilters.isNotEmpty() ->
                "No encontramos productos que coincidan con \"$searchQuery\" en las categorías seleccionadas"
            searchQuery.isNotEmpty() ->
                "No encontramos productos que coincidan con \"$searchQuery\""
            selectedFilters.isNotEmpty() ->
                "No hay productos en las categorías seleccionadas"
            else -> "¡Empieza a subir tus productos!\n\nToca el botón + para agregar tu primer producto"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
    }
}
