package lunalauf.rms.utilities.network.server

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.slf4j.LoggerFactory
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException

class ClientCatcher(
    private val clientHandler: ClientHandler,
    private val serverSocket: ServerSocket
) {
    companion object {
        private const val TIMEOUT = 1000 // ms
        private const val MAX_ITERATIONS = 30
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    private val _state = MutableStateFlow(State.Idling)
    val state get() = _state.asStateFlow()

    init {
        try {
            serverSocket.setSoTimeout(TIMEOUT)
        } catch (e: SocketException) {
            logger.error("Could not set socket timeout. Behaviour unpredictable", e)
        }

        clientHandler.onLostClients { start() }
    }

    @Synchronized
    fun start() {
        _state.value = State.Transitioning
        scope.launch {
            job?.cancelAndJoin()

            job = scope.launch {
                _state.value = State.Running
                var it = 0
                while (isActive && it < MAX_ITERATIONS) {
                    try {
                        val socket: Socket = serverSocket.accept()
                        val client = Client(socket)
                        clientHandler.handleClient(client)
                    } catch (_: SocketTimeoutException) {
                        it++
                    }
                }
                _state.value = State.Idling
            }
        }

    }

    @Synchronized
    fun stop() {
        _state.value = State.Transitioning
        scope.launch {
            job?.cancelAndJoin()
            _state.value = State.Idling
        }
    }

    enum class State {
        Running, Idling, Transitioning
    }
}
