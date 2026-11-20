package com.example.vinylcdvault.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.model.Roles
import com.example.vinylcdvault.model.User
import com.example.vinylcdvault.ui.common.showAvatar
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.toCop
import com.example.vinylcdvault.util.toDateText
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * Detalle de un disco para el administrador: muestra el contenido tal como lo ve el comprador
 * (sin favorito ni carrito), quién lo publicó y permite suspender al vendedor o eliminar el disco.
 */
class AdminProductDetailActivity : AppCompatActivity() {

    private val viewModel: ModerationViewModel by viewModels { ModerationViewModelFactory() }

    private var seller: User? = null
    private var deleting = false   // true mientras se elimina el disco: al terminar, se cierra la pantalla

    private lateinit var btnToggle: MaterialButton
    private lateinit var tvNote: TextView

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_product_detail)

        val p = intent.getSerializableExtra("product") as Product
        btnToggle = findViewById(R.id.btnToggleSeller)
        tvNote = findViewById(R.id.tvSellerNote)
        val loading = findViewById<View>(R.id.loadingOverlay)

        // Datos del disco (solo lectura)
        findViewById<TextView>(R.id.tvDetailTitle).text = p.title
        findViewById<TextView>(R.id.tvDetailArtist).text = p.artist
        findViewById<TextView>(R.id.tvDetailFormat).text = p.format
        findViewById<TextView>(R.id.tvDetailPrice).text = p.price.toCop()
        findViewById<TextView>(R.id.tvDetailCondition).text = p.condition
        findViewById<TextView>(R.id.tvDetailDescription).text = p.description
        findViewById<ImageView>(R.id.ivCover).load(p.imageUrl.ifBlank { null }) {
            crossfade(true)
            placeholder(R.drawable.bg_cover)
            error(R.drawable.bg_cover)
        }
        findViewById<TextView>(R.id.tvPublishedOn).apply {
            if (p.createdAt > 0) text = getString(R.string.moderation_published_on, p.createdAt.toDateText())
            else visibility = View.GONE
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        btnToggle.setOnClickListener { confirmToggle() }
        findViewById<View>(R.id.btnDeleteProduct).setOnClickListener { confirmDelete(p) }

        val hasSeller = p.sellerId.isNotBlank()
        if (hasSeller) {
            viewModel.loadSeller(p.sellerId)
        } else {
            // Discos publicados antes de registrar la autoría: no hay a quién moderar
            loading.visibility = View.GONE
            findViewById<TextView>(R.id.tvSellerName).setText(R.string.value_placeholder)
            findViewById<ImageView>(R.id.ivSellerAvatar).showAvatar("")
            btnToggle.visibility = View.GONE
            tvNote.setText(R.string.moderation_no_seller)
            tvNote.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Datos de quien publicó el disco; el spinner se muestra mientras llegan
                launch {
                    viewModel.seller.collect { state ->
                        if (!hasSeller) return@collect
                        loading.visibility = if (state is Resource.Loading) View.VISIBLE else View.GONE
                        when (state) {
                            is Resource.Success -> renderSeller(state.data)
                            is Resource.Error -> renderMissingSeller()
                            Resource.Loading -> Unit
                        }
                    }
                }
                // Resultado de suspender, reactivar o eliminar
                launch {
                    viewModel.opState.collect { state ->
                        when (state) {
                            Resource.Loading -> loading.visibility = View.VISIBLE
                            is Resource.Success -> {
                                loading.visibility = View.GONE
                                toast(getString(state.data))
                                viewModel.resetOp()
                                if (deleting) finish()
                            }
                            is Resource.Error -> {
                                loading.visibility = View.GONE
                                deleting = false
                                toast(state.message)
                                viewModel.resetOp()
                            }
                            null -> Unit
                        }
                    }
                }
            }
        }
    }

    // Muestra los datos del vendedor y el botón que corresponde a su estado actual
    private fun renderSeller(user: User) {
        seller = user
        findViewById<TextView>(R.id.tvSellerName).text = user.name
        findViewById<TextView>(R.id.tvSellerEmail).text = user.email
        findViewById<ImageView>(R.id.ivSellerAvatar).showAvatar(user.photoUrl)
        findViewById<TextView>(R.id.tvSellerStatus).apply {
            setText(if (user.active) R.string.user_status_active else R.string.user_status_suspended)
            setTextColor(ContextCompat.getColor(context, if (user.active) R.color.success else R.color.accent))
        }

        // A los administradores no se les puede suspender desde aquí
        val protected = user.role == Roles.ADMIN
        btnToggle.visibility = if (protected) View.GONE else View.VISIBLE
        tvNote.setText(R.string.moderation_admin_note)
        tvNote.visibility = if (protected) View.VISIBLE else View.GONE
        btnToggle.setText(if (user.active) R.string.moderation_suspend_seller else R.string.moderation_reactivate_seller)
    }

    // La cuenta del vendedor ya no existe: se informa y no se ofrece suspenderlo
    private fun renderMissingSeller() {
        findViewById<TextView>(R.id.tvSellerName).setText(R.string.moderation_seller_missing)
        findViewById<TextView>(R.id.tvSellerEmail).text = ""
        findViewById<ImageView>(R.id.ivSellerAvatar).showAvatar("")
        btnToggle.visibility = View.GONE
    }

    // Pide confirmación antes de suspender o reactivar al vendedor
    private fun confirmToggle() {
        val user = seller ?: return
        val suspending = user.active
        MaterialAlertDialogBuilder(this)
            .setTitle(if (suspending) R.string.moderation_suspend_seller else R.string.moderation_reactivate_seller)
            .setMessage(
                getString(
                    if (suspending) R.string.moderation_suspend_msg else R.string.moderation_reactivate_msg,
                    user.name
                )
            )
            .setPositiveButton(
                if (suspending) R.string.moderation_suspend_confirm else R.string.moderation_reactivate_confirm
            ) { _, _ -> viewModel.setSellerActive(user, active = !suspending) }
            .setNegativeButton(R.string.seller_delete_cancel, null)
            .show()
    }

    // Pide confirmación antes de eliminar el disco
    private fun confirmDelete(product: Product) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.moderation_delete_product)
            .setMessage(R.string.moderation_delete_msg)
            .setPositiveButton(R.string.seller_delete_confirm) { _, _ ->
                deleting = true
                viewModel.deleteProduct(product.id)
            }
            .setNegativeButton(R.string.seller_delete_cancel, null)
            .show()
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}