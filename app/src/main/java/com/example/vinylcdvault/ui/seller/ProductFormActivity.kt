package com.example.vinylcdvault.ui.seller

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.vinylcdvault.R
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.ui.common.ProductViewModel
import com.example.vinylcdvault.ui.common.ProductViewModelFactory
import com.example.vinylcdvault.util.ImageHelper
import com.example.vinylcdvault.util.Resource
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Formulario para publicar un disco nuevo o editar uno existente (si llega "product" por el Intent). */
class ProductFormActivity : AppCompatActivity() {

    private val viewModel: ProductViewModel by viewModels { ProductViewModelFactory() }

    private lateinit var btnSave: MaterialButton
    private lateinit var ivPhoto: ImageView
    private lateinit var tvPhotoHint: TextView

    // Foto elegida (se subirá al almacenamiento en el siguiente paso) y destino temporal de la cámara
    private var selectedImage: Uri? = null
    private var cameraUri: Uri? = null

    // Chips de formato y de estado, enlazados a los textos que se guardan en Firestore
    private val formatChips = mapOf(
        R.id.chipVinyl to R.string.format_vinyl,
        R.id.chipCd to R.string.format_cd,
        R.id.chipCassette to R.string.format_cassette
    )
    private val conditionChips = mapOf(
        R.id.chipSealed to R.string.condition_sealed,
        R.id.chipNm to R.string.condition_nm,
        R.id.chipVgPlus to R.string.condition_vgplus,
        R.id.chipVg to R.string.condition_vg
    )

    // Selector de galería del sistema (no necesita permisos)
    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) showPhoto(uri)
    }

    // Cámara del sistema: guarda la foto en el archivo temporal que le indicamos
    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) cameraUri?.let { showPhoto(it) }
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_form)

        val etTitle = findViewById<TextInputEditText>(R.id.etTitle)
        val etArtist = findViewById<TextInputEditText>(R.id.etArtist)
        val etPrice = findViewById<TextInputEditText>(R.id.etPrice)
        val etDescription = findViewById<TextInputEditText>(R.id.etDescription)
        val tilTitle = findViewById<TextInputLayout>(R.id.tilTitle)
        val tilArtist = findViewById<TextInputLayout>(R.id.tilArtist)
        val tilPrice = findViewById<TextInputLayout>(R.id.tilPrice)
        val cgFormat = findViewById<ChipGroup>(R.id.cgFormat)
        val cgCondition = findViewById<ChipGroup>(R.id.cgCondition)
        ivPhoto = findViewById(R.id.ivPhoto)
        tvPhotoHint = findViewById(R.id.tvPhotoHint)
        btnSave = findViewById(R.id.btnSave)

        // Modo editar: se rellenan los campos con el disco recibido
        val product = intent.getSerializableExtra("product") as? Product
        if (product != null) {
            findViewById<TextView>(R.id.tvFormTitle).setText(R.string.form_edit_title)
            etTitle.setText(product.title)
            etArtist.setText(product.artist)
            etPrice.setText(product.price.toString())
            etDescription.setText(product.description)
            formatChips.entries.firstOrNull { product.format.startsWith(getString(it.value)) }
                ?.let { cgFormat.check(it.key) }
            conditionChips.entries.firstOrNull { getString(it.value) == product.condition }
                ?.let { cgCondition.check(it.key) }
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.btnGallery).setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        findViewById<View>(R.id.btnCamera).setOnClickListener { launchCamera() }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val artist = etArtist.text.toString().trim()
            val price = etPrice.text.toString().toLongOrNull()

            // Validaciones de los campos obligatorios
            tilTitle.error = if (title.isEmpty()) getString(R.string.field_required) else null
            tilArtist.error = if (artist.isEmpty()) getString(R.string.field_required) else null
            tilPrice.error = if (price == null || price <= 0) getString(R.string.error_price_invalid) else null
            if (tilTitle.error != null || tilArtist.error != null || tilPrice.error != null) return@setOnClickListener

            val format = getString(formatChips[cgFormat.checkedChipId] ?: R.string.format_vinyl)
            val condition = getString(conditionChips[cgCondition.checkedChipId] ?: R.string.condition_sealed)

            // Al crear, el disco queda asociado al vendedor con sesión abierta
            val base = product ?: Product(sellerId = AppContainer.auth.currentUser?.uid.orEmpty())
            val updated = base.copy(
                title = title, artist = artist, format = format, price = price!!,
                condition = condition, description = etDescription.text.toString().trim()
            )

            lifecycleScope.launch {
                btnSave.isEnabled = false
                // Comprime la foto en un hilo secundario para no congelar la pantalla
                val bytes = try {
                    selectedImage?.let {
                        withContext(Dispatchers.IO) { ImageHelper.compress(this@ProductFormActivity, it) }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ProductFormActivity, R.string.error_image, Toast.LENGTH_LONG).show()
                    btnSave.isEnabled = true
                    return@launch
                }
                viewModel.save(updated, bytes)   // si bytes no es null, el repositorio sube la foto
            }
        }

        observeResult()
    }

    // Crea un archivo temporal, lo expone con FileProvider y abre la cámara del sistema
    private fun launchCamera() {
        val dir = File(cacheDir, "images").apply { mkdirs() }
        val file = File.createTempFile("disco_", ".jpg", dir)
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        cameraUri = uri
        takePhoto.launch(uri)
    }

    // Muestra la vista previa de la foto elegida
    private fun showPhoto(uri: Uri) {
        selectedImage = uri
        ivPhoto.setImageURI(uri)
        ivPhoto.visibility = View.VISIBLE
        tvPhotoHint.visibility = View.GONE
    }

    // Cierra el formulario si se guardó bien; si no, muestra el error
    private fun observeResult() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.opState.collect { state ->
                    when (state) {
                        Resource.Loading -> btnSave.isEnabled = false
                        is Resource.Success -> {
                            viewModel.resetOp()
                            Toast.makeText(this@ProductFormActivity, state.data, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        is Resource.Error -> {
                            btnSave.isEnabled = true
                            Toast.makeText(this@ProductFormActivity, state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetOp()
                        }
                        null -> btnSave.isEnabled = true
                    }
                }
            }
        }
    }
}