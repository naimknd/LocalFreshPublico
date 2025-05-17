package com.example.localfresh.activitys.comprador.registro

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.localfresh.R
import com.example.localfresh.activitys.comprador.paginaprincipal.PaginaPrincipalCompradorActivity
import com.example.localfresh.model.comprador.PreferenciasCompradorRequest
import com.example.localfresh.viewmodel.comprador.signup.CompletarPerfilViewModel

class CompletarPerfilCompradorActivity : AppCompatActivity() {

    private val viewModel: CompletarPerfilViewModel by viewModels()
    private var userId: Int = 0

    private var preferenciasSeleccionadas: Map<String, Int> = emptyMap()
    private var distanciaSeleccionada: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completar_perfil_comprador)

        // Obtener el ID del usuario desde el Intent
        userId = intent.getIntExtra("USER_ID", 0)

        // Configurar el fragmento inicial
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PreferenciasFragment())
                .commit()
        }

        // Observar el resultado del completado de perfil
        viewModel.responseMessage.observe(this, Observer { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            if (message == "Preferencias guardadas correctamente.") {
                // Redireccionar a la página principal
                val intent = Intent(this, PaginaPrincipalCompradorActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }

    fun guardarPreferencias(preferencias: Map<String, Int>) {
        preferenciasSeleccionadas = preferencias
    }

    fun guardarDistancia(distancia: Int) {
        distanciaSeleccionada = distancia
        enviarDatosAlServidor()
    }

    fun mostrarSeleccionarDistanciaBottomSheet() {
        SeleccionarDistanciaBottomSheet { distanciaSeleccionada ->
            guardarDistancia(distanciaSeleccionada)
        }.show(supportFragmentManager, "SeleccionarDistanciaBottomSheet")
    }

    private fun enviarDatosAlServidor() {
        val preferencias = PreferenciasCompradorRequest(
            user_id = userId,
            preferencias_bebidas = preferenciasSeleccionadas["preferencias_bebidas"] ?: 0,
            preferencias_carnes = preferenciasSeleccionadas["preferencias_carnes"] ?: 0,
            preferencias_comida_mascotas = preferenciasSeleccionadas["preferencias_comida_mascotas"] ?: 0,
            preferencias_congelados = preferenciasSeleccionadas["preferencias_congelados"] ?: 0,
            preferencias_granos_cereales = preferenciasSeleccionadas["preferencias_granos_cereales"] ?: 0,
            preferencias_huevos = preferenciasSeleccionadas["preferencias_huevos"] ?: 0,
            preferencias_lacteos = preferenciasSeleccionadas["preferencias_lacteos"] ?: 0,
            preferencias_legumbres = preferenciasSeleccionadas["preferencias_legumbres"] ?: 0,
            preferencias_panaderia = preferenciasSeleccionadas["preferencias_panaderia"] ?: 0,
            preferencias_snacks = preferenciasSeleccionadas["preferencias_snacks"] ?: 0,
            rango_distancia = distanciaSeleccionada
        )

        // Llamar al ViewModel para guardar las preferencias
        viewModel.guardarPreferencias(preferencias)
    }
}