package dev.shushant.deviceauthenticationsample.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


class MainActivityOAuth : ComponentActivity() {

    private val viewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            val result by viewModel.result.collectAsState()
            val status by viewModel.status.collectAsState()
            val retry by viewModel.retry.collectAsState()
            WearApp(
                currentStatus = status,
                retry = retry,
                result = result,
            ) {
                viewModel.startAuthFlow()
            }
        }
    }


}



