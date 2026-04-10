package lunalauf.rms.utilities.network.server

import lunalauf.rms.model.api.ModelManager
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.message.request.Request
import lunalauf.rms.utilities.network.communication.message.response.ResponseFactory
import lunalauf.rms.utilities.network.server.javasocket.JavaSocketServer
import org.slf4j.LoggerFactory


private val logger = LoggerFactory.getLogger(NetworkManager::class.java)

sealed class NetworkManager {
    companion object {
        fun initialize(modelManager: ModelManager): NetworkManager {
            return try {
                Available(modelManager)
            } catch (e: Exception) {
                InitializationError(
                    message = "Could not create server.\n Please restart",
                    exception = e
                )
            }
        }
    }

    class InitializationError internal constructor(
        val message: String,
        val exception: Exception
    ) : NetworkManager() {
        init {
            logger.error(message, exception)
        }
    }

    class Available internal constructor(
        modelManager: ModelManager
    ) : NetworkManager() {
        val server: Server = JavaSocketServer()

        val port get() = server.port

        init {
            if (modelManager !is ModelManager.Available)
                throw IllegalStateException("Model manager is not available")

            server.setOnMessageReceived { client, message ->
                if (message is Request) {
                    val constModelState = modelManager.model.value
                    if (constModelState is ModelState.Loaded) {
                        val modelUpdater = ModelUpdater(constModelState, message)
                        try {
                            modelUpdater.run()
                        } catch (e: Exception) {
                            logger.error("Exception occurred while updating the model", e)
                            ResponseFactory.createErrorResponse(message.messageId, ErrorType.BAD_SERVER_STATE)
                        }
                    } else {
                        logger.warn("Request handler needs available model!")
                        ResponseFactory.createErrorResponse(message.messageId, ErrorType.NO_MODEL_STATE)
                    }
                } else {
                    logger.warn("Received unexpected client message: {} from: {}", message, client.address)
                    ResponseFactory.createErrorResponse(message.messageId, ErrorType.UNEXPECTED_CLIENT_MESSAGE)
                }
            }
        }

        fun shutdown() {
            server.shutdown()
        }
    }
}
