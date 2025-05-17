package com.example.localfresh.activitys.comprador.registro

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.localfresh.R
import com.example.localfresh.activitys.vendedor.registrovendedores.SignUpVendedoresActivity
import com.example.localfresh.databinding.SignupCompradorLayoutBinding
import com.example.localfresh.model.comprador.SignUpCompradorRequest
import com.example.localfresh.utils.ValidationUtils
import com.example.localfresh.viewmodel.comprador.signup.SignUpCompradorViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar
import java.util.Locale

class SignUpCompradorActivity : AppCompatActivity() {

    private lateinit var binding: SignupCompradorLayoutBinding
    private lateinit var signUpCompradorViewModel: SignUpCompradorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar binding
        binding = SignupCompradorLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupListeners()
        setupInputValidation()
    }

    private fun setupViewModel() {
        // Inicializamos el ViewModel
        signUpCompradorViewModel = ViewModelProvider(this)[SignUpCompradorViewModel::class.java]

        // Observamos la respuesta del registro
        signUpCompradorViewModel.signUpResponse.observe(this, Observer { message ->
            binding.progressBar.visibility = View.GONE
            binding.createAccountButton.isEnabled = true

            // Determinar si es un mensaje de éxito o error
            val isSuccess = message.contains("Registro exitoso")
            if (isSuccess) {
                showSuccessMessage(message)
            } else {
                showError(message)
            }
        })

        // Observamos el éxito del registro
        signUpCompradorViewModel.registrationSuccess.observe(this, Observer { success ->
            if (success) {
                val intent = Intent(this, ConfirmarCorreoCompradorActivity::class.java)
                intent.putExtra("USER_EMAIL", binding.emailInput.text.toString().trim())
                startActivity(intent)
                finish()
            }
        })
    }

    private fun setupListeners() {
        // Manejar el botón de retroceso
        binding.backButton.setOnClickListener {
            finish() // Cierra la actividad actual
        }

        // Configurar el selector de fecha
        binding.birthdateInput.setOnClickListener {
            showDatePickerDialog()
        }

        // Configurar el link para ir a registro de vendedor
        binding.businessLogin.setOnClickListener {
            val intent = Intent(this, SignUpVendedoresActivity::class.java)
            startActivity(intent)
        }

        // Configuramos el botón de registro
        binding.createAccountButton.setOnClickListener {
            if (validateAllInputs()) {
                binding.progressBar.visibility = View.VISIBLE
                binding.createAccountButton.isEnabled = false
                registerUser()
            }
        }
    }

    private fun setupInputValidation() {
        // Validación en tiempo real para el email
        binding.emailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                ValidationUtils.validarEmail(binding.emailInputLayout, binding.emailInput)
            }
        })

        // Validación en tiempo real para el teléfono
        binding.phoneInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                ValidationUtils.validarTelefono(binding.phoneInputLayout, binding.phoneInput)
            }
        })

        binding.passwordInput.addTextChangedListener(
            ValidationUtils.createPasswordStrengthWatcher(
                binding.passwordInputLayout,
                binding.passwordInput
            )
        )

        // Configurar México como país por defecto (código +52)
        binding.ccp.setDefaultCountryUsingNameCode("MX")
        binding.ccp.resetToDefaultCountry()
    }

    private fun registerUser() {
        // Obtener el número completo con código de país
        val completePhoneNumber = binding.ccp.selectedCountryCodeWithPlus + binding.phoneInput.text.toString()

        val request = SignUpCompradorRequest(
            username = binding.usernameInput.text.toString().trim(),
            email = binding.emailInput.text.toString().trim(),
            password = binding.passwordInput.text.toString().trim(),
            firstName = binding.firstNameInput.text.toString().trim(),
            lastName = binding.lastNameInput.text.toString().trim(),
            birthdate = binding.birthdateInput.text.toString().trim(),
            phone = completePhoneNumber, // Aquí usamos el número completo con prefijo internacional
            firstTimeLogin = 1 // Valor predeterminado
        )
        signUpCompradorViewModel.signUpComprador(request)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Configurar fecha máxima (13 años atrás)
        val maxDate = Calendar.getInstance()
        maxDate.add(Calendar.YEAR, -13)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, dayOfMonth ->
                val selectedDate = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    dayOfMonth
                )
                binding.birthdateInput.setText(selectedDate)  // Muestra la fecha en el formato correcto
                ValidationUtils.validarFechaNacimiento(
                    binding.birthdateInputLayout,
                    binding.birthdateInput
                )
            }, year, month, day
        )

        datePickerDialog.datePicker.maxDate = maxDate.timeInMillis
        datePickerDialog.show()
    }

    // Función para validar todos los campos
    private fun validateAllInputs(): Boolean {
        return ValidationUtils.validarFormulario(
            { ValidationUtils.validarCampoRequerido(
                binding.firstNameInputLayout,
                binding.firstNameInput,
                "El nombre no puede estar vacío"
            ) },
            { ValidationUtils.validarCampoRequerido(
                binding.lastNameInputLayout,
                binding.lastNameInput,
                "El apellido no puede estar vacío"
            ) },
            { ValidationUtils.validarCampoRequerido(
                binding.usernameInputLayout,
                binding.usernameInput,
                "El nombre de usuario no puede estar vacío"
            ) },
            { ValidationUtils.validarEmail(
                binding.emailInputLayout,
                binding.emailInput
            ) },
            { ValidationUtils.validarPassword(
                binding.passwordInputLayout,
                binding.passwordInput
            ) },
            { ValidationUtils.validarFechaNacimiento(
                binding.birthdateInputLayout,
                binding.birthdateInput
            ) },
            { ValidationUtils.validarTelefono(
                binding.phoneInputLayout,
                binding.phoneInput
            ) }
        )
    }

    private fun showError(message: String) {
        // Usando Material Dialog para errores importantes
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Aceptar", null)
            .setIcon(R.drawable.ic_warning)
            .show()
    }

    private fun showSuccessMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.green))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }
}
