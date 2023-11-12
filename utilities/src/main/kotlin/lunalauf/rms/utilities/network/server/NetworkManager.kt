package lunalauf.rms.utilities.network.server

import kotlinx.coroutines.flow.StateFlow
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.utilities.network.util.PortProvider.getPreferredPorts
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.ServerSocket


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
        private var serverSocket = createServerSocket()
        val clientHandler = ClientHandler(modelState)
        val clientCatcher = ClientCatcher(clientHandler, serverSocket)

        val port get() = serverSocket.localPort
        val localAddress get() = serverSocket.inetAddress?.hostAddress ?: "None"

        @Throws(IOException::class)
        private fun createServerSocket(): ServerSocket {
            for (port in getPreferredPorts()) {
                try {
                    val socket = ServerSocket(port)
                    logger.info("Created server socket. Bound to port {}", socket.localPort)
                    return socket
                } catch (_: IOException) {
                }
            }
            val socket = ServerSocket(0)
            logger.info("Created server socket. Bound to port {}", socket.localPort)
            return socket
        }

        fun shutdown() {
            clientCatcher.stop()
            clientHandler.clients.value.forEach { it.stopListening() }
        }
    }
}
