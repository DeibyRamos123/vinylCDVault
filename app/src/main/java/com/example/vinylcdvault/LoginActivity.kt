package com.example.vinylcdvault

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<View>(R.id.tvForgotPassword).setOnClickListener {
            startActivity(Intent(this, RecoverPasswordActivity::class.java))
        }

        // Temporal: luego aquí irá la navegación por rol
        findViewById<View>(R.id.btnLogin).setOnClickListener {
            Toast.makeText(this, "Login (pendiente: navegación por rol)", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btnBiometric).setOnClickListener {
            Toast.makeText(this, "Biometría (pendiente)", Toast.LENGTH_SHORT).show()
        }
    }
}