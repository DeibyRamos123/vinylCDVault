package com.example.vinylcdvault

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class UserFormActivity : AppCompatActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_form)

        val etName = findViewById<TextInputEditText>(R.id.etUserName)
        val etEmail = findViewById<TextInputEditText>(R.id.etUserEmail)
        val tilName = findViewById<TextInputLayout>(R.id.tilUserName)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilUserEmail)
        val cgRole = findViewById<ChipGroup>(R.id.cgRole)
        val swActive = findViewById<MaterialSwitch>(R.id.swActive)

        // Modo editar: si llega un usuario, se rellenan los campos
        val user = intent.getSerializableExtra("user") as? User
        if (user != null) {
            findViewById<TextView>(R.id.tvUserFormTitle).setText(R.string.user_form_edit)
            etName.setText(user.name)
            etEmail.setText(user.email)
            swActive.isChecked = user.active
            cgRole.check(
                when (user.role) {
                    "Vendedor" -> R.id.chipSeller
                    "Administrador" -> R.id.chipAdmin
                    else -> R.id.chipBuyer
                }
            )
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.btnSaveUser).setOnClickListener {
            val required = getString(R.string.field_required)
            tilName.error = if (etName.text.isNullOrBlank()) required else null
            tilEmail.error = if (etEmail.text.isNullOrBlank()) required else null

            if (tilName.error == null && tilEmail.error == null) {
                Toast.makeText(this, R.string.user_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}