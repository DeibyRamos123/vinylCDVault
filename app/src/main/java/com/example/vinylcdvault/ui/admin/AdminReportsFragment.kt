package com.example.vinylcdvault.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.PaymentStatus
import com.example.vinylcdvault.util.ReportExporter
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.toCompactCop
import com.example.vinylcdvault.util.toCop
import kotlinx.coroutines.launch
import java.io.File

/** Reporte de ventas de los últimos 6 meses: total, gráfica de barras, más vendidos y exportación. */
class AdminReportsFragment : Fragment(R.layout.fragment_admin_reports) {

    private val viewModel: AdminViewModel by viewModels { AdminViewModelFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.btnExport).setOnClickListener { exportReport() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.report.collect { state ->
                    if (state is Resource.Success) show(view, state.data)
                }
            }
        }
    }

    private fun show(root: View, report: ReportStats) {
        root.findViewById<TextView>(R.id.tvReportTotal).text = report.totalSales.toCop()
        root.findViewById<TextView>(R.id.tvReportOrders).text =
            getString(R.string.reports_orders_count, report.orderCount)
        drawChart(root.findViewById(R.id.llBars), root.findViewById(R.id.llMonthLabels), report.months)

        // Discos más vendidos, o un aviso si todavía no hay ventas
        val top = root.findViewById<LinearLayout>(R.id.llTop)
        top.removeAllViews()
        val rows = report.topProducts.mapIndexed { i, p ->
            getString(R.string.reports_top_line, i + 1, p.title, p.units)
        }.ifEmpty { listOf(getString(R.string.reports_empty)) }
        rows.forEach { text ->
            top.addView(TextView(requireContext(), null, 0, R.style.App_ProfileRow).apply {
                this.text = text
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            })
        }
    }

    // Dibuja una columna por mes: el valor arriba y una barra cuya altura es proporcional a las ventas
    private fun drawChart(bars: LinearLayout, labels: LinearLayout, months: List<MonthSales>) {
        bars.removeAllViews()
        labels.removeAllViews()
        val max = months.maxOf { it.total }.coerceAtLeast(1)
        val gap = resources.getDimensionPixelSize(R.dimen.spacing_small) / 2

        months.forEach { month ->
            val pct = (month.total * 100 / max).toInt()   // 0 a 100

            // Columna vertical con weightSum 100: el espacio vacío y la barra se reparten el alto
            val column = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                weightSum = 100f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                    .apply { marginStart = gap; marginEnd = gap }
            }
            column.addView(TextView(requireContext(), null, 0, R.style.App_Text_Small).apply {
                text = if (month.total > 0) month.total.toCompactCop() else ""
                textSize = 10f
                maxLines = 1
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            })
            column.addView(View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, (100 - pct).toFloat())
            })
            column.addView(View(requireContext()).apply {
                setBackgroundResource(R.drawable.bg_bar)
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, pct.toFloat())
            })
            bars.addView(column)

            labels.addView(TextView(requireContext(), null, 0, R.style.App_Text_Small).apply {
                text = month.label
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
        }
    }

    // Genera un CSV con los pedidos pagados y abre el selector para compartirlo
    private fun exportReport() {
        val paid = viewModel.orders.value.filter { it.paymentStatus == PaymentStatus.PAID }
        if (paid.isEmpty()) {
            Toast.makeText(requireContext(), R.string.reports_nothing_to_export, Toast.LENGTH_SHORT).show()
            return
        }
        val dir = File(requireContext().cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, "reporte_ventas.csv").apply { writeText(ReportExporter.toCsv(paid)) }
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/csv")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(send, getString(R.string.reports_export)))
    }
}