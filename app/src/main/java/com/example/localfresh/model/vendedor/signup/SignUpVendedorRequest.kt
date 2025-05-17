package com.example.localfresh.model.vendedor.signup

data class SignUpVendedorRequest(
    val store_name: String,
    val email: String,
    val password: String,
    val store_description: String,
    val store_phone: String,
    val store_address: String,
    val latitude: Double,
    val longitude: Double,
    val organic_products: Int, // 1 para "Sí", 0 para "No"
    val store_type: String, // Tipo de tienda
    val opening_time: String, // Agregar de apertura
    val closing_time: String // Agregar de cierre
)