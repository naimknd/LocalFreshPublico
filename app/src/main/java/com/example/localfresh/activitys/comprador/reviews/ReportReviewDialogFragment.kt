package com.example.localfresh.activitys.comprador.reviews

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.localfresh.databinding.DialogReportReviewBinding
import com.example.localfresh.viewmodel.comprador.reviews.ReviewViewModel
import com.google.android.material.chip.Chip

class ReportReviewDialogFragment : DialogFragment() {
    private var _binding: DialogReportReviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReviewViewModel
    private var reviewId: Int = -1

    fun setViewModel(reviewViewModel: ReviewViewModel) {
        this.viewModel = reviewViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { reviewId = it.getInt(ARG_REVIEW_ID) }

        // Auto-inicializar ViewModel si no fue asignado externamente
        if (!::viewModel.isInitialized && parentFragment is VerReviewsCompradorFragment) {
            viewModel = (parentFragment as VerReviewsCompradorFragment).viewModel
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogReportReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verificar que tenemos el ViewModel antes de continuar
        if (!::viewModel.isInitialized) {
            showToast("Error: No se pudo inicializar el reporte")
            dismiss()
            return
        }

        setupListeners()
        observeReportResult()
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnReportar.setOnClickListener { submitReport() }
    }

    private fun submitReport() {
        val userId = getUserId() ?: return

        // Obtener el motivo seleccionado
        val selectedChipId = binding.chipGroupReasons.checkedChipId
        if (selectedChipId == View.NO_ID) {
            showToast("Por favor selecciona un motivo")
            return
        }

        val selectedChip = binding.chipGroupReasons.findViewById<Chip>(selectedChipId)
        val reason = selectedChip?.tag?.toString() ?: "other"
        val additionalInfo = binding.editTextAdditionalInfo.text.toString().trim().takeIf { it.isNotEmpty() }

        // Enviar reporte
        viewModel.reportReview(userId, reviewId, reason, additionalInfo)
        binding.btnReportar.isEnabled = false
    }

    private fun getUserId(): Int? {
        val userId = requireContext()
            .getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            .getInt("USER_ID", -1)

        if (userId == -1) {
            showToast("Debes iniciar sesión para reportar reseñas")
            dismiss()
            return null
        }

        return userId
    }

    private fun observeReportResult() {
        viewModel.reportReviewResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.fold(
                onSuccess = { response ->
                    showToast(response.message)
                    dismiss()
                },
                onFailure = { error ->
                    showToast(error.message ?: "Error al reportar la reseña")
                    binding.btnReportar.isEnabled = true
                }
            )
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_REVIEW_ID = "review_id"

        fun newInstance(reviewId: Int) = ReportReviewDialogFragment().apply {
            arguments = Bundle().apply { putInt(ARG_REVIEW_ID, reviewId) }
        }
    }
}