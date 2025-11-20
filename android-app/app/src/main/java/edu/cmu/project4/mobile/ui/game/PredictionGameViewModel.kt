package edu.cmu.project4.mobile.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class PredictionGameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    private var priceJob: Job? = null
    private var countdownJob: Job? = null

    init {
        startPriceSimulation()
    }

    private fun startPriceSimulation() {
        if (priceJob?.isActive == true) return
        priceJob = viewModelScope.launch {
            while (true) {
                delay(100)
                val change = (Random.nextDouble() - 0.5) * 150
                _uiState.update { state ->
                    val newPrice = (state.currentPrice + change).coerceAtLeast(1000.0)
                    val history = (state.priceHistory + newPrice).takeLast(MAX_HISTORY_POINTS)
                    state.copy(currentPrice = newPrice, priceHistory = history)
                }
            }
        }
    }

    fun togglePrediction(type: PredictionType) {
        _uiState.update { state ->
            if (state.gameState != GameState.IDLE) return
            val next = if (state.prediction == type) null else type
            state.copy(prediction = next)
        }
    }

    fun adjustBet(delta: Int) {
        _uiState.update { state ->
            if (state.gameState != GameState.IDLE) return
            val next = (state.betAmount + delta).coerceIn(100, state.balance)
            state.copy(betAmount = next)
        }
    }

    fun betMax() {
        _uiState.update { state ->
            if (state.gameState != GameState.IDLE) return
            state.copy(betAmount = state.balance)
        }
    }

    fun startGame() {
        val state = _uiState.value
        val prediction = state.prediction ?: return
        if (state.betAmount > state.balance || state.gameState != GameState.IDLE) return
        val session = GameSession(
            startPrice = state.currentPrice,
            prediction = prediction,
            betAmount = state.betAmount
        )
        _uiState.update {
            it.copy(
                gameState = GameState.PLAYING,
                timeLeft = GAME_DURATION,
                result = null,
                gameSession = session
            )
        }
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            repeat(GAME_DURATION) {
                delay(1000)
                _uiState.update { cur -> cur.copy(timeLeft = cur.timeLeft - 1) }
            }
            finishGame()
        }
    }

    private fun finishGame() {
        countdownJob?.cancel()
        val state = _uiState.value
        val session = state.gameSession ?: return
        val finalPrice = state.currentPrice
        val win = when (session.prediction) {
            PredictionType.RISE -> finalPrice > session.startPrice
            PredictionType.FALL -> finalPrice < session.startPrice
        }
        val balance = if (win) state.balance + session.betAmount else state.balance - session.betAmount
        _uiState.update {
            it.copy(
                gameState = GameState.RESULT,
                balance = balance,
                result = GameResult(win, session.betAmount, finalPrice, session.startPrice)
            )
        }
    }

    fun resetGame() {
        countdownJob?.cancel()
        _uiState.update {
            it.copy(
                gameState = GameState.IDLE,
                timeLeft = GAME_DURATION,
                prediction = null,
                result = null,
                gameSession = null
            )
        }
    }

    fun isWinning(currentPrice: Double, startPrice: Double?, prediction: PredictionType?): Boolean {
        val threshold = startPrice ?: return true
        return when (prediction) {
            PredictionType.RISE -> currentPrice >= threshold
            PredictionType.FALL -> currentPrice < threshold
            null -> true
        }
    }

    companion object {
        const val GAME_DURATION = 10
        private const val MAX_HISTORY_POINTS = 60
    }
}

data class GameUiState(
    val balance: Int = 12_345,
    val betAmount: Int = 1_000,
    val prediction: PredictionType? = null,
    val gameState: GameState = GameState.IDLE,
    val timeLeft: Int = PredictionGameViewModel.GAME_DURATION,
    val result: GameResult? = null,
    val currentPrice: Double = 64_230.5,
    val priceHistory: List<Double> = emptyList(),
    val gameSession: GameSession? = null
)

enum class PredictionType { RISE, FALL }

enum class GameState { IDLE, PLAYING, RESULT }

data class GameSession(
    val startPrice: Double,
    val prediction: PredictionType,
    val betAmount: Int
)

data class GameResult(
    val win: Boolean,
    val amount: Int,
    val finalPrice: Double,
    val startPrice: Double
)
