package com.example.localfresh.activitys.comprador.paginaprincipal.fragments.filtros

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.example.localfresh.databinding.BottomSheetFiltrosMapaCompradorBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FiltrosMapaBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFiltrosMapaCompradorBinding? = null
    private val binding get() = _binding!!

    private var maxDistancia = 5 // km
    private var distanciaSeleccionada = 3
    private var tipoTiendaSeleccionado: String? = null
    private var esOrganico: Boolean = false

    // Listener para comunicar los filtros seleccionados
    private var filtrosListener: OnFiltrosMapaSeleccionadosListener? = null

    // Interface para comunicar los resultados
    interface OnFiltrosMapaSeleccionadosListener {
        fun onFiltrosSeleccionados(
            tiposTienda: List<String>,
            distanciaMaxima: Int,
            soloOrganicos: Boolean
        )
    }

    fun setFiltrosListener(listener: OnFiltrosMapaSeleccionadosListener) {
        this.filtrosListener = listener
    }

    // Metodo para establecer los valores iniciales de los filtros
    fun setFiltrosIniciales(
        tipoTienda: String?,
        distanciaMaxima: Int,
        soloOrganicos: Boolean
    ) {
        this.tipoTiendaSeleccionado = tipoTienda
        this.distanciaSeleccionada = distanciaMaxima
        this.esOrganico = soloOrganicos
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFiltrosMapaCompradorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTiposTienda()
        setupOrganicSwitch()
        setupDistanciaSeekBar()
        setupApplyButton()
        setupResetButton()
    }

    private fun setupTiposTienda() {
        // Seleccionar el chip correspondiente al tipo de tienda actualmente seleccionado
        if (tipoTiendaSeleccionado != null) {
            when (tipoTiendaSeleccionado) {
                "Supermercados" -> binding.chipSupermercados.isChecked = true
                "Mercados Locales" -> binding.chipMercadosLocales.isChecked = true
                "Tiendas Locales" -> binding.chipTiendasLocales.isChecked = true
            }
        }
    }

    private fun setupOrganicSwitch() {
        // Establecer el estado del switch de productos orgánicos
        binding.switchOrganicos.isChecked = esOrganico
    }

    private fun setupDistanciaSeekBar() {
        binding.seekBarDistancia.max = maxDistancia
        binding.seekBarDistancia.progress = distanciaSeleccionada

        // Actualizar texto de distancia seleccionada
        actualizarTextoDistancia(binding.seekBarDistancia.progress)

        binding.seekBarDistancia.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                actualizarTextoDistancia(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun actualizarTextoDistancia(distancia: Int) {
        binding.tvDistanciaSeleccionada.text = "$distancia km"
    }

    private fun setupApplyButton() {
        binding.btnAplicar.setOnClickListener {
            // Recolectar tipos de tienda seleccionados
            val tiposTienda = mutableListOf<String>()
            if (binding.chipSupermercados.isChecked) tiposTienda.add("Supermercados")
            if (binding.chipMercadosLocales.isChecked) tiposTienda.add("Mercados Locales")
            if (binding.chipTiendasLocales.isChecked) tiposTienda.add("Tiendas Locales")

            // Obtener distancia máxima seleccionada
            val distanciaMaxima = binding.seekBarDistancia.progress

            // Obtener si solo se muestran tiendas orgánicas
            val soloOrganicos = binding.switchOrganicos.isChecked

            // Comunicar los filtros seleccionados
            filtrosListener?.onFiltrosSeleccionados(
                tiposTienda = tiposTienda,
                distanciaMaxima = distanciaMaxima,
                soloOrganicos = soloOrganicos
            )

            // Cerrar el bottom sheet
            dismiss()
        }
    }

    private fun setupResetButton() {
        binding.btnReset.setOnClickListener {
            // Desmarcar todos los chips
            binding.chipSupermercados.isChecked = false
            binding.chipMercadosLocales.isChecked = false
            binding.chipTiendasLocales.isChecked = false

            // Restablecer la barra de distancia a un valor por defecto
            binding.seekBarDistancia.progress = 1

            // Desactivar el switch de orgánicos
            binding.switchOrganicos.isChecked = false

            // Aplicar los cambios
            filtrosListener?.onFiltrosSeleccionados(
                tiposTienda = emptyList(),
                distanciaMaxima = 1,
                soloOrganicos = false
            )

            // Cerrar el bottom sheet
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}