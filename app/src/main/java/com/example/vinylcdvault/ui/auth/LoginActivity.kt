package com.example.vinylcdvault.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.vinylcdvault.ui.admin.AdminActivity
import com.example.vinylcdvault.ui.common.BiometricHelper
import com.example.vinylcdvault.ui.buyer.BuyerActivity
import com.example.vinylcdvault.R
import com.example.vinylcdvault.ui.seller.SellerActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<View>(R.id.tvForgotPassword).setOnClickListener {
            startActivity(Intent(this, RecoverPasswordActivity::class.java))
        }

        findViewById<View>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<View>(R.id.btnLogin).setOnClickListener { goToRoleHome() }

        // Biometría: si la huella es válida, entra igual que el login normal
        findViewById<View>(R.id.btnBiometric).setOnClickListener {
            BiometricHelper.authenticate(this) { goToRoleHome() }
        }
    }

    // Simulación: el rol se decide por el correo (sin backend)
    private fun goToRoleHome() {
        val email = findViewById<TextInputEditText>(R.id.etEmail).text.toString().lowercase()
        val destination = when {
            email.contains("admin") -> AdminActivity::class.java
            email.contains("vendedor") -> SellerActivity::class.java
            else -> BuyerActivity::class.java
        }
        startActivity(Intent(this, destination))
        finish()
    }
}