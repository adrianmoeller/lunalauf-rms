package lunalauf.rms.utilities.network.server

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.ServerSocket

class ClientCatcher(
    clientHandler: ClientHandler,
    serverSocket: ServerSocket
) {
    private val service: ClientCatchService
    private val _running = MutableStateFlow(false)
    val running get() = _running.asStateFlow()

    init {
        service = ClientCatchService(clientHandler, serverSocket)
        clientHandler.onLostClients { start() }
    }

    fun start() {
        if (service.getState().equals(State.RUNNING)) service.restart() else {
            service.reset()
            service.start()
        }
    }

    fun stop() {
        service.cancel()
    }

    fun setOnStarted(event: EventHandler<WorkerStateEvent?>?) {
        service.setOnRunning(event)
    }

    fun setOnStopped(event: EventHandler<WorkerStateEvent?>?) {
        service.setOnSucceeded(event)
        service.setOnCancelled(event)
        service.setOnFailed(event)
    }
}
