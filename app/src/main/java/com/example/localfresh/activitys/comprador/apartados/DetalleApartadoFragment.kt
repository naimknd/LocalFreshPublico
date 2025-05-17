package com.example.localfresh.activitys.comprador.apartados

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localfresh.R
import com.example.localfresh.activitys.comprador.reviews.ReviewVendedorFragment
import com.example.localfresh.adapters.comprador.DetalleApartadoAdapter
import com.example.localfresh.databinding.FragmentDetalleApartadoBinding
import com.example.localfresh.model.comprador.apartados.ReservationDetail
import com.example.localfresh.utils.DateUtils
import com.example.localfresh.utils.ReservationStatusUtils
import com.example.localfresh.utils.ReservationTimerUtils
import com.example.localfresh.viewmodel.comprador.apartados.CancelReservationState
import com.example.localfresh.viewmodel.comprador.apartados.ReservationDetailState
import com.example.localfresh.viewmodel.comprador.apartados.ReservationViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

class DetalleApartadoFragment : Fragment() {
    private var _binding: FragmentDetalleApartadoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReservationViewModel by viewModels()
    private lateinit var detalleAdapter: DetalleApartadoAdapter
    private var countdownHandler: Handler? = null
    private var countDownTimer: android.os.CountDownTimer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetalleApartadoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        loadReservationDetails()
    }

    private fun setupUI() = with(binding) {
        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        rvProductosApartados.layoutManager = LinearLayoutManager(context)
        detalleAdapter = DetalleApartadoAdapter()
        rvProductosApartados.adapter = detalleAdapter
        setupBackButtonHandling()
    }

    private fun setupBackButtonHandling() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { setLoading(it) }
        viewModel.reservationDetailState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = View.GONE
            when (state) {
                is ReservationDetailState.Success -> {
                    binding.contentLayout.visibility = View.VISIBLE
                    updateUI(state.response.reservation)
                }
                is ReservationDetailState.Error -> {
                    binding.contentLayout.visibility = View.GONE
                    showToast(state.message)
                }
            }
        }
        viewModel.cancelReservationState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = View.GONE
            when (state) {
                is CancelReservationState.Success -> {
                    showToast(state.response.message)
                    parentFragmentManager.popBackStack()
                }
                is CancelReservationState.Error -> {
                    showToast("Error: ${state.message}")
                    binding.btnCancelarApartado.isEnabled = true
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) = with(binding) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun loadReservationDetails() {
        arguments?.getInt("reservationId")?.let { viewModel.getReservationDetails(it) }
    }

    private fun updateUI(detail: ReservationDetail) = with(binding) {
        tvTitulo.text = "Apartado #${detail.reservation_id}"
        tvEstadoApartado.text = "Estado: ${detail.status}"
        tvEstadoApartado.setTextColor(ContextCompat.getColor(requireContext(), ReservationStatusUtils.getStatusColorResource(detail.status)))
        tvNombreTienda.text = detail.seller.store_name
        tvHorarioTienda.text = "Horario: ${detail.seller.store_hours}"
        btnLlamarTienda.setOnClickListener { callSeller(detail.seller.store_phone) }
        btnComoLlegar.setOnClickListener { openMap(detail.seller.latitude, detail.seller.longitude) }
        tvFechaCreacion.text = "Fecha de creación: ${detail.formatted_date ?: DateUtils.formatearFecha(detail.reservation_date)}"
        updateLimitTime(detail)
        tvTotalOriginal.apply {
            text = detail.original_price.toCurrency()
            paintFlags = paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        }
        tvTotalDescuento.text = detail.total_price.toCurrency()
        tvAhorroTotal.text = "Ahorro Total: ${detail.total_savings.toCurrency()} (${detail.savings_percentage}%)"
        tvAhorroTotal.visibility = View.VISIBLE
        btnVerQR.setOnClickListener {
            if (!detail.qr_code.isNullOrEmpty()) showQRDialog(detail.qr_code, detail.reservation_id)
            else showToast("No hay código QR disponible para este apartado")
        }
        val canCancel = ReservationStatusUtils.canCancelReservation(detail.status, detail.expiration_date)
        btnCancelarApartado.visibility = if (canCancel) View.VISIBLE else View.GONE
        btnCancelarApartado.setOnClickListener { showCancelDialog(detail.reservation_id) }
        btnCalificarVendedor.visibility = if (detail.status == "Completado") View.VISIBLE else View.GONE
        btnCalificarVendedor.setOnClickListener { openReview(detail) }
        detalleAdapter.submitList(detail.products)
    }

    private fun callSeller(phone: String) {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    }

    private fun openMap(latitude: Double, longitude: Double) {
        try {
            val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply { setPackage("com.google.android.apps.maps") }
            startActivity(mapIntent)
        } catch (e: Exception) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")))
            } catch (e2: Exception) {
                showToast("No se encontró una aplicación para mostrar la ubicación")
            }
        }
    }

    private fun showCancelDialog(reservationId: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancelar apartado")
            .setIcon(R.drawable.ic_warning)
            .setMessage("¿Estás seguro que deseas cancelar este apartado? Los productos volverán a estar disponibles para otros usuarios.")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                binding.progressBar.visibility = View.VISIBLE
                binding.btnCancelarApartado.isEnabled = false
                viewModel.cancelReservation(reservationId)
            }
            .setNegativeButton("No, mantener apartado", null)
            .show()
    }

    private fun openReview(detail: ReservationDetail) {
        val userId = requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            .getInt("USER_ID", -1)
        val reviewFragment = ReviewVendedorFragment.newInstance(userId, detail.seller.seller_id, detail.reservation_id)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, reviewFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun updateLimitTime(detail: ReservationDetail) {
        countdownHandler?.removeCallbacksAndMessages(null)
        countDownTimer?.cancel()
        if (ReservationStatusUtils.needsTimer(detail.status, detail.expiration_date)) {
            binding.tvTiempoLimite.visibility = View.VISIBLE
            countDownTimer = ReservationTimerUtils.startCountDownTimer(
                expirationDate = detail.expiration_date,
                textView = binding.tvTiempoLimite
            ) {
                if (isAdded) loadReservationDetails()
            }
        } else {
            ReservationTimerUtils.setStaticReservationStatus(binding.tvTiempoLimite, detail.status)
            if (detail.status == ReservationStatusUtils.STATUS_CANCELLED) {
                binding.tvTiempoLimite.visibility = View.VISIBLE
                binding.tvTiempoLimite.text = "Apartado cancelado"
                binding.tvTiempoLimite.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color))
            }
        }
    }

    private fun showQRDialog(qrCode: String, reservationId: Int) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_qr_code)
        dialog.findViewById<TextView>(R.id.tvTitleQR).text = "Código QR del Apartado #$reservationId"
        generarCodigoQR(qrCode, dialog.findViewById(R.id.imageViewQR))
        dialog.findViewById<Button>(R.id.btnCloseQR).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun generarCodigoQR(contenido: String, imageView: ImageView) {
        try {
            val bitMatrix = MultiFormatWriter().encode(contenido, BarcodeFormat.QR_CODE, 512, 512)
            val bitmap = BarcodeEncoder().createBitmap(bitMatrix)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            showToast("Error al generar el código QR")
            Log.e("QR_GENERATOR", "Error generando QR: ${e.message}")
        }
    }

    private fun Double.toCurrency(): String = "$${String.format("%.2f", this)}"

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countdownHandler?.removeCallbacksAndMessages(null)
        countDownTimer?.cancel()
        countdownHandler = null
        countDownTimer = null
        _binding = null
    }
}