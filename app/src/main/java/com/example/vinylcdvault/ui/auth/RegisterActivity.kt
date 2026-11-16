package com.example.vinylcdvault.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Roles
import com.example.vinylcdvault.util.Resource
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/** Pantalla de registro: valida el formulario y crea la cuenta con su rol. */
class RegisterActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory() }
    private lateinit var btnRegister: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<TextInputEditText>(R.id.etRegName)
        val etEmail = findViewById<TextInputEditText>(R.id.etRegEmail)
        val etPhone = findViewById<TextInputEditText>(R.id.etRegPhone)
        val etPass = findViewById<TextInputEditText>(R.id.etRegPassword)
        val etConfirm = findViewById<TextInputEditText>(R.id.etRegConfirm)
        val tilName = findViewById<TextInputLayout>(R.id.tilRegName)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilRegEmail)
        val tilPass = findViewById<TextInputLayout>(R.id.tilRegPassword)
        val tilConfirm = findViewById<TextInputLayout>(R.id.tilRegConfirm)
        val chipSeller = findViewById<Chip>(R.id.chipRegSeller)
        val cbPrivacy = findViewById<CheckBox>(R.id.cbPrivacy)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString()

            // Validaciones del formulario antes de llamar al servidor
            tilName.error = if (etName.text.isNullOrBlank()) getString(R.string.field_required) else null
            tilEmail.error = if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) null
            else getString(R.string.error_email_invalid)
            tilPass.error = if (pass.length < 6) getString(R.string.error_password_short) else null
            tilConfirm.error = if (pass != etConfirm.text.toString())
                getString(R.string.error_password_mismatch) else null

            if (!cbPrivacy.isChecked) {
                Toast.makeText(this, R.string.error_privacy, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (tilName.error == null && tilEmail.error == null &&
                tilPass.error == null && tilConfirm.error == null
            ) {
                // Un registro público solo puede ser comprador o vendedor, nunca administrador
                val role = if (chipSeller.isChecked) Roles.SELLER else Roles.BUYER
                viewModel.register(
                    etName.text.toString().trim(), email,
                    etPhone.text.toString().trim(), pass, role
                )
            }
        }

        findViewById<View>(R.id.tvHaveAccount).setOnClickListener { finish() }
        observeRegister()
    }

    private fun observeRegister() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { state ->
                    when (state) {
                        Resource.Loading -> btnRegister.isEnabled = false
                        is Resource.Success -> {
                            viewModel.resetStates()
                            // Firebase deja la sesión abierta al registrar; se cierra para pedir login
                            com.example.vinylcdvault.di.AppContainer.authRepository.logout()
                            Toast.makeText(this@RegisterActivity, R.string.register_success, Toast.LENGTH_LONG).show()
                            finish()
                        }
                        is Resource.Error -> {
                            btnRegister.isEnabled = true
                            Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetStates()
                        }
                        null -> btnRegister.isEnabled = true
                    }
                }
            }
        }
    }
}