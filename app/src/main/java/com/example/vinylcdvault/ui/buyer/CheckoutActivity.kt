package com.example.vinylcdvault.ui.buyer

import android.Manifest
import android.content.Intent
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
import com.example.vinylcdvault.data.WompiClient
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
 * Pantalla de pago: captura la ubicación, exige la huella, abre el checkout de Wompi y,
 * solo si el pago es aprobado, crea el pedido con los discos del carrito.
 */
class CheckoutActivity : AppCompatActivity() {

    private val viewModel: CartViewModel by viewModels { CartViewModelFactory() }

    private var biometricVerified = false
    private var cartItems: List<CartItem> = emptyList()
    private var latitude = 0.0
    private var longitude = 0.0

    // Datos del intento de pago en curso, para crear el pedido cuando Wompi apruebe
    private var pendingAddress = ""
    private var pendingReference = ""

    private lateinit var btnPay: MaterialButton
    private lateinit var btnLocation: MaterialButton
    private lateinit var tvLocation: TextView
    private lateinit var etAddress: TextInputEditText

    // Permisos de ubicación: con que se conceda uno (exacta o aproximada) se puede continuar
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) captureLocation()
        else toast(R.string.location_permission_denied)
    }

    // Resultado de la pantalla de pago de Wompi
    private val paymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val status = result.data?.getStringExtra(PaymentActivity.EXTRA_STATUS)
        val transactionId = result.data?.getStringExtra(PaymentActivity.EXTRA_TRANSACTION_ID).orEmpty()
        when {
            result.resultCode != RESULT_OK -> toast(R.string.payment_cancelled)
            status == WompiClient.APPROVED ->
                // Pago aprobado: ahora sí se crea el pedido y se vacía el carrito
                viewModel.placeOrder(cartItems, pendingAddress, latitude, longitude, pendingReference, transactionId)
            status == WompiClient.PENDING -> toast(R.string.payment_pending)
            else -> toast(R.string.payment_declined)
        }
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

        // La huella es obligatoria antes de pagar
        findViewById<View>(R.id.btnPayBiometric).setOnClickListener {
            BiometricHelper.authenticate(this) {
                biometricVerified = true
                toast(R.string.biometric_verified)
            }
        }

        btnPay.setOnClickListener {
            val address = etAddress.text.toString().trim()
            when {
                cartItems.isEmpty() -> toast(R.string.cart_empty)
                address.isEmpty() -> etAddress.error = getString(R.string.field_required)
                !biometricVerified -> toast(R.string.biometric_required)
                else -> {
                    pendingAddress = address
                    pendingReference = "VV-${System.currentTimeMillis()}"   // referencia única por intento
                    paymentLauncher.launch(
                        Intent(this, PaymentActivity::class.java)
                            .putExtra(PaymentActivity.EXTRA_REFERENCE, pendingReference)
                            .putExtra(PaymentActivity.EXTRA_AMOUNT_CENTS, Pricing.total(cartItems) * 100)
                    )
                }
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
                // Resultado de la creación del pedido
                launch {
                    viewModel.opState.collect { state ->
                        when (state) {
                            Resource.Loading -> btnPay.isEnabled = false
                            is Resource.Success -> {
                                viewModel.resetOp()
                                toast(state.data)
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

    // Si ya hay permiso, captura la ubicación; si no, lo solicita
    private fun requestLocation() {
        val granted = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
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
                    toast(R.string.location_unavailable)
                } else {
                    latitude = location.latitude
                    longitude = location.longitude
                    tvLocation.text = getString(R.string.checkout_location_ok, latitude, longitude)
                    LocationHelper.addressFor(this@CheckoutActivity, latitude, longitude)
                        ?.let { etAddress.setText(it) }
                }
            } catch (e: Exception) {
                tvLocation.setText(R.string.checkout_location_none)
                toast(R.string.location_unavailable)
            } finally {
                btnLocation.isEnabled = true
            }
        }
    }

    private fun toast(messageRes: Int) = Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show()
}