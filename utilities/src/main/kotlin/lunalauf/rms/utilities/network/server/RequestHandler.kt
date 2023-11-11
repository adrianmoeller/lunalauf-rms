package lunalauf.rms.utilities.network.server

import com.google.gson.JsonParseException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.MessageProcessor
import lunalauf.rms.utilities.network.communication.message.request.Request
import lunalauf.rms.utilities.network.communication.message.response.ResponseFactory
import org.slf4j.LoggerFactory

class RequestHandler(
    private val client: Client,
    private val modelState: ModelState,
    private val status: MutableStateFlow<Int>,
    scope: CoroutineScope
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val responseFactory = ResponseFactory()
    private val job: Job

    init {
        status.value = 0
        job = scope.launch {
            try {
                executeProtocol()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                logger.error("Exception occurred while running the protocol", e)
            } finally {
                status.value = -1
            }
        }
    }

    private suspend fun CoroutineScope.executeProtocol() {
        status.value = 1
        while (true) {
            if (!this.isActive)
                return

            val messageString = client.receive()
                ?: return

            val response = try {
                val message = MessageProcessor.fromJsonString(messageString)

                if (message is Request) {
                    if (modelState is ModelState.Loaded) {
                        val modelUpdater = ModelUpdater(modelState, message, responseFactory)
                        try {
                            modelUpdater.run()
                        } catch (e: Exception) {
                            logger.error("Exception occurred while updating the model", e)
                            responseFactory.createErrorResponse(message.messageId, ErrorType.BAD_SERVER_STATE)
                        }
                    } else {
                        logger.warn("Request handler needs available model!")
                        responseFactory.createErrorResponse(message.messageId, ErrorType.NO_MODEL_STATE)
                    }
                } else {
                    logger.warn("Received unexpected client message: {} from: {}", message, client.remoteAddress)
                    responseFactory.createErrorResponse(message.messageId, ErrorType.UNEXPECTED_CLIENT_MESSAGE)
                }
            } catch (_: JsonParseException) {
                logger.warn("Received corrupted client message from: {}", client.remoteAddress)
                responseFactory.createErrorResponse(-1, ErrorType.CORRUPTED_CLIENT_MESSAGE)
            }

            client.send(MessageProcessor.toJsonString(response))
            logger.info("Response sent: {} to: {}", response, client.remoteAddress)
        }
    }

    fun cancel() {
        job.cancel()
    }
}
