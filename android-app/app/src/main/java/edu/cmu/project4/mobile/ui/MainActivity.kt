package edu.cmu.project4.mobile.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.snackbar.Snackbar
import android.content.Intent
import edu.cmu.project4.mobile.BuildConfig
import edu.cmu.project4.mobile.R
import edu.cmu.project4.mobile.data.PriceRepository
import edu.cmu.project4.mobile.databinding.ActivityMainBinding
import edu.cmu.project4.mobile.util.ClientInfoProvider
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: PriceViewModel by viewModels {
        val clientId = ClientInfoProvider.buildClientId(this)
        PriceViewModelFactory(PriceRepository(), clientId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupChart()

        binding.symbolInput.setText(viewModel.uiState.value.symbolInput)
        binding.serverInput.setText(BuildConfig.DEFAULT_BASE_URL)

        binding.symbolInput.doAfterTextChanged { editable ->
            viewModel.updateSymbolInput(editable?.toString().orEmpty())
        }
        binding.serverInput.doAfterTextChanged { editable ->
            viewModel.updateServerInput(editable?.toString().orEmpty())
        }

        binding.fetchButton.setOnClickListener {
            viewModel.fetchLatestPrice()
        }
        binding.gameButton.setOnClickListener {
            startActivity(Intent(this, edu.cmu.project4.mobile.ui.game.PredictionGameActivity::class.java))
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { renderState(it) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.startRealtimeStream()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopRealtimeStream()
    }

    private fun renderState(state: PriceUiState) = with(binding) {
        progressBar.isVisible = state.isLoading
        fetchButton.isEnabled = !state.isLoading
        priceValue.text = state.lastPrice?.price ?: getString(R.string.placeholder_dash)
        timestampValue.text = state.lastPrice?.fetchedAt ?: getString(R.string.placeholder_dash)
        statusValue.text = state.statusMessage ?: getString(R.string.placeholder_dash)
        errorValue.isVisible = !state.errorMessage.isNullOrBlank()
        errorValue.text = state.errorMessage.orEmpty()
        updateChart(state.priceHistory)

        if (!state.errorMessage.isNullOrBlank()) {
            Snackbar.make(root, state.errorMessage, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupChart() = with(binding.priceChart) {
        description.isEnabled = false
        legend.isEnabled = false
        setTouchEnabled(true)
        setScaleEnabled(true)
        axisRight.isEnabled = false
        axisLeft.apply {
            textColor = getColor(R.color.md_theme_dark_onBackground)
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        }
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            textColor = getColor(R.color.md_theme_dark_onBackground)
            granularity = 1f
        }
    }

    private fun updateChart(points: List<PricePoint>) {
        val entries = points.mapIndexedNotNull { index, point ->
            if (point.price.isNaN()) null else Entry(index.toFloat(), point.price.toFloat())
        }
        if (entries.isEmpty()) {
            binding.priceChart.clear()
            binding.priceChart.invalidate()
            return
        }
        val dataSet = LineDataSet(entries, getString(R.string.label_chart)).apply {
            color = getColor(R.color.crypto_orange)
            setDrawCircles(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            valueTextSize = 0f
        }
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
        val labels = points.map { formatter.format(it.timestamp) }
        binding.priceChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.priceChart.data = LineData(dataSet)
        binding.priceChart.notifyDataSetChanged()
        binding.priceChart.invalidate()
    }
}
