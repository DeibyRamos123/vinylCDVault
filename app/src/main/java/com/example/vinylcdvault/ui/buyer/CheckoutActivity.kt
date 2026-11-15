package com.example.vinylcdvault.ui.buyer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vinylcdvault.ui.common.BiometricHelper
import com.example.vinylcdvault.R

class CheckoutActivity : AppCompatActivity() {

    private var biometricVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.btnPayBiometric).setOnClickListener {
            BiometricHelper.authenticate(this) {
                biometricVerified = true
                Toast.makeText(this, R.string.biometric_verified, Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.btnPay).setOnClickListener {
            if (biometricVerified) {
                Toast.makeText(this, R.string.checkout_success, Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, R.string.biometric_required, Toast.LENGTH_LONG).show()
            }
        }
    }
}