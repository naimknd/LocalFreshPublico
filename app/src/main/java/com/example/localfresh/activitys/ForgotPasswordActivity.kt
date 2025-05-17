package com.example.localfresh.activitys

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.localfresh.R
import com.example.localfresh.model.PasswordResetRequest
import com.example.localfresh.model.PasswordResetResponse
import com.example.localfresh.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailResetInput: EditText
    private lateinit var resetPasswordButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageButton
    private lateinit var loginLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password_layout)

        emailResetInput = findViewById(R.id.email_reset_input)
        resetPasswordButton = findViewById(R.id.reset_password_button)
        progressBar = findViewById(R.id.progressBar)
        backButton = findViewById(R.id.back_button)
        loginLink = findViewById(R.id.login_link)

        resetPasswordButton.setOnClickListener {
            val email = emailResetInput.text.toString().trim()
            if (email.isNotEmpty()) {
                requestPasswordReset(email)
            } else {
                Toast.makeText(this, "Por favor ingresa un correo electrónico", Toast.LENGTH_SHORT).show()
            }
        }

        // Manejar el botón de retroceso
        backButton.setOnClickListener {
            finish() // Cierra la actividad actual
        }

        // Manejar el clic en el enlace de inicio de sesión
        loginLink.setOnClickListener {
        finish() // Cierra la actividad actual
        }
    }

    private fun requestPasswordReset(email: String) {
        progressBar.visibility = ProgressBar.VISIBLE
        val request = PasswordResetRequest(email)

        RetrofitInstance.LocalFreshApiService.requestPasswordReset(request).enqueue(object : Callback<PasswordResetResponse> {
            override fun onResponse(call: Call<PasswordResetResponse>, response: Response<PasswordResetResponse>) {
                progressBar.visibility = ProgressBar.GONE
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        Toast.makeText(this@ForgotPasswordActivity, responseBody.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PasswordResetResponse>, t: Throwable) {
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(this@ForgotPasswordActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}