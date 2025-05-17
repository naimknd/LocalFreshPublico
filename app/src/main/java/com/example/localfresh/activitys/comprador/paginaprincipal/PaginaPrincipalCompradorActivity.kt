package com.example.localfresh.activitys.comprador.paginaprincipal

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.localfresh.R
import com.example.localfresh.activitys.comprador.apartados.VerApartadosFragment
import com.example.localfresh.activitys.comprador.apartados.VerCarritoFragment
import com.example.localfresh.activitys.comprador.paginaprincipal.fragments.PaginaPrincipalFragment
import com.example.localfresh.activitys.comprador.paginaprincipal.fragments.ProfileFragment
import com.example.localfresh.viewmodel.comprador.apartados.CartViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PaginaPrincipalCompradorActivity : AppCompatActivity() {
    private var token: String? = null
    private lateinit var fabCart: FloatingActionButton
    private lateinit var badgeTextView: TextView
    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pagina_principal_comprador_layout)

        val sharedPreferences = getSharedPreferences("LocalFreshPrefs", MODE_PRIVATE)
        token = sharedPreferences.getString("TOKEN", null)

        initializeViews()
        setupCartBadge()
        setupListeners()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Verificar si ya estamos en PaginaPrincipalFragment
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                    if (currentFragment is PaginaPrincipalFragment) {
                        // Si ya estamos en este fragmento, refrescar los datos
                        currentFragment.refreshStoresData()
                        return@setOnItemSelectedListener true
                    }

                    loadHomeFragment()
                }
                R.id.nav_profile -> loadFragment(ProfileFragment())
                R.id.nav_sections -> loadFragment(VerApartadosFragment())
            }
            true
        }
        // Cargar el fragmento inicial
        if (savedInstanceState == null) {
            loadHomeFragment()

        }
    }

    private fun initializeViews() {
        fabCart = findViewById(R.id.fabCart)
        badgeTextView = findViewById(R.id.badge_text_view)
    }

    private fun setupCartBadge() {
        val userId = getSharedPreferences("LocalFreshPrefs", MODE_PRIVATE)
            .getInt("USER_ID", -1)

        if (userId != -1) {
            cartViewModel.cartItems.observe(this) { items ->
                badgeTextView.apply {
                    text = items.size.toString()
                    visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
                }
                // Mostrar u ocultar el FAB según si hay items
                fabCart.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            }
            cartViewModel.getCart(userId)
        }
    }

    fun refreshCartBadge() {
        val userId = getSharedPreferences("LocalFreshPrefs", MODE_PRIVATE)
            .getInt("USER_ID", -1)
        if (userId != -1) {
            cartViewModel.refreshCart(userId)
        }
    }

    private fun setupListeners() {
        fabCart.setOnClickListener {
            // Verificar si ya existe una instancia de VerCarritoFragment
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment !is VerCarritoFragment) {
                val verCarritoFragment = VerCarritoFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, verCarritoFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun loadHomeFragment() {
        val fragment = PaginaPrincipalFragment()
        val bundle = Bundle()
        bundle.putString("TOKEN", token)
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()


    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
