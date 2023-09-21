package dev.shushant.deviceauthenticationsample.presentation

import android.content.Context
import android.content.Intent
import android.content.Intent.CATEGORY_APP_BROWSER
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Login
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.NodeClient
import dev.shushant.deviceauthenticationsample.presentation.theme.WearOsAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun WearApp(
    viewModel: AuthViewModel,
    nodeClient: NodeClient,
) {

    val isSuccess by viewModel.isSuccess.collectAsState()
    val retry by viewModel.retry.collectAsState()

    val powerManager = LocalContext.current.getSystemService(Context.POWER_SERVICE) as PowerManager


    val wakeLock =
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WearApp::MyWakelockTag")
    LaunchedEffect(key1 = isSuccess, key2 = retry) {
        wakeLock.acquire(5 * 60 * 1000L)
        if (isSuccess) {
            wakeLock.release()
        }
    }

    val lazyListState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val navController = rememberNavController()

    LaunchedEffect(key1 = Unit, block = {
        //focusRequester.requestFocus()
    })

    WearOsAppTheme {
        Scaffold(
            positionIndicator = {
                PositionIndicator(scalingLazyListState = lazyListState)
            }
        ) {
            NavHost(navController = navController, startDestination = "Login") {
                composable("Login") {
                    Login(lazyListState, focusRequester, viewModel, nodeClient) {
                        navController.navigate("QR")
                    }
                }
                composable("QR") {
                    QRCodeScreen()
                }
            }
        }
    }
}

@Composable
fun Login(
    lazyListState: ScalingLazyListState,
    focusRequester: FocusRequester,
    viewModel: AuthViewModel,
    nodeClient: NodeClient,
    openQR: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val currentStatus by viewModel.status.collectAsState()
    val retry by viewModel.retry.collectAsState()
    val context = LocalContext.current
    var nodeid by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        nodeid = nodeClient.localNode.await().id
        packageName = nodeClient.getCompanionPackageForNode(nodeid).await()
    }
    ScalingLazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onRotaryScrollEvent {
                coroutineScope.launch {
                    lazyListState.scrollBy(it.verticalScrollPixels)
                }
                true
            }
            .background(MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            LoginWithMobile(currentStatus, isSuccess, retry, viewModel.getToken()) {
                //viewModel.startAuthFlow()
                //openQR.invoke()
                val result = RemoteActivityHelper(context = context, Dispatchers.IO.asExecutor()).startRemoteActivity(
                    Intent(Intent.ACTION_VIEW)
                        .addCategory(Intent.CATEGORY_BROWSABLE)
                        .setData(
                            Uri.parse("https://www.google.com")
                        ),
                    nodeid
                )
                Log.e("LoginWithMobile", result.toString().plus("\n $nodeid"))
                Log.e("packageName", packageName)
            }
        }
    }
}

@Composable
fun LoginWithMobile(
    currentStatus: String = "",
    isSuccess: Boolean,
    retry: Boolean,
    token: String,
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
        } else if (isSuccess) {
            Text(text = "---Succeeded---\n".plus(token), textAlign = TextAlign.Center)
        } else {
            Text(text = currentStatus)
        }
    }
}