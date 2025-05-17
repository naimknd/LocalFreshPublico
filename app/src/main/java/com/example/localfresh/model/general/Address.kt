package com.example.localfresh.model.general

import com.google.gson.annotations.SerializedName

class Address {
    @SerializedName("road")
    var road: String? = null

    @SerializedName("house_number")
    var houseNumber: String? = null

    @SerializedName("suburb")
    var suburb: String? = null

    @SerializedName("city")
    var city: String? = null

    @SerializedName("state")
    var state: String? = null

    @SerializedName("postcode")
    var postcode: String? = null

    // Metodo para obtener la dirección formateada
    fun getFormattedAddress(): String {
        return listOfNotNull(road, houseNumber, suburb, city, state, postcode)
            .joinToString(", ")
    }
}
