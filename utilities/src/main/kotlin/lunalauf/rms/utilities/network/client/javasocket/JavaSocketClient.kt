package lunalauf.rms.utilities.network.client.javasocket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import lunalauf.rms.utilities.network.client.Client
import lunalauf.rms.utilities.network.client.Client.ConnectResult
import lunalauf.rms.utilities.network.client.Client.ConnectionState
import lunalauf.rms.utilities.network.util.PortProvider
import lunalauf.rms.utilities.network.util.Service
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class JavaSocketClient : Client<JavaSocketClient.Input> {
    companion object {
        private const val TIMEOUT = 200 // ms
    }

    private val connectService = object : Service<Input, ConnectResult>() {
        override fun CoroutineScope.run(input: Input): ConnectResult {
            while (true) {
                if (!isActive) return ConnectResult.Aborted

                if (input.port in 1..65535) {
                    try {
                        return connect(input.host, input.port, TIMEOUT * 2)
                    } catch (_: Exception) {
                    }
                } else {
                    for (prefPort in PortProvider.getPreferredPorts()) {
                        if (!isActive) return ConnectResult.Aborted

                        try {
                            return connect(input.host, prefPort, TIMEOUT)
                        } catch (_: Exception) {
                        }
                    }
                    logger.info("Could no connect to any preferred port. Retrying...")
                }
            }
        }

        @Throws(IOException::class)
        private fun connect(host: String, port: Int, timeout: Int): ConnectResult {
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), timeout)
            val connection = JavaSocketConnection(socket)
            return if (connection.initiateCommunication()) {
                logger.info("Connected to port: {}", port)
                _connectionState.value = ConnectionState.Available(connection)
                ConnectResult.Successful(connection)
            } else {
                connection.close()
                ConnectResult.Failed("Communication test failed")
            }
        }
    }

    private var _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)

    override val connectionState: StateFlow<ConnectionState>
        get() = _connectionState.asStateFlow()
    override val connectState: StateFlow<Service.State>
        get() = connectService.state

    override fun connect(input: Input, onResultAvailable: (ConnectResult) -> Unit) {
        connectService.start(input, onResultAvailable)
    }

    override fun abort() {
        connectService.stop()
        _connectionState.value = ConnectionState.Disconnected
    }

    data class Input(
        val host: String,
        val port: Int
    )
}