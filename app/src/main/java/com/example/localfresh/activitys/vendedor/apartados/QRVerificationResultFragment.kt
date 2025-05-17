package com.example.localfresh.activitys.vendedor.apartados

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.localfresh.R
import com.example.localfresh.adapters.vendedor.QRProductsAdapter
import com.example.localfresh.databinding.FragmentQrVerificationResultBinding
import com.example.localfresh.model.vendedor.apartados.QRVerificationResponse
import com.example.localfresh.utils.ReservationStatusUtils
import com.example.localfresh.utils.ReservationTimerUtils
import com.example.localfresh.viewmodel.vendedor.apartados.MarkCompleteState
import com.example.localfresh.viewmodel.vendedor.apartados.ReservationSellerViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class QRVerificationResultFragment : Fragment() {
    private var _binding: FragmentQrVerificationResultBinding? = null
    private val binding get() = _binding!!
    private var reservationData: QRVerificationResponse? = null
    private var countDownTimer: CountDownTimer? = null
    private val viewModel: ReservationSellerViewModel by viewModels()
    private val sellerId by lazy {
        requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            .getInt("SELLER_ID", -1)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQrVerificationResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupListeners()
        loadReservationData()
    }

    private fun loadReservationData() {
        arguments?.let {
            reservationData = getParcelableCompat(it, "verification_response")
            reservationData?.let(::updateUI) ?: navigateBackToList()
        }
    }

    private fun <T> getParcelableCompat(bundle: Bundle, key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(key, QRVerificationResponse::class.java) as? T
        } else {
            @Suppress("DEPRECATION")
            bundle.getParcelable(key) as? T
        }
    }

    private fun setupObservers() {
        viewModel.markCompleteState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = View.GONE
            binding.btnMarcarCompletado.isEnabled = true

            when (state) {
                is MarkCompleteState.Success -> {
                    showToast(state.response.message)
                    navigateBackToList()
                }
                is MarkCompleteState.Error -> showToast("Error: ${state.message}")
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnMarcarCompletado.isEnabled = !isLoading
        }
    }

    private fun updateUI(data: QRVerificationResponse) = with(binding) {
        tvTitulo.text = "Apartado #${data.reservation.reservation_id}"

        // Configurar estado y color del apartado
        val statusMessage = ReservationStatusUtils.getStatusMessage(data.reservation.status)
        val colorRes = ReservationStatusUtils.getStatusColorResource(data.reservation.status)
        val iconRes = ReservationStatusUtils.getStatusIconResource(data.reservation.status)

        estadoContainer.setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes))
        iconEstado.setImageResource(iconRes)
        tvVerificationStatus.text = statusMessage
        btnMarcarCompletado.visibility = if (data.reservation.status == ReservationStatusUtils.STATUS_PENDING)
            View.VISIBLE else View.GONE

        // Información del cliente
        tvNombreCliente.text = "${data.reservation.customer_name} (@${data.reservation.username})"
        tvTelefonoCliente.text = data.reservation.phone
        tvEmailCliente.text = data.reservation.email
        tvFechaCreacion.text = "Fecha de creación: ${data.reservation.reservation_date}"

        // Configuración de la fecha de expiración
        setupExpirationTime(data)

        // Configuración de precios
        tvPrecioOriginal.apply {
            text = "$${String.format("%.2f", data.reservation.original_price.toDouble())}"
            paintFlags = paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        }
        tvPrecioTotal.text = "$${String.format("%.2f", data.reservation.total_price.toDouble())}"
        tvCantidadProductos.text = "${data.reservation.products.size}"
        tvDescuento.text = "${data.reservation.discount_percentage}%"

        rvQRProducts.adapter = QRProductsAdapter(data.reservation.products)
    }

    private fun setupExpirationTime(data: QRVerificationResponse) {
        countDownTimer?.cancel()

        if (ReservationStatusUtils.needsTimer(data.reservation.status, data.reservation.expiration_date)) {
            binding.tvTiempoLimite.visibility = View.VISIBLE
            countDownTimer = ReservationTimerUtils.startCountDownTimer(
                data.reservation.expiration_date,
                binding.tvTiempoLimite
            ) { if (isAdded) navigateBackToList() }
        } else {
            ReservationTimerUtils.setStaticReservationStatus(binding.tvTiempoLimite, data.reservation.status)
        }
    }

    private fun setupListeners() {
        with(binding) {
            btnBack.setOnClickListener { navigateBackToList() }

            btnMarcarCompletado.setOnClickListener {
                showCompletionConfirmationDialog()
            }

            btnLlamar.setOnClickListener {
                reservationData?.reservation?.phone?.takeIf { it.isNotEmpty() }?.let { phone ->
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                } ?: showToast("No hay número de teléfono disponible")
            }

            btnEmail.setOnClickListener {
                reservationData?.reservation?.email?.takeIf { it.isNotEmpty() }?.let { email ->
                    try {
                        startActivity(Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$email")
                            putExtra(Intent.EXTRA_SUBJECT, "Sobre su apartado en LocalFresh")
                        })
                    } catch (e: Exception) {
                        showToast("No se encontró aplicación de correo")
                    }
                } ?: showToast("No hay dirección de correo disponible")
            }
        }
    }

    private fun showCompletionConfirmationDialog() {
        reservationData?.let { data ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirmar completado")
                .setIcon(R.drawable.ic_info)
                .setMessage("¿Estás seguro que deseas marcar este apartado como completado? Esta acción confirma que el cliente recogió y pagó los productos.")
                .setPositiveButton("Sí, completar") { _, _ ->
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnMarcarCompletado.isEnabled = false

                    if (sellerId != -1) {
                        viewModel.markReservationComplete(data.reservation.reservation_id, sellerId)
                    } else {
                        showToast("Error: No se pudo obtener el ID del vendedor")
                        binding.progressBar.visibility = View.GONE
                        binding.btnMarcarCompletado.isEnabled = true
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateBackToList() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, VerApartadosVendedorFragment())
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }

    companion object {
        fun newInstance(response: QRVerificationResponse) = QRVerificationResultFragment().apply {
            arguments = Bundle().apply {
                putParcelable("verification_response", response)
            }
        }
    }
}