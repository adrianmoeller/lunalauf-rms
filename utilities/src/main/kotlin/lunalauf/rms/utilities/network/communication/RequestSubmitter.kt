package lunalauf.rms.utilities.network.communication

import com.google.gson.JsonParseException
import javafx.concurrent.Task
import lunalauf.rms.utilities.network.client.Client
import lunalauf.rms.utilities.network.communication.message.request.Request
import lunalauf.rms.utilities.network.communication.message.response.Response
import java.util.function.Consumer

class RequestSubmitter(private val responseHandler: Consumer<Response>, private val errorHandler: Consumer<ErrorType>) {
    private val executor: ExecutorService
    private val numMessagesToDump = 5

    init {
        executor = Executors.newSingleThreadExecutor()
    }

    fun submit(request: Request, client: Client?) {
        val task = SubmissionTask(request, client)
        task.setOnSucceeded { event ->
            try {
                val response: Response = task.get()
                responseHandler.accept(response)
            } catch (e: Exception) {
                errorHandler.accept(ErrorType.UNWANTED_TERMINATION)
            }
        }
        task.setOnCancelled { event -> errorHandler.accept(ErrorType.UNWANTED_TERMINATION) }
        task.setOnFailed { event ->
            val exception: Throwable = task.getException()
            val error: ErrorType
            error =
                if (exception is NoAnswerException) ErrorType.RESPONSE_TIMEOUT else if (exception is NoConnectionException) ErrorType.DISCONNECTED else if (exception is CorruptedMessageException) ErrorType.CORRUPTED_SERVER_MESSAGE else ErrorType.CORRUPTED_SERVER_MESSAGE
            errorHandler.accept(error)
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

    private inner class SubmissionTask internal constructor(private val request: Request, private val client: Client?) :
        Task<Response?>() {
        @Throws(NoAnswerException::class, NoConnectionException::class, CorruptedMessageException::class)
        protected fun call(): Response {
            if (client == null || !client.isConnected()) throw NoConnectionException()
            client.send(MessageProcessor.toJsonString(request))
            return getMatchingResponse(request) ?: throw CorruptedMessageException()
        }

        @Throws(NoAnswerException::class, NoConnectionException::class, CorruptedMessageException::class)
        private fun getMatchingResponse(request: Request): Response? {
            var it = 0
            while (it < numMessagesToDump) {
                val receivedString = client!!.receive()
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
            throw NoAnswerException()
        }
    }

    class CorruptedMessageException : Exception {
        constructor() : super()
        constructor(cause: Throwable?) : super(cause)
    }
}
