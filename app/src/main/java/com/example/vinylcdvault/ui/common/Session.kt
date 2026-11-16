package com.example.vinylcdvault.ui.common

import android.content.Intent
import androidx.fragment.app.Fragment
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.ui.auth.LoginActivity

/** Cierra la sesión de Firebase y vuelve al Login borrando el historial de pantallas. */
fun Fragment.logoutToLogin() {
    AppContainer.authRepository.logout()
    startActivity(
        Intent(requireContext(), LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
}