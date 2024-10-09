package lunalauf.rms.utilities.network.server

import kotlinx.coroutines.flow.StateFlow
import lunalauf.rms.utilities.network.communication.message.Message
import lunalauf.rms.utilities.network.util.Service

interface Server {
    val port: Int
    val connections: StateFlow<List<Connection>>
    val acceptingConnectionsState: StateFlow<Service.State>

    fun removeConnection(connection: Connection)
    fun startAcceptingConnections()
    fun stopAcceptingConnections()
    fun setOnMessageReceived(handler: suspend (Connection, Message) -> Message)
    fun shutdown()
}