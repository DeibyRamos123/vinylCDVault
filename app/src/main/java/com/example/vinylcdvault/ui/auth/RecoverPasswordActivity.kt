package com.example.vinylcdvault.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.vinylcdvault.R
import com.example.vinylcdvault.util.Resource
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/** Pantalla de recuperación: envía el correo de Firebase para crear una nueva contraseña. */
class RecoverPasswordActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory() }
    private lateinit var btnSend: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)

        val til = findViewById<TextInputLayout>(R.id.tilRecoverEmail)
        val et = findViewById<TextInputEditText>(R.id.etRecoverEmail)
        btnSend = findViewById(R.id.btnSendLink)

        btnSend.setOnClickListener {
            val email = et.text.toString().trim()
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                til.error = null
                viewModel.sendPasswordReset(email)
            } else {
                til.error = getString(R.string.error_email_invalid)
            }
        }

        findViewById<View>(R.id.tvBackToLogin).setOnClickListener { finish() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recoverState.collect { state ->
                    when (state) {
                        Resource.Loading -> btnSend.isEnabled = false
                        is Resource.Success -> {
                            viewModel.resetStates()
                            // Mensaje genérico: Firebase no revela si el correo existe o no
                            Toast.makeText(this@RecoverPasswordActivity, R.string.recover_sent, Toast.LENGTH_LONG).show()
                            finish()
                        }
                        is Resource.Error -> {
                            btnSend.isEnabled = true
                            Toast.makeText(this@RecoverPasswordActivity, state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetStates()
                        }
                        null -> btnSend.isEnabled = true
                    }
                }
            }
        }
    }
}