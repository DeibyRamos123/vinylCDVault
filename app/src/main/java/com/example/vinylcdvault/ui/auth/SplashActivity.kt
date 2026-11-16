package com.example.vinylcdvault.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.vinylcdvault.R
import com.example.vinylcdvault.di.AppContainer

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Prueba temporal: escribe un documento para confirmar la conexión con Firestore
        AppContainer.firestore.collection("test").document("ping")
            .set(mapOf("ok" to true, "time" to System.currentTimeMillis()))
            .addOnSuccessListener { Log.d("Firebase", "Firestore conectado") }
            .addOnFailureListener { Log.e("Firebase", "Error: ${it.message}") }

        // Espera 2 segundos y pasa al login (como un setTimeout en JS)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java)) //redirige al login
            finish() // cierra el splash para que "atrás" no vuelva a él
        }, 2000)
    }
}