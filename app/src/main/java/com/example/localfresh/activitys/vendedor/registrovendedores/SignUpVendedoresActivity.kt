package com.example.localfresh.activitys.vendedor.registrovendedores

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.localfresh.activitys.comprador.registro.SignUpCompradorActivity
import com.example.localfresh.databinding.SignupvendedoresLayoutBinding
import com.example.localfresh.model.vendedor.signup.SignUpVendedorRequest
import com.example.localfresh.viewmodel.vendedor.signup.SignUpVendedoresViewModel
import com.example.localfresh.utils.ValidationUtils

class SignUpVendedoresActivity : AppCompatActivity() {

    private lateinit var binding: SignupvendedoresLayoutBinding
    private lateinit var viewModel: SignUpVendedoresViewModel

    private var openingTime: String = ""
    private var closingTime: String = ""
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    private val selectAddressLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            binding.storeAddressInput.setText(result.data?.getStringExtra("address"))
            selectedLatitude = result.data?.getDoubleExtra("latitude", 0.0)
            selectedLongitude = result.data?.getDoubleExtra("longitude", 0.0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignupvendedoresLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SignUpVendedoresViewModel::class.java]

        binding.selectAddressButton.setOnClickListener {
            val intent = Intent(this, SelectAddressStoreSignUp::class.java)
            selectAddressLauncher.launch(intent)
        }

        binding.openingTimeButton.setOnClickListener { showTimePicker(true) }
        binding.closingTimeButton.setOnClickListener { showTimePicker(false) }

        binding.emailInput.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: android.text.Editable?) {
                ValidationUtils.validarEmail(binding.emailInputLayout, binding.emailInput)
            }
        })

        binding.registerStoreButton.setOnClickListener {
            if (validateAllInputs()) {
                registrarVendedor()
            }
        }

        binding.buyerRedirectLink.setOnClickListener {
            val intent = Intent(this, SignUpCompradorActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Observa la respuesta del registro
        viewModel.registrationResponse.observe(this) { response ->
            if (response.status == "success") {
                val intent = Intent(this, ConfirmarCorreoVendedorActivity::class.java)
                intent.putExtra("USER_EMAIL", binding.emailInput.text.toString().trim())
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    private fun showTimePicker(isOpening: Boolean) {
        val listener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val time = String.format("%02d:%02d", hourOfDay, minute)
            if (isOpening) {
                openingTime = time
                binding.openingTimeButton.text = "Apertura: $time"
            } else {
                closingTime = time
                binding.closingTimeButton.text = "Cierre: $time"
            }
        }
        TimePickerDialog(this, listener, 9, 0, true).show()
    }

    private fun validateAllInputs(): Boolean {
        return ValidationUtils.validarFormulario(
            { ValidationUtils.validarCampoRequerido(binding.storeNameInputLayout, binding.storeNameInput, "El nombre de la tienda es obligatorio") },
            { ValidationUtils.validarEmail(binding.emailInputLayout, binding.emailInput) },
            { ValidationUtils.validarPassword(binding.passwordInputLayout, binding.passwordInput) },
            { ValidationUtils.validarCampoRequerido(binding.storeDescriptionInputLayout, binding.storeDescriptionInput, "La descripción es obligatoria") },
            { ValidationUtils.validarTelefono(binding.storePhoneInputLayout, binding.storePhoneInput) },
            { ValidationUtils.validarCampoRequerido(binding.storeAddressInputLayout, binding.storeAddressInput, "La dirección es obligatoria") }
        )
    }

    private fun registrarVendedor() {
        val storeName = binding.storeNameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()
        val storeDescription = binding.storeDescriptionInput.text.toString().trim()
        val phoneNumber = binding.storePhoneInput.text.toString().trim()
        val completePhoneNumber = "+52$phoneNumber"
        val storeAddress = binding.storeAddressInput.text.toString().trim()
        val organicProducts = if (binding.organicYesOption.isChecked) 1 else 0
        val storeType = when {
            binding.supermarketsOption.isChecked -> "Supermercados"
            binding.localMarketsOption.isChecked -> "Mercados Locales"
            binding.localShopsOption.isChecked -> "Tiendas Locales"
            else -> "Tiendas Locales"
        }

        if (selectedLatitude == null || selectedLongitude == null) {
            Toast.makeText(this, "Selecciona una dirección válida en el mapa", Toast.LENGTH_SHORT).show()
            return
        }

        val request = SignUpVendedorRequest(
            store_name = storeName,
            email = email,
            password = password,
            store_description = storeDescription,
            store_phone = completePhoneNumber,
            store_address = storeAddress,
            latitude = selectedLatitude!!,
            longitude = selectedLongitude!!,
            organic_products = organicProducts,
            store_type = storeType,
            opening_time = openingTime,
            closing_time = closingTime
        )

        viewModel.registerVendedor(request)
        Toast.makeText(this, "Registro enviado", Toast.LENGTH_SHORT).show()
    }
}

// Utilidad simple para TextWatcher
abstract class SimpleTextWatcher : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}