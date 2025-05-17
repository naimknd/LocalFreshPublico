package com.example.localfresh.model.general

import com.google.gson.annotations.SerializedName

class LocationResult(
    @JvmField @field:SerializedName("lat") var latitude: String,
    @JvmField @field:SerializedName("lon") var longitude: String,
    @JvmField @field:SerializedName("display_name") var displayName: String
) {
    @SerializedName("address")
    var address: Address? = null

    val formattedAddress: String
        get() {
            return address?.let {
                // Filtrar componentes vacíos
                val components = listOfNotNull(
                    it.road?.replace("Calle", "C."),
                    it.houseNumber,
                    it.suburb,
                    it.city,
                    it.state,
                    it.postcode
                )
                components.joinToString(", ").trim()
            } ?: displayName
        }
}
