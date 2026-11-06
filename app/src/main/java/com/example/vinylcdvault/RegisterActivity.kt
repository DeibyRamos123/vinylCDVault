package com.example.vinylcdvault

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<TextInputEditText>(R.id.etRegName)
        val etEmail = findViewById<TextInputEditText>(R.id.etRegEmail)
        val etPass = findViewById<TextInputEditText>(R.id.etRegPassword)
        val etConfirm = findViewById<TextInputEditText>(R.id.etRegConfirm)
        val tilName = findViewById<TextInputLayout>(R.id.tilRegName)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilRegEmail)
        val tilPass = findViewById<TextInputLayout>(R.id.tilRegPassword)
        val tilConfirm = findViewById<TextInputLayout>(R.id.tilRegConfirm)
        val cbPrivacy = findViewById<CheckBox>(R.id.cbPrivacy)

        findViewById<MaterialButton>(R.id.btnRegister).setOnClickListener {
            val pass = etPass.text.toString()

            tilName.error = if (etName.text.isNullOrBlank()) getString(R.string.field_required) else null
            tilEmail.error = if (Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString().trim()).matches())
                null else getString(R.string.error_email_invalid)
            tilPass.error = if (pass.length < 6) getString(R.string.error_password_short) else null
            tilConfirm.error = if (pass != etConfirm.text.toString()) getString(R.string.error_password_mismatch) else null

            if (!cbPrivacy.isChecked) {
                Toast.makeText(this, R.string.error_privacy, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (tilName.error == null && tilEmail.error == null &&
                tilPass.error == null && tilConfirm.error == null
            ) {
                Toast.makeText(this, R.string.register_success, Toast.LENGTH_LONG).show()
                finish()
            }
        }

        findViewById<View>(R.id.tvHaveAccount).setOnClickListener { finish() }
    }
}