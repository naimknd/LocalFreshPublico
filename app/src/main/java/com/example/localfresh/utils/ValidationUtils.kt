package com.example.localfresh.utils

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

object ValidationUtils {

    /**
     * Valida múltiples campos y retorna true solo si todos son válidos
     */
    fun validarFormulario(vararg validaciones: () -> Boolean): Boolean {
        var isValid = true
        for (validacion in validaciones) {
            // Si alguna validación falla, marcamos como inválido pero seguimos validando
            // los demás campos para mostrar todos los errores
            if (!validacion()) {
                isValid = false
            }
        }
        return isValid
    }

    /**
     * Valida que un campo no esté vacío
     */
    fun validarCampoRequerido(
        layout: TextInputLayout,
        input: TextInputEditText,
        errorMessage: String = "Este campo es requerido"
    ): Boolean {
        val value = input.text.toString().trim()
        return if (value.isEmpty()) {
            layout.error = errorMessage
            false
        } else {
            layout.error = null
            true
        }
    }

    /**
     * Valida formato de email
     */
    fun validarEmail(
        layout: TextInputLayout,
        input: TextInputEditText
    ): Boolean {
        val email = input.text.toString().trim()
        return when {
            email.isEmpty() -> {
                layout.error = "El correo electrónico no puede estar vacío"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                layout.error = "El correo electrónico no es válido"
                false
            }
            else -> {
                layout.error = null
                true
            }
        }
    }

    /**
     * Valida contraseña cumpliendo todos los requisitos de seguridad:
     * - Al menos 8 caracteres
     * - Al menos una letra mayúscula
     * - Al menos una letra minúscula
     * - Al menos un número
     * - Al menos un carácter especial (@, #, $, %, &, *)
     */
    fun validarPassword(
        layout: TextInputLayout,
        input: TextInputEditText
    ): Boolean {
        val password = input.text.toString().trim()

        // Lista para recopilar todos los errores
        val errores = mutableListOf<String>()

        // Comprobaciones individuales
        if (password.isEmpty()) {
            errores.add("La contraseña no puede estar vacía")
        } else {
            if (password.length < 8) {
                errores.add("Debe tener al menos 8 caracteres")
            }
            if (!password.matches(".*[A-Z].*".toRegex())) {
                errores.add("Debe incluir al menos una letra mayúscula")
            }
            if (!password.matches(".*[a-z].*".toRegex())) {
                errores.add("Debe incluir al menos una letra minúscula")
            }
            if (!password.matches(".*\\d.*".toRegex())) {
                errores.add("Debe incluir al menos un número")
            }
            if (!password.matches(".*[@#$%&*].*".toRegex())) {
                errores.add("Debe incluir al menos un carácter especial (@, #, $, %, &, *)")
            }
        }

        // Establecer el mensaje de error o limpiarlo si no hay errores
        return if (errores.isNotEmpty()) {
            layout.error = errores.joinToString("\n")
            false
        } else {
            layout.error = null
            true
        }
    }

    /**
     * Valida fecha de nacimiento (edad mínima 13 años)
     */
    fun validarFechaNacimiento(
        layout: TextInputLayout,
        input: TextInputEditText
    ): Boolean {
        val birthdate = input.text.toString().trim()
        return when {
            birthdate.isEmpty() -> {
                layout.error = "La fecha de nacimiento no puede estar vacía"
                false
            }
            !DateUtils.validarEdadMinima(birthdate, 13) -> {
                layout.error = "Debes tener al menos 13 años para registrarte"
                false
            }
            else -> {
                layout.error = null
                true
            }
        }
    }

    /**
     * Valida número telefónico (10 dígitos para México)
     */
    fun validarTelefono(
        layout: TextInputLayout,
        input: TextInputEditText
    ): Boolean {
        val phone = input.text.toString().trim()
        return when {
            phone.isEmpty() -> {
                layout.error = "El teléfono no puede estar vacío"
                false
            }
            !phone.matches(Regex("^[0-9]{10}$")) -> {
                layout.error = "El número debe tener 10 dígitos"
                false
            }
            else -> {
                layout.error = null
                true
            }
        }
    }

    /**
     * Crea un TextWatcher para validación en tiempo real de contraseñas
     * Muestra indicadores visuales de los requisitos que se van cumpliendo
     */
    fun createPasswordStrengthWatcher(
        layout: TextInputLayout,
        input: TextInputEditText,
        strengthIndicatorView: TextView? = null
    ): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s?.toString() ?: ""

                // Si está vacío, no mostrar validaciones aún
                if (password.isEmpty()) {
                    layout.error = null
                    strengthIndicatorView?.visibility = View.GONE
                    return
                }

                // Inicializar lista de requisitos cumplidos/faltantes
                val reqs = mutableListOf(
                    Pair("Al menos 8 caracteres", password.length >= 8),
                    Pair("Al menos una mayúscula", password.matches(".*[A-Z].*".toRegex())),
                    Pair("Al menos una minúscula", password.matches(".*[a-z].*".toRegex())),
                    Pair("Al menos un número", password.matches(".*\\d.*".toRegex())),
                    Pair("Al menos un carácter especial (@,#,$,%,&,*)", password.matches(".*[@#$%&*].*".toRegex()))
                )

                // Mostrar estado actual de la validación
                val pendientes = reqs.filter { !it.second }.map { it.first }

                if (pendientes.isEmpty()) {
                    layout.error = null
                    strengthIndicatorView?.text = "Contraseña segura"
                    strengthIndicatorView?.setTextColor(Color.GREEN)
                } else {
                    layout.error = "Requisitos pendientes:\n${pendientes.joinToString("\n• ", "• ")}"

                    // Actualizar indicador de fortaleza si existe
                    strengthIndicatorView?.let {
                        val cumplidos = reqs.count { it.second }
                        it.text = when {
                            cumplidos <= 2 -> "Contraseña débil"
                            cumplidos <= 4 -> "Contraseña media"
                            else -> "Contraseña fuerte"
                        }
                        it.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}