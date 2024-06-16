package lunalauf.rms.utilities.network.communication

import com.google.gson.JsonParseException
import kotlinx.coroutines.*
import lunalauf.rms.utilities.network.client.Client
import lunalauf.rms.utilities.network.communication.message.request.Request
import lunalauf.rms.utilities.network.communication.message.response.Response
import org.slf4j.LoggerFactory

class RequestSubmitter(
    private val responseHandler: (Response) -> Unit,
    private val errorHandler: (ErrorType) -> Unit
) {
    companion object {
        private const val NUM_MESSAGES_TO_DUMP = 5
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

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
        if (!client.isConnected) {
            logger.error("Not connected to server")
            throw Client.NoConnectionException()
        }
        client.send(MessageProcessor.toJsonString(request))
        logger.info("Request sent: {}", request)

        var it = 0
        while (it < NUM_MESSAGES_TO_DUMP) {
            if (!isActive)
                throw CancellationException()

            val receivedString = client.receive()
            try {
                val receivedMessage = MessageProcessor.fromJsonString(receivedString)
                if (receivedMessage is Response) {
                    if (receivedMessage.messageId == request.messageId || receivedMessage.messageId == -1L) {
                        logger.info("Received message: {}", receivedMessage)
                        return receivedMessage
                    }
                }
            } catch (e: JsonParseException) {
                logger.warn("Received corrupted server message", e)
                throw CorruptedMessageException(e)
            }
            it++
        }
        logger.error("Server did not respond")
        throw Client.NoAnswerException()
    }

    class CorruptedMessageException : Exception {
        constructor() : super()
        constructor(cause: Throwable?) : super(cause)
    }
}
