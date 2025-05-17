package com.example.localfresh.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.localfresh.R
import com.example.localfresh.model.general.LocationResult

class AddressAdapter(
    context: Context,
    private val addressList: MutableList<LocationResult>
) : ArrayAdapter<LocationResult>(context, R.layout.location_list_item, addressList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var viewHolder: ViewHolder
        val convertView = convertView ?: LayoutInflater.from(context).inflate(R.layout.location_list_item, parent, false).apply {
            viewHolder = ViewHolder()
            viewHolder.addressText = findViewById(R.id.address_text)
            tag = viewHolder
        }

        viewHolder = convertView.tag as ViewHolder

        // Obtener la dirección de la lista
        val address = addressList[position]
        // Usar el metodo formattedAddress o displayName para mostrar la dirección
        viewHolder.addressText.text = address.displayName

        return convertView
    }

    fun updateLocations(locations: List<LocationResult>) {
        addressList.clear()
        addressList.addAll(locations)
        notifyDataSetChanged()
    }

    internal class ViewHolder {
        lateinit var addressText: TextView
    }
}