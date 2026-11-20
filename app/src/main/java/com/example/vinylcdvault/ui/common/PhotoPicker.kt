package com.example.vinylcdvault.ui.common

import android.content.Context
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.vinylcdvault.R
import com.example.vinylcdvault.util.ImageHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Pregunta si la foto viene de la galería o de la cámara y entrega su Uri en onPicked.
 * Debe crearse como propiedad del Fragment, porque registra los lanzadores de resultados.
 */
class PhotoPicker(private val fragment: Fragment, private val onPicked: (Uri) -> Unit) {

    private var cameraUri: Uri? = null

    private val gallery = fragment.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onPicked(uri)
    }
    private val camera = fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) cameraUri?.let(onPicked)
    }

    /** Muestra el diálogo de elección. */
    fun show() {
        val context = fragment.requireContext()
        val options = arrayOf(context.getString(R.string.photo_gallery), context.getString(R.string.photo_camera))
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.photo_change)
            .setItems(options) { _, which ->
                if (which == 0) gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                else launchCamera(context)
            }
            .show()
    }

    // Crea un archivo temporal, lo expone con FileProvider y abre la cámara del sistema
    private fun launchCamera(context: Context) {
        val dir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File.createTempFile("perfil_", ".jpg", dir)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraUri = uri
        camera.launch(uri)
    }
}

/** Lee la foto, la endereza y la comprime en un hilo secundario para no congelar la pantalla. */
suspend fun Context.readCompressed(uri: Uri): ByteArray =
    withContext(Dispatchers.IO) { ImageHelper.compress(this@readCompressed, uri) }