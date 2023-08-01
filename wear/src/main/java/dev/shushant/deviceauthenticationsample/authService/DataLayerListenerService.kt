package dev.shushant.deviceauthenticationsample.authService

import android.annotation.SuppressLint
import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class DataLayerListenerService : WearableListenerService() {

    private val messageClient by lazy { Wearable.getMessageClient(this) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @SuppressLint("VisibleForTests")
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        dataEvents.forEach { dataEvent ->
            val uri = dataEvent.dataItem.uri
            when (uri.path) {
                TOKEN_KEY -> {
                    scope.launch {
                        try {
                            val nodeId = uri.host!!
                            val payload = uri.toString().toByteArray()
                            messageClient.sendMessage(
                                nodeId,
                                DATA_ITEM_RECEIVED_PATH,
                                payload
                            ).await()
                            Log.d(TAG, "Message sent successfully")
                        } catch (cancellationException: CancellationException) {
                            throw cancellationException
                        } catch (exception: Exception) {
                            Log.d(TAG, "Message failed")
                        }
                    }
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        when (messageEvent.path) {
            TOKEN_KEY -> {

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        private const val TAG = "DataLayerService"
        private const val DATA_ITEM_RECEIVED_PATH = "/data-item-received"
        const val TOKEN_KEY = "/token"
    }
}
