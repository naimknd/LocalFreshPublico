package com.example.localfresh.activitys.comprador.perfil

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.localfresh.R
import com.example.localfresh.databinding.FragmentEditarPreferenciasBinding
import com.example.localfresh.utils.PreferenciasChipUtils
import com.example.localfresh.viewmodel.comprador.signup.CompletarPerfilViewModel
import com.google.android.material.chip.Chip

class EditarPreferenciasFragment : Fragment() {

    private var _binding: FragmentEditarPreferenciasBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CompletarPerfilViewModel by viewModels()
    private var userId: Int = -1
    private lateinit var chipsCategorias: Map<String, Chip>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditarPreferenciasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el ID del usuario
        userId = requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            .getInt("USER_ID", -1)

        // Obtener mapa de chips usando la utilidad
        chipsCategorias = PreferenciasChipUtils.getChipsMapFromEditBinding(binding)

        setupUI()
        setupObservers()

        // Cargar preferencias si hay un usuario válido
        if (userId != -1) {
            binding.progressBar.visibility = View.VISIBLE
            viewModel.obtenerPreferencias(userId)
        } else {
            Toast.makeText(requireContext(), "No se pudo obtener el ID de usuario", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupUI() {
        // Configurar botón de retroceso
        binding.btnBack?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Configurar el botón de categorías usando la utilidad centralizada
        PreferenciasChipUtils.setupCategoriesButton(
            binding.btnCategorias,
            binding.chipGroupPreferencias.parent.parent as View,
            R.drawable.ic_arrow_right,
            R.drawable.down_arrow
        )

        // Configurar SeekBar
        binding.seekBarRangoDistancia.max = 5
        binding.seekBarRangoDistancia.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Asegurar valor mínimo de 1
                val realProgress = if (progress < 1) 1 else progress
                binding.tvRangoDistancia.text = "Rango de distancia: $realProgress km"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Configurar botón guardar
        binding.btnGuardarCambios.setOnClickListener {
            guardarPreferencias()
        }
    }

    private fun setupObservers() {
        // Observar las preferencias cargadas
        viewModel.preferencias.observe(viewLifecycleOwner) { preferencias ->
            binding.progressBar.visibility = View.GONE

            preferencias?.let {
                // Actualizar chips usando la utilidad centralizada
                PreferenciasChipUtils.updateChipsFromPreferences(chipsCategorias, it)

                // Actualizar rango de distancia
                val rangoDistancia = (it.rango_distancia ?: 3).coerceIn(1, 5)
                binding.seekBarRangoDistancia.progress = rangoDistancia
                binding.tvRangoDistancia.text = "Rango de distancia: $rangoDistancia km"
            }
        }

        // Observar mensajes del guardado
        viewModel.responseMessage.observe(viewLifecycleOwner) { message ->
            binding.progressBar.visibility = View.GONE
            binding.btnGuardarCambios.isEnabled = true

            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

            if (message.contains("guardadas correctamente")) {
                parentFragmentManager.popBackStack()
            }
        }

        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnGuardarCambios.isEnabled = !isLoading
        }
    }

    private fun guardarPreferencias() {
        if (userId == -1) {
            Toast.makeText(requireContext(), "No se pudo obtener el ID de usuario", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnGuardarCambios.isEnabled = false

        // Usar la utilidad para crear el request con las preferencias seleccionadas
        val request = PreferenciasChipUtils.createRequestFromSelection(
            userId,
            chipsCategorias,
            binding.seekBarRangoDistancia.progress
        )

        viewModel.guardarPreferencias(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = EditarPreferenciasFragment()
    }
}