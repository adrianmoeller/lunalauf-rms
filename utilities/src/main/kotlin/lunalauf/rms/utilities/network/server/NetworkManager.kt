package lunalauf.rms.utilities.network.server

import kotlinx.coroutines.flow.StateFlow
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.message.request.Request
import lunalauf.rms.utilities.network.communication.message.response.ResponseFactory
import lunalauf.rms.utilities.network.server.javasocket.JavaSocketClientController
import org.slf4j.LoggerFactory


private val logger = LoggerFactory.getLogger(NetworkManager::class.java)

sealed class NetworkManager {
    companion object {
        fun initialize(modelState: StateFlow<ModelState>): NetworkManager {
            return try {
                Available(modelState)
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
        modelState: StateFlow<ModelState>
    ) : NetworkManager() {
        val clientController: ClientController = JavaSocketClientController()

        val port get() = clientController.port

        init {
            clientController.setOnMessageReceived { client, message ->
                if (message is Request) {
                    val constModelState = modelState.value
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
            clientController.shutdown()
        }
    }
}
