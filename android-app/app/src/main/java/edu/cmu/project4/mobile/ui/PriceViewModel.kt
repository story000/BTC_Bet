package edu.cmu.project4.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.cmu.project4.mobile.BuildConfig
import edu.cmu.project4.mobile.data.PriceRepository
import edu.cmu.project4.mobile.data.PriceResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Locale

class PriceViewModel(
    private val repository: PriceRepository,
    private val clientId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(PriceUiState(serverInput = BuildConfig.DEFAULT_BASE_URL))
    val uiState: StateFlow<PriceUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    fun updateSymbolInput(text: String) {
        _uiState.update { it.copy(symbolInput = text.uppercase(Locale.US)) }
    }

    fun updateServerInput(text: String) {
        _uiState.update { it.copy(serverInput = text.trim()) }
    }

    fun startRealtimeStream() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (true) {
                refreshPrice(showLoading = false)
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stopRealtimeStream() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun fetchLatestPrice() {
        viewModelScope.launch {
            refreshPrice(showLoading = true)
        }
    }

    private suspend fun refreshPrice(showLoading: Boolean) {
        val symbol = _uiState.value.symbolInput.ifBlank { "BTCUSD" }
        val server = _uiState.value.serverInput.ifBlank { BuildConfig.DEFAULT_BASE_URL }
        if (showLoading) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, statusMessage = "请求中...") }
        }
        try {
            val response = repository.fetchPrice(server, symbol, clientId)
            val newPoint = PricePoint(
                timestamp = runCatching { Instant.parse(response.fetchedAt) }.getOrDefault(Instant.now()),
                price = response.price.toDoubleOrNull() ?: Double.NaN
            )
            _uiState.update {
                val updatedHistory = (it.priceHistory + newPoint).takeLast(MAX_HISTORY_POINTS)
                it.copy(
                    isLoading = false,
                    lastPrice = response,
                    statusMessage = if (showLoading) "Success" else "Realtime Update",
                    errorMessage = null,
                    priceHistory = updatedHistory
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: e.toString(),
                    statusMessage = "失败"
                )
            }
        }
    }
}

data class PriceUiState(
    val isLoading: Boolean = false,
    val symbolInput: String = "BTCUSD",
    val serverInput: String = BuildConfig.DEFAULT_BASE_URL,
    val lastPrice: PriceResponse? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val priceHistory: List<PricePoint> = emptyList()
)

class PriceViewModelFactory(
    private val repository: PriceRepository,
    private val clientId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PriceViewModel::class.java)) {
            return PriceViewModel(repository, clientId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}

data class PricePoint(
    val timestamp: Instant,
    val price: Double
)

private const val MAX_HISTORY_POINTS = 60
private const val POLL_INTERVAL_MS = 2000L
