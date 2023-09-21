package dev.shushant.deviceauthenticationsample.presentation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.wearable.Wearable


class MainActivityOAuth : ComponentActivity() {

    private val viewModel by viewModels<AuthViewModel>()
    private val nodeClient by lazy { Wearable.getNodeClient(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        setContent {
            WearApp(
                viewModel = viewModel,
                nodeClient = nodeClient
            )
        }

    }
}



