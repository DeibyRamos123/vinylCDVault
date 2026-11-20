package com.example.vinylcdvault.ui.buyer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vinylcdvault.R
import com.example.vinylcdvault.data.WompiClient
import com.example.vinylcdvault.util.WompiConfig
import com.example.vinylcdvault.util.toFriendlyMessage
import kotlinx.coroutines.launch

/**
 * Muestra el checkout alojado de Wompi dentro de un WebView. Cuando Wompi redirige a la URL de
 * retorno, la app la intercepta, verifica la transacción con la API y devuelve el resultado.
 */
class PaymentActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_REFERENCE = "reference"
        const val EXTRA_AMOUNT_CENTS = "amount_cents"
        const val EXTRA_STATUS = "status"
        const val EXTRA_TRANSACTION_ID = "transaction_id"
    }

    private lateinit var progress: View
    private lateinit var tvVerifying: View
    private lateinit var reference: String
    private var amountInCents = 0L
    private var verifying = false   // evita verificar dos veces si Wompi redirige más de una vez

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val webView = findViewById<WebView>(R.id.webView)
        progress = findViewById(R.id.progress)
        tvVerifying = findViewById(R.id.tvVerifying)
        reference = intent.getStringExtra(EXTRA_REFERENCE).orEmpty()
        amountInCents = intent.getLongExtra(EXTRA_AMOUNT_CENTS, 0)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }   // cancelar el pago

        // El checkout de Wompi necesita JavaScript y almacenamiento del navegador
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                // Al terminar, Wompi redirige a REDIRECT_URL con el id de la transacción
                if (request.url.toString().startsWith(WompiConfig.REDIRECT_URL)) {
                    verifyPayment(request.url.getQueryParameter("id"))
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                progress.visibility = View.GONE
            }
        }
        webView.loadUrl(WompiClient.checkoutUrl(reference, amountInCents))
    }

    // Confirma el estado real con la API de Wompi y devuelve el resultado a la pantalla de pago
    private fun verifyPayment(transactionId: String?) {
        if (verifying) return
        if (transactionId.isNullOrBlank()) {
            finish()   // sin id no hay transacción: se toma como cancelado
            return
        }
        verifying = true
        tvVerifying.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val status = WompiClient.verify(transactionId, reference, amountInCents)
                setResult(
                    RESULT_OK,
                    Intent().putExtra(EXTRA_STATUS, status).putExtra(EXTRA_TRANSACTION_ID, transactionId)
                )
            } catch (e: Exception) {
                Toast.makeText(this@PaymentActivity, e.toFriendlyMessage(), Toast.LENGTH_LONG).show()
                setResult(RESULT_CANCELED)
            }
            finish()
        }
    }
}