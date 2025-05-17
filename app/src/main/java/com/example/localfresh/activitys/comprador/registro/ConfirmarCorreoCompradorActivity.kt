package com.example.localfresh.activitys.comprador.registro

import android.os.Bundle
import com.example.localfresh.R
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.localfresh.viewmodel.comprador.signup.ConfirmarCorreoViewModel
import android.view.View

import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.localfresh.activitys.LogInActivity

class ConfirmarCorreoCompradorActivity : AppCompatActivity() {
    private lateinit var resendButton: Button
    private lateinit var continueButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: ConfirmarCorreoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirmar_correo_comprador_layout)

        // Inicializar vistas
        resendButton = findViewById(R.id.resend_verification_button)
        continueButton = findViewById(R.id.continue_button)
        progressBar = findViewById(R.id.progress_bar)

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(ConfirmarCorreoViewModel::class.java)

        // Obtener email
        val email = intent.getStringExtra("USER_EMAIL") ?: run {
            Toast.makeText(this, "Error: Email no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Botón reenviar correo
        resendButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            viewModel.resendVerificationEmail(email)
        }

        // Botón continuar
        continueButton.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
            finish()
        }

        // Observar respuestas del ViewModel
        viewModel.resendResponse.observe(this) { response ->
            progressBar.visibility = View.GONE
            // Determinar si es un mensaje de éxito o error
            val isSuccess = response.status == "success"
            showToast(response.message, isSuccess)
        }
    }

    private fun showToast(message: String, isSuccess: Boolean) {
        // Inflar el diseño personalizado
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.toast_message))

        // Configurar el mensaje
        val toastMessage = layout.findViewById<TextView>(R.id.toast_message)
        toastMessage.text = message

        // Cambiar el color de fondo según el tipo de mensaje
        if (isSuccess) {
            layout.setBackgroundResource(R.drawable.toast_background_success) // Fondo verde
        } else {
            layout.setBackgroundResource(R.drawable.toast_background_error) // Fondo rojo
        }

        // Crear y mostrar el Toast
        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }
}
