package com.example.localfresh.activitys.vendedor.paginaprincipal.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.localfresh.R
import com.example.localfresh.activitys.vendedor.registrovendedores.SelectAddressStoreSignUp
import com.example.localfresh.databinding.EditarInformacionTiendaLayoutBinding
import com.example.localfresh.model.vendedor.paginaprincipal.UpdateSellerInfoRequest
import com.example.localfresh.utils.ImageUtils
import com.example.localfresh.viewmodel.vendedor.paginaprincipal.EditarInformacionTiendaViewModel
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import java.io.File

class EditarInformacionTiendaFragment : Fragment() {

    private var _binding: EditarInformacionTiendaLayoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditarInformacionTiendaViewModel by viewModels()

    // Variables de estado
    private var selectedImageUri: Uri? = null
    private var selectedLatitude: Double = 0.0
    private var selectedLongitude: Double = 0.0
    private var openingTime: String = ""
    private var closingTime: String = ""
    private var sellerId: Int = -1

    // Launchers para actividades externas
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { ImageUtils.getCropIntent(requireContext(), it, 800, 800) }?.let(cropImageLauncher::launch)
    }

    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            com.yalantis.ucrop.UCrop.getOutput(result.data!!)?.let { uri ->
                binding.storeImage.setImageURI(uri)
                selectedImageUri = uri
            }
        } else if (result.resultCode == com.yalantis.ucrop.UCrop.RESULT_ERROR) {
            showToast(com.yalantis.ucrop.UCrop.getError(result.data!!)?.message ?: "Error al recortar la imagen", false)
        }
    }

    private val getLocationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.apply {
                binding.storeLocationInput.setText(getStringExtra(SelectAddressStoreSignUp.EXTRA_ADDRESS))
                selectedLatitude = getDoubleExtra("latitude", 0.0)
                selectedLongitude = getDoubleExtra("longitude", 0.0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sellerId = arguments?.getInt("SELLER_ID", -1) ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = EditarInformacionTiendaLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración inicial
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        binding.ccpEdit.setDefaultCountryUsingNameCode("MX")

        setupUI()
        setupObservers()

        // Cargar datos del vendedor
        if (sellerId != -1) viewModel.fetchSellerInfo(sellerId)
        else {
            showToast("ID de vendedor no válido", false)
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupUI() {
        with(binding) {
            // Configurar selección de imagen
            storeImage.setOnClickListener { getContent.launch("image/*") }
            changeImageButton.setOnClickListener { getContent.launch("image/*") }

            // Configurar selección de ubicación
            storeLocationInput.setOnClickListener {
                getLocationLauncher.launch(Intent(requireContext(), SelectAddressStoreSignUp::class.java))
            }

            // Configurar selección de horario
            storeHoursInput.setOnClickListener { showTimePickers() }

            // Guardar cambios
            saveButton.setOnClickListener { saveChanges() }
        }
    }

    private fun showTimePickers() {
        TimePickerDialog.newInstance(
            { _, hourOfDay, minute, _ ->
                openingTime = String.format("%02d:%02d", hourOfDay, minute)

                // Mostrar picker para hora de cierre
                TimePickerDialog.newInstance(
                    { _, closingHour, closingMinute, _ ->
                        closingTime = String.format("%02d:%02d", closingHour, closingMinute)
                        binding.storeHoursInput.setText("De $openingTime a $closingTime")
                    },
                    true
                ).apply {
                    title = "Selecciona la hora de cierre"
                    show(parentFragmentManager, "ClosingTimePicker")
                }
            },
            true
        ).apply {
            title = "Selecciona la hora de apertura"
            show(parentFragmentManager, "OpeningTimePicker")
        }
    }

    private fun setupObservers() {
        // Obtener información del vendedor
        viewModel.sellerInfo.observe(viewLifecycleOwner) { response ->
            response?.data?.let { seller ->
                with(binding) {
                    // Rellenar datos básicos
                    storeNameInput.setText(seller.store_name)
                    storeLocationInput.setText(seller.store_address)
                    storeDescriptionInput.setText(seller.store_description)

                    // Configurar horario
                    openingTime = seller.opening_time
                    closingTime = seller.closing_time
                    storeHoursInput.setText("De $openingTime a $closingTime")

                    // Configurar teléfono
                    setupPhoneWithCountryCode(seller.store_phone)

                    // Configurar ubicación
                    selectedLatitude = seller.latitude
                    selectedLongitude = seller.longitude

                    // Cargar logo de la tienda
                    if (!seller.store_logo.isNullOrEmpty()) {
                        Glide.with(this@EditarInformacionTiendaFragment)
                            .load(seller.store_logo)
                            .placeholder(R.drawable.ic_placeholder)
                            .into(storeImage)
                    }
                }
            }
        }

        // Respuestas de actualización
        viewModel.updateSuccess.observe(viewLifecycleOwner) { if (it) handleSuccess("Cambios guardados exitosamente") }
        viewModel.errorMessage.observe(viewLifecycleOwner) { showToast(it, false) }
        viewModel.infoMessage.observe(viewLifecycleOwner) { it?.let { handleSuccess(it) } }
    }

    private fun handleSuccess(message: String) {
        showToast(message, true)
        parentFragmentManager.popBackStack()
    }

    private fun saveChanges() {
        with(binding) {
            // Validar campos obligatorios
            val updatedName = storeNameInput.text.toString()
            val updatedLocation = storeLocationInput.text.toString()
            val phoneNumber = storePhoneInput.text.toString()

            if (updatedName.isEmpty() || updatedLocation.isEmpty() || openingTime.isEmpty() || closingTime.isEmpty()) {
                showToast("Por favor, completa todos los campos obligatorios", false)
                return
            }

            if (!isValidPhone(phoneNumber)) return

            // Enviar actualización
            viewModel.updateSellerInfo(UpdateSellerInfoRequest(
                seller_id = sellerId,
                store_name = updatedName,
                store_address = updatedLocation,
                store_phone = binding.ccpEdit.selectedCountryCodeWithPlus + phoneNumber,
                store_description = storeDescriptionInput.text.toString(),
                opening_time = openingTime,
                closing_time = closingTime,
                store_logo = selectedImageUri?.path?.let { File(it) },
                latitude = selectedLatitude,
                longitude = selectedLongitude
            ))
        }
    }

    private fun isValidPhone(phone: String): Boolean {
        when {
            phone.isEmpty() -> {
                showToast("El teléfono no puede estar vacío", false)
                return false
            }
            !phone.matches("^[0-9]{10}$".toRegex()) -> {
                showToast("El número debe tener 10 dígitos", false)
                return false
            }
            else -> return true
        }
    }

    private fun setupPhoneWithCountryCode(phoneNumber: String) {
        if (phoneNumber.startsWith("+")) {
            try {
                // Intentar extraer código de país y número
                for (i in 1 until phoneNumber.length.coerceAtMost(5)) {
                    val potentialCode = phoneNumber.substring(1, i + 1)
                    binding.ccpEdit.setCountryForPhoneCode(potentialCode.toIntOrNull() ?: continue)

                    if (binding.ccpEdit.selectedCountryCode == potentialCode) {
                        binding.storePhoneInput.setText(phoneNumber.substring(i + 1))
                        return
                    }
                }
                // Si no se pudo extraer el código, mostrar sin el "+"
                binding.storePhoneInput.setText(phoneNumber.substring(1))
            } catch (e: Exception) {
                binding.storePhoneInput.setText(phoneNumber)
            }
        } else {
            binding.storePhoneInput.setText(phoneNumber)
        }
    }

    private fun showToast(message: String, isSuccess: Boolean) {
        val layout = layoutInflater.inflate(R.layout.custom_toast, null).apply {
            findViewById<android.widget.TextView>(R.id.toast_message).text = message
            setBackgroundResource(if (isSuccess) R.drawable.toast_background_success else R.drawable.toast_background_error)
        }

        android.widget.Toast(requireContext()).apply {
            duration = android.widget.Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(sellerId: Int) = EditarInformacionTiendaFragment().apply {
            arguments = Bundle().apply { putInt("SELLER_ID", sellerId) }
        }
    }
}