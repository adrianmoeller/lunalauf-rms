package lunalauf.rms.utilities.network.server

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import lunalauf.rms.utilities.network.util.Service
import org.slf4j.LoggerFactory
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException

class ClientCatcher(
    private val clientHandler: ClientHandler,
    private val serverSocket: ServerSocket
) : Service<Unit, Unit>(Dispatchers.IO) {
    companion object {
        private const val TIMEOUT = 1000 // ms
        private const val MAX_ITERATIONS = 30
    }

    init {
        try {
            serverSocket.setSoTimeout(TIMEOUT)
        } catch (e: SocketException) {
            logger.error("Could not set socket timeout. Behaviour unpredictable", e)
        }
        clientHandler.onLostClients { start(Unit) }
    }

    override fun CoroutineScope.run(input: Unit) {
        var it = 0
        while (isActive && it < MAX_ITERATIONS) {
            try {
                val socket = serverSocket.accept()
                val client = Client(socket)
                logger.info("Client accepted: {}. Passing to client handler", client.remoteAddress)
                clientHandler.handleClient(client)
            } catch (_: SocketTimeoutException) {
                it++
            }
        }
    }
}
