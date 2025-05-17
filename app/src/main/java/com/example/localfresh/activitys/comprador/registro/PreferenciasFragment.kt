package com.example.localfresh.activitys.comprador.registro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.localfresh.R
import com.example.localfresh.databinding.FragmentPreferenciasBinding
import com.example.localfresh.utils.PreferenciasChipUtils
import com.google.android.material.chip.Chip

class PreferenciasFragment : Fragment() {

    private var _binding: FragmentPreferenciasBinding? = null
    private val binding get() = _binding!!

    private lateinit var chipsCategorias: Map<String, Chip>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreferenciasBinding.inflate(inflater, container, false)

        // Obtener mapa de chips usando la utilidad centralizada
        chipsCategorias = PreferenciasChipUtils.getChipsMapFromRegistrationBinding(binding)

        // Aplicar animación de entrada
        val layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_staggered_fade_in)
        binding.chipGroupPreferencias.layoutAnimation = layoutAnimation

        setupListeners()
        return binding.root
    }

    private fun setupListeners() {
        binding.btnSiguiente.setOnClickListener {
            // Usar la utilidad para obtener las preferencias seleccionadas
            val preferencias = PreferenciasChipUtils.getSelectedPreferences(chipsCategorias)
            (activity as CompletarPerfilCompradorActivity).guardarPreferencias(preferencias)
            (activity as CompletarPerfilCompradorActivity).mostrarSeleccionarDistanciaBottomSheet()
        }

        // Configurar botón de categorías usando la utilidad
        PreferenciasChipUtils.setupCategoriesButton(
            binding.btnCategorias,
            binding.chipGroupPreferencias.parent.parent as View,
            R.drawable.ic_arrow_right,
            R.drawable.down_arrow
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Evitar memory leaks
    }
}