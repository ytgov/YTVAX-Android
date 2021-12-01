package ca.yk.gov.vaxcheck

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ca.yk.gov.vaxcheck.utils.LanguageConstants.setLocale
import ca.yk.gov.vaxcheck.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * [SplashActivity]
 *
 * @author Amit Metri
 */
@SuppressLint("CustomSplashScreen")

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            sharedViewModel.getSelectedLanguage.collect {
                setLocale(it)
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }
}
