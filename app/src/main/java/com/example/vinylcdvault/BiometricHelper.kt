package com.example.vinylcdvault

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

object BiometricHelper {

    fun authenticate(activity: AppCompatActivity, onSuccess: () -> Unit) {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG

        // 1. ¿El dispositivo puede usar biometría?
        when (BiometricManager.from(activity).canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Unit
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(activity, R.string.biometric_none_enrolled, Toast.LENGTH_LONG).show()
                return
            }
            else -> {
                Toast.makeText(activity, R.string.biometric_unavailable, Toast.LENGTH_LONG).show()
                return
            }
        }

        // 2. Qué hacer según el resultado
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                ) {
                    Toast.makeText(activity, R.string.biometric_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 3. El diálogo del sistema
        val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.biometric_title))
            .setSubtitle(activity.getString(R.string.biometric_subtitle))
            .setNegativeButtonText(activity.getString(R.string.biometric_cancel))
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(info)
    }
}