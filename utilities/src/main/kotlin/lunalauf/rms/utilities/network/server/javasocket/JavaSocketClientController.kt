package lunalauf.rms.utilities.network.server.javasocket

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.message.Message
import lunalauf.rms.utilities.network.communication.message.response.ResponseFactory
import lunalauf.rms.utilities.network.server.Client
import lunalauf.rms.utilities.network.server.ClientController
import lunalauf.rms.utilities.network.util.Service
import org.slf4j.LoggerFactory

class JavaSocketClientController: ClientController {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private var messageHandler: suspend (Client, Message) -> Message = { _, _ ->
        ResponseFactory.createErrorResponse(-1, ErrorType.BAD_SERVER_STATE)
    }
    private val _clients = MutableStateFlow<List<JavaSocketClient>>(listOf())
    private val clientAcceptService: ClientAcceptService

    override val port: Int
        get() = clientAcceptService.port
    override val clients: StateFlow<List<Client>>
        get() = _clients.asStateFlow()
    override val acceptingClientsState: StateFlow<Service.State>
        get() = clientAcceptService.state

    init {
        clientAcceptService = ClientAcceptService { handleAcceptedClient(it) }
    }

    private fun handleAcceptedClient(client: JavaSocketClient) {
        if (client.initiateCommunication()) {
            registerClient(client)
            client.startListening { messageHandler(client, it) }
        } else {
            client.close()
        }
    }

    private fun registerClient(client: JavaSocketClient) {
        client.setOnConnectionLost { startAcceptingClients() }

        var existing: JavaSocketClient? = null
        _clients.value.forEach {
            if (it.address == client.address)
                if (it.status.value == -1)
                    existing = it
        }
        _clients.update {
            it.toMutableList().apply {
                if (existing != null) set(it.indexOf(existing), client)
                else add(client)
            }
        }
        logger.info("Client registered: {}", client.address)
    }

    override fun removeClient(client: Client) {
        if (client.status.value < 0)
            _clients.update {
                it.toMutableList().apply {
                    remove(client)
                }
            }
    }

    override fun startAcceptingClients() {
        clientAcceptService.start(Unit)
    }

    override fun stopAcceptingClients() {
        clientAcceptService.stop()
    }

    override fun setOnMessageReceived(handler: suspend (Client, Message) -> Message) {
        messageHandler = handler
    }

    override fun shutdown() {
        clientAcceptService.stop()
        _clients.value.forEach { it.stopListening() }
    }
}