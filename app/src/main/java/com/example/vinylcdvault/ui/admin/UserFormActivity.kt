package com.example.vinylcdvault.ui.admin

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.vinylcdvault.R
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.Roles
import com.example.vinylcdvault.model.User
import com.example.vinylcdvault.util.Resource
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/** Formulario para crear un usuario nuevo o editar uno existente (si llega "user" por el Intent). */
class UserFormActivity : AppCompatActivity() {

    private val viewModel: UserViewModel by viewModels { UserViewModelFactory() }
    private lateinit var btnSave: MaterialButton

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_form)

        val etName = findViewById<TextInputEditText>(R.id.etUserName)
        val etEmail = findViewById<TextInputEditText>(R.id.etUserEmail)
        val etPass = findViewById<TextInputEditText>(R.id.etUserPassword)
        val tilName = findViewById<TextInputLayout>(R.id.tilUserName)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilUserEmail)
        val tilPass = findViewById<TextInputLayout>(R.id.tilUserPassword)
        val cgRole = findViewById<ChipGroup>(R.id.cgRole)
        val swActive = findViewById<MaterialSwitch>(R.id.swActive)
        btnSave = findViewById(R.id.btnSaveUser)

        // Modo editar: se rellenan los campos; el correo y la contraseña no se pueden cambiar
        val user = intent.getSerializableExtra("user") as? User
        if (user != null) {
            findViewById<TextView>(R.id.tvUserFormTitle).setText(R.string.user_form_edit)
            etName.setText(user.name)
            etEmail.setText(user.email)
            etEmail.isEnabled = false
            tilPass.visibility = View.GONE
            swActive.isChecked = user.active
            cgRole.check(
                when (user.role) {
                    Roles.SELLER -> R.id.chipSeller
                    Roles.ADMIN -> R.id.chipAdmin
                    else -> R.id.chipBuyer
                }
            )
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString()
            val role = when (cgRole.checkedChipId) {
                R.id.chipSeller -> Roles.SELLER
                R.id.chipAdmin -> Roles.ADMIN
                else -> Roles.BUYER
            }

            // Validaciones: en modo crear también se exige correo válido y contraseña
            tilName.error = if (name.isEmpty()) getString(R.string.field_required) else null
            tilEmail.error = if (user != null || Patterns.EMAIL_ADDRESS.matcher(email).matches()) null
            else getString(R.string.error_email_invalid)
            tilPass.error = if (user != null || pass.length >= 6) null
            else getString(R.string.error_password_short)
            if (tilName.error != null || tilEmail.error != null || tilPass.error != null) return@setOnClickListener

            if (user != null) {
                // El admin no puede quitarse su propio rol ni suspenderse (se quedaría sin acceso)
                val isSelf = user.uid == AppContainer.auth.currentUser?.uid
                if (isSelf && (role != Roles.ADMIN || !swActive.isChecked)) {
                    Toast.makeText(this, R.string.error_own_role, Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                viewModel.updateUser(user.uid, name, role, swActive.isChecked)
            } else {
                viewModel.createUser(name, email, pass, role, swActive.isChecked)
            }
        }

        observeResult()
    }

    // Cierra el formulario si la operación salió bien; si no, muestra el error
    private fun observeResult() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.opState.collect { state ->
                    when (state) {
                        Resource.Loading -> btnSave.isEnabled = false
                        is Resource.Success -> {
                            viewModel.resetOp()
                            Toast.makeText(this@UserFormActivity, state.data, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        is Resource.Error -> {
                            btnSave.isEnabled = true
                            Toast.makeText(this@UserFormActivity, state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetOp()
                        }
                        null -> btnSave.isEnabled = true
                    }
                }
            }
        }
    }
}