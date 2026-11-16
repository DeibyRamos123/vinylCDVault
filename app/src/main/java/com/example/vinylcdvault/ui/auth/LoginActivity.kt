package com.example.vinylcdvault.ui.auth

import android.content.Intent
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
import com.example.vinylcdvault.model.Roles
import com.example.vinylcdvault.model.User
import com.example.vinylcdvault.ui.admin.AdminActivity
import com.example.vinylcdvault.ui.buyer.BuyerActivity
import com.example.vinylcdvault.ui.common.BiometricHelper
import com.example.vinylcdvault.ui.seller.SellerActivity
import com.example.vinylcdvault.util.Resource
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/** Pantalla de inicio de sesión: correo y contraseña, o huella si ya hay sesión guardada. */
class LoginActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory() }

    private lateinit var btnLogin: MaterialButton
    private lateinit var btnBiometric: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnBiometric = findViewById(R.id.btnBiometric)

        findViewById<View>(R.id.tvForgotPassword).setOnClickListener {
            startActivity(Intent(this, RecoverPasswordActivity::class.java))
        }
        findViewById<View>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Login con correo y contraseña: primero valida los campos, luego llama al ViewModel
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            tilEmail.error = if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) null
            else getString(R.string.error_email_invalid)
            tilPassword.error = if (password.isEmpty()) getString(R.string.field_required) else null

            if (tilEmail.error == null && tilPassword.error == null) viewModel.login(email, password)
        }

        // Login con huella: solo funciona si ya hay una sesión de Firebase guardada
        btnBiometric.setOnClickListener {
            if (viewModel.hasSession()) {
                BiometricHelper.authenticate(this) { viewModel.restoreSession() }
            } else {
                Toast.makeText(this, R.string.login_need_session, Toast.LENGTH_LONG).show()
            }
        }

        observeLogin()
    }

    // Reacciona a los estados del ViewModel mientras la pantalla está visible
    private fun observeLogin() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        Resource.Loading -> setLoading(true)
                        is Resource.Success -> {
                            viewModel.resetStates()
                            goToRoleHome(state.data)
                        }
                        is Resource.Error -> {
                            setLoading(false)
                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetStates()
                        }
                        null -> setLoading(false)
                    }
                }
            }
        }
    }

    // Evita dobles toques mientras se espera la respuesta del servidor
    private fun setLoading(loading: Boolean) {
        btnLogin.isEnabled = !loading
        btnBiometric.isEnabled = !loading
    }

    // El rol que viene de Firestore decide a qué panel entra el usuario
    private fun goToRoleHome(user: User) {
        val destination = when (user.role) {
            Roles.ADMIN -> AdminActivity::class.java
            Roles.SELLER -> SellerActivity::class.java
            else -> BuyerActivity::class.java
        }
        startActivity(Intent(this, destination))
        finish()
    }
}