package com.example.localfresh.activitys.comprador.favoritos

import com.example.localfresh.R
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localfresh.activitys.comprador.paginaprincipal.fragments.DetalleProductoCompradorFragment
import com.example.localfresh.activitys.comprador.paginaprincipal.fragments.StoreInfoBottomSheetFragment
import com.example.localfresh.adapters.comprador.favoritos.FavoritesProductAdapter
import com.example.localfresh.adapters.comprador.favoritos.FavoritesStoreAdapter
import com.example.localfresh.databinding.FragmentFavoritosBinding
import com.example.localfresh.model.comprador.favoritos.ProductFavorite
import com.example.localfresh.model.comprador.favoritos.StoreFavorite
import com.example.localfresh.viewmodel.comprador.favoritos.FavoritesViewModel
import com.google.android.material.tabs.TabLayout

class FavoritosFragment : Fragment() {

    private var _binding: FragmentFavoritosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoritesViewModel by viewModels()
    private lateinit var storesAdapter: FavoritesStoreAdapter
    private lateinit var productsAdapter: FavoritesProductAdapter

    companion object {
        private val FILTER_ALL: String? = null
        private const val FILTER_STORES = "stores"
        private const val FILTER_PRODUCTS = "products"
        private const val TAB_ALL = 0
        private const val TAB_STORES = 1
        private const val TAB_PRODUCTS = 2

        fun newInstance() = FavoritosFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupTabs()
        setupObservers()
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites() // Recargar favoritos al volver al fragmento
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerViews() = with(binding) {
        // Configurar adapter para tiendas favoritas
        storesAdapter = FavoritesStoreAdapter { store ->
            StoreInfoBottomSheetFragment.newInstance(store)
                .show(parentFragmentManager, "StoreInfoBottomSheet")
        }

        recyclerViewStores.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = storesAdapter
        }

        // Configurar adapter para productos favoritos
        productsAdapter = FavoritesProductAdapter { product ->
            navigateToProductDetail(product.unit_id)
        }

        recyclerViewProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productsAdapter
        }
    }

    private fun navigateToProductDetail(unitId: Int) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DetalleProductoCompradorFragment.newInstance(unitId))
            .addToBackStack(null)
            .commit()
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    TAB_ALL -> loadFavorites(FILTER_ALL)
                    TAB_STORES -> {
                        hideProductsSection()
                        loadFavorites(FILTER_STORES)
                    }
                    TAB_PRODUCTS -> {
                        hideStoresSection()
                        loadFavorites(FILTER_PRODUCTS)
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun hideProductsSection() = with(binding) {
        recyclerViewProducts.visibility = View.GONE
        emptyStateProducts.visibility = View.GONE
        productsLabel.visibility = View.GONE
    }

    private fun hideStoresSection() = with(binding) {
        recyclerViewStores.visibility = View.GONE
        emptyStateStores.visibility = View.GONE
        storesLabel.visibility = View.GONE
    }

    private fun loadFavorites(type: String? = FILTER_ALL) {
        val userId = requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            .getInt("USER_ID", -1)

        if (userId != -1) {
            viewModel.getFavorites(userId, type)
            binding.progressBar.visibility = View.VISIBLE
        } else {
            showErrorMessage("Debes iniciar sesión para ver tus favoritos")
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.favorites.observe(viewLifecycleOwner) { response ->
            if (response?.status == "success") {
                updateUI(response.stores, response.products)
            } else {
                showErrorMessage(response?.message ?: "Error al cargar favoritos")
            }
        }
    }

    private fun updateUI(stores: List<StoreFavorite>?, products: List<ProductFavorite>?) {
        val currentTab = binding.tabLayout.selectedTabPosition
        updateStoresSection(stores, currentTab)
        updateProductsSection(products, currentTab)
        updateEmptyStateMessages(currentTab)
    }

    private fun updateStoresSection(stores: List<StoreFavorite>?, currentTab: Int) {
        if (stores == null) return

        val showStoresSection = currentTab == TAB_ALL || currentTab == TAB_STORES
        binding.storesLabel.visibility = if (showStoresSection) View.VISIBLE else View.GONE

        if (showStoresSection) {
            val isEmpty = stores.isEmpty()
            binding.emptyStateStores.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.recyclerViewStores.visibility = if (isEmpty) View.GONE else View.VISIBLE

            if (!isEmpty) storesAdapter.submitList(stores)
        }
    }

    private fun updateProductsSection(products: List<ProductFavorite>?, currentTab: Int) {
        if (products == null) return

        val showProductsSection = currentTab == TAB_ALL || currentTab == TAB_PRODUCTS
        binding.productsLabel.visibility = if (showProductsSection) View.VISIBLE else View.GONE

        if (showProductsSection) {
            val isEmpty = products.isEmpty()
            binding.emptyStateProducts.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.recyclerViewProducts.visibility = if (isEmpty) View.GONE else View.VISIBLE

            if (!isEmpty) productsAdapter.submitList(products)
        }
    }

    private fun updateEmptyStateMessages(currentTab: Int) = with(binding) {
        when (currentTab) {
            TAB_ALL -> {
                emptyStateStores.text = "No tienes tiendas favoritas"
                emptyStateProducts.text = "No tienes productos favoritos"
            }
            TAB_STORES -> emptyStateStores.text = "No tienes tiendas favoritas"
            TAB_PRODUCTS -> emptyStateProducts.text = "No tienes productos favoritos"
        }
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

        val currentTab = binding.tabLayout.selectedTabPosition

        if (currentTab == TAB_ALL || currentTab == TAB_STORES) {
            binding.emptyStateStores.text = message
            binding.emptyStateStores.visibility = View.VISIBLE
            binding.recyclerViewStores.visibility = View.GONE
        }

        if (currentTab == TAB_ALL || currentTab == TAB_PRODUCTS) {
            binding.emptyStateProducts.text = message
            binding.emptyStateProducts.visibility = View.VISIBLE
            binding.recyclerViewProducts.visibility = View.GONE
        }
    }
}