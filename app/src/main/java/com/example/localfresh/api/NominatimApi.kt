package com.example.localfresh.api

import com.example.localfresh.model.general.LocationResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApi {
    @GET("search")
    fun searchLocations(
        @Query("q") query: String?,
        @Query("format") format: String?,
        @Query("addressdetails") addressDetails: Int,
        @Query("limit") limit: Int
    ): Call<List<LocationResult>>? // Cambiado a List<LocationResult>

    // Codificación geográfica inversa
    @GET("reverse")
    fun reverseGeocode(
        @Query("lat") latitude: String?,
        @Query("lon") longitude: String?,
        @Query("format") format: String?
    ): Call<LocationResult?>?
}
