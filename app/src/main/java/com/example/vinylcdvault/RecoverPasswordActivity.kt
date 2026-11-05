package com.example.vinylcdvault

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RecoverPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)

        val til = findViewById<TextInputLayout>(R.id.tilRecoverEmail)
        val et = findViewById<TextInputEditText>(R.id.etRecoverEmail)

        findViewById<MaterialButton>(R.id.btnSendLink).setOnClickListener {
            val email = et.text.toString().trim()
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                til.error = null
                Toast.makeText(this, R.string.recover_sent, Toast.LENGTH_LONG).show()
                finish()
            } else {
                til.error = getString(R.string.error_email_invalid)
            }
        }

        findViewById<android.view.View>(R.id.tvBackToLogin).setOnClickListener { finish() }
    }
}