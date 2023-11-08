package lunalauf.rms.utilities.network.server

import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException

class ClientCatchService(
    private val clientHandler: ClientHandler,
    private val serverSocket: ServerSocket
) : Service<Void?>() {
    companion object {
        private const val TIMEOUT = 1000 // ms
    }

    private val maxIterations = 30

    init {
        this.setExecutor(threadPool)
        try {
            serverSocket.setSoTimeout(TIMEOUT)
        } catch (e: SocketException) {
            e.printStackTrace()
        }
    }

    protected fun createTask(): Task<Void> {
        return object : Task() {
            @Throws(Exception::class)
            protected fun call(): Void? {
                var it = 0
                while (!isCancelled() && it < maxIterations) {
                    try {
                        val socket: Socket = serverSocket.accept()
                        val client = Client(socket)
                        clientHandler.handleClient(client)
                    } catch (eTimeout: SocketTimeoutException) {
                        it++
                    }
                }
                return null
            }
        }
    }
}
