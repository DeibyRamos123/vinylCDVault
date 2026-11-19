package com.example.vinylcdvault.ui.buyer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.CartItem
import com.example.vinylcdvault.ui.common.BiometricHelper
import com.example.vinylcdvault.util.LocationHelper
import com.example.vinylcdvault.util.Pricing
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.toCop
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * Pantalla de pago: captura la ubicación de entrega, pide la huella, valida la dirección
 * y crea el pedido con los discos del carrito.
 */
class CheckoutActivity : AppCompatActivity() {

    private val viewModel: CartViewModel by viewModels { CartViewModelFactory() }

    private var biometricVerified = false
    private var cartItems: List<CartItem> = emptyList()
    private var latitude = 0.0
    private var longitude = 0.0

    private lateinit var btnPay: MaterialButton
    private lateinit var btnLocation: MaterialButton
    private lateinit var tvLocation: TextView
    private lateinit var etAddress: TextInputEditText

    // Pide los permisos de ubicación; con que se conceda uno (exacta o aproximada) se puede continuar
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) captureLocation()
        else Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        etAddress = findViewById(R.id.etAddress)
        tvLocation = findViewById(R.id.tvLocation)
        btnLocation = findViewById(R.id.btnLocation)
        btnPay = findViewById(R.id.btnPay)
        val tvTotal = findViewById<TextView>(R.id.tvCheckoutTotal)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        btnLocation.setOnClickListener { requestLocation() }

        // La huella es obligatoria antes de confirmar el pago
        findViewById<View>(R.id.btnPayBiometric).setOnClickListener {
            BiometricHelper.authenticate(this) {
                biometricVerified = true
                Toast.makeText(this, R.string.biometric_verified, Toast.LENGTH_SHORT).show()
            }
        }

        btnPay.setOnClickListener {
            val address = etAddress.text.toString().trim()
            when {
                cartItems.isEmpty() -> Toast.makeText(this, R.string.cart_empty, Toast.LENGTH_SHORT).show()
                address.isEmpty() -> etAddress.error = getString(R.string.field_required)
                !biometricVerified -> Toast.makeText(this, R.string.biometric_required, Toast.LENGTH_LONG).show()
                else -> viewModel.placeOrder(cartItems, address, latitude, longitude)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Total en tiempo real, calculado con los discos del carrito
                launch {
                    viewModel.items.collect { state ->
                        if (state is Resource.Success) {
                            cartItems = state.data
                            tvTotal.text = Pricing.total(state.data).toCop()
                        }
                    }
                }
                // Resultado del pedido
                launch {
                    viewModel.opState.collect { state ->
                        when (state) {
                            Resource.Loading -> btnPay.isEnabled = false
                            is Resource.Success -> {
                                viewModel.resetOp()
                                Toast.makeText(this@CheckoutActivity, state.data, Toast.LENGTH_LONG).show()
                                finish()
                            }
                            is Resource.Error -> {
                                btnPay.isEnabled = true
                                Toast.makeText(this@CheckoutActivity, state.message, Toast.LENGTH_LONG).show()
                                viewModel.resetOp()
                            }
                            null -> btnPay.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    // Si ya hay permiso, captura la ubicación; si no, lo solicita al usuario
    private fun requestLocation() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (granted) captureLocation()
        else permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // Obtiene las coordenadas, las convierte en dirección y rellena el campo
    private fun captureLocation() {
        lifecycleScope.launch {
            btnLocation.isEnabled = false
            tvLocation.setText(R.string.checkout_location_loading)
            try {
                val location = LocationHelper.currentLocation(this@CheckoutActivity)
                if (location == null) {
                    tvLocation.setText(R.string.checkout_location_none)
                    Toast.makeText(this@CheckoutActivity, R.string.location_unavailable, Toast.LENGTH_LONG).show()
                } else {
                    latitude = location.latitude
                    longitude = location.longitude
                    tvLocation.text = getString(R.string.checkout_location_ok, latitude, longitude)
                    // Si hay dirección legible, se escribe en el campo (el usuario puede corregirla)
                    LocationHelper.addressFor(this@CheckoutActivity, latitude, longitude)
                        ?.let { etAddress.setText(it) }
                }
            } catch (e: Exception) {
                tvLocation.setText(R.string.checkout_location_none)
                Toast.makeText(this@CheckoutActivity, R.string.location_unavailable, Toast.LENGTH_LONG).show()
            } finally {
                btnLocation.isEnabled = true
            }
        }
    }
}