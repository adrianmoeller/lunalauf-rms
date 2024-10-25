package lunalauf.rms.counterapp.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import lunalauf.rms.utilities.network.client.*
import lunalauf.rms.utilities.network.client.javasocket.JavaSocketClient

class MainScreenModel : AbstractScreenModel() {
    private var _counterType = MutableStateFlow(CounterType.RoundCounter)
    val counterType get() = _counterType.asStateFlow()

    var port by mutableStateOf("")
        private set

    var host by mutableStateOf("127.0.0.1")
        private set

    private val client: Client<JavaSocketClient.Input> = JavaSocketClient()

    private val _mainState: MutableStateFlow<MainState> = MutableStateFlow(MainState.Start)
    val mainState = _mainState.asStateFlow()

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
        val operator = when (_counterType.value) {
            CounterType.RoundCounter -> RoundCounter(connection) { tryReconnect() }
            CounterType.InfoDisplay -> InfoDisplay(connection) { tryReconnect() }
        }
        _mainState.value = MainState.Operating(operator)
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

    fun disconnect() {
        val constMainState = mainState.value
        if (constMainState is MainState.Operating) {
            constMainState.operator.shutdown()
            constMainState.operator.connection.close()
        }
        client.abort()
        _mainState.value = MainState.Start
    }
}

sealed class MainState {
    data object Start : MainState()
    data class Operating(val operator: AbstractOperator) : MainState()
}

enum class CounterType {
    RoundCounter, InfoDisplay
}