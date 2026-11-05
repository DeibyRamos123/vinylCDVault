package com.example.vinylcdvault

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnPayBiometric).setOnClickListener {
            Toast.makeText(this, R.string.checkout_biometric_toast, Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btnPay).setOnClickListener {
            Toast.makeText(this, R.string.checkout_success, Toast.LENGTH_LONG).show()
            finish()
        }
    }
}