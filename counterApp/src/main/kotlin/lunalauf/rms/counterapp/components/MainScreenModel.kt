package lunalauf.rms.counterapp.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import lunalauf.rms.utilities.network.client.Client
import lunalauf.rms.utilities.network.client.Connection
import lunalauf.rms.utilities.network.client.InfoDisplay
import lunalauf.rms.utilities.network.client.RoundCounter
import lunalauf.rms.utilities.network.client.javasocket.JavaSocketClient

class MainScreenModel : AbstractScreenModel() {
    private var _counterType = MutableStateFlow(CounterType.RoundCounter)
    val counterType get() = _counterType.asStateFlow()

    var port by mutableStateOf("")
        private set

    var host by mutableStateOf("127.0.0.1")
        private set

    private val client: Client<JavaSocketClient.Input> = JavaSocketClient()

    private val _connectionStatus: MutableStateFlow<MainConnectionStatus> =
        MutableStateFlow(MainConnectionStatus.Disconnected)
    val connectionStatus = _connectionStatus.asStateFlow()

    val connectState get() = client.connectState

    var reconnecting by mutableStateOf(false)
        private set

    fun updateCounterType(value: CounterType) {
        _counterType.value = value
    }

    fun updatePort(value: String) {
        if (value.isBlank())
            port = ""
        else
            value.toIntOrNull()?.toString()?.let { port = it }
    }

    fun updateHost(value: String) {
        host = value
    }

    fun startConnecting() {
        client.connect(
            input = JavaSocketClient.Input(
                host = host,
                port = port.toIntOrNull() ?: 0
            ),
            onResultAvailable = {
                when (it) {
                    is Client.ConnectResult.Successful -> {
                        connected(it.connection)
                    }

                    Client.ConnectResult.Aborted -> {}

                    is Client.ConnectResult.Failed -> {
                        // TODO show snack bar with error
                    }
                }
            }
        )
    }

    private fun connected(connection: Connection) {
        when (_counterType.value) {
            CounterType.RoundCounter -> {
                val roundCounter = RoundCounter(connection).apply {
                    setOnConnectionLost { tryReconnect() }
                }
                _connectionStatus.value = MainConnectionStatus.ConnectedRC(roundCounter)
            }

            CounterType.InfoDisplay -> {
                val infoDisplay = InfoDisplay(connection).apply {
                    setOnConnectionLost { tryReconnect() }
                }
                _connectionStatus.value = MainConnectionStatus.ConnectedID(infoDisplay)
            }
        }
    }

    private fun tryReconnect() {
        reconnecting = true
        client.connect(
            input = JavaSocketClient.Input(
                host = host,
                port = 0
            ),
            onResultAvailable = {
                reconnecting = false
                when (it) {
                    is Client.ConnectResult.Successful -> {
                        connected(it.connection)
                    }

                    Client.ConnectResult.Aborted -> {}

                    is Client.ConnectResult.Failed -> {
                        // TODO show snack bar with error
                    }
                }
            }
        )
    }

    fun stopConnecting() {
        client.abort()
    }

    fun disconnect(connection: Connection) {
        connection.close()
        client.abort()
    }
}

sealed class MainConnectionStatus {
    data object Disconnected : MainConnectionStatus()
    data class ConnectedRC(val roundCounter: RoundCounter) : MainConnectionStatus()
    data class ConnectedID(val infoDisplay: InfoDisplay) : MainConnectionStatus()
}

enum class CounterType {
    RoundCounter, InfoDisplay
}