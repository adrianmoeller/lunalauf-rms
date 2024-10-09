package lunalauf.rms.utilities.network.server.javasocket

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.message.Message
import lunalauf.rms.utilities.network.communication.message.response.ResponseFactory
import lunalauf.rms.utilities.network.server.Connection
import lunalauf.rms.utilities.network.server.Server
import lunalauf.rms.utilities.network.util.Service
import org.slf4j.LoggerFactory

class JavaSocketServer: Server {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private var messageHandler: suspend (Connection, Message) -> Message = { _, _ ->
        ResponseFactory.createErrorResponse(-1, ErrorType.BAD_SERVER_STATE)
    }
    private val _connections = MutableStateFlow<List<JavaSocketConnection>>(listOf())
    private val connectionAcceptService: ConnectionAcceptService

    override val port: Int
        get() = connectionAcceptService.port
    override val connections: StateFlow<List<Connection>>
        get() = _connections.asStateFlow()
    override val acceptingConnectionsState: StateFlow<Service.State>
        get() = connectionAcceptService.state

    init {
        connectionAcceptService = ConnectionAcceptService { handleAcceptedConnection(it) }
    }

    private fun handleAcceptedConnection(connection: JavaSocketConnection) {
        if (connection.initiateCommunication()) {
            registerConnection(connection)
            connection.startListening { messageHandler(connection, it) }
        } else {
            connection.close()
        }
    }

    private fun registerConnection(connection: JavaSocketConnection) {
        connection.setOnConnectionLost { startAcceptingConnections() }

        var existing: JavaSocketConnection? = null
        _connections.value.forEach {
            if (it.address == connection.address)
                if (it.status.value == -1)
                    existing = it
        }
        _connections.update {
            it.toMutableList().apply {
                if (existing != null) set(it.indexOf(existing), connection)
                else add(connection)
            }
        }
        logger.info("Client registered: {}", connection.address)
    }

    override fun removeConnection(connection: Connection) {
        if (connection.status.value < 0)
            _connections.update {
                it.toMutableList().apply {
                    remove(connection)
                }
            }
    }

    override fun startAcceptingConnections() {
        connectionAcceptService.start(Unit)
    }

    override fun stopAcceptingConnections() {
        connectionAcceptService.stop()
    }

    override fun setOnMessageReceived(handler: suspend (Connection, Message) -> Message) {
        messageHandler = handler
    }

    override fun shutdown() {
        connectionAcceptService.stop()
        _connections.value.forEach { it.stopListening() }
    }
}