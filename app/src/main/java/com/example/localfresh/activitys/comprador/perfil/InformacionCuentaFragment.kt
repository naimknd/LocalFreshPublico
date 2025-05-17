package com.example.localfresh.activitys.comprador.perfil

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.localfresh.R
import com.example.localfresh.databinding.FragmentInformacionCuentaBinding
import com.example.localfresh.model.comprador.perfil.UpdateUserProfileRequest
import com.example.localfresh.model.comprador.perfil.User
import com.example.localfresh.utils.DateUtils
import com.example.localfresh.utils.ValidationUtils
import com.example.localfresh.viewmodel.comprador.perfil.UserProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InformacionCuentaFragment : Fragment() {

    private var _binding: FragmentInformacionCuentaBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: UserProfileViewModel
    private var isEditMode = false
    private var hasDataChanged = false
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInformacionCuentaBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackButtonHandling()
        setupObservers()
        setupInputFields()
        setupTextChangeListeners()
        setupEditButtons()
        updateUIMode()
        loadUserProfile()
    }

    private fun setupBackButtonHandling() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackNavigation()
                }
            }
        )
    }

    private fun handleBackNavigation() {
        if (isEditMode && hasDataChanged) {
            showDiscardChangesDialog()
        } else {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun showDiscardChangesDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Descartar cambios")
            .setIcon(R.drawable.ic_warning)
            .setMessage("Los cambios que has realizado se perderán. ¿Estás seguro que deseas salir?")
            .setPositiveButton("Descartar cambios") { _, _ ->
                isEditMode = false
                hasDataChanged = false
                currentUser?.let { updateUI(it) }
                updateUIMode()
                requireActivity().supportFragmentManager.popBackStack()
            }
            .setNegativeButton("Continuar editando", null)
            .show()
    }

    private fun loadUserProfile() {
        val userId = requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            .getInt("USER_ID", -1)

        if (userId != -1) {
            showLoading(true)
            viewModel.fetchUserProfile(userId)
        } else {
            showError("ID de usuario no válido")
        }
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner) { response ->
            showLoading(false)
            if (response != null) {
                currentUser = response.user
                updateUI(response.user)
            } else {
                showError("Error al cargar el perfil")
            }
        }

        viewModel.isUpdating.observe(viewLifecycleOwner) { isUpdating ->
            binding.progressBar.visibility = if (isUpdating) View.VISIBLE else View.GONE
            binding.btnEditarPerfil.isEnabled = !isUpdating
            if (isEditMode) {
                binding.btnSave.isEnabled = !isUpdating
                binding.btnCancel.isEnabled = !isUpdating
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                showSuccessMessage("Perfil actualizado correctamente")
                isEditMode = false
                hasDataChanged = false
                loadUserProfile()
                updateUIMode()
            }
        }

        viewModel.updateError.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { errorMsg ->
                showError(errorMsg)
            }
        }
    }

    private fun updateUI(user: User) {
        with(binding) {
            // Modo de visualización con TextViews
            txtFirstName.text = user.first_name
            txtLastName.text = user.last_name
            txtUsername.text = user.username
            txtEmail.text = user.email
            txtPhone.text = user.phone
            txtBirthdate.text = user.birthdate

            // Rellenar también los campos de edición
            etFirstName.setText(user.first_name)
            etLastName.setText(user.last_name)
            etUsername.setText(user.username)
            etEmail.setText(user.email)
            etPhone.setText(user.phone)
            etBirthdate.setText(user.birthdate)
        }
    }

    private fun setupInputFields() {
        // Selector de fecha
        binding.etBirthdate.setOnClickListener {
            showDatePickerDialog()
        }

        // Validación en tiempo real usando ValidationUtils
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                ValidationUtils.validarEmail(binding.emailLayout, binding.etEmail)
                hasDataChanged = true
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                ValidationUtils.validarTelefono(binding.phoneLayout, binding.etPhone)
                hasDataChanged = true
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etBirthdate.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                ValidationUtils.validarFechaNacimiento(binding.birthdateLayout, binding.etBirthdate)
            }
        }
    }

    private fun setupTextChangeListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                hasDataChanged = true
            }
        }
        binding.etFirstName.addTextChangedListener(textWatcher)
        binding.etLastName.addTextChangedListener(textWatcher)
        binding.etUsername.addTextChangedListener(textWatcher)
    }

    private fun setupEditButtons() {
        binding.btnEditarPerfil.setOnClickListener {
            isEditMode = true
            updateUIMode()
        }
        binding.btnCancel.setOnClickListener {
            if (hasDataChanged) {
                showDiscardChangesDialog()
            } else {
                isEditMode = false
                updateUIMode()
            }
        }
        binding.btnSave.setOnClickListener {
            saveChanges()
        }
        binding.btnBack.setOnClickListener {
            handleBackNavigation()
        }
    }

    private fun updateUIMode() {
        with(binding) {
            toolbarTitle.text = if (isEditMode) getString(R.string.edit_profile) else getString(R.string.account_information)
            btnBack.setImageResource(if (isEditMode) R.drawable.ic_close else R.drawable.ic_back)
            viewModeLayout.visibility = if (isEditMode) View.GONE else View.VISIBLE
            editModeLayout.visibility = if (isEditMode) View.VISIBLE else View.GONE
            editButtonsLayout.visibility = if (isEditMode) View.VISIBLE else View.GONE
            btnEditarPerfil.visibility = if (isEditMode) View.GONE else View.VISIBLE
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(binding.etBirthdate.text.toString())
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {}

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val maxDate = Calendar.getInstance()
        maxDate.add(Calendar.YEAR, -13)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = String.format(
                Locale.getDefault(),
                "%04d-%02d-%02d",
                selectedYear,
                selectedMonth + 1,
                selectedDay
            )
            binding.etBirthdate.setText(selectedDate)
            hasDataChanged = true
            ValidationUtils.validarFechaNacimiento(binding.birthdateLayout, binding.etBirthdate)
        }, year, month, day).apply {
            datePicker.maxDate = maxDate.timeInMillis
            show()
        }
    }

    private fun saveChanges() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val birthdate = binding.etBirthdate.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        val isValid = ValidationUtils.validarFormulario(
            { ValidationUtils.validarCampoRequerido(binding.firstNameLayout, binding.etFirstName, "El nombre no puede estar vacío") },
            { ValidationUtils.validarCampoRequerido(binding.lastNameLayout, binding.etLastName, "El apellido no puede estar vacío") },
            { ValidationUtils.validarCampoRequerido(binding.usernameLayout, binding.etUsername, "El usuario no puede estar vacío") },
            { ValidationUtils.validarEmail(binding.emailLayout, binding.etEmail) },
            { ValidationUtils.validarTelefono(binding.phoneLayout, binding.etPhone) },
            { ValidationUtils.validarFechaNacimiento(binding.birthdateLayout, binding.etBirthdate) }
        )
        if (!isValid) return

        val userId = requireContext().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE)
            .getInt("USER_ID", -1)

        if (userId == -1) {
            showError("ID de usuario no válido")
            return
        }

        if (!hasDataChanged) {
            isEditMode = false
            updateUIMode()
            showInfoMessage("No se realizaron cambios")
            return
        }

        val request = UpdateUserProfileRequest(
            user_id = userId,
            username = username,
            email = email,
            first_name = firstName,
            last_name = lastName,
            birthdate = birthdate,
            phone = phone
        )

        viewModel.updateUserProfile(request)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentContainer.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccessMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.green, null))
            .setTextColor(resources.getColor(R.color.white, null))
            .show()
    }

    private fun showInfoMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = InformacionCuentaFragment()
    }
}
