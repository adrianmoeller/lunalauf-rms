package lunalauf.rms.utilities.network.communication

import com.google.gson.JsonParseException
import lunalauf.rms.utilities.network.client.Client
import lunalauf.rms.utilities.network.communication.message.request.Request
import lunalauf.rms.utilities.network.communication.message.response.Response

class RequestSubmitter(
    private val responseHandler: (Response) -> Unit,
    private val errorHandler: (ErrorType) -> Unit
) {
    private val numMessagesToDump = 5

    fun submit(request: Request, client: Client) {
        val task = SubmissionTask(request, client)
        task.setOnSucceeded { event ->
            try {
                val response: Response = task.get()
                responseHandler(response)
            } catch (e: Exception) {
                errorHandler(ErrorType.UNWANTED_TERMINATION)
            }
        }
        task.setOnCancelled { event -> errorHandler(ErrorType.UNWANTED_TERMINATION) }
        task.setOnFailed { event ->
            val error = when (task.getException()) {
                    is Client.NoAnswerException -> ErrorType.RESPONSE_TIMEOUT
                    is Client.NoConnectionException -> ErrorType.DISCONNECTED
                    is CorruptedMessageException -> ErrorType.CORRUPTED_SERVER_MESSAGE
                    else -> ErrorType.CORRUPTED_SERVER_MESSAGE
                }
            errorHandler(error)
        }
        executor.execute(task)
    }

    fun shutdown() {
        try {
            executor.shutdown()
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) executor.shutdownNow()
        } catch (ignored: InterruptedException) {
        }
    }

    private inner class SubmissionTask internal constructor(private val request: Request, private val client: Client) :
        Task<Response?>() {
        @Throws(Client.NoAnswerException::class, Client.NoConnectionException::class, CorruptedMessageException::class)
        protected fun call(): Response {
            if (!client.isConnected()) throw Client.NoConnectionException()
            client.send(MessageProcessor.toJsonString(request))
            return getMatchingResponse(request) ?: throw CorruptedMessageException()
        }

        @Throws(Client.NoAnswerException::class, Client.NoConnectionException::class, CorruptedMessageException::class)
        private fun getMatchingResponse(request: Request): Response {
            var it = 0
            while (it < numMessagesToDump) {
                val receivedString = client.receive()
                try {
                    val receivedMessage = MessageProcessor.fromJsonString(receivedString)
                    if (receivedMessage is Response) {
                        if (receivedMessage.messageId == request.messageId || receivedMessage.messageId == -1L) return receivedMessage
                    }
                } catch (e: JsonParseException) {
                    throw CorruptedMessageException(e)
                }
                it++
            }
            throw Client.NoAnswerException()
        }
    }

    class CorruptedMessageException : Exception {
        constructor() : super()
        constructor(cause: Throwable?) : super(cause)
    }
}
