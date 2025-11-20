package edu.cmu.project4.mobile.ui.game

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import edu.cmu.project4.mobile.BuildConfig
import edu.cmu.project4.mobile.data.PriceRepository
import edu.cmu.project4.mobile.databinding.ActivityWebviewGameBinding
import edu.cmu.project4.mobile.util.ClientInfoProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WebViewPredictionGameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebviewGameBinding
    private lateinit var webView: WebView
    private val repository = PriceRepository()
    private var priceUpdateJob: Job? = null
    private lateinit var clientId: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebviewGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clientId = ClientInfoProvider.buildClientId(this)

        setupWebView()
        loadGame()
        startPriceUpdates()

        binding.closeButton.setOnClickListener { finish() }
    }

    private fun startPriceUpdates() {
        priceUpdateJob?.cancel()
        priceUpdateJob = lifecycleScope.launch {
            var successCount = 0
            var failCount = 0

            while (isActive) {
                try {
                    val response = repository.fetchPrice(
                        baseUrl = BuildConfig.DEFAULT_BASE_URL,
                        symbol = "BTCUSD",
                        clientId = clientId
                    )
                    val price = response.price.toDoubleOrNull()

                    if (price == null) {
                        Log.e(TAG, "Invalid price format: ${response.price}")
                        failCount++
                        delay(500)
                        continue
                    }

                    // Update price in WebView
                    webView.post {
                        webView.evaluateJavascript(
                            "window.updatePrice && window.updatePrice($price);",
                            null
                        )
                    }

                    successCount++
                    if (successCount % 10 == 0) { // 每10次记录一次
                        Log.d(TAG, "Price updates: $successCount successful, $failCount failed. Latest: $$price")
                    }
                } catch (e: Exception) {
                    failCount++
                    Log.e(TAG, "Failed to fetch price (attempt $failCount): ${e.message}", e)

                    // 如果连续失败太多次，提示用户
                    if (failCount % 5 == 0) {
                        runOnUiThread {
                            Snackbar.make(
                                binding.root,
                                "Unable to fetch prices. Check server connection.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                delay(500) // Update every 200ms (0.2 second) for smooth chart
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "ObsoleteSdkInt")
    private fun setupWebView() {
        webView = binding.webView
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true

            // Critical: Allow loading local files and ES modules
            // These are deprecated but necessary for file:// URLs with modules
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = true
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = true

            // Additional settings for better compatibility
            databaseEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            loadsImagesAutomatically = true

            // Enable mixed content for development
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        // Add JavaScript Bridge
        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "Page loaded: $url")

                // Initialize with balance if needed
                webView.evaluateJavascript("window.updateBalance && window.updateBalance(12345);", null)
            }

            @Deprecated("Deprecated in API 23")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                @Suppress("DEPRECATION")
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e(TAG, "WebView Error: $errorCode - $description at $failingUrl")
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: android.webkit.ConsoleMessage?): Boolean {
                message?.let {
                    Log.d(TAG, "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}")
                }
                return true
            }
        }
    }

    private fun loadGame() {
        webView.loadUrl("file:///android_asset/game/index.html")
    }

    inner class AndroidBridge {
        @JavascriptInterface
        fun onGameFinished(
            win: Boolean,
            amount: Int,
            newBalance: Int,
            finalPrice: Double,
            startPrice: Double
        ) {
            Log.d(TAG, "Game finished: win=$win, amount=$amount, balance=$newBalance")

            runOnUiThread {
                val message = if (win) {
                    "Victory! +$amount points"
                } else {
                    "Defeat! -$amount points"
                }
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            }
        }

        @JavascriptInterface
        fun log(message: String) {
            Log.d(TAG, "JS Log: $message")
        }
    }

    override fun onDestroy() {
        priceUpdateJob?.cancel()
        webView.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        priceUpdateJob?.cancel()
    }

    override fun onResume() {
        super.onResume()
        startPriceUpdates()
    }

    companion object {
        private const val TAG = "WebViewGameActivity"
    }
}
