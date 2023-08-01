package dev.shushant.deviceauthenticationsample.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Login
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import dev.shushant.deviceauthenticationsample.presentation.theme.DeviceAuthenticationSampleTheme

@Composable
fun WearApp(
    result: String,
    currentStatus: String,
    retry: Boolean,
    requestLogin: () -> Unit,
) {

    DeviceAuthenticationSampleTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            LoginWithMobile(currentStatus, result, retry) {
                requestLogin.invoke()
            }
        }
    }
}

@Composable
fun LoginWithMobile(
    currentStatus: String = "",
    result: String,
    retry: Boolean,
    onSignInClick: () -> Unit
) {
    var clickable by remember {
        mutableStateOf(true)
    }
    Column(modifier = Modifier.clickable(enabled = clickable) {
        clickable = clickable.not()
        onSignInClick.invoke()
    }, horizontalAlignment = Alignment.CenterHorizontally) {
        if (clickable || retry) {
            clickable = clickable.not()
            Image(
                imageVector = Icons.Rounded.Login,
                contentDescription = "Login",
                colorFilter = ColorFilter.tint(
                    Color.Blue
                )
            )
            Text(text = "Sign In")
        } else if (result.isNotBlank() && clickable.not()) {
            Text(text = "---Succeeded---\n".plus(result))
        } else {
            Text(text = currentStatus)
        }
    }
}