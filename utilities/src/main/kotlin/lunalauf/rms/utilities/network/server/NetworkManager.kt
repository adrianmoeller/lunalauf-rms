package lunalauf.rms.utilities.network.server

import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.utilities.network.util.PortProvider.getPreferredPorts
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.ServerSocket


private val logger = LoggerFactory.getLogger(NetworkManager::class.java)

sealed class NetworkManager {
    companion object {
        fun initialize(modelState: ModelState): NetworkManager {
            return try {
                Available(modelState)
            } catch (e: Exception) {
                InitializationError(
                    message = "Could not create server socket",
                    exception = e
                )
            }
        }
    }

    data class InitializationError(
        val message: String,
        val exception: Exception
    ) : NetworkManager() {
        init {
            logger.error(message, exception)
        }
    }

    class Available(
        modelState: ModelState
    ) : NetworkManager() {
        private var serverSocket = createServerSocket()
        val clientHandler: ClientHandler
        val clientCatcher: ClientCatcher

        val port get() = serverSocket.getLocalPort()
        val localAddress get() = serverSocket.getInetAddress()?.hostAddress ?: "None"

        init {
            createServerSocket()
            clientHandler = ClientHandler(modelState)
            clientCatcher = ClientCatcher(clientHandler, serverSocket)
        }

        private fun createServerSocket(): ServerSocket {
            for (port in getPreferredPorts()) {
                try {
                    return ServerSocket(port)
                } catch (_: IOException) {
                }
            }
            return ServerSocket(0)
        }

        fun shutdown() {
            clientCatcher.stop()
            clientHandler.clients.value.forEach { obj: Client -> obj.stopListening() }
        }
    }
}
