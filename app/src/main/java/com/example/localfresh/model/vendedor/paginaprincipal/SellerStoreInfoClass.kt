package com.example.localfresh.model.vendedor.paginaprincipal

import android.os.Parcel
import android.os.Parcelable

data class SellerStoreInfoClass(
    val seller_id: Int,
    val email: String,
    val store_name: String,
    val store_description: String,
    val store_phone: String,
    val store_address: String,
    val latitude: Double,
    val longitude: Double,
    val is_verified: Int,
    val organic_products: Int,
    val store_type: String,
    val store_logo: String?, // Puede ser nulo si no hay logo
    val opening_time: String,
    val closing_time: String,
    val store_rating: Double?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(seller_id)
        parcel.writeString(email)
        parcel.writeString(store_name)
        parcel.writeString(store_description)
        parcel.writeString(store_phone)
        parcel.writeString(store_address)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeInt(is_verified)
        parcel.writeInt(organic_products)
        parcel.writeString(store_type)
        parcel.writeString(store_logo)
        parcel.writeString(opening_time)
        parcel.writeString(closing_time)
        parcel.writeDouble(store_rating ?: 0.0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SellerStoreInfoClass> {
        override fun createFromParcel(parcel: Parcel): SellerStoreInfoClass {
            return SellerStoreInfoClass(parcel)
        }

        override fun newArray(size: Int): Array<SellerStoreInfoClass?> {
            return arrayOfNulls(size)
        }
    }
}
