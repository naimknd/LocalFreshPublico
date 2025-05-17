package com.example.localfresh.activitys.vendedor.notificaciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localfresh.R
import com.example.localfresh.activitys.vendedor.apartados.DetalleApartadoVendedorFragment
import com.example.localfresh.activitys.vendedor.paginaprincipal.fragments.ProductsVendedorFragment
import com.example.localfresh.activitys.vendedor.paginaprincipal.fragments.VerUnidadesVendedorFragment
import com.example.localfresh.adapters.vendedor.ProductosNotificacionAdapter
import com.example.localfresh.databinding.FragmentDetalleNotificacionBinding
import com.example.localfresh.model.vendedor.notificaciones.ProductExpiryInfo
import com.example.localfresh.model.vendedor.notificaciones.SellerNotification
import org.json.JSONArray
import org.json.JSONObject

class DetalleNotificacionFragment : Fragment() {
    private var _binding: FragmentDetalleNotificacionBinding? = null
    private val binding get() = _binding!!
    private lateinit var notification: SellerNotification

    companion object {
        fun newInstance(notification: SellerNotification): DetalleNotificacionFragment {
            val fragment = DetalleNotificacionFragment()
            fragment.notification = notification
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleNotificacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        handleNotification()
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun setupUI() {
        binding.tvTitle.text = notification.title
        binding.tvMessage.text = notification.message
        binding.tvFecha.text = notification.created_at
    }

    private fun handleNotification() {
        try {
            val dataJson = JSONObject(notification.data ?: "{}")
            when (notification.type) {
                "alerta_caducidad" -> mostrarDetalleAlertaCaducidad(dataJson)
                "producto_expirado" -> mostrarDetalleProductoExpirado(dataJson)
                "nuevo_apartado", "cancelado", "caducado", "pago" -> {
                    // Navegar directamente al detalle del apartado
                    val reservationId = dataJson.optInt("reservation_id", -1)
                    if (reservationId != -1) {
                        navigateToReservationDetail(reservationId)
                    } else {
                        binding.tvDetalles.visibility = View.VISIBLE
                        binding.tvDetalles.text = "No se pudo obtener el ID del apartado"
                    }
                }
                else -> {
                    binding.tvDetalles.visibility = View.VISIBLE
                    binding.tvDetalles.text = "Tipo de notificación no soportado"
                }
            }
        } catch (e: Exception) {
            binding.tvDetalles.visibility = View.VISIBLE
            binding.tvDetalles.text = "No se pudieron cargar los detalles"
        }
    }

    private fun mostrarDetalleAlertaCaducidad(data: JSONObject) {
        val productosJson = data.getString("products_json")
        val productosArray = JSONArray(productosJson)
        val productos = mutableListOf<ProductExpiryInfo>()
        for (i in 0 until productosArray.length()) {
            val producto = productosArray.getJSONObject(i)
            productos.add(
                ProductExpiryInfo(
                    product_id = producto.getInt("product_id"),
                    unit_id = producto.getInt("unit_id"),
                    name = producto.getString("name"),
                    category = producto.getString("category"),
                    expiry_date = producto.getString("expiry_date"),
                    expiry_type = producto.getString("expiry_type"),
                    quantity = producto.getInt("quantity")
                )
            )
        }
        binding.rvProductos.visibility = View.VISIBLE
        binding.rvProductos.layoutManager = LinearLayoutManager(context)
        binding.rvProductos.adapter = ProductosNotificacionAdapter(productos) { unitId ->
            // Navegar a la pantalla de unidades del producto
            val fragment = VerUnidadesVendedorFragment.newInstance(unitId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        val recommendedAction = data.optString("recommended_action", "")
        if (recommendedAction.isNotBlank()) {
            binding.tvAccionRecomendada.text = "Acción recomendada: $recommendedAction"
            binding.tvAccionRecomendada.visibility = View.VISIBLE
        } else {
            binding.tvAccionRecomendada.visibility = View.GONE
        }
    }

    private fun mostrarDetalleProductoExpirado(data: JSONObject) {
        try {
            // Verificar si usamos el formato completo o el formato reducido
            if (data.has("products_json")) {
                // Usar el mismo metodo que para alerta_caducidad
                mostrarDetalleAlertaCaducidad(data)
            }
        } catch (e: Exception) {
            binding.tvDetalles.visibility = View.VISIBLE
            binding.tvDetalles.text = "Error al cargar detalles: ${e.message}"
        }
    }

    private fun navigateToReservationDetail(reservationId: Int) {
        parentFragmentManager.popBackStack() // Cerrar este fragmento antes de navegar
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DetalleApartadoVendedorFragment().apply {
                arguments = Bundle().apply {
                    putInt("reservation_id", reservationId)
                }
            })
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}