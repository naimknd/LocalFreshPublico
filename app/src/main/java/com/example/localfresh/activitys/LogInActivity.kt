package com.example.localfresh.activitys

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.localfresh.R
import com.example.localfresh.activitys.comprador.registro.CompletarPerfilCompradorActivity
import com.example.localfresh.activitys.comprador.registro.ConfirmarCorreoCompradorActivity
import com.example.localfresh.activitys.comprador.paginaprincipal.PaginaPrincipalCompradorActivity
import com.example.localfresh.activitys.comprador.registro.SignUpCompradorActivity
import com.example.localfresh.activitys.vendedor.paginaprincipal.PaginaPrincipalVendedorActivity
import com.example.localfresh.activitys.vendedor.registrovendedores.ConfirmarCorreoVendedorActivity
import com.example.localfresh.databinding.LoginLayoutBinding
import com.example.localfresh.model.LoginResponse
import com.example.localfresh.viewmodel.LogInViewModel
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest

class LogInActivity : AppCompatActivity() {
    private lateinit var binding: LoginLayoutBinding
    private val viewModel: LogInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar permisos de notificaciones en Android 13 y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }

        validateStoredToken()
        setupListeners()
        observeViewModel()
    }

    private fun validateStoredToken() {
        val sharedPreferences = getSharedPreferences("LocalFreshPrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)

        Log.d("LogInActivity", "Verificando token almacenado...")
        Log.d("LogInActivity", "Token encontrado: ${token != null}")

        if (token != null) {
            viewModel.validateToken(token)
        }
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener { login() }
        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, SignUpCompradorActivity::class.java))
        }
        binding.forgotPassword.setOnClickListener {
            navigateToForgotPasswordActivity()
        }
    }

    private fun observeViewModel() {
        viewModel.loginResponse.observe(this) { response ->
            if (response != null) {
                Log.d("LogInActivity", "Login response: $response")
                if (response.status == "success") {
                    handleSuccessfulLogin(response)
                } else {
                    handleLoginError(response)
                }
            } else {
                Log.e("LogInActivity", "Login response is null")
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }

        viewModel.networkStatus.observe(this) { isConnected ->
            if (!isConnected) {
                Toast.makeText(this, "No hay conexión a internet", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.tokenValidation.observe(this) { isValid ->
            if (isValid) {
                navigateToMainActivity()
            } else {
                getSharedPreferences("LocalFreshPrefs", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
            }
        }
    }

    private fun login() {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        // Obtener el token FCM actual
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                // Pasar el token FCM al metodo de login
                viewModel.login(email, password, deviceId, fcmToken)
            } else {
                // Si no se puede obtener el token, continuar sin él
                viewModel.login(email, password, deviceId, null)
            }
        }
    }

    private fun handleSuccessfulLogin(response: LoginResponse) {
        Log.d("LogInActivity", "Response seller: ${response.seller}")
        Log.d("LogInActivity", "Response user: ${response.user}")

        val sharedPreferences = getSharedPreferences("LocalFreshPrefs", MODE_PRIVATE)

        sharedPreferences.edit().apply {
            putString("TOKEN", response.token)
            if (response.seller != null) {
                putBoolean("IS_SELLER", true)
                putInt("SELLER_ID", response.seller.seller_id)
            } else if (response.user != null) {
                putBoolean("IS_SELLER", false)
                putInt("USER_ID", response.user.user_id)
            }
        }.apply()

        when {
            response.seller?.is_verified == 1 -> {
                startActivity(Intent(this, PaginaPrincipalVendedorActivity::class.java).apply {
                    putExtra("SELLER_ID", response.seller.seller_id)
                })
                finish()
            }
            response.user?.is_verified == 1 -> {
                val intent = if (response.user.first_time_login == 1) {
                    Intent(this, CompletarPerfilCompradorActivity::class.java)
                } else {
                    Intent(this, PaginaPrincipalCompradorActivity::class.java)
                }
                intent.putExtra("USER_ID", response.user.user_id)
                startActivity(intent)
                finish()
            }
            else -> Log.e("LogInActivity", "User or seller not verified")
        }
    }

    private fun handleLoginError(response: LoginResponse) {
        when (response.code) {
            1002 -> {
                Toast.makeText(this, "El vendedor no está verificado. Por favor verifica tu correo.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, ConfirmarCorreoVendedorActivity::class.java).apply {
                    putExtra("USER_EMAIL", binding.emailInput.text.toString().trim())
                })
                finish()
            }
            1001 -> {
                Toast.makeText(this, "El usuario no está verificado. Por favor verifica tu correo.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, ConfirmarCorreoCompradorActivity::class.java).apply {
                    putExtra("USER_EMAIL", binding.emailInput.text.toString().trim())
                })
                finish()
            }
            1003 -> Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_LONG).show()
            1004 -> Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_LONG).show()
            else -> Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToMainActivity() {
        val sharedPreferences = getSharedPreferences("LocalFreshPrefs", MODE_PRIVATE)
        val isSeller = sharedPreferences.getBoolean("IS_SELLER", false)

        val nextIntent = if (isSeller) {
            Intent(this, PaginaPrincipalVendedorActivity::class.java)
        } else {
            Intent(this, PaginaPrincipalCompradorActivity::class.java)
        }

        startActivity(nextIntent)
        finish()
    }

    private fun navigateToForgotPasswordActivity() {
        startActivity(Intent(this, ForgotPasswordActivity::class.java))
    }
}