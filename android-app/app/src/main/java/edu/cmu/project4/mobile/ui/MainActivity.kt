package edu.cmu.project4.mobile.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import edu.cmu.project4.mobile.BuildConfig
import edu.cmu.project4.mobile.R
import edu.cmu.project4.mobile.data.PriceRepository
import edu.cmu.project4.mobile.databinding.ActivityMainBinding
import edu.cmu.project4.mobile.util.ClientInfoProvider
import kotlinx.coroutines.launch

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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { renderState(it) }
            }
        }
    }

    private fun renderState(state: PriceUiState) = with(binding) {
        progressBar.isVisible = state.isLoading
        fetchButton.isEnabled = !state.isLoading
        priceValue.text = state.lastPrice?.price ?: getString(R.string.placeholder_dash)
        timestampValue.text = state.lastPrice?.fetchedAt ?: getString(R.string.placeholder_dash)
        statusValue.text = state.statusMessage ?: getString(R.string.placeholder_dash)
        errorValue.isVisible = !state.errorMessage.isNullOrBlank()
        errorValue.text = state.errorMessage.orEmpty()

        if (!state.errorMessage.isNullOrBlank()) {
            Snackbar.make(root, state.errorMessage, Snackbar.LENGTH_LONG).show()
        }
    }
}
