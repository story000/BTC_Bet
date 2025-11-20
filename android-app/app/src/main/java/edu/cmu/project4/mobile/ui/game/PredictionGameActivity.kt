package edu.cmu.project4.mobile.ui.game

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
import edu.cmu.project4.mobile.R
import edu.cmu.project4.mobile.databinding.ActivityPredictionGameBinding
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class PredictionGameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPredictionGameBinding
    private val viewModel: PredictionGameViewModel by viewModels()
    private val priceFormat = DecimalFormat("#,###.00")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPredictionGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupChart()
        setupClicks()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { renderState(it) }
            }
        }
    }

    private fun setupClicks() = with(binding) {
        closeButton.setOnClickListener { finish() }
        minusButton.setOnClickListener { viewModel.adjustBet(-100) }
        plusButton.setOnClickListener { viewModel.adjustBet(100) }
        maxButton.setOnClickListener { viewModel.betMax() }
        riseButton.setOnClickListener { viewModel.togglePrediction(PredictionType.RISE) }
        fallButton.setOnClickListener { viewModel.togglePrediction(PredictionType.FALL) }
        startGameButton.setOnClickListener { viewModel.startGame() }
        playAgainButton.setOnClickListener { viewModel.resetGame() }
    }

    private fun renderState(state: GameUiState) = with(binding) {
        balanceValue.text = priceFormat.format(state.balance)
        betValue.text = state.betAmount.toString()
        priceValue.text = priceFormat.format(state.currentPrice)
        timerValue.text = state.timeLeft.toString()

        val session = state.gameSession
        val winning = viewModel.isWinning(state.currentPrice, session?.startPrice, state.prediction)
        sessionStatus.apply {
            text = if (winning) getString(R.string.game_status_winning) else getString(R.string.game_status_losing)
            setTextColor(getColor(if (winning) R.color.emeraldAccent else R.color.roseAccent))
        }

        updatePredictionButton(riseButton, state.prediction == PredictionType.RISE)
        updatePredictionButton(fallButton, state.prediction == PredictionType.FALL)

        startGameButton.isEnabled = state.prediction != null && state.gameState == GameState.IDLE
        startGameButton.text = when (state.gameState) {
            GameState.IDLE -> getString(if (state.prediction == null) R.string.game_select_prediction else R.string.game_start)
            GameState.PLAYING -> getString(R.string.game_in_progress)
            GameState.RESULT -> getString(R.string.game_start)
        }

        countdownContainer.isVisible = state.gameState == GameState.PLAYING
        timeValue.text = getString(R.string.game_timer_value, state.timeLeft)

        overlayContainer.isVisible = state.gameState == GameState.RESULT
        state.result?.let { result ->
            overlayTitle.text = if (result.win) getString(R.string.game_result_victory) else getString(R.string.game_result_defeat)
            overlayTitle.setTextColor(getColor(if (result.win) R.color.emeraldAccent else R.color.roseAccent))
            overlaySubtitle.text = getString(if (result.win) R.string.game_gain else R.string.game_loss, result.amount)
        }

        errorLabel.isVisible = state.gameState == GameState.PLAYING
        errorLabel.text = getString(R.string.live_price_label, priceFormat.format(state.currentPrice))
        updateChart(state.priceHistory)
    }

    private fun updatePredictionButton(view: View, selected: Boolean) {
        view.isSelected = selected
        view.alpha = if (selected) 1f else 0.6f
        view.scaleX = if (selected) 1.05f else 1f
        view.scaleY = if (selected) 1.05f else 1f
    }

    private fun setupChart() = with(binding.priceChart) {
        description.isEnabled = false
        legend.isEnabled = false
        axisRight.isEnabled = false
        setTouchEnabled(false)
        setDrawGridBackground(false)
        axisLeft.apply {
            textColor = getColor(R.color.crypto_axis)
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            enableGridDashedLine(10f, 4f, 0f)
        }
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = getColor(R.color.crypto_axis)
            setDrawGridLines(false)
            granularity = 1f
        }
    }

    private fun updateChart(history: List<Double>) {
        if (history.isEmpty()) {
            binding.priceChart.clear()
            return
        }
        val entries = history.mapIndexed { index, price -> Entry(index.toFloat(), price.toFloat()) }
        val dataSet = LineDataSet(entries, "BTC").apply {
            color = getColor(R.color.crypto_orange)
            valueTextSize = 0f
            lineWidth = 2.5f
            setDrawCircles(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setGradientColor(getColor(R.color.crypto_orange), getColor(R.color.crypto_orange_dark))
        }
        val labels = history.mapIndexed { index, _ -> index.toString() }
        binding.priceChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.priceChart.data = LineData(dataSet)
        binding.priceChart.invalidate()
    }
}
