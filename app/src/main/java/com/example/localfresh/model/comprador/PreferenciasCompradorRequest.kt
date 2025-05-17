package com.example.localfresh.model.comprador

data class PreferenciasCompradorRequest(
    val user_id: Int,
    val preferencias_bebidas: Int,
    val preferencias_carnes: Int,
    val preferencias_comida_mascotas: Int,
    val preferencias_congelados: Int,
    val preferencias_granos_cereales: Int,
    val preferencias_huevos: Int,
    val preferencias_lacteos: Int,
    val preferencias_legumbres: Int,
    val preferencias_panaderia: Int,
    val preferencias_snacks: Int,
    val rango_distancia: Int
)