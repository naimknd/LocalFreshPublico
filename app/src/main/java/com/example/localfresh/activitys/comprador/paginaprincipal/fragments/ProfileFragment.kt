package com.example.localfresh.activitys.comprador.paginaprincipal.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.localfresh.R
import com.example.localfresh.activitys.LogInActivity
import com.example.localfresh.activitys.comprador.apartados.VerApartadosFragment
import com.example.localfresh.activitys.comprador.estadisticas.EstadisticasFragment
import com.example.localfresh.activitys.comprador.favoritos.FavoritosFragment
import com.example.localfresh.activitys.comprador.perfil.EditarPreferenciasFragment
import com.example.localfresh.activitys.comprador.perfil.InformacionCuentaFragment
import com.example.localfresh.activitys.comprador.perfil.NotificacionesPreferenciasFragment
import com.example.localfresh.databinding.FragmentPerfilCompradorBinding
import com.example.localfresh.viewmodel.LogoutViewModel
import com.example.localfresh.viewmodel.comprador.perfil.UserProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentPerfilCompradorBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private val logoutViewModel: LogoutViewModel by viewModels()
    private val userProfileViewModel: UserProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilCompradorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
        setupUserProfile()
        setupLogout()
        setupCardClickListeners()
    }

    private fun setupUserProfile() {
        val userId = sharedPreferences.getInt("USER_ID", -1)
        if (userId != -1) {
            showLoading(true)
            userProfileViewModel.fetchUserProfile(userId)
        } else {
            showError("ID de usuario no válido")
        }

        userProfileViewModel.userProfile.observe(viewLifecycleOwner) { response ->
            showLoading(false)
            if (response != null) {
                response.user.let { user ->
                    binding.txtUsername.text = user.username
                }
            } else {
                showError("Error al cargar el perfil")
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentContainer.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        showLoading(false)
    }

    private fun setupLogout() {
        binding.logoutButton.setOnClickListener {
            val token = sharedPreferences.getString("TOKEN", null)
            if (token != null) {
                logoutViewModel.logout(token)
            }
        }

        logoutViewModel.logoutResponse.observe(viewLifecycleOwner) { response ->
            if (response != null && response.status == "success") {
                clearUserDataAndNavigateToLogin()
            } else {
                showError("Error al cerrar sesión")
            }
        }

        logoutViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            showError(error)
        }
    }

    private fun clearUserDataAndNavigateToLogin() {
        sharedPreferences.edit().clear().apply()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LogInActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun setupCardClickListeners() = with(binding) {
        // Mapear cada tarjeta a su fragmento correspondiente
        val cardToFragmentMap = mapOf(
            cardAccountInfo to InformacionCuentaFragment.newInstance(),
            cardPreferences to EditarPreferenciasFragment.newInstance(),
            cardStats to EstadisticasFragment(),
            cardFavorites to FavoritosFragment(),
            cardNotifications to NotificacionesPreferenciasFragment.newInstance()
        )

        // Configurar listeners para cada tarjeta
        cardToFragmentMap.forEach { (card, fragment) ->
            card.setOnClickListener { navigateToFragment(fragment) }
        }

        // Caso especial para historial de compras, que requiere argumentos
        cardPurchaseHistory.setOnClickListener {
            val historyFragment = VerApartadosFragment().apply {
                arguments = Bundle().apply { putBoolean("SHOW_HISTORY", true) }
            }
            navigateToFragment(historyFragment)
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}