package com.example.localfresh.utils

import android.view.View
import com.example.localfresh.databinding.FragmentPreferenciasBinding
import com.example.localfresh.databinding.FragmentEditarPreferenciasBinding
import com.example.localfresh.model.comprador.PreferenciasCompradorRequest
import com.google.android.material.chip.Chip

/**
 * Clase de utilidad para manejar los chips de preferencias tanto en el registro como en la edición
 */
object PreferenciasChipUtils {

    /**
     * Obtiene un mapa de chips desde el binding del fragmento de registro de preferencias
     */
    fun getChipsMapFromRegistrationBinding(binding: FragmentPreferenciasBinding): Map<String, Chip> {
        return mapOf(
            "preferencias_lacteos" to binding.chipLacteos,
            "preferencias_carnes" to binding.chipCarnes,
            "preferencias_huevos" to binding.chipHuevos,
            "preferencias_granos_cereales" to binding.chipGranosCereales,
            "preferencias_legumbres" to binding.chipLegumbres,
            "preferencias_panaderia" to binding.chipPanaderia,
            "preferencias_bebidas" to binding.chipBebidas,
            "preferencias_snacks" to binding.chipSnacks,
            "preferencias_congelados" to binding.chipCongelados,
            "preferencias_comida_mascotas" to binding.chipComidaMascotas
        )
    }

    /**
     * Obtiene un mapa de chips desde el binding del fragmento de edición de preferencias
     */
    fun getChipsMapFromEditBinding(binding: FragmentEditarPreferenciasBinding): Map<String, Chip> {
        return mapOf(
            "preferencias_lacteos" to binding.chipLacteos,
            "preferencias_carnes" to binding.chipCarnes,
            "preferencias_huevos" to binding.chipHuevos,
            "preferencias_granos_cereales" to binding.chipGranosCereales,
            "preferencias_legumbres" to binding.chipLegumbres,
            "preferencias_panaderia" to binding.chipPanaderia,
            "preferencias_bebidas" to binding.chipBebidas,
            "preferencias_snacks" to binding.chipSnacks,
            "preferencias_congelados" to binding.chipCongelados,
            "preferencias_comida_mascotas" to binding.chipComidaMascotas
        )
    }

    /**
     * Obtiene las preferencias seleccionadas a partir de un mapa de chips
     */
    fun getSelectedPreferences(chipsMap: Map<String, Chip>): Map<String, Int> {
        val preferencias = mutableMapOf<String, Int>()
        for ((key, chip) in chipsMap) {
            preferencias[key] = if (chip.isChecked) 1 else 0
        }
        return preferencias
    }

    /**
     * Actualiza los chips según las preferencias guardadas en la base de datos
     */
    fun updateChipsFromPreferences(chipsMap: Map<String, Chip>, preferencias: PreferenciasCompradorRequest) {
        chipsMap["preferencias_lacteos"]?.isChecked = preferencias.preferencias_lacteos == 1
        chipsMap["preferencias_carnes"]?.isChecked = preferencias.preferencias_carnes == 1
        chipsMap["preferencias_huevos"]?.isChecked = preferencias.preferencias_huevos == 1
        chipsMap["preferencias_granos_cereales"]?.isChecked = preferencias.preferencias_granos_cereales == 1
        chipsMap["preferencias_legumbres"]?.isChecked = preferencias.preferencias_legumbres == 1
        chipsMap["preferencias_panaderia"]?.isChecked = preferencias.preferencias_panaderia == 1
        chipsMap["preferencias_bebidas"]?.isChecked = preferencias.preferencias_bebidas == 1
        chipsMap["preferencias_snacks"]?.isChecked = preferencias.preferencias_snacks == 1
        chipsMap["preferencias_congelados"]?.isChecked = preferencias.preferencias_congelados == 1
        chipsMap["preferencias_comida_mascotas"]?.isChecked = preferencias.preferencias_comida_mascotas == 1
    }

    /**
     * Configura el botón de categorías para mostrar/ocultar los chips
     */
    fun setupCategoriesButton(
        btnCategorias: View,
        chipGroupContainer: View,
        downArrowResId: Int,
        upArrowResId: Int
    ) {
        btnCategorias.setOnClickListener {
            if (chipGroupContainer.visibility == View.VISIBLE) {
                chipGroupContainer.visibility = View.GONE
                (btnCategorias as? android.widget.Button)?.setCompoundDrawablesWithIntrinsicBounds(0, 0, downArrowResId, 0)
            } else {
                chipGroupContainer.visibility = View.VISIBLE
                (btnCategorias as? android.widget.Button)?.setCompoundDrawablesWithIntrinsicBounds(0, 0, upArrowResId, 0)
            }
        }
    }

    /**
     * Crea un objeto PreferenciasCompradorRequest a partir de las preferencias seleccionadas
     */
    fun createRequestFromSelection(userId: Int, chipsMap: Map<String, Chip>, distanceRange: Int): PreferenciasCompradorRequest {
        val preferencias = getSelectedPreferences(chipsMap)
        return PreferenciasCompradorRequest(
            user_id = userId,
            preferencias_lacteos = preferencias["preferencias_lacteos"] ?: 0,
            preferencias_carnes = preferencias["preferencias_carnes"] ?: 0,
            preferencias_huevos = preferencias["preferencias_huevos"] ?: 0,
            preferencias_granos_cereales = preferencias["preferencias_granos_cereales"] ?: 0,
            preferencias_legumbres = preferencias["preferencias_legumbres"] ?: 0,
            preferencias_panaderia = preferencias["preferencias_panaderia"] ?: 0,
            preferencias_bebidas = preferencias["preferencias_bebidas"] ?: 0,
            preferencias_snacks = preferencias["preferencias_snacks"] ?: 0,
            preferencias_congelados = preferencias["preferencias_congelados"] ?: 0,
            preferencias_comida_mascotas = preferencias["preferencias_comida_mascotas"] ?: 0,
            rango_distancia = distanceRange
        )
    }
}