package lunalauf.rms.utilities.network.communication

import com.google.gson.JsonParseException
import kotlinx.coroutines.*
import lunalauf.rms.utilities.network.client.Client
import lunalauf.rms.utilities.network.communication.message.request.Request
import lunalauf.rms.utilities.network.communication.message.response.Response

class RequestSubmitter(
    private val responseHandler: (Response) -> Unit,
    private val errorHandler: (ErrorType) -> Unit
) {
    companion object {
        private const val NUM_MESSAGES_TO_DUMP = 5
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    @Synchronized
    fun submit(
        request: Request,
        client: Client
    ) {
        job = scope.launch {
            try {
                val response = run(request, client)
                responseHandler(response)
            } catch (e: Throwable) {
                val error = when (e) {
                    is CancellationException -> ErrorType.UNWANTED_TERMINATION
                    is Client.NoAnswerException -> ErrorType.RESPONSE_TIMEOUT
                    is Client.NoConnectionException -> ErrorType.DISCONNECTED
                    is CorruptedMessageException -> ErrorType.CORRUPTED_SERVER_MESSAGE
                    else -> ErrorType.CORRUPTED_SERVER_MESSAGE
                }
                errorHandler(error)
            }
        }
    }

    @Synchronized
    fun shutdown() {
        job?.cancel()
    }

    @Throws(
        Client.NoAnswerException::class,
        Client.NoConnectionException::class,
        CorruptedMessageException::class,
        CancellationException::class
    )
    private fun CoroutineScope.run(
        request: Request,
        client: Client
    ): Response {
        if (!client.isConnected)
            throw Client.NoConnectionException()
        client.send(MessageProcessor.toJsonString(request))

        var it = 0
        while (it < NUM_MESSAGES_TO_DUMP) {
            if (!isActive)
                throw CancellationException()

            val receivedString = client.receive()
            try {
                val receivedMessage = MessageProcessor.fromJsonString(receivedString)
                if (receivedMessage is Response) {
                    if (receivedMessage.messageId == request.messageId || receivedMessage.messageId == -1L)
                        return receivedMessage
                }
            } catch (e: JsonParseException) {
                throw CorruptedMessageException(e)
            }
            it++
        }
        throw Client.NoAnswerException()
    }

    class CorruptedMessageException : Exception {
        constructor() : super()
        constructor(cause: Throwable?) : super(cause)
    }
}
