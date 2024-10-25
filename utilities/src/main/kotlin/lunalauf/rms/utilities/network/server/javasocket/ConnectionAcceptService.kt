package lunalauf.rms.utilities.network.server.javasocket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import lunalauf.rms.utilities.network.util.PortProvider
import lunalauf.rms.utilities.network.util.Service
import java.io.IOException
import java.net.ServerSocket
import java.net.SocketException
import java.net.SocketTimeoutException

class ConnectionAcceptService(
    private val onConnectionAccepted: (JavaSocketConnection) -> Unit
) : Service<Unit, Unit>(Dispatchers.IO) {
    companion object {
        private const val TIMEOUT = 1000 // ms
        private const val MAX_ITERATIONS = 30
    }
    private val scope = CoroutineScope(Dispatchers.IO)
    private val serverSocket: ServerSocket

    val port: Int
        get() = serverSocket.localPort

    init {
        serverSocket = createServerSocket()
        try {
            serverSocket.setSoTimeout(TIMEOUT)
        } catch (e: SocketException) {
            logger.error("Could not set socket timeout. Behaviour unpredictable", e)
        }
    }

    @Throws(IOException::class)
    private fun createServerSocket(): ServerSocket {
        for (port in PortProvider.getPreferredPorts()) {
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

    override suspend fun CoroutineScope.run(input: Unit) {
        logger.info("Accepting connections started")
        var it = 0
        while (isActive && it < MAX_ITERATIONS) {
            try {
                val socket = serverSocket.accept()
                val client = JavaSocketConnection(socket)
                logger.info("Client accepted: {}", client.address)
                scope.launch { onConnectionAccepted(client) }
            } catch (_: SocketTimeoutException) {
                it++
            }
        }
        logger.info("Accepting connections stopped")
    }
}
