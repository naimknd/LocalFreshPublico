package com.example.localfresh.activitys.comprador.estadisticas

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.localfresh.R
import com.example.localfresh.databinding.FragmentEstadisticasCompradorBinding
import com.example.localfresh.model.comprador.estadisticas.EstadisticasResponse
import com.example.localfresh.model.comprador.estadisticas.GroupedData
import com.example.localfresh.model.comprador.estadisticas.TimelineData
import com.example.localfresh.viewmodel.comprador.estadisticas.EstadisticasViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class EstadisticasFragment : Fragment() {

    private var _binding: FragmentEstadisticasCompradorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EstadisticasViewModel by viewModels()

    // Variables para los filtros
    private var currentPeriod = "month"
    private var currentGroupBy = "category"

    // Caché para datos de gráficos
    private var timelineDataCache = listOf<TimelineData>()
    private var dateLabelsCache = listOf<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEstadisticasCompradorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        loadData()
    }

    private fun setupUI() {
        // Botón de regreso
        binding.btnBack.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        // Botones de periodo
        binding.periodToggleGroup.apply {
            check(R.id.btnMonth)
            addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    currentPeriod = when (checkedId) {
                        R.id.btnWeek -> "week"
                        R.id.btnYear -> "year"
                        else -> "month"
                    }
                    loadData()
                }
            }
        }

        // Botones de agrupación
        binding.groupByToggleGroup.apply {
            check(R.id.btnGroupByCategory)
            addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    currentGroupBy = if (checkedId == R.id.btnGroupByCategory) "category" else "store"
                    loadData()
                }
            }
        }

        // Configurar gráficos vacíos
        setupEmptyCharts()
    }

    private fun setupObservers() {
        viewModel.estadisticasData.observe(viewLifecycleOwner) { response ->
            if (response.status == "success") {
                if (response.grouped_data.isEmpty() || response.summary.total_spent == 0.0) {
                    showViewState(ViewState.EMPTY)
                } else {
                    showViewState(ViewState.CONTENT)
                    updateUI(response)
                }
            } else {
                showViewState(ViewState.ERROR(response.status))
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg.isNotEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                showViewState(ViewState.EMPTY)
            }
        }
    }

    private fun loadData() {
        val userId = requireActivity().getSharedPreferences("LocalFreshPrefs", Context.MODE_PRIVATE).getInt("USER_ID", -1)
        if (userId != -1) {
            viewModel.loadStatistics(userId, currentPeriod, currentGroupBy)
        } else {
            Toast.makeText(requireContext(), "Debes iniciar sesión para ver estadísticas", Toast.LENGTH_SHORT).show()
            showViewState(ViewState.EMPTY)
        }
    }

    private fun updateUI(data: EstadisticasResponse) {
        updateSummary(data.summary)
        updateBarChart(data.timeline_data)
        updatePieChart(data.grouped_data)
        binding.tvPieChartTitle.text = if (currentGroupBy == "category") "Gastos por Categoría" else "Gastos por Tienda"
    }

    private fun updateSummary(summary: com.example.localfresh.model.comprador.estadisticas.EstadisticasSummary) {
        val currencyFormat = NumberFormat.getCurrencyInstance().apply { currency = Currency.getInstance("MXN") }

        binding.apply {
            tvTotalSpent.text = currencyFormat.format(summary.total_spent)
            tvTotalSavings.text = currencyFormat.format(summary.total_savings)
            tvSavingsPercentage.text = "${summary.savings_percentage}%"
            tvPeriodInfo.text = "Período: ${formatDate(summary.start_date)} - ${formatDate(summary.end_date)}"
        }
    }

    private fun updateBarChart(timelineData: List<TimelineData>) {
        val barChart = binding.barChart
        barChart.clear()

        if (timelineData.isEmpty()) {
            barChart.setNoDataText("No hay datos disponibles")
            return
        }

        // Limitar a los últimos 7 puntos para mejor visualización
        val limitedData = if (timelineData.size > 7) timelineData.takeLast(7) else timelineData
        timelineDataCache = limitedData

        val spentEntries = ArrayList<BarEntry>()
        val savingsEntries = ArrayList<BarEntry>()
        val dates = ArrayList<String>()

        limitedData.forEachIndexed { index, data ->
            spentEntries.add(BarEntry(index.toFloat(), data.spent_amount.toFloat()))
            savingsEntries.add(BarEntry(index.toFloat(), data.saved_amount.toFloat()))
            dates.add(data.date.formatDate())
        }
        dateLabelsCache = dates

        val textColor = ContextCompat.getColor(requireContext(), R.color.text_color)

        // Conjuntos de datos
        val spentDataSet = BarDataSet(spentEntries, "Gasto").apply {
            color = Color.rgb(255, 193, 7)
            valueTextColor = textColor
            valueTextSize = 10f
        }

        val savingsDataSet = BarDataSet(savingsEntries, "Ahorro").apply {
            color = Color.rgb(104, 159, 56)
            valueTextColor = textColor
            valueTextSize = 10f
        }

        val barData = BarData(spentDataSet, savingsDataSet).apply {
            barWidth = 0.3f
        }

        configureBarChart(barChart, dates, barData)
    }

    private fun configureBarChart(barChart: com.github.mikephil.charting.charts.BarChart, dates: List<String>, barData: BarData) {
        val textColor = ContextCompat.getColor(requireContext(), R.color.text_color)

        barChart.apply {
            data = barData
            description.isEnabled = false
            legend.isEnabled = true
            legend.textColor = textColor

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(dates)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setCenterAxisLabels(true)
                setTextColor(textColor)
            }

            axisLeft.apply {
                axisMinimum = 0f
                setTextColor(textColor)
            }

            axisRight.isEnabled = false

            val groupSpace = 0.4f
            val barSpace = 0.05f
            groupBars(0f, groupSpace, barSpace)

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let { entry ->
                        val index = entry.x.toInt()
                        if (index in timelineDataCache.indices && index in dateLabelsCache.indices) {
                            showDayDetailDialog(dateLabelsCache[index], timelineDataCache[index])
                        }
                    }
                }
                override fun onNothingSelected() {}
            })

            animateY(1000)
            invalidate()
        }
    }

    private fun updatePieChart(groupedData: List<GroupedData>) {
        val pieChart = binding.pieChart
        pieChart.clear()

        if (groupedData.isEmpty()) {
            pieChart.setNoDataText("No hay datos disponibles")
            return
        }

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        val topGroups = groupedData.sortedByDescending { it.spent_amount }.take(5)

        topGroups.forEach { group ->
            entries.add(PieEntry(group.spent_amount.toFloat(), group.group_name))
            colors.add(ColorTemplate.MATERIAL_COLORS[entries.size % ColorTemplate.MATERIAL_COLORS.size])
        }

        // Si hay más grupos, añadir "Otros"
        if (groupedData.size > 5) {
            val otherAmount = groupedData.drop(5).sumOf { it.spent_amount }
            if (otherAmount > 0) {
                entries.add(PieEntry(otherAmount.toFloat(), "Otros"))
                colors.add(Color.GRAY)
            }
        }

        val textColor = ContextCompat.getColor(requireContext(), R.color.text_color)

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            sliceSpace = 3f
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter())
        }

        pieChart.apply {
            data = pieData
            description.isEnabled = false
            legend.textColor = textColor
            isDrawHoleEnabled = true
            setUsePercentValues(true)
            setEntryLabelColor(Color.WHITE)
            centerText = if (currentGroupBy == "category") "Por Categoría" else "Por Tienda"
            setCenterTextSize(14f)
            setCenterTextColor(R.color.black)

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e is PieEntry) {
                        groupedData.find { it.group_name == e.label }?.let { showGroupDetailDialog(it) }
                    }
                }
                override fun onNothingSelected() {}
            })

            animateY(1000)
            invalidate()
        }
    }

    private fun showDayDetailDialog(date: String, data: TimelineData) {
        val currencyFormat = NumberFormat.getCurrencyInstance().apply { currency = Currency.getInstance("MXN") }
        val totalOriginal = data.spent_amount + data.saved_amount
        val savingsPercentage = if (totalOriginal > 0) (data.saved_amount / totalOriginal * 100).toInt() else 0

        val message = """
            Fecha: $date
            
            Gasto total: ${currencyFormat.format(data.spent_amount)}
            Ahorro: ${currencyFormat.format(data.saved_amount)}
            Porcentaje de ahorro: $savingsPercentage%
            
            Precio original: ${currencyFormat.format(totalOriginal)}
        """.trimIndent()

        showDialog("Detalle del día", message)
    }

    private fun showGroupDetailDialog(group: GroupedData) {
        val currencyFormat = NumberFormat.getCurrencyInstance().apply { currency = Currency.getInstance("MXN") }
        val totalOriginal = group.spent_amount + group.saved_amount
        val savingsPercentage = if (totalOriginal > 0) (group.saved_amount / totalOriginal * 100).toInt() else 0
        val groupType = if (currentGroupBy == "category") "Categoría" else "Tienda"
        val avgPerPurchase = if (group.purchase_count > 0) group.spent_amount / group.purchase_count else 0.0

        val message = """
            $groupType: ${group.group_name}
            
            Gasto total: ${currencyFormat.format(group.spent_amount)}
            Ahorro: ${currencyFormat.format(group.saved_amount)}
            Porcentaje de ahorro: $savingsPercentage%
            
            Número de compras: ${group.purchase_count}
            Gasto promedio por compra: ${currencyFormat.format(avgPerPurchase)}
        """.trimIndent()

        showDialog("Detalle de $groupType", message)
    }

    private fun setupEmptyCharts() {
        val textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
        binding.barChart.apply {
            setNoDataText("No hay datos disponibles")
            setNoDataTextColor(textColor)
            description.isEnabled = false
        }
        binding.pieChart.apply {
            setNoDataText("No hay datos disponibles")
            setNoDataTextColor(textColor)
            description.isEnabled = false
        }
    }

    // Helpers
    private fun showViewState(state: ViewState) {
        when (state) {
            ViewState.CONTENT -> {
                binding.contentContainer.visibility = View.VISIBLE
                binding.emptyStateContainer.visibility = View.GONE
            }
            ViewState.EMPTY -> {
                binding.contentContainer.visibility = View.GONE
                binding.emptyStateContainer.visibility = View.VISIBLE
                binding.tvEmptyState.text = "No hay datos disponibles"
            }
            is ViewState.ERROR -> {
                binding.contentContainer.visibility = View.GONE
                binding.emptyStateContainer.visibility = View.VISIBLE
                binding.tvEmptyState.text = "Error: ${state.message}"
            }
        }
    }

    private fun showDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun formatDate(dateString: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date!!)
        } catch (e: Exception) {
            dateString
        }
    }

    private fun String.formatDate(inputPattern: String = "yyyy-MM-dd", outputPattern: String = "dd/MM"): String {
        return try {
            val date = SimpleDateFormat(inputPattern, Locale.getDefault()).parse(this)
            SimpleDateFormat(outputPattern, Locale.getDefault()).format(date!!)
        } catch (e: Exception) {
            this
        }
    }

    sealed class ViewState {
        object CONTENT : ViewState()
        object EMPTY : ViewState()
        data class ERROR(val message: String) : ViewState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
