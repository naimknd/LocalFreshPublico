package com.example.localfresh.activitys.vendedor.apartados

import com.example.localfresh.R
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.localfresh.databinding.FragmentQrScannerVendedorBinding
import com.example.localfresh.model.vendedor.apartados.QRVerificationResponse
import com.example.localfresh.viewmodel.vendedor.apartados.QRScannerViewModel
import com.example.localfresh.viewmodel.vendedor.apartados.QRVerificationState
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory

class QRScannerVendedorFragment : Fragment() {
    private var _binding: FragmentQrScannerVendedorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QRScannerViewModel by viewModels()
    private var isProcessing = false

    // Acceso perezoso a sellerId para evitar accesos repetidos a SharedPreferences
    private val sellerId by lazy {
        requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            .getInt("SELLER_ID", -1)
    }

    // Registrar launcher para solicitud de permisos usando la API ActivityResult
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) setupScanner()
        else {
            Toast.makeText(context, "Necesitas permiso de cámara para escanear códigos QR", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQrScannerVendedorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración inicial
        if (hasCameraPermission()) setupScanner() else requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        setupObservers()
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun setupScanner() {
        binding.barcodeView.apply {
            barcodeView.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
            initializeFromIntent(requireActivity().intent)
            decodeContinuous(object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult) {
                    if (!isProcessing && result.text.isNotEmpty()) {
                        isProcessing = true
                        processQRCode(result.text)
                    }
                }
                override fun possibleResultPoints(resultPoints: List<com.google.zxing.ResultPoint>) {}
            })
        }
    }

    private fun processQRCode(qrContent: String) {
        binding.progressBar.visibility = View.VISIBLE

        if (sellerId != -1) {
            viewModel.verifyReservationQR(qrContent, sellerId)
        } else {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(context, "Error: ID de vendedor no válido", Toast.LENGTH_SHORT).show()
            isProcessing = false
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.qrVerificationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is QRVerificationState.Success -> navigateToResultFragment(state.response)
                is QRVerificationState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    isProcessing = false
                }
            }
        }
    }

    private fun navigateToResultFragment(response: QRVerificationResponse) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, QRVerificationResultFragment().apply {
                arguments = Bundle().apply { putParcelable("verification_response", response) }
            })
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        if (hasCameraPermission()) {
            binding.barcodeView.resume()
            isProcessing = false
        }
    }

    override fun onPause() {
        super.onPause()
        if (hasCameraPermission()) binding.barcodeView.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}