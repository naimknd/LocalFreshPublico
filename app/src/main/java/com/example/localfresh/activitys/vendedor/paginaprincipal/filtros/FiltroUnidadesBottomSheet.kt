package com.example.localfresh.activitys.vendedor.paginaprincipal.filtros

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.example.localfresh.R
import com.example.localfresh.databinding.BottomSheetFiltroUnidadesVendedorBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class FiltroUnidadesBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFiltroUnidadesVendedorBinding? = null
    private val binding get() = _binding!!

    private var onFiltrosAplicadosListener: ((String, String) -> Unit)? = null
    private var initialDate: String = ""
    private var initialStatus: String = ""

    fun setInitialValues(date: String, status: String) {
        initialDate = date
        initialStatus = status
    }

    fun setOnFiltrosAplicadosListener(listener: (String, String) -> Unit) {
        onFiltrosAplicadosListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFiltroUnidadesVendedorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdown()
        setupInitialValues()
        setupClickListeners()
    }

    private fun setupDropdown() {
        // Estados incluyen "Expirado (Consumo Pref.)" y "Expirado (Caducado)"
        val estados = arrayOf(
            "Todos",
            "Disponible",
            "Apartado",
            "Vendido",
            "Expirado (Consumo Pref.)",
            "Expirado (Caducado)"
        )
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_dropdown_item, estados)
        (binding.statusDropdown as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setupInitialValues() {
        if (initialDate.isNotEmpty()) {
            binding.expiryDateInput.setText(initialDate)
        }

        if (initialStatus.isNotEmpty()) {
            val statusToShow = when (initialStatus.lowercase()) {
                "disponible" -> "Disponible"
                "apartado" -> "Apartado"
                "vendido" -> "Vendido"
                "expirado_consumo" -> "Expirado (Consumo Pref.)"
                "expirado_caducado" -> "Expirado (Caducado)"
                else -> "Todos"
            }
            (binding.statusDropdown as? AutoCompleteTextView)?.setText(statusToShow, false)
        }
    }

    private fun setupClickListeners() {
        binding.expiryDateInput.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnApply.setOnClickListener {
            val selectedDate = binding.expiryDateInput.text.toString()

            val statusText = (binding.statusDropdown as? AutoCompleteTextView)?.text.toString()
            val selectedStatus = when (statusText) {
                "Todos" -> ""
                "Expirado (Consumo Pref.)" -> "expirado_consumo"
                "Expirado (Caducado)" -> "expirado_caducado"
                else -> statusText.lowercase()
            }

            onFiltrosAplicadosListener?.invoke(selectedDate, selectedStatus)
            dismiss()
        }

        binding.btnReset.setOnClickListener {
            binding.expiryDateInput.text?.clear()
            (binding.statusDropdown as? AutoCompleteTextView)?.setText("Todos", false)
            onFiltrosAplicadosListener?.invoke("", "")
            dismiss()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        if (initialDate.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(initialDate)
                if (date != null) {
                    calendar.time = date
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.expiryDateInput.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}