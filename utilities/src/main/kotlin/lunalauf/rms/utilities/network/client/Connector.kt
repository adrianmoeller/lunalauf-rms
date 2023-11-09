package lunalauf.rms.utilities.network.client

import lunalauf.rms.utilities.network.util.ConnectionInitiationHelper
import lunalauf.rms.utilities.network.util.PortProvider
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class Connector {
    companion object {
        private const val TIMEOUT = 200 // ms
    }

    private var currentTask: ConnectionTask? = null

    fun connect(
        host: String,
        port: Int,
        onConnected: (Client) -> Unit,
        onAborted: () -> Unit
    ) {
        cancel()
        currentTask = ConnectionTask(host, port)
        currentTask.setOnSucceeded { event ->
            try {
                val client: Client = currentTask.get()
                onConnected(client)
            } catch (e: Exception) {
                onAborted()
            }
        }
        currentTask.setOnCancelled { event -> onAborted() }
        currentTask.setOnFailed { event -> onAborted() }
        executor.execute(currentTask)
    }

    fun cancel() {
        if (currentTask != null) currentTask.cancel()
    }

    private inner class ConnectionTask(private val host: String, private val port: Int) : Task<Client?>() {
        @Throws(Exception::class)
        protected fun call(): Client? {
            while (true) {
                if (isCancelled()) return null
                if (port > 0 && port <= 65535) {
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(host, port), TIMEOUT * 2)
                        val client = Client(socket)
                        return if (initialCommunicationTest(client)) client else {
                            client.close()
                            throw Exception("Communication test failed")
                        }
                    } catch (ignored: IOException) {
                    }
                } else {
                    for (prefPort in PortProvider.getPreferredPorts()) {
                        if (isCancelled()) return null
                        try {
                            val socket = Socket()
                            socket.connect(InetSocketAddress(host, prefPort), TIMEOUT)
                            val client = Client(socket)
                            if (initialCommunicationTest(client)) return client else client.close()
                        } catch (ignored: Exception) {
                        }
                    }
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun initialCommunicationTest(client: Client): Boolean {
        client.setTimeout(5000)
        return try {
            val synMessage: String = ConnectionInitiationHelper.synMessage
            val expectedAckMessage: String = ConnectionInitiationHelper.getAckMessage(synMessage)
            client.send(synMessage)
            val ackMessage = client.receive()
            if (expectedAckMessage != ackMessage) return false
            val synAckMessage: String = ConnectionInitiationHelper.getAckMessage(ackMessage)
            client.send(synAckMessage)
            true
        } catch (_: Exception) {
            false
        } finally {
            client.resetTimeout()
        }
    }
}
