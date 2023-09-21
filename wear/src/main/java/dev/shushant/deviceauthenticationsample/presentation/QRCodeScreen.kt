package dev.shushant.deviceauthenticationsample.presentation

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import dev.shushant.deviceauthenticationsample.qrcode.QRGEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun QRCodeScreen() {
    val link = "$BASE_URL/auth"
    val qrcodeGenerator = remember { QRGEncoder(data = link, dimension = 200) }
    val coroutineScope = rememberCoroutineScope()
    var bitmap: Bitmap? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            bitmap = qrcodeGenerator.bitmap
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround,
    ) {
        Text(link, textAlign = TextAlign.Center)
        bitmap?.asImageBitmap()
            ?.let { Image(bitmap = it, contentDescription = "") }
    }
}