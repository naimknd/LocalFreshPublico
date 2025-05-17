package com.example.localfresh.activitys.vendedor.paginaprincipal.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.localfresh.R
import com.example.localfresh.databinding.AgregarUnidadVendedorLayoutBinding
import com.example.localfresh.model.vendedor.producto.InfoProductSeller
import com.example.localfresh.model.vendedor.unidad.AddUnitRequest
import com.example.localfresh.utils.DiscountUtils
import com.example.localfresh.viewmodel.vendedor.paginaprincipal.AgregarUnidadViewModel
import java.util.*

class AgregarUnidadVendedorFragment : Fragment() {

    private var _binding: AgregarUnidadVendedorLayoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AgregarUnidadViewModel by viewModels()
    private var selectedProductId: Int? = null
    private var productList: List<InfoProductSeller> = listOf()
    private var discountPrice: Double? = null
    private var sellerId: Int = -1
    private var preselectedProductId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sellerId = it.getInt("SELLER_ID", -1)
            preselectedProductId = it.getInt("PRODUCT_ID", -1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = AgregarUnidadVendedorLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            // Inicializar UI
            productDetailsCard.visibility = View.GONE
            discountPriceLayout.visibility = View.GONE
            progressBar.visibility = View.GONE

            // Configurar eventos
            backButton.setOnClickListener { parentFragmentManager.popBackStack() }
            unitExpiryDateInput.setOnClickListener { showDatePickerDialog() }
            selectProductCheckedTextView.setOnClickListener {
                if (productList.isNotEmpty()) showProductSelectionDialog() else loadProducts()
            }
            addUnitButton.setOnClickListener { if (validateInputs()) addUnit() }
            btnAjustarPrecio.setOnClickListener { showPriceInputDialog() }
        }

        setupObservers()
        loadProducts()
    }

    private fun loadProducts() {
        if (sellerId != -1) {
            binding.progressBar.visibility = View.VISIBLE
            viewModel.getSellerProducts(sellerId)
        } else {
            showToast("Error al obtener ID del vendedor")
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupObservers() {
        // Respuesta al añadir unidad
        viewModel.addUnitResponse.observe(viewLifecycleOwner) { response ->
            binding.progressBar.visibility = View.GONE
            binding.addUnitButton.isEnabled = true
            showToast(response.message)
            if (response.status == "success") parentFragmentManager.popBackStack()
        }

        // Respuesta al obtener productos
        viewModel.getSellerProductsResponse.observe(viewLifecycleOwner) { response ->
            binding.progressBar.visibility = View.GONE

            if (response.status == "success") {
                productList = response.data ?: listOf()

                // Preseleccionar producto si se indicó en argumentos
                if (preselectedProductId != -1) {
                    productList.find { it.product_id == preselectedProductId }?.let {
                        selectedProductId = it.product_id
                        updateProductUI(it)
                        binding.selectProductLayout.visibility = View.GONE
                    } ?: setupProductDropdown()
                } else {
                    setupProductDropdown()
                }
            } else {
                showToast("Error al obtener productos: ${response.message}")
            }
        }
    }

    private fun setupProductDropdown() {
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_dropdown_item,
            productList.map { it.name ?: "Sin nombre" })

        binding.selectProductCheckedTextView.apply {
            setAdapter(adapter)
            setOnItemClickListener { _, _, position, _ -> updateProductUI(productList[position]) }
        }
    }

    private fun showProductSelectionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar producto")
            .setItems(productList.map { it.name ?: "Sin nombre" }.toTypedArray()) { _, which ->
                updateProductUI(productList[which])
            }
            .show()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(),
            { _, year, month, day ->
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                binding.unitExpiryDateInput.setText(formattedDate)

                // Calcular descuento si hay un producto seleccionado
                selectedProductId?.let { id ->
                    productList.find { it.product_id == id }?.price?.let {
                        calculateDiscountPrice(it, formattedDate)
                    }
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = calendar.timeInMillis
            show()
        }
    }

    private fun calculateDiscountPrice(originalPrice: Double, expiryDate: String) {
        discountPrice = DiscountUtils.calculateDiscountPrice(originalPrice, expiryDate)
        binding.apply {
            discountPriceLayout.visibility = View.VISIBLE
            txtDiscountPrice.text = String.format("$%.2f", discountPrice)
            txtDiscountPercentage.text = "Descuento: ${DiscountUtils.getFormattedDiscountPercentage(expiryDate)}"
        }
    }

    private fun updateProductUI(product: InfoProductSeller) {
        binding.apply {
            productDetailsCard.visibility = View.VISIBLE
            selectedProductId = product.product_id
            txtNombreProducto.text = product.name
            txtCategoria.text = product.category
            txtPrecioOriginal.text = "$${product.price ?: "N/A"}"
            txtTipoExpiracion.text = if (product.expiry_type == "consumo_preferente")
                "Consumo preferente" else "Fecha de caducidad"

            // Cargar imagen
            Glide.with(this@AgregarUnidadVendedorFragment)
                .load(product.image_url)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(imgProducto)

            // Resetear campos
            unitExpiryDateInput.setText("")
            unitQuantityInput.setText("1")
            discountPriceLayout.visibility = View.GONE
            discountPrice = null
        }
    }

    private fun validateInputs(): Boolean = when {
        selectedProductId == null -> showToast("Debe seleccionar un producto").let { false }
        binding.unitExpiryDateInput.text.isNullOrEmpty() -> showToast("Debe ingresar una fecha").let { false }
        binding.unitQuantityInput.text.toString().toIntOrNull()?.let { it > 0 } != true ->
            showToast("Debe ingresar una cantidad válida").let { false }
        else -> true
    }

    private fun addUnit() {
        binding.progressBar.visibility = View.VISIBLE
        binding.addUnitButton.isEnabled = false

        viewModel.addUnit(AddUnitRequest(
            product_id = selectedProductId!!,
            discount_price = discountPrice,
            expiry_date = binding.unitExpiryDateInput.text.toString(),
            status = "disponible",
            quantity = binding.unitQuantityInput.text.toString().toIntOrNull() ?: 1
        ))
    }

    private fun showPriceInputDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input_price, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextPrice)
        val originalPrice = productList.find { it.product_id == selectedProductId }?.price ?: 0.0

        discountPrice?.let { editText.setText(String.format("%.2f", it)) }

        AlertDialog.Builder(requireContext())
            .setTitle("Ajustar precio con descuento")
            .setMessage("El precio con descuento debe ser menor o igual a $${String.format("%.2f", originalPrice)}")
            .setView(dialogView)
            .setPositiveButton("Aceptar") { _, _ ->
                val price = editText.text.toString().toDoubleOrNull()
                when {
                    price == null -> showToast("Por favor ingrese un precio válido")
                    price < 0 -> showToast("El precio no puede ser negativo")
                    price > originalPrice -> {
                        showToast("El precio con descuento no puede ser mayor al original")
                        showPriceInputDialog()
                    }
                    else -> {
                        discountPrice = price
                        binding.txtDiscountPrice.text = String.format("$%.2f", price)
                        val discountPercentage = ((originalPrice - price) / originalPrice * 100).toInt()
                        binding.txtDiscountPercentage.text = "Descuento: $discountPercentage% (ajustado manualmente)"
                        binding.discountPriceLayout.visibility = View.VISIBLE
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(sellerId: Int, productId: Int = -1) = AgregarUnidadVendedorFragment().apply {
            arguments = Bundle().apply {
                putInt("SELLER_ID", sellerId)
                putInt("PRODUCT_ID", productId)
            }
        }
    }
}
