package com.example.vinylcdvault.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Roles
import com.example.vinylcdvault.model.User
import com.example.vinylcdvault.ui.admin.AdminActivity
import com.example.vinylcdvault.ui.buyer.BuyerActivity
import com.example.vinylcdvault.ui.seller.SellerActivity
import com.example.vinylcdvault.util.Resource
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/** Pantalla de inicio de sesión: correo y contraseña, o Google. Si ya hay sesión guardada, entra directo. */
class LoginActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory() }

    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoogle: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollView)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)

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

        // Login con Google
        btnGoogle.setOnClickListener { signInWithGoogle() }

        observeLogin()

        // Si ya hay sesión guardada, entra directo sin pedir credenciales
        if (savedInstanceState == null && viewModel.hasSession()) {
            viewModel.restoreSession()
        }
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

    // Abre el selector de cuentas de Google y manda el idToken al ViewModel
    private fun signInWithGoogle() {
        val option = GetSignInWithGoogleOption.Builder(getString(R.string.default_web_client_id)).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        lifecycleScope.launch {
            try {
                val result = CredentialManager.create(this@LoginActivity)
                    .getCredential(this@LoginActivity, request)
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    viewModel.loginWithGoogle(idToken)
                }
            } catch (e: GetCredentialCancellationException) {
                // El usuario cerró el selector: no hace falta mostrar error
            } catch (e: GetCredentialException) {
                Toast.makeText(this@LoginActivity, R.string.login_google_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Evita dobles toques mientras se espera la respuesta del servidor
    private fun setLoading(loading: Boolean) {
        btnLogin.isEnabled = !loading
        btnGoogle.isEnabled = !loading
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