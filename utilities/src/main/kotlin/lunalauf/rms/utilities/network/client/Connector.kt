package lunalauf.rms.utilities.network.client

import kotlinx.coroutines.*
import lunalauf.rms.utilities.network.util.ConnectionInitiationHelper
import lunalauf.rms.utilities.network.util.PortProvider
import lunalauf.rms.utilities.network.util.Service
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class Connector : Service<Connector.Input, Connector.Result>(Dispatchers.IO) {
    companion object {
        private const val TIMEOUT = 200 // ms
        private const val TIMEOUT_COMMUNICATION_TEST = 5000 // ms
    }

    override fun CoroutineScope.run(input: Input): Result {
        while (true) {
            if (!isActive) return Result.Aborted

            if (input.port in 1..65535) {
                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(input.host, input.port), TIMEOUT * 2)
                    val client = Client(socket)
                    return if (initialCommunicationTest(client)) {
                        logger.info("Connected to port: {}", client.port)
                        Result.Connected(client)
                    } else {
                        client.close()
                        Result.Failed("Communication test failed")
                    }
                } catch (_: Exception) {
                }
            } else {
                for (prefPort in PortProvider.getPreferredPorts()) {
                    if (!isActive) return Result.Aborted

                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(input.host, prefPort), TIMEOUT)
                        val client = Client(socket)
                        return if (initialCommunicationTest(client)) {
                            logger.info("Connected to port: {}", client.port)
                            Result.Connected(client)
                        } else {
                            client.close()
                            Result.Failed("Communication test failed")
                        }
                    } catch (_: Exception) {
                    }
                }
                logger.info("Could no connect to any preferred port. Retrying...")
            }
        }
    }

    @Throws(IOException::class)
    private fun initialCommunicationTest(client: Client): Boolean {
        client.setTimeout(TIMEOUT_COMMUNICATION_TEST)
        return try {
            val synMessage: String = ConnectionInitiationHelper.synMessage
            val expectedAckMessage: String = ConnectionInitiationHelper.getAckMessage(synMessage)
            client.send(synMessage)
            val ackMessage = client.receive()
            if (expectedAckMessage != ackMessage) {
                logger.warn("Communication test failed")
                return false
            }
            val synAckMessage: String = ConnectionInitiationHelper.getAckMessage(ackMessage)
            client.send(synAckMessage)
            logger.info("Communication test successful")
            true
        } catch (e: Exception) {
            logger.warn("Communication test failed due to an exception", e)
            false
        } finally {
            client.resetTimeout()
        }
    }

    data class Input(
        val host: String,
        val port: Int
    )

    sealed class Result {
        data object Aborted : Result()
        data class Failed(val reason: String) : Result()
        data class Connected(val client: Client) : Result()
    }
}
