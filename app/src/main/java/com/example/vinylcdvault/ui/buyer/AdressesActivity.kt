package com.example.vinylcdvault.ui.buyer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Address
import com.example.vinylcdvault.ui.common.AddressAdapter
import com.example.vinylcdvault.util.LocationHelper
import com.example.vinylcdvault.util.Resource
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/** Direcciones guardadas del comprador: listar, agregar, editar, predeterminar y eliminar. */
class AddressesActivity : AppCompatActivity() {

    private val viewModel: AddressViewModel by viewModels { AddressViewModelFactory() }
    private val addressAdapter = AddressAdapter(
        onEdit = { showDialog(it) },
        onMakePrimary = { viewModel.setPrimary(it.id) },
        onDelete = { confirmDelete(it) }
    )

    // Estado del diálogo abierto (coordenadas capturadas y vistas que la ubicación debe actualizar)
    private var dialogLat = 0.0
    private var dialogLon = 0.0
    private var dialogAddressField: TextInputEditText? = null
    private var dialogLocationText: TextView? = null
    private var dialogLocationButton: MaterialButton? = null

    // Permisos de ubicación: con que se conceda uno se puede continuar
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) captureLocation() else toast(R.string.location_permission_denied)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adresses)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnAddAddress).setOnClickListener { showDialog(null) }
        findViewById<RecyclerView>(R.id.rvAddresses).apply {
            layoutManager = LinearLayoutManager(this@AddressesActivity)
            adapter = addressAdapter
        }
        val tvEmpty = findViewById<View>(R.id.tvAddressesEmpty)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.addresses.collect { state ->
                        if (state is Resource.Success) {
                            addressAdapter.submitList(state.data)
                            tvEmpty.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }
                }
                launch {
                    viewModel.opState.collect { state ->
                        when (state) {
                            is Resource.Success -> { toast(state.data); viewModel.resetOp() }
                            is Resource.Error -> {
                                Toast.makeText(this@AddressesActivity, state.message, Toast.LENGTH_LONG).show()
                                viewModel.resetOp()
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    // Diálogo para crear (address = null) o editar una dirección
    private fun showDialog(address: Address?) {
        val view = layoutInflater.inflate(R.layout.dialog_adress, null)
        val tilLabel = view.findViewById<TextInputLayout>(R.id.tilAddressLabel)
        val tilText = view.findViewById<TextInputLayout>(R.id.tilAddressText)
        val etLabel = view.findViewById<TextInputEditText>(R.id.etAddressLabel)
        val etText = view.findViewById<TextInputEditText>(R.id.etAddressText)
        val btnLocation = view.findViewById<MaterialButton>(R.id.btnDialogLocation)
        val tvLocation = view.findViewById<TextView>(R.id.tvDialogLocation)

        // Se cargan los datos actuales al editar, incluidas las coordenadas que ya tenía
        dialogLat = address?.latitude ?: 0.0
        dialogLon = address?.longitude ?: 0.0
        etLabel.setText(address?.label)
        etText.setText(address?.address)
        if (dialogLat != 0.0 || dialogLon != 0.0) {
            tvLocation.text = getString(R.string.checkout_location_ok, dialogLat, dialogLon)
        }
        dialogAddressField = etText
        dialogLocationText = tvLocation
        dialogLocationButton = btnLocation
        btnLocation.setOnClickListener { requestLocation() }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(if (address == null) R.string.address_add else R.string.address_edit)
            .setView(view)
            .setPositiveButton(R.string.address_save, null)   // el click se define abajo para validar sin cerrar
            .setNegativeButton(R.string.seller_delete_cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val label = etLabel.text.toString().trim()
                val text = etText.text.toString().trim()
                tilLabel.error = if (label.isEmpty()) getString(R.string.field_required) else null
                tilText.error = if (text.isEmpty()) getString(R.string.field_required) else null
                if (tilLabel.error != null || tilText.error != null) return@setOnClickListener

                viewModel.save(
                    (address ?: Address()).copy(label = label, address = text, latitude = dialogLat, longitude = dialogLon)
                )
                dialog.dismiss()
            }
        }
        // Al cerrar se sueltan las referencias a las vistas del diálogo
        dialog.setOnDismissListener {
            dialogAddressField = null
            dialogLocationText = null
            dialogLocationButton = null
        }
        dialog.show()
    }

    private fun confirmDelete(address: Address) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.seller_delete_title)
            .setMessage(R.string.address_delete_message)
            .setPositiveButton(R.string.seller_delete_confirm) { _, _ -> viewModel.delete(address.id) }
            .setNegativeButton(R.string.seller_delete_cancel, null)
            .show()
    }

    // Si ya hay permiso, captura la ubicación; si no, lo solicita
    private fun requestLocation() {
        val granted = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
        if (granted) captureLocation()
        else permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // Obtiene las coordenadas y escribe la dirección legible en el campo del diálogo
    private fun captureLocation() {
        val field = dialogAddressField ?: return
        val label = dialogLocationText ?: return
        val button = dialogLocationButton ?: return
        lifecycleScope.launch {
            button.isEnabled = false
            label.setText(R.string.checkout_location_loading)
            try {
                val location = LocationHelper.currentLocation(this@AddressesActivity)
                if (location == null) {
                    label.setText(R.string.checkout_location_none)
                    toast(R.string.location_unavailable)
                } else {
                    dialogLat = location.latitude
                    dialogLon = location.longitude
                    label.text = getString(R.string.checkout_location_ok, dialogLat, dialogLon)
                    LocationHelper.addressFor(this@AddressesActivity, dialogLat, dialogLon)
                        ?.let { field.setText(it) }
                        ?: toast(R.string.address_lookup_failed)
                }
            } catch (e: Exception) {
                label.setText(R.string.checkout_location_none)
                toast(R.string.location_unavailable)
            } finally {
                button.isEnabled = true
            }
        }
    }

    private fun toast(messageRes: Int) = Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show()
}