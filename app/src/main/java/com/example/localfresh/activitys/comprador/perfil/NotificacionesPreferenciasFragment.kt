package com.example.localfresh.activitys.comprador.perfil

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.localfresh.R
import com.example.localfresh.databinding.FragmentNotificacionesPreferenciasBinding
import com.example.localfresh.model.comprador.notificaciones.NotificationPreferencesRequest
import com.example.localfresh.viewmodel.comprador.notificaciones.PreferenciasNotificacionesViewModel
import com.google.android.material.snackbar.Snackbar

class NotificacionesPreferenciasFragment : Fragment() {

    private var _binding: FragmentNotificacionesPreferenciasBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PreferenciasNotificacionesViewModel by viewModels()
    private var userId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificacionesPreferenciasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener ID de usuario
        userId = requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            .getInt("USER_ID", -1)

        setupUI()
        setupObservers()

        if (userId != -1) {
            binding.progressBar.visibility = View.VISIBLE
            viewModel.getNotificationPreferences(userId)
        } else {
            showError("No se pudo obtener el ID de usuario")
        }
    }

    private fun setupUI() {
        // Configurar botón de regreso
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Configurar botón de guardar
        binding.btnGuardar.setOnClickListener {
            savePreferences()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnGuardar.isEnabled = !isLoading
        }

        viewModel.preferences.observe(viewLifecycleOwner) { preferences ->
            // Configurar switches
            binding.switchOfertas.isChecked = preferences.notif_ofertas == 1
            binding.switchNuevosProductos.isChecked = preferences.notif_nuevos_productos == 1
            binding.switchRecordatorios.isChecked = preferences.notif_recordatorios == 1

            // Configurar radio buttons
            val chipId = when (preferences.frequency) {
                "diaria" -> R.id.chipDiaria
                "semanal" -> R.id.chipSemanal
                "ofertas_especiales" -> R.id.chipOfertas
                "sin_restriccion" -> R.id.chipSinRestriccion
                else -> R.id.chipDiaria
            }
            binding.chipGroupFrecuencia.check(chipId)
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(resources.getColor(R.color.green, null))
                    .setTextColor(resources.getColor(R.color.white, null))
                    .show()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                showError(message)
            }
        }
    }

    private fun savePreferences() {
        if (userId == -1) {
            showError("No se pudo obtener el ID de usuario")
            return
        }

        // Obtener frecuencia seleccionada
        val frequency = when (binding.chipGroupFrecuencia.checkedChipId) {
            R.id.chipDiaria -> "diaria"
            R.id.chipSemanal -> "semanal"
            R.id.chipOfertas -> "ofertas_especiales"
            R.id.chipSinRestriccion -> "sin_restriccion"
            else -> "diaria"
        }

        // Obtener estados de los switches
        val notifOfertas = if (binding.switchOfertas.isChecked) 1 else 0
        val notifNuevosProductos = if (binding.switchNuevosProductos.isChecked) 1 else 0
        val notifRecordatorios = if (binding.switchRecordatorios.isChecked) 1 else 0

        // Crear solicitud
        val request = NotificationPreferencesRequest(
            user_id = userId,
            frequency = frequency,
            notif_ofertas = notifOfertas,
            notif_nuevos_productos = notifNuevosProductos,
            notif_recordatorios = notifRecordatorios
        )

        // Actualizar preferencias
        viewModel.updateNotificationPreferences(request)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.red, null))
            .setTextColor(resources.getColor(R.color.white, null))
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = NotificacionesPreferenciasFragment()
    }
}