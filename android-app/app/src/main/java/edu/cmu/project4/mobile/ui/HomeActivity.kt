package edu.cmu.project4.mobile.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.cmu.project4.mobile.databinding.ActivityHomeBinding
import edu.cmu.project4.mobile.ui.game.WebViewPredictionGameActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        binding.gameButton.setOnClickListener {
            startActivity(Intent(this, WebViewPredictionGameActivity::class.java))
        }
    }
}
