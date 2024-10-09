package lunalauf.rms.utilities.network.client

import kotlinx.coroutines.flow.StateFlow
import lunalauf.rms.utilities.network.util.Service

interface Client<I> {
    val connectionState: StateFlow<ConnectionState>
    val connectState: StateFlow<Service.State>
    fun connect(input: I, onResultAvailable: (ConnectResult) -> Unit)
    fun abort()

    sealed class ConnectResult {
        data object Aborted : ConnectResult()
        data class Failed(val reason: String) : ConnectResult()
        data class Successful(val connection: Connection) : ConnectResult()
    }

    sealed class ConnectionState {
        data object Disconnected : ConnectionState()
        data class Available(val connection: Connection) : ConnectionState()
    }
}