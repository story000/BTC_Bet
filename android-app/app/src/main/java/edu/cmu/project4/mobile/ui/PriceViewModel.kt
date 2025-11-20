package edu.cmu.project4.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.cmu.project4.mobile.BuildConfig
import edu.cmu.project4.mobile.data.PriceRepository
import edu.cmu.project4.mobile.data.PriceResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class PriceViewModel(
    private val repository: PriceRepository,
    private val clientId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(PriceUiState(serverInput = BuildConfig.DEFAULT_BASE_URL))
    val uiState: StateFlow<PriceUiState> = _uiState.asStateFlow()

    fun updateSymbolInput(text: String) {
        _uiState.update { it.copy(symbolInput = text.uppercase(Locale.US)) }
    }

    fun updateServerInput(text: String) {
        _uiState.update { it.copy(serverInput = text.trim()) }
    }

    fun fetchLatestPrice() {
        val symbol = _uiState.value.symbolInput.ifBlank { "BTCUSDT" }
        val server = _uiState.value.serverInput.ifBlank { BuildConfig.DEFAULT_BASE_URL }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, statusMessage = "请求中...") }
            try {
                val response = repository.fetchPrice(server, symbol, clientId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lastPrice = response,
                        statusMessage = "成功",
                        errorMessage = null
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
}

data class PriceUiState(
    val isLoading: Boolean = false,
    val symbolInput: String = "BTCUSDT",
    val serverInput: String = BuildConfig.DEFAULT_BASE_URL,
    val lastPrice: PriceResponse? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null
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
