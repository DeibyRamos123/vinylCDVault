package com.example.vinylcdvault.ui.buyer

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.CartItem
import com.example.vinylcdvault.ui.common.BiometricHelper
import com.example.vinylcdvault.util.Pricing
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.toCop
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/** Pantalla de pago: pide la huella, valida la dirección y crea el pedido con los discos del carrito. */
class CheckoutActivity : AppCompatActivity() {

    private val viewModel: CartViewModel by viewModels { CartViewModelFactory() }

    private var biometricVerified = false
    private var cartItems: List<CartItem> = emptyList()
    private lateinit var btnPay: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val etAddress = findViewById<TextInputEditText>(R.id.etAddress)
        val tvTotal = findViewById<TextView>(R.id.tvCheckoutTotal)
        btnPay = findViewById(R.id.btnPay)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

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
                else -> viewModel.placeOrder(cartItems, address)
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
}