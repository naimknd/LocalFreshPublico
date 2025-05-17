package com.example.localfresh.activitys.vendedor.apartados

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localfresh.R
import com.example.localfresh.adapters.vendedor.DetalleApartadoVendedorAdapter
import com.example.localfresh.databinding.FragmentDetalleApartadoVendedorBinding
import com.example.localfresh.model.vendedor.apartados.ReservationDetailSeller
import com.example.localfresh.utils.ReservationStatusUtils
import com.example.localfresh.utils.ReservationTimerUtils
import com.example.localfresh.viewmodel.vendedor.apartados.ReservationDetailSellerState
import com.example.localfresh.viewmodel.vendedor.apartados.ReservationSellerViewModel

class DetalleApartadoVendedorFragment : Fragment() {
    private var _binding: FragmentDetalleApartadoVendedorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReservationSellerViewModel by viewModels()
    private val detalleAdapter by lazy { DetalleApartadoVendedorAdapter() }

    private var reservationId: Int = -1
    private var sellerId: Int = -1
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetalleApartadoVendedorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackNavigation()
        setupRecyclerView()
        initializeObservers()
        loadReservationDetails()
    }

    private fun setupBackNavigation() {
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    parentFragmentManager.popBackStack()
                }
            }
        )
    }

    private fun setupRecyclerView() {
        binding.rvProductos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = detalleAdapter
        }
    }

    private fun loadReservationDetails() {
        reservationId = arguments?.getInt("reservation_id", -1) ?: -1
        sellerId = requireContext().getSharedPreferences("LocalFreshPrefs", 0)
            .getInt("SELLER_ID", -1)

        if (reservationId != -1 && sellerId != -1) {
            viewModel.getSellerReservationDetails(reservationId, sellerId)
        } else {
            showToast("Error: No se pudo obtener el ID del apartado o del vendedor")
            parentFragmentManager.popBackStack()
        }
    }

    private fun initializeObservers() {
        viewModel.reservationDetailState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = View.GONE

            when (state) {
                is ReservationDetailSellerState.Success -> {
                    binding.contentLayout.visibility = View.VISIBLE
                    updateUI(state.response.reservation)
                }
                is ReservationDetailSellerState.Error -> {
                    binding.contentLayout.visibility = View.GONE
                    showToast("Error: ${state.message}")
                    parentFragmentManager.popBackStack()
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun updateUI(detail: ReservationDetailSeller) {
        with(binding) {
            // Información general
            tvTitulo.text = "Apartado #${detail.reservation_id}"
            tvEstadoApartado.apply {
                text = "Estado: ${detail.status}"
                setTextColor(ContextCompat.getColor(
                    requireContext(),
                    ReservationStatusUtils.getStatusColorResource(detail.status)
                ))
            }

            // Información del cliente
            tvNombreCliente.text = "${detail.user.full_name} (@${detail.user.username})"
            tvTelefonoCliente.text = detail.user.phone
            tvCorreoCliente.text = detail.user.email

            // Fecha de creación
            tvFechaCreacion.text = "Fecha de creación: ${detail.formatted_date}"

            // Información de precios
            tvTotalOriginal.text = "$${String.format("%.2f", detail.original_price)}"
            tvTotalDescuento.text = "$${String.format("%.2f", detail.total_price)}"
            tvDescuento.text = "${detail.savings_percentage}%"
            tvCantidadProductos.text = "${detail.products.size}"
            tvAhorroTotal.text = "Ahorro Total: $${String.format("%.2f", detail.total_savings)} (${detail.savings_percentage}%)"

            // Configurar botones de contacto
            setupContactButtons(detail)

            // Tiempo límite
            actualizarTiempoLimite(detail)

            // Actualizar lista de productos
            detalleAdapter.submitList(detail.products)
        }
    }

    private fun setupContactButtons(detail: ReservationDetailSeller) {
        // Botón para llamar al cliente
        binding.btnLlamar.setOnClickListener {
            detail.user.phone?.takeIf { it.isNotEmpty() }?.let { phone ->
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
            } ?: showToast("No hay número de teléfono disponible")
        }

        // Botón para enviar email al cliente
        binding.btnEmail.setOnClickListener {
            detail.user.email?.takeIf { it.isNotEmpty() }?.let { email ->
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$email")
                    putExtra(Intent.EXTRA_SUBJECT, "Sobre su apartado #${reservationId} en LocalFresh")
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    showToast("No se pudo abrir la aplicación de correo")
                }
            } ?: showToast("No hay dirección de correo disponible")
        }
    }

    private fun actualizarTiempoLimite(detail: ReservationDetailSeller) {
        // Cancelar timer previo
        countDownTimer?.cancel()

        if (ReservationStatusUtils.needsTimer(detail.status, detail.expiration_date)) {
            binding.tvTiempoLimite.visibility = View.VISIBLE

            countDownTimer = ReservationTimerUtils.startCountDownTimer(
                expirationDate = detail.expiration_date,
                textView = binding.tvTiempoLimite,
                onTimerFinished = {
                    if (isAdded) viewModel.getSellerReservationDetails(reservationId, sellerId)
                }
            )
        } else {
            ReservationTimerUtils.setStaticReservationStatus(binding.tvTiempoLimite, detail.status)

            if (detail.status == ReservationStatusUtils.STATUS_CANCELLED) {
                binding.tvTiempoLimite.apply {
                    visibility = View.VISIBLE
                    text = "Apartado cancelado"
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        countDownTimer = null
        _binding = null
    }
}