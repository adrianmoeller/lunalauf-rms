package lunalauf.rms.utilities.network.communication

import com.google.gson.JsonParseException
import kotlinx.coroutines.*
import lunalauf.rms.utilities.network.client.Connection
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
        connection: Connection
    ) {
        job = scope.launch {
            try {
                val response = run(request, connection)
                responseHandler(response)
            } catch (e: Throwable) {
                val error = when (e) {
                    is CancellationException -> ErrorType.UNWANTED_TERMINATION
                    is Connection.NoAnswerException -> ErrorType.RESPONSE_TIMEOUT
                    is Connection.NoConnectionException -> ErrorType.DISCONNECTED
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
        Connection.NoAnswerException::class,
        Connection.NoConnectionException::class,
        CorruptedMessageException::class,
        CancellationException::class
    )
    private fun CoroutineScope.run(
        request: Request,
        connection: Connection
    ): Response {
        connection.sendMessage(request)
        logger.info("Request sent: {}", request)

        var it = 0
        while (it < NUM_MESSAGES_TO_DUMP) {
            if (!isActive)
                throw CancellationException()

            try {
                val receivedMessage = connection.receiveMessage()
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
        throw Connection.NoAnswerException()
    }

    class CorruptedMessageException : Exception {
        constructor() : super()
        constructor(cause: Throwable?) : super(cause)
    }
}
