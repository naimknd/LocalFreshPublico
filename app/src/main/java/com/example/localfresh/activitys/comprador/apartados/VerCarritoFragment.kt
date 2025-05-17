package com.example.localfresh.activitys.comprador.apartados

import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.localfresh.R
import com.example.localfresh.activitys.comprador.paginaprincipal.PaginaPrincipalCompradorActivity
import com.example.localfresh.adapters.comprador.CartItemAdapter
import com.example.localfresh.databinding.FragmentVerCarritoBinding
import com.example.localfresh.model.comprador.apartados.CartItem
import com.example.localfresh.model.comprador.apartados.CartResponse
import com.example.localfresh.utils.CartTimer
import com.example.localfresh.viewmodel.comprador.apartados.CartState
import com.example.localfresh.viewmodel.comprador.apartados.CartViewModel
import com.example.localfresh.viewmodel.comprador.apartados.ReservationState
import com.example.localfresh.viewmodel.comprador.apartados.ReservationViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class VerCarritoFragment : Fragment() {
    private val viewModel: CartViewModel by viewModels()
    private val reservationViewModel: ReservationViewModel by viewModels()
    private var _binding: FragmentVerCarritoBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartAdapter: CartItemAdapter
    private var cartTimer: CartTimer? = null
    private val userId by lazy {
        requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE).getInt("USER_ID", -1)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVerCarritoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()
        loadCart()

        // Manejar botón de atrás
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    parentFragmentManager.popBackStack()
                }
            }
        )
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        // Configurar RecyclerView
        cartAdapter = CartItemAdapter()
        binding.rvProducts.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Configurar swipe para eliminar
        setupSwipeToDelete()
    }

    private fun setupObservers() {
        // Estado del carrito
        viewModel.cartState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CartState.Success -> {
                    updateViewState(state.response.items.isEmpty() || state.response.cart == null)
                    if (state.response.cart != null) updateUI(state.response)
                }
                is CartState.Error -> {
                    showToast(state.message)
                    updateViewState(true)
                }
            }
            binding.loadingContainer.isVisible = false
        }

        // Estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) binding.loadingContainer.isVisible = true
        }

        // Resultados de eliminar del carrito
        viewModel.removeFromCartResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    if (it.items_remaining == 0) {
                        showToast("Carrito vacío")
                        updateViewState(true)
                    } else {
                        showToast("Producto eliminado")
                        loadCart()
                    }
                },
                onFailure = { showToast("Error: ${it.message}") }
            )
        }

        // Estado de la reservación
        reservationViewModel.reservationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ReservationState.Success -> {
                    showToast(state.response.message)
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                is ReservationState.Error -> showToast(state.message)
            }
        }

        // Estado de carga para reservación
        reservationViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnConfirm.isEnabled = !isLoading
        }
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val item = cartAdapter.currentList[position]

                if (item.quantity > 1) {
                    showQuantityDialog(item)
                    cartAdapter.notifyItemChanged(position)
                } else {
                    removeCartItem(item.unit_id)
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                     dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                drawSwipeBackground(c, viewHolder.itemView, dX)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvProducts)
    }

    private fun drawSwipeBackground(c: Canvas, itemView: View, dX: Float) {
        val deleteIcon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete)
        val background = ColorDrawable(Color.rgb(244, 67, 54))

        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        background.draw(c)

        val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2
        val iconTop = itemView.top + iconMargin
        val iconBottom = iconTop + deleteIcon.intrinsicHeight
        val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
        val iconRight = itemView.right - iconMargin
        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        deleteIcon.setTint(Color.WHITE)
        deleteIcon.draw(c)
    }

    private fun updateViewState(isEmpty: Boolean) {
        binding.apply {
            emptyStateContainer.isVisible = isEmpty
            contentContainer.isVisible = !isEmpty
        }
    }

    private fun updateUI(response: CartResponse) {
        response.cart?.let { cart ->
            binding.apply {
                // Información básica
                tvStoreName.text = cart.seller_name
                tvStoreHours.text = cart.store_hours
                tvTotal.text = getString(R.string.price_format, cart.total)

                // Precios con descuento
                val hasDiscount = cart.total_savings_percentage > 0
                tvTotalOriginal.apply {
                    text = getString(R.string.price_format, cart.total_original_price)
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    isVisible = hasDiscount
                }

                tvSavingsPercentage.apply {
                    text = "Ahorro Total: ${cart.total_savings_percentage}%"
                    isVisible = hasDiscount
                }

                // Botón de confirmar
                btnConfirm.setOnClickListener { confirmReservation() }

                // Temporizador
                setupTimer(cart.expiration_time, cart.expires_in_minutes)
            }

            // Productos
            cartAdapter.submitList(response.items)
        }
    }

    private fun setupTimer(expirationTime: String, expiresInMinutes: Int) {
        cartTimer?.stop()
        cartTimer = CartTimer(
            onTick = { minutes, seconds, remainingTime ->
                _binding?.tvTimeLimit?.apply {
                    text = "Tiempo restante: $minutes:${seconds.toString().padStart(2, '0')}"
                    setTextColor(ContextCompat.getColor(requireContext(), when {
                        remainingTime < 60000 -> R.color.red
                        remainingTime < 180000 -> R.color.warning_color
                        else -> R.color.text_color
                    }))
                }
            },
            onFinish = {
                _binding?.apply {
                    tvTimeLimit.text = "¡Tiempo expirado!"
                    tvTimeLimit.setTextColor(Color.RED)
                    btnConfirm.isEnabled = false
                    updateViewState(true)
                }
            }
        ).apply { start(expirationTime, expiresInMinutes) }
    }

    private fun confirmReservation() {
        val cartId = (viewModel.cartState.value as? CartState.Success)?.response?.cart?.cart_id
        val total = (viewModel.cartState.value as? CartState.Success)?.response?.cart?.total ?: 0.0

        if (userId != -1 && cartId != null) {
            MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
                .setTitle("Información de Pago")
                .setIcon(R.drawable.ic_info)
                .setMessage("Recuerda que el pago de $${String.format("%.2f", total)} se realizará FÍSICAMENTE cuando recojas tu apartado en la tienda.\n\n" +
                        "• Deberás mostrar el código QR al vendedor.\n" +
                        "• El apartado tiene un tiempo límite antes de que expire.\n" +
                        "• No se requiere pago en línea.")
                .setPositiveButton("Confirmar Apartado") { _, _ ->
                    reservationViewModel.confirmReservation(userId, cartId)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            showToast("No se puede confirmar la reservación")
        }
    }

    private fun showQuantityDialog(item: CartItem) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_quantity_picker, null)
        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.numberPicker).apply {
            minValue = 1
            maxValue = item.quantity
            value = 1
        }

        AlertDialog.Builder(requireContext())
            .setTitle("¿Cuántas unidades desea eliminar?")
            .setView(dialogView)
            .setPositiveButton("Eliminar") { _, _ -> removeCartItem(item.unit_id, numberPicker.value) }
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Eliminar todo") { _, _ -> removeCartItem(item.unit_id) }
            .show()
    }

    private fun removeCartItem(unitId: Int, quantity: Int? = null) {
        if (userId != -1) viewModel.removeFromCart(userId, unitId, quantity)
    }

    private fun loadCart() {
        if (userId != -1) viewModel.getCart(userId)
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        (activity as? PaginaPrincipalCompradorActivity)?.apply {
            findViewById<FloatingActionButton>(R.id.fabCart)?.hide()
            findViewById<TextView>(R.id.badge_text_view)?.visibility = View.GONE
        }
        cartTimer?.resume()
    }

    override fun onPause() {
        super.onPause()
        (activity as? PaginaPrincipalCompradorActivity)?.apply {
            findViewById<FloatingActionButton>(R.id.fabCart)?.show()
            refreshCartBadge()
        }
        cartTimer?.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cartTimer?.stop()
        cartTimer = null
        _binding = null
    }
}