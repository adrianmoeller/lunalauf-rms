package lunalauf.rms.utilities.network.server

import kotlinx.coroutines.flow.StateFlow
import lunalauf.rms.utilities.network.communication.message.Message
import lunalauf.rms.utilities.network.util.Service

interface ClientController {
    val port: Int
    val clients: StateFlow<List<Client>>
    val acceptingClientsState: StateFlow<Service.State>

    fun removeClient(client: Client)
    fun startAcceptingClients()
    fun stopAcceptingClients()
    fun setOnMessageReceived(handler: suspend (Client, Message) -> Message)
    fun shutdown()
}