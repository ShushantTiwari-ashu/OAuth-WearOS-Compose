package dev.shushant.deviceauthenticationsample.authService

import dev.shushant.deviceauthenticationsample.presentation.TokenData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

suspend fun doPostRequest(
    url: String,
    params: Map<String, String>,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): TokenData {
    return withContext(dispatcher) {
        KtorClient.client().post<TokenData>(url) {
            params.map {
                parameter(it.key, it.value)
            }
        }
    }
}

suspend fun doGetRequest(
    url: String,
    requestHeaders: Map<String, String>,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): JSONObject {
    return withContext(dispatcher) {
        with(KtorClient.client()) {
            get<JSONObject>(url) {
                headers {
                    requestHeaders.map {
                        append(it.key, it.value)
                    }
                }
            }
        }
    }
}
