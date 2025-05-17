package com.example.localfresh.activitys.vendedor.paginaprincipal.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.localfresh.R
import com.example.localfresh.databinding.EditarUnidadVendedorLayoutBinding
import com.example.localfresh.model.vendedor.unidad.EditUnitRequest
import com.example.localfresh.utils.DateUtils
import com.example.localfresh.utils.DiscountUtils
import com.example.localfresh.viewmodel.vendedor.paginaprincipal.EditarUnidadViewModel
import java.util.*

class EditarUnidadVendedorFragment : Fragment() {

    private var _binding: EditarUnidadVendedorLayoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditarUnidadViewModel by viewModels()
    private var unitId: Int = -1
    private var originalPrice: Double = 0.0

    companion object {
        private const val ARG_UNIT_ID = "unit_id"
        private const val ARG_PRODUCT_NAME = "product_name"

        fun newInstance(unitId: Int, productName: String?) = EditarUnidadVendedorFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_UNIT_ID, unitId)
                putString(ARG_PRODUCT_NAME, productName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        unitId = arguments?.getInt(ARG_UNIT_ID, -1) ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = EditarUnidadVendedorLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()

        // Cargar datos de la unidad
        if (unitId != -1) viewModel.getUnit(unitId)
    }

    private fun setupUI() {
        with(binding) {
            // Configurar toolbar y título
            toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
            unitNameTextView.text = arguments?.getString(ARG_PRODUCT_NAME) ?: ""

            // Configurar clickListeners
            expiryDateEditText.setOnClickListener { showDatePickerDialog() }
            discountPriceEditText.setOnClickListener { showPriceInputDialog() }

            // Configurar botón de guardar
            saveChangesButton.setOnClickListener { saveChanges() }
        }
    }

    private fun saveChanges() {
        with(binding) {
            val discountPrice = discountPriceEditText.text.toString().toDoubleOrNull()
            val expiryDate = expiryDateEditText.text.toString()
            val quantity = unitQuantityInput.text.toString().toIntOrNull() ?: 0
            val status = statusDropdown.text.toString().ifEmpty { "disponible" }

            if (validateInputs(expiryDate, quantity)) {
                viewModel.editUnit(EditUnitRequest(
                    unit_id = unitId,
                    discount_price = discountPrice,
                    expiry_date = DateUtils.formatDateForServer(expiryDate),
                    status = status,
                    quantity = quantity
                ))
            }
        }
    }

    private fun setupObservers() {
        // Observar datos de la unidad
        viewModel.getUnitResponse.observe(viewLifecycleOwner) { response ->
            if (response.status == "success") {
                response.data?.let { unit ->
                    originalPrice = unit.original_price
                    binding.apply {
                        discountPriceEditText.setText(String.format(Locale.getDefault(), "%.2f", unit.discount_price ?: originalPrice))
                        expiryDateEditText.setText(unit.expiry_date)
                        unitQuantityInput.setText(unit.quantity.toString())
                    }
                }
            } else {
                Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
            }
        }

        // Observar respuesta de edición
        viewModel.editUnitResponse.observe(viewLifecycleOwner) { response ->
            if (response.status == "success") {
                Toast.makeText(requireContext(), "Unidad actualizada correctamente", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(expiryDate: String, quantity: Int): Boolean = when {
        expiryDate.isEmpty() -> {
            Toast.makeText(requireContext(), "Por favor, seleccione una fecha de caducidad", Toast.LENGTH_SHORT).show()
            false
        }
        quantity <= 0 -> {
            Toast.makeText(requireContext(), "Por favor, ingrese una cantidad válida", Toast.LENGTH_SHORT).show()
            false
        }
        else -> true
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val formattedDate = DateUtils.formatDate(year, month, day)
                binding.expiryDateEditText.setText(formattedDate)
                calculateAndShowDiscountPrice(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun calculateAndShowDiscountPrice(expiryDate: String) {
        if (originalPrice <= 0) return

        val discountPrice = DiscountUtils.calculateDiscountPrice(originalPrice, expiryDate)
        val percentage = DiscountUtils.getFormattedDiscountPercentage(expiryDate)

        AlertDialog.Builder(requireContext())
            .setTitle("Precio con descuento calculado")
            .setMessage(
                "Basado en la fecha de caducidad, se aplicará un descuento del $percentage.\n\n" +
                        "Precio original: $${String.format("%.2f", originalPrice)}\n" +
                        "Precio con descuento: $${String.format("%.2f", discountPrice)}"
            )
            .setPositiveButton("Aceptar") { _, _ ->
                binding.discountPriceEditText.setText(String.format("%.2f", discountPrice))
            }
            .setNegativeButton("Ajustar manualmente") { _, _ -> showPriceInputDialog() }
            .show()
    }

    private fun showPriceInputDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input_price, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextPrice)

        AlertDialog.Builder(requireContext())
            .setTitle("Ingrese el precio con descuento")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                binding.discountPriceEditText.setText(editText.text.toString())
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}