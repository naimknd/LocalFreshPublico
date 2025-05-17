package com.example.localfresh.activitys.vendedor.registrovendedores

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.localfresh.activitys.LogInActivity
import com.example.localfresh.databinding.ConfirmarCorreoVendedorLayoutBinding
import com.example.localfresh.viewmodel.vendedor.signup.ConfirmarCorreoVendedorViewModel
import com.google.android.material.snackbar.Snackbar

class ConfirmarCorreoVendedorActivity : AppCompatActivity() {

    private lateinit var binding: ConfirmarCorreoVendedorLayoutBinding
    private lateinit var viewModel: ConfirmarCorreoVendedorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ConfirmarCorreoVendedorLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ConfirmarCorreoVendedorViewModel::class.java]

        val email = intent.getStringExtra("USER_EMAIL")
        if (email.isNullOrEmpty()) {
            showSnackbar("Error: Email no encontrado", false)
            finish()
            return
        }

        binding.resendVerificationButton.setOnClickListener {
            setLoading(true)
            viewModel.resendVerificationEmail(email)
        }

        binding.continueButton.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
            finish()
        }

        viewModel.resendResponse.observe(this) { response ->
            setLoading(false)
            showSnackbar(response.message, response.status == "success")
        }
    }

    private fun setLoading(loading: Boolean) = with(binding) {
        progressBar.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        resendVerificationButton.isEnabled = !loading
        continueButton.isEnabled = !loading
    }

    private fun showSnackbar(message: String, isSuccess: Boolean) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(if (isSuccess) com.example.localfresh.R.color.green else com.example.localfresh.R.color.red))
            .setTextColor(getColor(com.example.localfresh.R.color.white))
            .show()
    }
}