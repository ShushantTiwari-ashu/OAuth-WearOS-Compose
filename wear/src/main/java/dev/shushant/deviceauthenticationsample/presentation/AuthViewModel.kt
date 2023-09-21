package dev.shushant.deviceauthenticationsample.presentation

import android.app.Application
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.phone.interactions.authentication.CodeChallenge
import androidx.wear.phone.interactions.authentication.CodeVerifier
import androidx.wear.phone.interactions.authentication.OAuthRequest
import androidx.wear.phone.interactions.authentication.OAuthResponse
import androidx.wear.phone.interactions.authentication.RemoteAuthClient
import dev.shushant.deviceauthenticationsample.R
import dev.shushant.deviceauthenticationsample.authService.doPostRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "AuthViewModel"
private const val CLIENT_ID = ""
private const val CLIENT_SECRET = ""
const val BASE_URL = "http://192.168.1.33:8080"


class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val token = MutableStateFlow("")
    val isSuccess = MutableStateFlow(false)
    val status = MutableStateFlow("")
    val retry = MutableStateFlow(false)

    fun getToken() = token.value

    private fun showStatus(statusString: Int, resultString: String = "") {
        status.updateAndGet { getApplication<Application>().resources.getString(statusString) }
        token.updateAndGet { resultString }
    }

    private fun updateRetry() {
        isSuccess.updateAndGet { false }
    }

    fun startAuthFlow() {
        viewModelScope.launch {
            val codeVerifier = CodeVerifier()

            Log.d("Build.MANUFACTURER",Build.MANUFACTURER)
            Log.d("Build.ID",Build.ID)
            Log.d("Build.MODEL",Build.MODEL)
            Log.d("Build.BRAND",Build.BRAND)


            val uri = Uri.Builder().encodedPath("${BASE_URL}auth").build()
            val oauthRequest = OAuthRequest.Builder(getApplication()).setAuthProviderUrl(uri)
                .setCodeChallenge(CodeChallenge(codeVerifier)).setClientId(CLIENT_ID)
                .setRedirectUrl(Uri.parse("com.samsung.android.waterplugin:/3p_auth/"))
                .build()

            // Step 1: Retrieve the OAuth code
            showStatus(R.string.status_switch_to_phone)
            val code = retrieveOAuthCode(oauthRequest).getOrElse {
                showStatus(R.string.status_failed)
                updateRetry()
                return@launch
            }

            // Step 2: Retrieve the access token
            showStatus(R.string.status_retrieving_token,"For $code")
            val token = retrieveToken(code, codeVerifier, oauthRequest).getOrElse {
                showStatus(R.string.status_failure_token)
                updateRetry()
                return@launch
            }
            isSuccess.updateAndGet { true }
            showStatus(R.string.status_token_received, token)
        }
    }

    private suspend fun retrieveOAuthCode(
        oauthRequest: OAuthRequest
    ): Result<String> {
        Log.d(TAG, "Authorization requested. Request URL: ${oauthRequest.requestUrl}")

        // Wrap the callback-based request inside a coroutine wrapper
        return suspendCoroutine { c ->
            RemoteAuthClient.create(getApplication())
                .sendAuthorizationRequest(request = oauthRequest,
                    executor = { command -> command?.run() },
                    clientCallback = object : RemoteAuthClient.Callback() {
                        override fun onAuthorizationError(request: OAuthRequest, errorCode: Int) {
                            Log.w(TAG, "Authorization failed with errorCode $errorCode")
                            c.resume(Result.failure(IOException("Authorization failed - PHONE_UNAVAILABLE")))
                        }

                        override fun onAuthorizationResponse(
                            request: OAuthRequest, response: OAuthResponse
                        ) {
                            val responseUrl = response.responseUrl
                            Log.d(TAG, "Authorization success. ResponseUrl: $responseUrl")

                            val code = responseUrl?.getQueryParameter("code")
                            Log.d(TAG, "Authorization Code: $code")
                            if (code.isNullOrBlank()) {
                                c.resume(Result.failure(IOException("Authorization failed")))
                            } else {
                                c.resume(Result.success(code))
                            }
                        }
                    })
        }
    }

    private suspend fun retrieveToken(
        code: String, codeVerifier: CodeVerifier, oauthRequest: OAuthRequest
    ): Result<String> {
        return runCatching {
            val responseJson = doPostRequest(
                url = "${BASE_URL}token", params = mapOf(
                    "client_id" to CLIENT_ID,
                    "client_secret" to CLIENT_SECRET,
                    "code" to code,
                    "code_verifier" to codeVerifier.value,
                    "grant_type" to "authorization_code",
                    "redirect_uri" to oauthRequest.redirectUrl
                )
            )
            Result.success(responseJson.accessToken)
        }.getOrElse {
            Result.failure(it)
        }
    }
}

@Serializable
data class TokenData(@SerialName("access_token") val accessToken: String)