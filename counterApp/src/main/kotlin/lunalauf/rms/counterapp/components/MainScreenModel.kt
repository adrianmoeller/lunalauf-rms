package lunalauf.rms.counterapp.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import lunalauf.rms.utilities.network.client.Connector
import lunalauf.rms.utilities.network.client.InfoDisplay
import lunalauf.rms.utilities.network.client.RoundCounter

class MainScreenModel : AbstractScreenModel() {
    private var _counterType = MutableStateFlow(CounterType.RoundCounter)
    val counterType get() = _counterType.asStateFlow()

    var port by mutableStateOf("")
        private set

    var host by mutableStateOf("127.0.0.1")
        private set

    private val connector = Connector()
    val connectorState get() = connector.state

    private val _connectionStatus: MutableStateFlow<ConnectionStatus> = MutableStateFlow(ConnectionStatus.Disconnected)
    val connectionStatus = _connectionStatus.asStateFlow()

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

    fun startConnector() {
        connector.start(
            input = Connector.Input(
                host = host,
                port = port.toIntOrNull() ?: 0
            ),
            onResultAvailable = {
                when (it) {
                    Connector.Result.Aborted -> {}
                    is Connector.Result.Connected -> {
                        when (_counterType.value) {
                            CounterType.RoundCounter -> {
                                val roundCounter = RoundCounter(it.client).apply {
                                    setActions(
                                        accepted = { TODO() },
                                        rejected = { TODO() },
                                        failed = { TODO() }
                                    )
                                    setOnConnectionLost { TODO() }
                                }
                                _connectionStatus.value = ConnectionStatus.ConnectedRC(roundCounter)
                            }
                            CounterType.InfoDisplay -> {
                                val infoDisplay = InfoDisplay(it.client).apply {
                                    setActions(
                                        succeeded = { TODO() },
                                        failed = { TODO() }
                                    )
                                    setOnConnectionLost { TODO() }
                                }
                                _connectionStatus.value = ConnectionStatus.ConnectedID(infoDisplay)
                            }
                        }
                    }
                    is Connector.Result.Failed -> {
                        // TODO show snack bar with error
                    }
                }
            }
        )
    }

    fun stopConnector() {
        connector.stop()
    }
}

sealed class ConnectionStatus {
    data object Disconnected : ConnectionStatus()
    data class ConnectedRC(val roundCounter: RoundCounter) : ConnectionStatus()
    data class ConnectedID(val infoDisplay: InfoDisplay) : ConnectionStatus()
}

enum class CounterType {
    RoundCounter, InfoDisplay
}