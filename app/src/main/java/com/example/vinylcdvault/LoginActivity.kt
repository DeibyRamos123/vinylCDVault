package com.example.vinylcdvault

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // intent: redirige a la pantalla deseada
        findViewById<View>(R.id.tvForgotPassword).setOnClickListener {
            startActivity(Intent(this, RecoverPasswordActivity::class.java))
        }

        // Temporal: luego aquí irá la navegación por rol
        findViewById<View>(R.id.btnLogin).setOnClickListener {
            // Toast: mensaje de aviso tipo alert en web
            val email = findViewById<TextInputEditText>(R.id.etEmail).text.toString().lowercase()
            val destination = when {
                email.contains("vendedor") -> SellerActivity::class.java
                email.contains("admin") -> AdminActivity::class.java
                else -> BuyerActivity::class.java
            }
            startActivity(Intent(this, destination))
        }

        // FindViewById Como un document.getElementById() de javascript y el setOnClickListener como el .addEventListener()
        findViewById<View>(R.id.btnBiometric).setOnClickListener {
            Toast.makeText(this, "Biometría (pendiente)", Toast.LENGTH_SHORT).show()
        }
    }
}