package com.example.localfresh.activitys.vendedor.paginaprincipal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.localfresh.R
import com.example.localfresh.activitys.vendedor.apartados.VerApartadosVendedorFragment
import com.example.localfresh.activitys.vendedor.notificaciones.NotificacionesVendedorFragment
import com.example.localfresh.activitys.vendedor.paginaprincipal.fragments.PaginaPrincipalVendedorFragment
import com.example.localfresh.activitys.vendedor.paginaprincipal.fragments.ProductsVendedorFragment
import com.example.localfresh.databinding.PaginaPrincipalVendedorLayoutBinding

class PaginaPrincipalVendedorActivity : AppCompatActivity() {

    private lateinit var binding: PaginaPrincipalVendedorLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PaginaPrincipalVendedorLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PaginaPrincipalVendedorFragment())
                .commit()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PaginaPrincipalVendedorFragment())
                        .commit()
                    true
                }
                R.id.nav_products -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProductsVendedorFragment())
                        .commit()
                    true
                }
                R.id.nav_apartados -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, VerApartadosVendedorFragment())
                        .commit()
                    true
                }
                R.id.nav_notifications -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, NotificacionesVendedorFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}